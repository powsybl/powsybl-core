/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.wca;

import eu.itesla_project.commons.util.StringToIntMapper;
import eu.itesla_project.iidm.datasource.DataSource;
import eu.itesla_project.iidm.export.ampl.AmplConstants;
import eu.itesla_project.iidm.export.ampl.AmplSubset;
import eu.itesla_project.iidm.export.ampl.util.Column;
import eu.itesla_project.iidm.export.ampl.util.TableFormatter;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.modules.histo.HistoDbAttr;
import eu.itesla_project.modules.histo.HistoDbNetworkAttributeId;
import eu.itesla_project.modules.rules.RuleAttributeSet;
import eu.itesla_project.modules.rules.RuleId;
import eu.itesla_project.modules.rules.SecurityRuleExpression;
import eu.itesla_project.modules.rules.SecurityRuleStatus;
import eu.itesla_project.modules.rules.expr.*;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class WCASecurityRulesWriter implements AmplConstants, WCAConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(WCASecurityRulesWriter.class);

    private final Network network;

    private final List<SecurityRuleExpression> rules;

    private final DataSource dataSource;

    private final StringToIntMapper<AmplSubset> mapper;

    private final boolean debug;
    
    private final boolean stopWcaOnViolations;

    public WCASecurityRulesWriter(Network network, List<SecurityRuleExpression> rules, DataSource dataSource, StringToIntMapper<AmplSubset> mapper, boolean debug, boolean stopWcaOnViolations) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.network = Objects.requireNonNull(network);
        this.rules = Objects.requireNonNull(rules);
        this.mapper = Objects.requireNonNull(mapper);
        this.debug = debug;
        this.stopWcaOnViolations = stopWcaOnViolations;
    }

    private static class WCAEntity {
        final int type;
        final int num;
        final int sideNum;
        final float nomV;

        private WCAEntity(int type, int num, int sideNum, float nomV) {
            this.type = type;
            this.num = num;
            this.sideNum = sideNum;
            this.nomV = nomV;
        }
    }

    private static WCAEntity toEntity(HistoDbNetworkAttributeId attrId, Network network, StringToIntMapper<AmplSubset> mapper) {
        int entityType;
        int entityNum;
        int sideNum = 0;
        float nomV;
        TwoTerminalsConnectable branch = network.getLine(attrId.getEquipmentId());
        if (branch == null) {
            branch = network.getTwoWindingsTransformer(attrId.getEquipmentId());
        }
        if (branch != null) {
            entityType = 1;
            entityNum = mapper.getInt(AmplSubset.BRANCH, attrId.getEquipmentId());
            if (branch.getTerminal1().getVoltageLevel().getId().equals(attrId.getSide())) {
                sideNum = 1;
                nomV = branch.getTerminal1().getVoltageLevel().getNominalV();
            } else if (branch.getTerminal2().getVoltageLevel().getId().equals(attrId.getSide())) {
                sideNum = 2;
                nomV = branch.getTerminal2().getVoltageLevel().getNominalV();
            } else {
                throw new RuntimeException("No side '" + attrId.getSide() + "' for branch '" + attrId.getEquipmentId() + "'");
            }
        } else {
            DanglingLine dl = network.getDanglingLine(attrId.getEquipmentId());
            if (dl != null) {
                entityType = 1;
                entityNum = mapper.getInt(AmplSubset.BRANCH, attrId.getEquipmentId());
                sideNum = 1; // see IIDM2DB
                nomV = dl.getTerminal().getVoltageLevel().getNominalV();
            } else {
                Load load = network.getLoad(attrId.getEquipmentId());
                if (load != null) {
                    entityType = 2;
                    entityNum = mapper.getInt(AmplSubset.LOAD, attrId.getEquipmentId());
                    nomV = load.getTerminal().getVoltageLevel().getNominalV();
                } else {
                    Generator generator = network.getGenerator(attrId.getEquipmentId());
                    if (generator != null) {
                        entityType = 3;
                        entityNum = mapper.getInt(AmplSubset.GENERATOR, attrId.getEquipmentId());
                        nomV = generator.getTerminal().getVoltageLevel().getNominalV();
                    } else {
                        ShuntCompensator shunt = network.getShunt(attrId.getEquipmentId());
                        if (shunt != null) {
                            entityType = 4;
                            entityNum = mapper.getInt(AmplSubset.SHUNT, attrId.getEquipmentId());
                            nomV = shunt.getTerminal().getVoltageLevel().getNominalV();
                        } else {
                            VoltageLevel vl = network.getVoltageLevel(attrId.getEquipmentId());
                            if (vl != null) {
                                entityType = 5;
                                entityNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, attrId.getEquipmentId());
                                nomV = vl.getNominalV();
                            } else {
                                return null;
                            }
                        }
                    }
                }
            }
        }
        return new WCAEntity(entityType, entityNum, sideNum, nomV);
    }

    public void write() {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream(SECURITY_RULES_FILE_SUFFIX, TXT_EXT, false), StandardCharsets.UTF_8)) {
            final TableFormatter formatter = new TableFormatter(LOCALE, writer, "Security rules", INVALID_FLOAT_VALUE,
                    new Column("inequality num"),
                    new Column("convex num"),
                    new Column("var type (1: P, 2: Q, 3: V)"),
                    new Column("entity type (1: branch, 2: load, 3: generator, 4: compensator shunt, 5: substation)"),
                    new Column("entity num"),
                    new Column("branch side (1 or 2, 0 if NA)"),
                    new Column("inequality coeff."),
                    new Column("constant value"),
                    new Column("contingency num"),
                    new Column("security index type"),
                    new Column("attribute set (0: active only, 1: active/reactive)"));
            formatter.writeHeader();

            class Context {

                int inegNum;
                int convexNum;

                Context(int inegNum, int convexNum) {
                    this.inegNum = inegNum;
                    this.convexNum = convexNum;
                }
            }

            Context context = new Context(1, 1);

            for (SecurityRuleExpression rule : rules) {
                RuleId ruleId = rule.getRuleId();
                SecurityIndexType securityIndexType = ruleId.getSecurityIndexId().getSecurityIndexType();
                RuleAttributeSet attributeSet = ruleId.getAttributeSet();
                final int indexTypeNum = securityIndexType.ordinal();
                final int attributeSetNum = attributeSet.ordinal();

                if (rule.getStatus() == SecurityRuleStatus.ALWAYS_UNSECURE) {
                    if ( stopWcaOnViolations )
                        throw new RuntimeException("Always unsecure rule " + ruleId);
                    else
                        continue;
                }
                if (rule.getStatus() == SecurityRuleStatus.ALWAYS_SECURE) {
                    continue;
                }
                ExpressionNode expr = rule.getCondition();
                if (expr != null) {
                    if (debug) {
                        System.out.println(ExpressionTreePrinter.toString(expr));
                    }

                    // for debugging write security expression as a comment
                    formatter.writeComment(ExpressionFlatPrinter.toString(expr));

                    expr.accept(new ExpressionVisitor<Void, Context>() {

                        @Override
                        public Void visit(Attribute attr, Context context) {
                            throw new AssertionError();
                        }

                        @Override
                        public Void visit(Litteral value, Context context) {
                            throw new AssertionError();
                        }

                        @Override
                        public Void visit(ComparisonOperator comp, Context context) {
                            try {
                                HistoDbNetworkAttributeId attrId = (HistoDbNetworkAttributeId) comp.getNode1().getId();

                                WCAEntity entity = toEntity(attrId, network, mapper);
                                if (entity == null) {
                                    LOGGER.warn("Equipment '" + attrId.getEquipmentId() + "' not found");
                                    return null;
                                }

                                double threshold = comp.getNode2().getValue();
                                int varType;
                                switch (attrId.getAttributeType()) {
                                    case P:
                                        varType = 1;
                                        break;
                                    case Q:
                                        varType = 2;
                                        break;
                                    case V:
                                        varType = 3;
                                        break;
                                    default:
                                        throw new RuntimeException("Unsupported attribute type '" + attrId.getAttributeType());
                                }
                                formatter.writeCell(context.inegNum++) // inequality num
                                        .writeCell(context.convexNum) // convex num
                                        .writeCell(varType); // var type

                                formatter.writeCell(entity.type) // entity type
                                        .writeCell(entity.num) // entity num
                                        .writeCell(entity.sideNum); // branch side

                                double thresholdPu;
                                if (attrId.getAttributeType() == HistoDbAttr.V) {
                                    thresholdPu = threshold / entity.nomV;
                                } else {
                                    thresholdPu = threshold;
                                }
                                switch (comp.getType()) {
                                    case LESS:
                                        formatter.writeCell(1f); // inequality coeff
                                        formatter.writeCell(thresholdPu);
                                        break;
                                    case GREATER_EQUAL:
                                        formatter.writeCell(-1f); // inequality coeff
                                        formatter.writeCell(-thresholdPu);
                                        break;
                                    default:
                                        throw new InternalError();
                                }
                                formatter.writeCell(mapper.getInt(AmplSubset.FAULT, ruleId.getSecurityIndexId().getContingencyId()))
                                        .writeCell(indexTypeNum)
                                        .writeCell(attributeSetNum)
                                        .newRow();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return null;
                        }

                        @Override
                        public Void visit(AndOperator and, Context context) {
                            and.getNode1().accept(this, context);
                            and.getNode2().accept(this, context);
                            return null;
                        }

                        @Override
                        public Void visit(OrOperator or, Context context) {
                            context.convexNum++;
                            or.getNode1().accept(this, context);
                            context.convexNum++;
                            or.getNode2().accept(this, context);
                            return null;
                        }
                    }, context);

                    context.convexNum++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
