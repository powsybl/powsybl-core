/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;

import static com.powsybl.iidm.serde.ConnectableSerDeUtil.readNodeOrBus;
import static com.powsybl.iidm.serde.ConnectableSerDeUtil.writeNodeOrBus;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ShuntSerDe extends AbstractComplexIdentifiableSerDe<ShuntCompensator, ShuntCompensatorAdder, VoltageLevel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShuntSerDe.class);

    static final ShuntSerDe INSTANCE = new ShuntSerDe();

    static final String ROOT_ELEMENT_NAME = "shunt";
    static final String ARRAY_ELEMENT_NAME = "shunts";

    private static final String B_PER_SECTION = "bPerSection";
    private static final String MAXIMUM_SECTION_COUNT = "maximumSectionCount";
    private static final String REGULATING_TERMINAL = "regulatingTerminal";
    private static final String SHUNT_LINEAR_MODEL = "shuntLinearModel";
    private static final String SHUNT_NON_LINEAR_MODEL = "shuntNonLinearModel";
    static final String SECTION_ARRAY_ELEMENT_NAME = "sections";
    static final String SECTION_ROOT_ELEMENT_NAME = "section";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected void writeRootElementAttributes(ShuntCompensator sc, VoltageLevel vl, NetworkSerializerContext context) {
        if (ShuntCompensatorModelType.NON_LINEAR == sc.getModelType()) {
            IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, SHUNT_NON_LINEAR_MODEL, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
        }
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
            int currentSectionCount = model instanceof ShuntCompensatorLinearModel ? sc.getSectionCount() : 1;
            context.getWriter().writeIntAttribute("currentSectionCount", currentSectionCount);
        });

        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> {
            OptionalInt sectionCount = sc.findSectionCount();
            context.getWriter().writeOptionalIntAttribute("sectionCount", sectionCount.isPresent() ? sectionCount.getAsInt() : null);
        });
        IidmSerDeUtil.writeBooleanAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "voltageRegulatorOn", sc.isVoltageRegulatorOn(), false, IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_2, context);
        IidmSerDeUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "targetV", sc.getTargetV(),
                IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_2, context);
        IidmSerDeUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "targetDeadband",
                sc.getTargetDeadband(), IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_2, context);
        writeNodeOrBus(null, sc.getTerminal(), context);
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
            IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmSerDeUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_2, context);
            IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_2, context, () -> TerminalRefSerDe.writeTerminalRef(sc.getRegulatingTerminal(), context, REGULATING_TERMINAL));
        }
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
            context.getWriter().writeEndNode();
        } else if (sc.getModelType() == ShuntCompensatorModelType.NON_LINEAR) {
            IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> {
                context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), SHUNT_NON_LINEAR_MODEL);
                context.getWriter().writeStartNodes();
                for (ShuntCompensatorNonLinearModel.Section s : sc.getModel(ShuntCompensatorNonLinearModel.class).getAllSections()) {
                    context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), SECTION_ROOT_ELEMENT_NAME);
                    context.getWriter().writeDoubleAttribute("b", s.getB());
                    context.getWriter().writeDoubleAttribute("g", s.getG());
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
    protected void readRootElementAttributes(ShuntCompensatorAdder adder, VoltageLevel parent, List<Consumer<ShuntCompensator>> toApply, NetworkDeserializerContext context) {
        IidmSerDeUtil.runUntilMaximumVersion(IidmVersion.V_1_1, context, () -> adder.setVoltageRegulatorOn(false));
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
            OptionalInt sectionCount = context.getReader().readOptionalIntAttribute("sectionCount");
            sectionCount.ifPresent(adder::setSectionCount);
        });
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_2, context, () -> {
            boolean voltageRegulatorOn = context.getReader().readBooleanAttribute("voltageRegulatorOn");
            double targetV = context.getReader().readDoubleAttribute("targetV");
            double targetDeadband = context.getReader().readDoubleAttribute("targetDeadband");
            adder.setTargetV(targetV)
                    .setTargetDeadband(targetDeadband)
                    .setVoltageRegulatorOn(voltageRegulatorOn);
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
    protected void readSubElements(String id, ShuntCompensatorAdder adder, List<Consumer<ShuntCompensator>> toApply, NetworkDeserializerContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case REGULATING_TERMINAL -> {
                    String regId = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("id"));
                    ThreeSides regSide = context.getReader().readEnumAttribute("side", ThreeSides.class);
                    context.getReader().readEndNode();
                    toApply.add(sc -> context.getEndTasks().add(() -> sc.setRegulatingTerminal(TerminalRefSerDe.resolve(regId, regSide, sc.getNetwork()))));
                }
                case SHUNT_LINEAR_MODEL -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, SHUNT_LINEAR_MODEL, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
                    double bPerSection = context.getReader().readDoubleAttribute(B_PER_SECTION);
                    double gPerSection = context.getReader().readDoubleAttribute("gPerSection");
                    int maximumSectionCount = context.getReader().readIntAttribute(MAXIMUM_SECTION_COUNT);
                    context.getReader().readEndNode();
                    adder.newLinearModel()
                            .setBPerSection(bPerSection)
                            .setGPerSection(gPerSection)
                            .setMaximumSectionCount(maximumSectionCount)
                            .add();
                }
                case SHUNT_NON_LINEAR_MODEL -> {
                    IidmSerDeUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, SHUNT_NON_LINEAR_MODEL, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
                    ShuntCompensatorNonLinearModelAdder modelAdder = adder.newNonLinearModel();
                    context.getReader().readChildNodes(nodeName -> {
                        if (SECTION_ROOT_ELEMENT_NAME.equals(nodeName)) {
                            double b = context.getReader().readDoubleAttribute("b");
                            double g = context.getReader().readDoubleAttribute("g");
                            modelAdder.beginSection()
                                    .setB(b)
                                    .setG(g)
                                    .endSection();
                            context.getReader().readEndNode();
                        } else {
                            throw new PowsyblException("Unknown element name '" + nodeName + "' in '" + id + "'");
                        }
                    });
                    modelAdder.add();
                }
                default -> readSubElement(elementName, id, toApply, context);
            }
        });
    }
}
