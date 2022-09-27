/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ShuntXml extends AbstractConnectableXml<ShuntCompensator, ShuntCompensatorAdder, VoltageLevel> {

    static final ShuntXml INSTANCE = new ShuntXml();

    static final String ROOT_ELEMENT_NAME = "shunt";

    private static final String B_PER_SECTION = "bPerSection";
    private static final String MAXIMUM_SECTION_COUNT = "maximumSectionCount";
    private static final String REGULATING_TERMINAL = "regulatingTerminal";
    private static final String SHUNT_LINEAR_MODEL = "shuntLinearModel";
    private static final String SHUNT_NON_LINEAR_MODEL = "shuntNonLinearModel";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(ShuntCompensator sc) {
        return sc != sc.getRegulatingTerminal().getConnectable();
    }

    @Override
    protected boolean hasSubElements(ShuntCompensator sc, NetworkXmlWriterContext context) {
        return hasSubElements(sc) || context.getVersion().compareTo(IidmXmlVersion.V_1_3) >= 0;
    }

    @Override
    protected void writeRootElementAttributes(ShuntCompensator sc, VoltageLevel vl, NetworkXmlWriterContext context) {
        if (ShuntCompensatorModelType.NON_LINEAR == sc.getModelType()) {
            IidmXmlUtil.assertMinimumVersion(getRootElementName(), SHUNT_NON_LINEAR_MODEL, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
        }
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_2, context, () -> {
            ShuntCompensatorModel model = sc.getModel();
            double bPerSection = model instanceof ShuntCompensatorLinearModel ? ((ShuntCompensatorLinearModel) model).getBPerSection() : sc.getB();
            context.getWriter().writeDoubleAttribute(B_PER_SECTION, bPerSection);
            int maximumSectionCount = model instanceof ShuntCompensatorLinearModel ? sc.getMaximumSectionCount() : 1;
            context.getWriter().writeIntAttribute(MAXIMUM_SECTION_COUNT, maximumSectionCount);
            int currentSectionCount = model instanceof ShuntCompensatorLinearModel ? sc.getSectionCount() : 1;
            context.getWriter().writeIntAttribute("currentSectionCount", currentSectionCount);
        });
        if (sc.findSectionCount().isPresent()) {
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> context.getWriter().writeIntAttribute("sectionCount", sc.getSectionCount()));
        }
        IidmXmlUtil.writeBooleanAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "voltageRegulatorOn", sc.isVoltageRegulatorOn(), false, IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
        IidmXmlUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "targetV", sc.getTargetV(),
                IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
        IidmXmlUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "targetDeadband",
                sc.getTargetDeadband(), IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
        writeNodeOrBus(null, sc.getTerminal(), context);
        writePQ(null, sc.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(ShuntCompensator sc, VoltageLevel vl, NetworkXmlWriterContext context) {
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> writeModel(sc, context));
        if (sc != sc.getRegulatingTerminal().getConnectable()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> TerminalRefXml.writeTerminalRef(sc.getRegulatingTerminal(), context, REGULATING_TERMINAL));
        }
    }

    private static void writeModel(ShuntCompensator sc, NetworkXmlWriterContext context) {
        if (sc.getModelType() == ShuntCompensatorModelType.LINEAR) {
            context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), SHUNT_LINEAR_MODEL);
            context.getWriter().writeDoubleAttribute(B_PER_SECTION, sc.getModel(ShuntCompensatorLinearModel.class).getBPerSection());
            context.getWriter().writeDoubleAttribute("gPerSection", sc.getModel(ShuntCompensatorLinearModel.class).getGPerSection());
            context.getWriter().writeIntAttribute(MAXIMUM_SECTION_COUNT, sc.getMaximumSectionCount());
            context.getWriter().writeEndNode();
        } else if (sc.getModelType() == ShuntCompensatorModelType.NON_LINEAR) {
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
                context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), SHUNT_NON_LINEAR_MODEL);
                for (ShuntCompensatorNonLinearModel.Section s : sc.getModel(ShuntCompensatorNonLinearModel.class).getAllSections()) {
                    context.getWriter().writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), "section");
                    context.getWriter().writeDoubleAttribute("b", s.getB());
                    context.getWriter().writeDoubleAttribute("g", s.getG());
                    context.getWriter().writeEndNode();
                }
                context.getWriter().writeEndNode();
            });
        } else {
            throw new PowsyblException("Unexpected shunt type " + sc.getModelType() + " for shunt " + sc.getId());
        }
    }

    @Override
    protected ShuntCompensatorAdder createAdder(VoltageLevel vl) {
        return vl.newShuntCompensator();
    }

    @Override
    protected ShuntCompensator readRootElementAttributes(ShuntCompensatorAdder adder, NetworkXmlReaderContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void readElement(String id, ShuntCompensatorAdder adder, NetworkXmlReaderContext context) throws XMLStreamException {
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> {
            boolean voltageRegulatorOn = context.getReader().readBooleanAttribute("voltageRegulatorOn");
            double targetV = context.getReader().readDoubleAttribute("targetV");
            double targetDeadband = context.getReader().readDoubleAttribute("targetDeadband");
            adder.setTargetV(targetV)
                    .setTargetDeadband(targetDeadband)
                    .setVoltageRegulatorOn(voltageRegulatorOn);
        });
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_1, context, () -> adder.setVoltageRegulatorOn(false));
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_2, context, () -> {
            double bPerSection = context.getReader().readDoubleAttribute(B_PER_SECTION);
            int maximumSectionCount = context.getReader().readIntAttribute(MAXIMUM_SECTION_COUNT);
            int sectionCount = context.getReader().readIntAttribute("currentSectionCount");
            adder.setSectionCount(sectionCount);
            adder.newLinearModel()
                    .setBPerSection(bPerSection)
                    .setMaximumSectionCount(maximumSectionCount)
                    .add();
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
            Integer sectionCount = context.getReader().readIntAttribute("sectionCount");
            if (sectionCount != null) {
                adder.setSectionCount(sectionCount);
            }
        });
        readNodeOrBus(adder, context);
        double p = context.getReader().readDoubleAttribute("p");
        double q = context.getReader().readDoubleAttribute("q");
        String[] regId = new String[1];
        String[] regSide = new String[1];
        Map<String, String> properties = new HashMap<>();
        Map<String, String> aliases = new HashMap<>();
        context.getReader().readUntilEndNode(getRootElementName(), () -> {
            switch (context.getReader().getNodeName()) {
                case REGULATING_TERMINAL:
                    regId[0] = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute("id"));
                    regSide[0] = context.getReader().readStringAttribute("side");
                    break;
                case PropertiesXml.PROPERTY:
                    String name = context.getReader().readStringAttribute("name");
                    String value = context.getReader().readStringAttribute("value");
                    properties.put(name, value);
                    break;
                case AliasesXml.ALIAS:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, AliasesXml.ALIAS, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
                    String aliasType = context.getReader().readStringAttribute("type");
                    String alias = context.getAnonymizer().deanonymizeString(context.getReader().readContent());
                    aliases.put(alias, aliasType);
                    break;
                case SHUNT_LINEAR_MODEL:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, SHUNT_LINEAR_MODEL, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
                    double bPerSection = context.getReader().readDoubleAttribute(B_PER_SECTION);
                    double gPerSection = context.getReader().readDoubleAttribute("gPerSection");
                    int maximumSectionCount = context.getReader().readIntAttribute(MAXIMUM_SECTION_COUNT);
                    adder.newLinearModel()
                            .setBPerSection(bPerSection)
                            .setGPerSection(gPerSection)
                            .setMaximumSectionCount(maximumSectionCount)
                            .add();
                    break;
                case SHUNT_NON_LINEAR_MODEL:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, SHUNT_NON_LINEAR_MODEL, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
                    ShuntCompensatorNonLinearModelAdder modelAdder = adder.newNonLinearModel();
                    context.getReader().readUntilEndNode(SHUNT_NON_LINEAR_MODEL, () -> {
                        if ("section".equals(context.getReader().getNodeName())) {
                            double b = context.getReader().readDoubleAttribute("b");
                            double g = context.getReader().readDoubleAttribute("g");
                            modelAdder.beginSection()
                                    .setB(b)
                                    .setG(g)
                                    .endSection();
                        } else {
                            throw new PowsyblException("Unknown element name <" + context.getReader().getNodeName() + "> in <" + id + ">");
                        }
                    });
                    modelAdder.add();
                    break;
                default:
                    throw new PowsyblException("Unknown element name <" + context.getReader().getNodeName() + "> in <" + id + ">");
            }
        });
        ShuntCompensator sc = adder.add();
        if (regId[0] != null) {
            context.getEndTasks().add(() -> sc.setRegulatingTerminal(TerminalRefXml.readTerminalRef(sc.getNetwork(), regId[0], regSide[0])));
        }
        properties.forEach(sc::setProperty);
        aliases.forEach(sc::addAlias);
        sc.getTerminal().setP(p).setQ(q);
    }
}
