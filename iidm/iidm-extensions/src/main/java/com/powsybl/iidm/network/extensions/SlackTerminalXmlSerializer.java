/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;

import javax.xml.stream.XMLStreamException;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class SlackTerminalXmlSerializer extends AbstractExtensionXmlSerializer<VoltageLevel, SlackTerminal> {

    public SlackTerminalXmlSerializer() {
        super("slackTerminal", "network", SlackTerminal.class, false, "slackTerminal.xsd",
            "http://www.powsybl.org/schema/iidm/ext/slack_bus/1_0", "slt");
    }

    @Override
    public void write(SlackTerminal slackTerminal, XmlWriterContext context) throws XMLStreamException {
        Terminal terminal = slackTerminal.getTerminal();
        if (terminal.getNodeBreakerView() != null) {
            int node = terminal.getNodeBreakerView().getNode();
            XmlUtil.writeInt("node", node, context.getExtensionsWriter());
        } else {
            String busId = terminal.getBusBreakerView().getBus().getId();
            context.getExtensionsWriter().writeAttribute("busId", busId);
        }
    }

    @Override
    public SlackTerminal read(VoltageLevel voltageLevel, XmlReaderContext context) {
        SlackTerminalAdder slackTerminalAdder = voltageLevel.newExtension(SlackTerminalAdder.class);
        Integer node = XmlUtil.readOptionalIntegerAttribute(context.getReader(), "node");
        if (node != null) {
            slackTerminalAdder.setNode(node);
        }
        String busId = context.getReader().getAttributeValue(null, "busId");
        if (busId != null) {
            slackTerminalAdder.setBusId(busId);
        }
        slackTerminalAdder.add();
        return voltageLevel.getExtension(SlackTerminalAdder.class);
    }
}
