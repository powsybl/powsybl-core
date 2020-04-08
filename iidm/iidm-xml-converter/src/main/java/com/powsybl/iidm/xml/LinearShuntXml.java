/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorAdder;
import com.powsybl.iidm.network.ShuntCompensatorLinearModel;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.xml.util.IidmXmlUtil;

import javax.xml.stream.XMLStreamException;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class LinearShuntXml extends AbstractConnectableXml<ShuntCompensator, ShuntCompensatorAdder, VoltageLevel> {

    static final LinearShuntXml INSTANCE = new LinearShuntXml();

    static final String ROOT_ELEMENT_NAME = "linearShunt";

    @Override
    protected String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }

    @Override
    protected boolean hasSubElements(ShuntCompensator sc) {
        return sc.getRegulatingTerminal().getConnectable() != sc;
    }

    @Override
    protected void writeRootElementAttributes(ShuntCompensator sc, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        XmlUtil.writeDouble("bPerSection", sc.getModel(ShuntCompensatorLinearModel.class).getbPerSection(), context.getWriter());
        XmlUtil.writeDouble("gPerSection", sc.getModel(ShuntCompensatorLinearModel.class).getgPerSection(), context.getWriter());
        context.getWriter().writeAttribute("maximumSectionCount", Integer.toString(sc.getMaximumSectionCount()));
        context.getWriter().writeAttribute("currentSectionCount", Integer.toString(sc.getCurrentSectionCount()));
        context.getWriter().writeAttribute("voltageRegulatorOn", Boolean.toString(sc.isVoltageRegulatorOn()));
        XmlUtil.writeDouble("targetV", sc.getTargetV(), context.getWriter());
        XmlUtil.writeDouble("targetDeadband", sc.getTargetDeadband(), context.getWriter());
        writeNodeOrBus(null, sc.getTerminal(), context);
        writePQ(null, sc.getTerminal(), context.getWriter());
    }

    @Override
    protected void writeSubElements(ShuntCompensator sc, VoltageLevel vl, NetworkXmlWriterContext context) throws XMLStreamException {
        if (sc != sc.getRegulatingTerminal().getConnectable()) {
            TerminalRefXml.writeTerminalRef(sc.getRegulatingTerminal(), context, "regulatingTerminal");
        }
    }

    @Override
    protected ShuntCompensatorAdder createAdder(VoltageLevel vl) {
        return vl.newShuntCompensator();
    }

    @Override
    protected ShuntCompensator readRootElementAttributes(ShuntCompensatorAdder adder, NetworkXmlReaderContext context) {
        IidmXmlUtil.assertMinimumVersion(ROOT_ELEMENT_NAME, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_2, context);
        int currentSectionCount = XmlUtil.readIntAttribute(context.getReader(), "currentSectionCount");
        double bPerSection = XmlUtil.readDoubleAttribute(context.getReader(), "bPerSection");
        double gPerSection = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "gPerSection");
        int maximumSectionCount = XmlUtil.readIntAttribute(context.getReader(), "maximumSectionCount");
        boolean voltageRegulatorOn = XmlUtil.readBoolAttribute(context.getReader(), "voltageRegulatorOn");
        double targetV = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetV");
        double targetDeadband = XmlUtil.readOptionalDoubleAttribute(context.getReader(), "targetDeadband");
        adder.setVoltageRegulatorOn(voltageRegulatorOn)
                .setTargetV(targetV)
                .setTargetDeadband(targetDeadband)
                .newLinearModel()
                    .setMaximumSectionCount(maximumSectionCount)
                    .setbPerSection(bPerSection)
                    .setgPerSection(gPerSection)
                    .add()
                .setCurrentSectionCount(currentSectionCount);
        readNodeOrBus(adder, context);
        ShuntCompensator sc = adder.add();
        readPQ(null, sc.getTerminal(), context.getReader());
        return sc;
    }

    @Override
    protected void readSubElements(ShuntCompensator sc, NetworkXmlReaderContext context) throws XMLStreamException {
        readUntilEndRootElement(context.getReader(), () -> {
            if ("regulatingTerminal".equals(context.getReader().getLocalName())) {
                String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, "id"));
                String side = context.getReader().getAttributeValue(null, "side");
                context.getEndTasks().add(() -> sc.setRegulatingTerminal(TerminalRefXml.readTerminalRef(sc.getTerminal().getVoltageLevel().getSubstation().getNetwork(), id, side)));
            } else {
                super.readSubElements(sc, context);
            }
        });
    }
}
