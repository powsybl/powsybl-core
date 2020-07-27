/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
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
// FIXME(floriand-e2r): uncomment line below once extension export is done only if exportable (that is, non-cleanable for the SlackTerminal)
// @AutoService(ExtensionXmlSerializer.class)
public class SlackTerminalXmlSerializer extends AbstractExtensionXmlSerializer<VoltageLevel, SlackTerminal> {

    public SlackTerminalXmlSerializer() {
        super("slackTerminal", "network", SlackTerminal.class, false, "slackTerminal.xsd",
            "http://www.powsybl.org/schema/iidm/ext/slack_terminal/1_0", "slt");
    }

    @Override
    public void write(SlackTerminal slackTerminal, XmlWriterContext context) throws XMLStreamException {
        Terminal terminal = slackTerminal.getTerminal();
        if (terminal != null) {
            TerminalRefXml.writeTerminalRefAttribute(slackTerminal.getTerminal(), (NetworkXmlWriterContext) context);
        }
    }

    @Override
    public SlackTerminal read(VoltageLevel voltageLevel, XmlReaderContext context) throws XMLStreamException {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        String id = networkContext.getAnonymizer().deanonymizeString(networkContext.getReader().getAttributeValue(null, "id"));
        String side = networkContext.getReader().getAttributeValue(null, "side");
        Terminal terminal = TerminalRefXml.readTerminalRef(voltageLevel.getNetwork(), id, side);
        voltageLevel.newExtension(SlackTerminalAdder.class).withTerminal(terminal).add();

        return voltageLevel.getExtension(SlackTerminal.class);
    }

}
