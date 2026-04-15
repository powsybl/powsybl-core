/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.readNodeOrBus;
import static com.powsybl.iidm.serde.ConnectableSerDeUtil.writeNodeOrBus;

/**
 * Abstract class for serializing/deserializing shunt compensator
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Samir Romdhani {@literal <samir.romdhani_externe at rte-france.com>}
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
abstract class AbstractShuntCompensatorSerDe extends AbstractComplexIdentifiableSerDe<ShuntCompensator, ShuntCompensatorAdder, VoltageLevel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractShuntCompensatorSerDe.class);

    static final String SECTION_ARRAY_ELEMENT_NAME = "sections";
    static final String SECTION_ROOT_ELEMENT_NAME = "section";

    private static final String B_PER_SECTION = "bPerSection";
    private static final String MAXIMUM_SECTION_COUNT = "maximumSectionCount";
    private static final String REGULATING_TERMINAL = "regulatingTerminal";
    private static final String SHUNT_LINEAR_MODEL = "shuntLinearModel";
    private static final String SHUNT_NON_LINEAR_MODEL = "shuntNonLinearModel";
    private static final String SECTION_COUNT = "sectionCount";

    private final String rootElementName;
    private final IidmVersion minVersionInclusive;
    private final IidmVersion maxVersionInclusive;

    protected AbstractShuntCompensatorSerDe(String rootElementName, IidmVersion minVersionInclusive, IidmVersion maxVersionInclusive) {
        this.rootElementName = rootElementName;
        this.minVersionInclusive = minVersionInclusive;
        this.maxVersionInclusive = maxVersionInclusive;
    }

    @Override
    protected String getRootElementName() {
        return rootElementName;
    }

    @Override
    protected void readRootElementAttributes(ShuntCompensatorAdder adder, VoltageLevel parent, List<Consumer<ShuntCompensator>> toApply, NetworkDeserializerContext context) {
        assertReadCompatibility(context);
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_1, context, () -> adder.newVoltageRegulation().withRegulating(false).add());
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_2, context, () -> {
            double bPerSection = context.getReader().readDoubleAttribute(B_PER_SECTION);
            int maximumSectionCount = context.getReader().readIntAttribute(MAXIMUM_SECTION_COUNT);
            int sectionCount = context.getReader().readIntAttribute("currentSectionCount");
            adder.setSectionCount(sectionCount);
            adder.newLinearModel()
                .setBPerSection(bPerSection)
                .setMaximumSectionCount(maximumSectionCount)
                .add();
        });
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> {
            OptionalInt sectionCount = context.getReader().readOptionalIntAttribute(SECTION_COUNT);
            sectionCount.ifPresent(adder::setSectionCount);
        });
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_14, context, () -> {
            OptionalInt solvedSectionCount = context.getReader().readOptionalIntAttribute("solvedSectionCount");
            solvedSectionCount.ifPresent(adder::setSolvedSectionCount);
        });
        IidmSerDeUtil.runFromMinimumVersionAndUntilMaximumVersion(IidmVersion.V_1_2, IidmVersion.V_1_15, context, () -> {
            boolean voltageRegulatorOn = context.getReader().readBooleanAttribute("voltageRegulatorOn");
            double targetV = context.getReader().readDoubleAttribute("targetV");
            double targetDeadband = context.getReader().readDoubleAttribute("targetDeadband");
            adder.newVoltageRegulation()
                .withTargetValue(targetV)
                .withTargetDeadband(targetDeadband)
                .withRegulating(voltageRegulatorOn)
                .withMode(RegulationMode.VOLTAGE)
                .add();
        });
        readNodeOrBus(adder, context, parent.getTopologyKind());
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_8, context, () -> {
            double q = context.getReader().readDoubleAttribute("q");
            toApply.add(sc -> sc.getTerminal().setQ(q));
        });
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_9, context, () -> {
            double p = context.getReader().readDoubleAttribute("p");
            double q = context.getReader().readDoubleAttribute("q");
            toApply.add(sc -> sc.getTerminal().setP(p).setQ(q));
        });
    }

    @Override
    protected void writeRootElementAttributes(ShuntCompensator sc, VoltageLevel vl, NetworkSerializerContext context) {
        assertWriteCompatibility(context);
        OptionalInt sectionCount = sc.findSectionCount();
        OptionalInt solvedSectionCount = sc.findSolvedSectionCount();
        assertModelCompatibility(rootElementName, sc, context);
        writeLegacySectionAttributes(sc, solvedSectionCount, context);
        writeSectionCountAttributes(sectionCount, solvedSectionCount, context);
        writeRegulationAttributes(rootElementName, sc, context);
        writeNodeOrBus(null, sc.getTerminal(), context);
        writePowerAttributes(sc, context);
    }

    private static void assertModelCompatibility(String rootElementName, ShuntCompensator sc, NetworkSerializerContext context) {
        if (ShuntCompensatorModelType.NON_LINEAR == sc.getModelType()) {
            IidmSerDeUtil.assertMinimumVersion(rootElementName, SHUNT_NON_LINEAR_MODEL, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
        }
    }

    private static void writeLegacySectionAttributes(ShuntCompensator sc, OptionalInt solvedSectionCount, NetworkSerializerContext context) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_2, context, () -> {
            ShuntCompensatorModel model = sc.getModel();
            double bPerSection = model instanceof ShuntCompensatorLinearModel shuntCompensatorLinearModel ? shuntCompensatorLinearModel.getBPerSection() : sc.getB();
            if (bPerSection == 0) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("bPerSection of {} is 0. It is set as {} since XIIDM version < 1.5 ({})", sc.getId(),
                            Double.MIN_NORMAL, context.getVersion().toString("."));
                }
                bPerSection = Double.MIN_NORMAL;
            }
            context.getWriter().writeDoubleAttribute(B_PER_SECTION, bPerSection);
            int maximumSectionCount = model instanceof ShuntCompensatorLinearModel ? sc.getMaximumSectionCount() : 1;
            context.getWriter().writeIntAttribute(MAXIMUM_SECTION_COUNT, maximumSectionCount);
            int currentSectionCount = model instanceof ShuntCompensatorLinearModel ? solvedSectionCount.orElse(sc.getSectionCount()) : 1;
            context.getWriter().writeIntAttribute("currentSectionCount", currentSectionCount);
        });
    }

    private static void writeSectionCountAttributes(OptionalInt sectionCount, OptionalInt solvedSectionCount, NetworkSerializerContext context) {
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> {
            IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_13, context, () -> {
                OptionalInt sectionCountToWrite = solvedSectionCount.isPresent() ? solvedSectionCount : sectionCount;
                context.getWriter().writeOptionalIntAttribute(SECTION_COUNT, sectionCountToWrite.isPresent() ? sectionCountToWrite.getAsInt() : null);
            });
            IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_14, context, () -> {
                context.getWriter().writeOptionalIntAttribute(SECTION_COUNT, sectionCount.isPresent() ? sectionCount.getAsInt() : null);
                context.getWriter().writeOptionalIntAttribute("solvedSectionCount", solvedSectionCount.isPresent() ? solvedSectionCount.getAsInt() : null);
            });
        });
    }

    private static void writeRegulationAttributes(String rootElementName, ShuntCompensator sc, NetworkSerializerContext context) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_15, context, () -> {
            IidmSerDeUtil.writeBooleanAttributeFromMinimumVersion(rootElementName, "voltageRegulatorOn", sc.isRegulatingWithMode(RegulationMode.VOLTAGE), false, IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_2, context);
            IidmSerDeUtil.writeDoubleAttributeFromMinimumVersion(rootElementName, "targetV", sc.getRegulatingTargetV(),
                IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_2, context);
            double targetDeadband = sc.getVoltageRegulation() != null ? sc.getVoltageRegulation().getTargetDeadband() : Double.NaN;
            IidmSerDeUtil.writeDoubleAttributeFromMinimumVersion(rootElementName, "targetDeadband",
                targetDeadband, IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_2, context);
        });
    }

    private static void writePowerAttributes(ShuntCompensator sc, NetworkSerializerContext context) {
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_9, context, () -> context.getWriter().writeDoubleAttribute("p", sc.getTerminal().getP(), Double.NaN));
        context.getWriter().writeDoubleAttribute("q", sc.getTerminal().getQ(), Double.NaN);
    }

    private static double getBPerSection(ShuntCompensator sc, IidmVersion version) {
        ShuntCompensatorModel model = sc.getModel();
        double bPerSection = model instanceof ShuntCompensatorLinearModel shuntCompensatorLinearModel ? shuntCompensatorLinearModel.getBPerSection() : sc.getB();
        if (bPerSection == 0 && version.compareTo(IidmVersion.V_1_4) <= 0) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("bPerSection of {} is 0. It is set as {} since XIIDM version < 1.5 ({})", sc.getId(),
                        Double.MIN_NORMAL, version.toString("."));
            }
            bPerSection = Double.MIN_NORMAL;
        }
        return bPerSection;
    }

    @Override
    protected void writeSubElements(ShuntCompensator sc, VoltageLevel vl, NetworkSerializerContext context) {
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> writeModel(sc, context));
        if (sc != sc.getRegulatingTerminal().getConnectable()) {
            IidmSerDeUtil.assertMinimumVersion(rootElementName, REGULATING_TERMINAL, IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_2, context);
            IidmSerDeUtil.runFromMinimumVersionAndUntilMaximumVersion(IidmVersion.V_1_2, IidmVersion.V_1_15, context, () -> TerminalRefSerDe.writeTerminalRef(sc.getRegulatingTerminal(), context, REGULATING_TERMINAL));
        }
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> VoltageRegulationSerDe.writeVoltageRegulation(sc.getVoltageRegulation(), context, sc));
    }

    @Override
    protected ShuntCompensatorAdder createAdder(VoltageLevel parent) {
        return parent.newShuntCompensator();
    }

    private static void writeModel(ShuntCompensator sc, NetworkSerializerContext context) {
        if (sc.getModelType() == ShuntCompensatorModelType.LINEAR) {
            context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), SHUNT_LINEAR_MODEL);
            context.getWriter().writeDoubleAttribute(B_PER_SECTION, getBPerSection(sc, context.getVersion()));
            context.getWriter().writeDoubleAttribute("gPerSection", sc.getModel(ShuntCompensatorLinearModel.class).getGPerSection());
            context.getWriter().writeIntAttribute(MAXIMUM_SECTION_COUNT, sc.getMaximumSectionCount());
            IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> PropertiesSerDe.write(sc.getModel(), context));
            context.getWriter().writeEndNode();
        } else if (sc.getModelType() == ShuntCompensatorModelType.NON_LINEAR) {
            IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> {
                context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), SHUNT_NON_LINEAR_MODEL);
                context.getWriter().writeStartNodes();
                IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> PropertiesSerDe.write(sc.getModel(), context));
                for (ShuntCompensatorNonLinearModel.Section s : sc.getModel(ShuntCompensatorNonLinearModel.class).getAllSections()) {
                    context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), SECTION_ROOT_ELEMENT_NAME);
                    context.getWriter().writeDoubleAttribute("b", s.getB());
                    context.getWriter().writeDoubleAttribute("g", s.getG());
                    IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_16, context, () -> PropertiesSerDe.write(s, context));
                    context.getWriter().writeEndNode();
                }
                context.getWriter().writeEndNodes();
                context.getWriter().writeEndNode();
            });
        } else {
            throw new PowsyblException("Unexpected shunt type " + sc.getModelType() + " for shunt " + sc.getId());
        }
    }

    @Override
    protected void readSubElements(String id, ShuntCompensatorAdder adder, List<Consumer<ShuntCompensator>> toApply, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case REGULATING_TERMINAL -> readRegulatingTerminal(toApply, context);
                case SHUNT_LINEAR_MODEL -> readShuntLinearModel(adder, context);
                case SHUNT_NON_LINEAR_MODEL -> readShuntNonLinearModel(id, adder, context);
                case VoltageRegulationSerDe.ELEMENT_NAME -> readVoltageRegulation(toApply, adder, context);
                default -> readSubElement(elementName, id, toApply, context);
            }
        });
    }

    private static void readRegulatingTerminal(List<Consumer<ShuntCompensator>> toApply, NetworkDeserializerContext context) {
        TerminalRefSerDe.TerminalData data = TerminalRefSerDe.readTerminalData(context);
        toApply.add(sc -> context.addEndTask(DeserializationEndTask.Step.AFTER_EXTENSIONS,
            () -> {
                if (sc.getVoltageRegulation() != null) {
                    sc.getVoltageRegulation().setTerminal(TerminalRefSerDe.resolve(data.id(), data.side(), data.number(), sc.getNetwork()));
                }
            }));
    }

    private void readShuntNonLinearModel(String id, ShuntCompensatorAdder adder, NetworkDeserializerContext context) {
        IidmSerDeUtil.assertMinimumVersion(rootElementName, SHUNT_NON_LINEAR_MODEL, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
        ShuntCompensatorNonLinearModelAdder modelAdder = adder.newNonLinearModel();
        context.getReader().readChildNodes(nodeName -> {
            if (PropertiesSerDe.ROOT_ELEMENT_NAME.equals(nodeName)) {
                PropertiesSerDe.read(modelAdder, context);
            } else if (SECTION_ROOT_ELEMENT_NAME.equals(nodeName)) {
                double b = context.getReader().readDoubleAttribute("b");
                double g = context.getReader().readDoubleAttribute("g");
                ShuntCompensatorNonLinearModelAdder.SectionAdder sectionAdder = modelAdder.beginSection()
                        .setB(b)
                        .setG(g);
                context.getReader().readChildNodes(sectionNodeName -> {
                    if (PropertiesSerDe.ROOT_ELEMENT_NAME.equals(sectionNodeName)) {
                        PropertiesSerDe.read(sectionAdder, context);
                    }
                });
                sectionAdder.endSection();
            } else {
                throw new PowsyblException("Unknown element name '" + nodeName + "' in '" + id + "'");
            }
        });
        modelAdder.add();
    }

    private void readShuntLinearModel(ShuntCompensatorAdder adder, NetworkDeserializerContext context) {
        IidmSerDeUtil.assertMinimumVersion(rootElementName, SHUNT_LINEAR_MODEL, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
        double bPerSection = context.getReader().readDoubleAttribute(B_PER_SECTION);
        double gPerSection = context.getReader().readDoubleAttribute("gPerSection");
        int maximumSectionCount = context.getReader().readIntAttribute(MAXIMUM_SECTION_COUNT);
        ShuntCompensatorLinearModelAdder linearAdder = adder.newLinearModel()
                .setBPerSection(bPerSection)
                .setGPerSection(gPerSection)
                .setMaximumSectionCount(maximumSectionCount);
        context.getReader().readChildNodes(nodeName -> {
            if (PropertiesSerDe.ROOT_ELEMENT_NAME.equals(nodeName)) {
                PropertiesSerDe.read(linearAdder, context);
            }
        });
        linearAdder.add();
    }

    private void readVoltageRegulation(List<Consumer<ShuntCompensator>> toApply, ShuntCompensatorAdder adder, NetworkDeserializerContext context) {
        VoltageRegulationSerDe.readVoltageRegulation(adder.newVoltageRegulation(), context);
        context.getReader().readChildNodes(subElementName -> {
            if (subElementName.equals(VoltageRegulationSerDe.TERMINAL)) {
                SubElementTerminalAttributes terminalAttributes = getSubElementTerminal(context);
                toApply.add(sc -> context.addEndTask(DeserializationEndTask.Step.AFTER_EXTENSIONS,
                    // The VoltageRegulation is not null here (was created juste before)
                    () -> sc.getVoltageRegulation().setTerminal(TerminalRefSerDe.resolve(terminalAttributes.regId(), terminalAttributes.regSide(), terminalAttributes.regNumber(), sc.getNetwork()))));

            } else {
                throw new PowsyblException("Unknown sub element name '" + subElementName + "' in 'voltageRegulation'");
            }
        });
    }

    private void assertReadCompatibility(NetworkDeserializerContext context) {
        if (minVersionInclusive != null) {
            IidmSerDeUtil.assertMinimumVersion(rootElementName, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, minVersionInclusive, context);
        }
        if (maxVersionInclusive != null) {
            IidmSerDeUtil.assertMaximumVersion(rootElementName, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, maxVersionInclusive, context);
        }
    }

    private void assertWriteCompatibility(NetworkSerializerContext context) {
        if (minVersionInclusive != null) {
            IidmSerDeUtil.assertMinimumVersion(rootElementName, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, minVersionInclusive, context);
        }
        if (maxVersionInclusive != null) {
            IidmSerDeUtil.assertMaximumVersion(rootElementName, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, maxVersionInclusive, context);
        }
    }

    private static SubElementTerminalAttributes getSubElementTerminal(NetworkDeserializerContext context) {
        String regId = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("id"));
        ThreeSides regSide = context.getReader().readEnumAttribute("side", ThreeSides.class);
        TerminalNumber regNumber = context.getReader().readEnumAttribute("number", TerminalNumber.class);
        context.getReader().readEndNode();
        return new SubElementTerminalAttributes(regId, regSide, regNumber);
    }

    private record SubElementTerminalAttributes(String regId, ThreeSides regSide, TerminalNumber regNumber) {
    }

}
