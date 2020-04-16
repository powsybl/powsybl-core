/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;
import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class NonLinearShuntXml extends AbstractConnectableXml<ShuntCompensator, ShuntCompensatorAdder, VoltageLevel> {

    static final NonLinearShuntXml INSTANCE = new NonLinearShuntXml();

    static final String ROOT_ELEMENT_NAME = "nonLinearShunt";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(ShuntCompensator sc) {
        return true;
    }

    @Override
    protected void writeRootElementAttributes(ShuntCompensator sc, VoltageLevel parent, NetworkXmlWriterContext context) throws XMLStreamException {
        context.getWriter().writeAttribute("currentSectionCount", Integer.toString(sc.getCurrentSectionCount()));
        context.getWriter().writeAttribute("voltageRegulatorOn", Boolean.toString(sc.isVoltageRegulatorOn()));
        XmlUtil.writeDouble("targetV", sc.getTargetV(), context.getWriter());
        XmlUtil.writeDouble("targetDeadband", sc.getTargetDeadband(), context.getWriter());
        writeNodeOrBus(null, sc.getTerminal(), context);
        writePQ(null, sc.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(ShuntCompensator sc, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        writeSections(sc, context);
        if (sc != sc.getRegulatingTerminal().getConnectable()) {
            TerminalRefXml.writeTerminalRef(sc.getRegulatingTerminal(), context, "regulatingTerminal");
        }
    }

    private static void writeSections(ShuntCompensator sc, NetworkXmlWriterContext context) throws XMLStreamException {
        for (Map.Entry<Integer, ShuntCompensatorNonLinearModel.Section> section : sc.getModel(ShuntCompensatorNonLinearModel.class).getSections().entrySet()) {
            context.getWriter().writeEmptyElement(context.getVersion().getNamespaceURI(), "section");
            context.getWriter().writeAttribute("num", Integer.toString(section.getKey()));
            XmlUtil.writeDouble("b", section.getValue().getB(), context.getWriter());
            XmlUtil.writeDouble("g", section.getValue().getG(), context.getWriter());
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
        IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
        int currentSectionCount = XmlUtil.readIntAttribute(context.getReader(), "currentSectionCount");
        boolean voltageRegulatorOn = XmlUtil.readBoolAttribute(context.getReader(), "voltageRegulatorOn");
        double targetV = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetV");
        double targetDeadband = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetDeadband");
        adder.setVoltageRegulatorOn(voltageRegulatorOn)
                .setTargetV(targetV)
                .setTargetDeadband(targetDeadband)
                .setCurrentSectionCount(currentSectionCount);
        readNodeOrBus(adder, context);
        readNonLinearShunt(id, adder, context);
    }

    private void readNonLinearShunt(String id, ShuntCompensatorAdder adder, NetworkXmlReaderContext context) throws XMLStreamException {
        double p = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "p");
        double q = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "q");
        String[] regId = new String[1];
        String[] regSide = new String[1];
        Map<String, String> properties = new HashMap<>();
        ShuntCompensatorNonLinearModelAdder modelAdder = adder.newNonLinearModel();
        readUntilEndRootElement(context.getReader(), () -> {
            switch (context.getReader().getLocalName()) {
                case "section":
                    readSection(modelAdder, context);
                    break;
                case "regulatingTerminal":
                    regId[0] = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
                    regSide[0] = context.getReader().getAttributeValue(null, "side");
                    break;
                case "property":
                    String name = context.getReader().getAttributeValue(null, "name");
                    String value = context.getReader().getAttributeValue(null, "value");
                    properties.put(name, value);
                    break;
                default:
                    throw new PowsyblException("Unknown element name <" + context.getReader().getLocalName() + "> in <" + id + ">");
            }
        });
        ShuntCompensator sc = modelAdder.add()
                .add();
        if (regId[0] != null) {
            context.getEndTasks().add(() -> sc.setRegulatingTerminal(TerminalRefXml.readTerminalRef(sc.getTerminal().getVoltageLevel().getSubstation().getNetwork(), regId[0], regSide[0])));
        }
        properties.forEach(sc::setProperty);
        sc.getTerminal().setP(p).setQ(q);
    }

    private static void readSection(ShuntCompensatorNonLinearModelAdder adder, NetworkXmlReaderContext context) {
        int sectionNum = XmlUtil.readIntAttribute(context.getReader(), "num");
        double b = XmlUtil.readDoubleAttribute(context.getReader(), "b");
        double g = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "g");
        adder.beginSection()
                .setSectionIndex(sectionNum)
                .setB(b)
                .setG(g)
                .endSection();
    }
}
