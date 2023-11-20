/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.serializer.util.IidmSerializerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

import static com.powsybl.iidm.serializer.ConnectableSerializerUtil.readNodeOrBus;
import static com.powsybl.iidm.serializer.ConnectableSerializerUtil.writeNodeOrBus;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ShuntSerializer extends AbstractComplexIdentifiableSerializer<ShuntCompensator, ShuntCompensatorAdder, VoltageLevel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShuntSerializer.class);

    static final ShuntSerializer INSTANCE = new ShuntSerializer();

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
    protected void writeRootElementAttributes(ShuntCompensator sc, VoltageLevel vl, NetworkSerializerWriterContext context) {
        if (ShuntCompensatorModelType.NON_LINEAR == sc.getModelType()) {
            IidmSerializerUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, SHUNT_NON_LINEAR_MODEL, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
        }
        IidmSerializerUtil.runUntilMaximumVersion(IidmVersion.V_1_2, context, () -> {
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
        if (sc.findSectionCount().isPresent()) {
            IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> context.getWriter().writeIntAttribute("sectionCount", sc.getSectionCount()));
        }
        IidmSerializerUtil.writeBooleanAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "voltageRegulatorOn", sc.isVoltageRegulatorOn(), false, IidmSerializerUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_2, context);
        IidmSerializerUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "targetV", sc.getTargetV(),
                IidmSerializerUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_2, context);
        IidmSerializerUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "targetDeadband",
                sc.getTargetDeadband(), IidmSerializerUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_2, context);
        writeNodeOrBus(null, sc.getTerminal(), context);
        IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_9, context, () -> context.getWriter().writeDoubleAttribute("p", sc.getTerminal().getP(), Double.NaN));
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
    protected void writeSubElements(ShuntCompensator sc, VoltageLevel vl, NetworkSerializerWriterContext context) {
        IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> writeModel(sc, context));
        if (sc != sc.getRegulatingTerminal().getConnectable()) {
            IidmSerializerUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmSerializerUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmVersion.V_1_2, context);
            IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_2, context, () -> TerminalRefSerializer.writeTerminalRef(sc.getRegulatingTerminal(), context, REGULATING_TERMINAL));
        }
    }

    @Override
    protected ShuntCompensatorAdder createAdder(VoltageLevel parent) {
        return parent.newShuntCompensator();
    }

    private static void writeModel(ShuntCompensator sc, NetworkSerializerWriterContext context) {
        if (sc.getModelType() == ShuntCompensatorModelType.LINEAR) {
            context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), SHUNT_LINEAR_MODEL);
            context.getWriter().writeDoubleAttribute(B_PER_SECTION, getBPerSection(sc, context.getVersion()));
            context.getWriter().writeDoubleAttribute("gPerSection", sc.getModel(ShuntCompensatorLinearModel.class).getGPerSection());
            context.getWriter().writeIntAttribute(MAXIMUM_SECTION_COUNT, sc.getMaximumSectionCount());
            context.getWriter().writeEndNode();
        } else if (sc.getModelType() == ShuntCompensatorModelType.NON_LINEAR) {
            IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> {
                context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), SHUNT_NON_LINEAR_MODEL);
                context.getWriter().writeStartNodes(SECTION_ARRAY_ELEMENT_NAME);
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
    protected void readRootElementAttributes(ShuntCompensatorAdder adder, List<Consumer<ShuntCompensator>> toApply, NetworkSerializerReaderContext context) {
        IidmSerializerUtil.runUntilMaximumVersion(IidmVersion.V_1_1, context, () -> adder.setVoltageRegulatorOn(false));
        IidmSerializerUtil.runUntilMaximumVersion(IidmVersion.V_1_2, context, () -> {
            double bPerSection = context.getReader().readDoubleAttribute(B_PER_SECTION);
            int maximumSectionCount = context.getReader().readIntAttribute(MAXIMUM_SECTION_COUNT);
            int sectionCount = context.getReader().readIntAttribute("currentSectionCount");
            adder.setSectionCount(sectionCount);
            adder.newLinearModel()
                    .setBPerSection(bPerSection)
                    .setMaximumSectionCount(maximumSectionCount)
                    .add();
        });
        IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_3, context, () -> {
            Integer sectionCount = context.getReader().readIntAttribute("sectionCount");
            if (sectionCount != null) {
                adder.setSectionCount(sectionCount);
            }
        });
        IidmSerializerUtil.runFromMinimumVersion(IidmVersion.V_1_2, context, () -> {
            boolean voltageRegulatorOn = context.getReader().readBooleanAttribute("voltageRegulatorOn");
            double targetV = context.getReader().readDoubleAttribute("targetV");
            double targetDeadband = context.getReader().readDoubleAttribute("targetDeadband");
            adder.setTargetV(targetV)
                    .setTargetDeadband(targetDeadband)
                    .setVoltageRegulatorOn(voltageRegulatorOn);
        });
        readNodeOrBus(adder, context);
        double p = context.getReader().readDoubleAttribute("p");
        double q = context.getReader().readDoubleAttribute("q");
        toApply.add(sc -> sc.getTerminal().setP(p).setQ(q));
    }

    @Override
    protected void readSubElements(String id, ShuntCompensatorAdder adder, List<Consumer<ShuntCompensator>> toApply, NetworkSerializerReaderContext context) {
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case REGULATING_TERMINAL -> {
                    String regId = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("id"));
                    String regSide = context.getReader().readStringAttribute("side");
                    context.getReader().readEndNode();
                    toApply.add(sc -> context.getEndTasks().add(() -> sc.setRegulatingTerminal(TerminalRefSerializer.resolve(regId, regSide, sc.getNetwork()))));
                }
                case SHUNT_LINEAR_MODEL -> {
                    IidmSerializerUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, SHUNT_LINEAR_MODEL, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
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
                    IidmSerializerUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, SHUNT_NON_LINEAR_MODEL, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_3, context);
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
