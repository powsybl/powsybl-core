/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.util.List;
import java.util.function.Consumer;

import static com.powsybl.iidm.xml.ConnectableXmlUtil.readNodeOrBus;
import static com.powsybl.iidm.xml.ConnectableXmlUtil.writeNodeOrBus;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ShuntXml extends AbstractComplexIdentifiableXml<ShuntCompensator, ShuntCompensatorAdder, VoltageLevel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShuntXml.class);

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
    protected void writeRootElementAttributes(ShuntCompensator sc, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        if (ShuntCompensatorModelType.NON_LINEAR == sc.getModelType()) {
            IidmXmlUtil.assertMinimumVersion(getRootElementName(), SHUNT_NON_LINEAR_MODEL, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
        }
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_2, context, () -> {
            ShuntCompensatorModel model = sc.getModel();
            double bPerSection = model instanceof ShuntCompensatorLinearModel shuntCompensatorLinearModel ? shuntCompensatorLinearModel.getBPerSection() : sc.getB();
            if (bPerSection == 0) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("bPerSection of {} is 0. It is set as {} since XIIDM version < 1.5 ({})", sc.getId(),
                            Double.MIN_NORMAL, context.getVersion().toString("."));
                }
                bPerSection = Double.MIN_NORMAL;
            }
            XmlUtil.writeDouble(B_PER_SECTION, bPerSection, context.getWriter());
            int maximumSectionCount = model instanceof ShuntCompensatorLinearModel ? sc.getMaximumSectionCount() : 1;
            context.getWriter().writeAttribute(MAXIMUM_SECTION_COUNT, Integer.toString(maximumSectionCount));
            int currentSectionCount = model instanceof ShuntCompensatorLinearModel ? sc.getSectionCount() : 1;
            context.getWriter().writeAttribute("currentSectionCount", Integer.toString(currentSectionCount));
        });
        if (sc.findSectionCount().isPresent()) {
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> context.getWriter().writeAttribute("sectionCount", Integer.toString(sc.getSectionCount())));
        }
        IidmXmlUtil.writeBooleanAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "voltageRegulatorOn", sc.isVoltageRegulatorOn(), false, IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
        IidmXmlUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "targetV", sc.getTargetV(),
                IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
        IidmXmlUtil.writeDoubleAttributeFromMinimumVersion(ROOT_ELEMENT_NAME, "targetDeadband",
                sc.getTargetDeadband(), IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
        writeNodeOrBus(null, sc.getTerminal(), context);
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_9, context, () -> XmlUtil.writeOptionalDouble("p", sc.getTerminal().getP(), Double.NaN, context.getWriter()));
        XmlUtil.writeOptionalDouble("q", sc.getTerminal().getQ(), Double.NaN, context.getWriter());
    }

    @Override
    protected void writeSubElements(ShuntCompensator sc, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> writeModel(sc, context));
        if (sc != sc.getRegulatingTerminal().getConnectable()) {
            IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, REGULATING_TERMINAL, IidmXmlUtil.ErrorMessage.NOT_DEFAULT_NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> TerminalRefXml.writeTerminalRef(sc.getRegulatingTerminal(), context, REGULATING_TERMINAL));
        }
    }

    @Override
    protected ShuntCompensatorAdder createAdder(VoltageLevel parent) {
        return parent.newShuntCompensator();
    }

    private static void writeModel(ShuntCompensator sc, NetworkXmlWriterContext context) throws XMLStreamException {
        if (sc.getModelType() == ShuntCompensatorModelType.LINEAR) {
            context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(context.isValid()), SHUNT_LINEAR_MODEL);
            double bPerSection = sc.getModel(ShuntCompensatorLinearModel.class).getBPerSection();
            if (bPerSection == 0 && context.getVersion().compareTo(IidmXmlVersion.V_1_4) <= 0) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("bPerSection of {} is 0. It is set as {} since XIIDM version < 1.5 ({})", sc.getId(),
                            Double.MIN_NORMAL, context.getVersion().toString("."));
                }
                bPerSection = Double.MIN_NORMAL;
            }
            XmlUtil.writeDouble(B_PER_SECTION, bPerSection, context.getWriter());
            XmlUtil.writeDouble("gPerSection", sc.getModel(ShuntCompensatorLinearModel.class).getGPerSection(), context.getWriter());
            context.getWriter().writeAttribute(MAXIMUM_SECTION_COUNT, Integer.toString(sc.getMaximumSectionCount()));
        } else if (sc.getModelType() == ShuntCompensatorModelType.NON_LINEAR) {
            IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
                context.getWriter().writeStartElement(context.getVersion().getNamespaceURI(context.isValid()), SHUNT_NON_LINEAR_MODEL);
                for (ShuntCompensatorNonLinearModel.Section s : sc.getModel(ShuntCompensatorNonLinearModel.class).getAllSections()) {
                    context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(context.isValid()), "section");
                    XmlUtil.writeDouble("b", s.getB(), context.getWriter());
                    XmlUtil.writeDouble("g", s.getG(), context.getWriter());
                }
                context.getWriter().writeEndElement();
            });
        } else {
            throw new PowsyblException("Unexpected shunt type " + sc.getModelType() + " for shunt " + sc.getId());
        }
    }

    @Override
    protected void readRootElementAttributes(ShuntCompensatorAdder adder, List<Consumer<ShuntCompensator>> toApply, NetworkXmlReaderContext context) {
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_2, context, () -> {
            String voltageRegulatorOn = context.getReader().getAttributeValue(null, "voltageRegulatorOn");
            double targetV = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetV");
            double targetDeadband = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetDeadband");
            adder.setTargetV(targetV)
                    .setTargetDeadband(targetDeadband)
                    .setVoltageRegulatorOn(Boolean.parseBoolean(voltageRegulatorOn));
        });
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_1, context, () -> adder.setVoltageRegulatorOn(false));
        IidmXmlUtil.runUntilMaximumVersion(IidmXmlVersion.V_1_2, context, () -> {
            double bPerSection = XmlUtil.readDoubleAttribute(context.getReader(), B_PER_SECTION);
            int maximumSectionCount = XmlUtil.readIntAttribute(context.getReader(), MAXIMUM_SECTION_COUNT);
            int sectionCount = XmlUtil.readIntAttribute(context.getReader(), "currentSectionCount");
            adder.setSectionCount(sectionCount);
            adder.newLinearModel()
                    .setBPerSection(bPerSection)
                    .setMaximumSectionCount(maximumSectionCount)
                    .add();
        });
        IidmXmlUtil.runFromMinimumVersion(IidmXmlVersion.V_1_3, context, () -> {
            Integer sectionCount = XmlUtil.readOptionalIntegerAttribute(context.getReader(), "sectionCount");
            if (sectionCount != null) {
                adder.setSectionCount(sectionCount);
            }
        });
        readNodeOrBus(adder, context);
        double p = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "p");
        double q = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "q");
        toApply.add(sc -> sc.getTerminal().setP(p).setQ(q));
    }

    @Override
    protected void readSubElements(String id, ShuntCompensatorAdder adder, List<Consumer<ShuntCompensator>> toApply, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case REGULATING_TERMINAL:
                    String regId = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
                    String regSide = context.getReader().getAttributeValue(null, "side");
                    toApply.add(sc -> context.getEndTasks().add(() -> sc.setRegulatingTerminal(TerminalRefXml.readTerminalRef(sc.getNetwork(), regId, regSide))));
                    break;
                case SHUNT_LINEAR_MODEL:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, SHUNT_LINEAR_MODEL, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
                    double bPerSection = XmlUtil.readDoubleAttribute(context.getReader(), B_PER_SECTION);
                    double gPerSection = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "gPerSection");
                    int maximumSectionCount = XmlUtil.readIntAttribute(context.getReader(), MAXIMUM_SECTION_COUNT);
                    adder.newLinearModel()
                            .setBPerSection(bPerSection)
                            .setGPerSection(gPerSection)
                            .setMaximumSectionCount(maximumSectionCount)
                            .add();
                    break;
                case SHUNT_NON_LINEAR_MODEL:
                    IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, SHUNT_NON_LINEAR_MODEL, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_3, context);
                    ShuntCompensatorNonLinearModelAdder modelAdder = adder.newNonLinearModel();
                    XmlUtil.readUntilEndElement(SHUNT_NON_LINEAR_MODEL, context.getReader(), () -> {
                        if ("section".equals(context.getReader().getLocalName())) {
                            double b = XmlUtil.readDoubleAttribute(context.getReader(), "b");
                            double g = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "g");
                            modelAdder.beginSection()
                                    .setB(b)
                                    .setG(g)
                                    .endSection();
                        } else {
                            throw new PowsyblException("Unknown element name <" + context.getReader().getLocalName() + "> in <" + id + ">");
                        }
                    });
                    modelAdder.add();
                    break;
                default:
                    super.readSubElements(id, toApply, context);
            }
        });
    }
}
