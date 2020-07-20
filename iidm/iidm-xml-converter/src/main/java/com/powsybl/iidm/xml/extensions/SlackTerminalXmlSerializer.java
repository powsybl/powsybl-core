/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;
import com.powsybl.iidm.xml.TerminalRefXml;

import javax.xml.stream.XMLStreamException;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
@AutoService(ExtensionXmlSerializer.class)
public class SlackTerminalXmlSerializer extends AbstractExtensionXmlSerializer<VoltageLevel, SlackTerminal> {

    private static final String ELEM_TERMINAL_REF = "terminalRef";

    public SlackTerminalXmlSerializer() {
        super("slackTerminal", "network", SlackTerminal.class, true, "slackTerminal.xsd",
            "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_0", "slt");
    }

    @Override
    public void write(SlackTerminal slackTerminal, XmlWriterContext context) throws XMLStreamException {
        TerminalRefXml.writeTerminalRef(slackTerminal.getTerminal(), (NetworkXmlWriterContext) context, getNamespaceUri(), ELEM_TERMINAL_REF);
    }

    @Override
    public SlackTerminal read(VoltageLevel voltageLevel, XmlReaderContext context) throws XMLStreamException {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        XmlUtil.readUntilEndElement(getExtensionName(), networkContext.getReader(), () -> {
            if (networkContext.getReader().getLocalName().equals(ELEM_TERMINAL_REF)) {
                String id = networkContext.getAnonymizer().deanonymizeString(networkContext.getReader().getAttributeValue(null, "id"));
                String side = networkContext.getReader().getAttributeValue(null, "side");
                Terminal terminal = TerminalRefXml.readTerminalRef(voltageLevel.getNetwork(), id, side);
                voltageLevel.newExtension(SlackTerminalAdder.class).setTerminal(terminal).add();
            } else {
                throw new AssertionError("Unexpected element: " + networkContext.getReader().getLocalName());
            }
        });
        return voltageLevel.getExtension(SlackTerminal.class);
    }

}
