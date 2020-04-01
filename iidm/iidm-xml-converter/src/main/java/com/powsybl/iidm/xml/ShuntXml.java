/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ShuntXml extends AbstractConnectableXml<ShuntCompensator, ShuntCompensatorAdder, VoltageLevel> {

    static final ShuntXml INSTANCE = new ShuntXml();

    static final String ROOT_ELEMENT_NAME = "shunt";

    private static final String B_PER_SECTION = "bPerSection";
    private static final String G_PER_SECTION = "gPerSection";
    private static final String MAXIMUM_SECTION_COUNT = "maximumSectionCount";
    private static final String MODEL = "model";
    private static final String REGULATING_TERMINAL = "regulatingTerminal";
    private static final String SECTION = "section";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(ShuntCompensator sc) {
        return ShuntCompensatorModelType.NON_LINEAR.equals(sc.getModelType()) || sc != sc.getRegulatingTerminal().getConnectable();
    }

    @Override
    protected void writeRootElementAttributes(ShuntCompensator sc, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        IidmXmlUtil.assertMinimumVersionIfNotDefault(ShuntCompensatorModelType.NON_LINEAR.equals(sc.getModelType()), ROOT_ELEMENT_NAME,
                SECTION, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> {
            try {
                context.getWriter().writeAttribute(MODEL, sc.getModelType().name());
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });

        if (ShuntCompensatorModelType.LINEAR.equals(sc.getModelType())) {
            XmlUtil.writeDouble(B_PER_SECTION, sc.getModel(ShuntCompensatorLinearModel.class).getbPerSection(), context.getWriter());
            if (!Double.isNaN(sc.getModel(ShuntCompensatorLinearModel.class).getgPerSection())) {
                IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, G_PER_SECTION, IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
                XmlUtil.writeDouble(G_PER_SECTION, sc.getModel(ShuntCompensatorLinearModel.class).getgPerSection(), context.getWriter());
            }
            context.getWriter().writeAttribute(MAXIMUM_SECTION_COUNT, Integer.toString(sc.getMaximumSectionCount()));
        }
        context.getWriter().writeAttribute("currentSectionCount", Integer.toString(sc.getCurrentSectionCount()));
        IidmXmlUtil.writeBooleanAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "voltageRegulatorOn", sc.isVoltageRegulatorOn(), false,
                IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
        IidmXmlUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "targetV", sc.getTargetV(),
                IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
        IidmXmlUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "targetDeadband",
                sc.getTargetDeadband(), IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
        writeNodeOrBus(null, sc.getTerminal(), context);
        writePQ(null, sc.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(ShuntCompensator sc, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        if (ShuntCompensatorModelType.NON_LINEAR.equals(sc.getModelType())) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, SECTION, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
            writeSections(sc, context);
        }
        if (sc != sc.getRegulatingTerminal().getConnectable()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
            TerminalRefXml.writeTerminalRef(sc.getRegulatingTerminal(), context, REGULATING_TERMINAL);
        }
    }

    private static void writeSections(ShuntCompensator sc, NetworkXmlWriterContext context) {
        sc.getModel(ShuntCompensatorNonLinearModel.class).getSections().forEach((sectionNum, section) -> {
            try {
                context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(), SECTION);
                context.getWriter().writeAttribute("num", Integer.toString(sectionNum));
                XmlUtil.writeDouble("b", section.getB(), context.getWriter());
                XmlUtil.writeDouble("g", section.getG(), context.getWriter());
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
    }

    @Override
    protected ShuntCompensatorAdder createAdder(VoltageLevel vl) {
        return vl.newShuntCompensator();
    }

    @Override
    protected ShuntCompensator readRootElementAttributes(ShuntCompensatorAdder adder, NetworkXmlReaderContext context) {
        int currentSectionCount = XmlUtil.readIntAttribute(context.getReader(), "currentSectionCount");
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_1, context, () -> {
            double bPerSection = XmlUtil.readDoubleAttribute(context.getReader(), B_PER_SECTION);
            int maximumSectionCount = XmlUtil.readIntAttribute(context.getReader(), MAXIMUM_SECTION_COUNT);
            adder.newLinearModel()
                    .setMaximumSectionCount(maximumSectionCount)
                    .setbPerSection(bPerSection)
                    .add();
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> {
            boolean voltageRegulatorOn = XmlUtil.readBoolAttribute(context.getReader(), "voltageRegulatorOn");
            double targetV = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetV");
            double targetDeadband = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetDeadband");
            adder.setVoltageRegulatorOn(voltageRegulatorOn)
                    .setTargetV(targetV)
                    .setTargetDeadband(targetDeadband);
            ShuntCompensatorModelType modelType = ShuntCompensatorModelType.valueOf(context.getReader().getAttributeValue(null, MODEL));
            if (ShuntCompensatorModelType.LINEAR.equals(modelType)) {
                double bPerSection = XmlUtil.readDoubleAttribute(context.getReader(), B_PER_SECTION);
                double gPerSection = XmlUtil.readOptionalDoubleAttribute(context.getReader(), G_PER_SECTION);
                int maximumSectionCount = XmlUtil.readIntAttribute(context.getReader(), MAXIMUM_SECTION_COUNT);
                adder.newLinearModel()
                        .setMaximumSectionCount(maximumSectionCount)
                        .setbPerSection(bPerSection)
                        .setgPerSection(gPerSection)
                        .add();
            } else if (ShuntCompensatorModelType.NON_LINEAR.equals(modelType)) {
                adder.newNonLinearModel()
                        .beginSection()
                            .setSectionNum(currentSectionCount)
                            .setB(Double.MIN_VALUE) // default value for non linear shunt (always overwritten)
                        .endSection()
                        .add();
            } else {
                throw new PowsyblException(String.format("Unexpected model type: %s", modelType));
            }
        });
        adder.setCurrentSectionCount(currentSectionCount);
        readNodeOrBus(adder, context);
        ShuntCompensator sc = adder.add();
        readPQ(null, sc.getTerminal(), context.getReader());
        return sc;
    }

    @Override
    protected void readSubElements(ShuntCompensator sc, NetworkXmlReaderContext context) throws XMLStreamException {
        boolean[] hasCurrentSection = new boolean[1];
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case SECTION:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, SECTION, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
                    hasCurrentSection[0] = hasCurrentSection[0] || readSection(sc, context);
                    break;
                case REGULATING_TERMINAL:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
                    String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
                    String side = context.getReader().getAttributeValue(null, "side");
                    context.getEndTasks().add(() -> sc.setRegulatingTerminal(TerminalRefXml.readTerminalRef(sc.getTerminal().getVoltageLevel().getSubstation().getNetwork(), id, side)));
                    break;
                default:
                    super.readSubElements(sc, context);
            }
        });
        if (!hasCurrentSection[0] && ShuntCompensatorModelType.NON_LINEAR.equals(sc.getModelType())) {
            throw new PowsyblException("Missing section for current section of " + sc.getId());
        }
    }

    private static boolean readSection(ShuntCompensator sc, NetworkXmlReaderContext context) {
        int sectionNum = XmlUtil.readIntAttribute(context.getReader(), "num");
        double b = XmlUtil.readDoubleAttribute(context.getReader(), "b");
        double g = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "g");
        sc.getModel(ShuntCompensatorNonLinearModel.class)
                .addOrReplaceSection(sectionNum, b, g);
        return sc.getCurrentSectionCount() == sectionNum;
    }
}
