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
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
        writeTerminalRef(slackTerminal.getTerminal(), context.getExtensionsWriter());
    }

    @Override
    public SlackTerminal read(VoltageLevel voltageLevel, XmlReaderContext context) {
        Terminal terminal = readTerminalRef(voltageLevel.getNetwork(), context);
        if (terminal != null) {
            voltageLevel.newExtension(SlackTerminalAdder.class)
                .setTerminal(terminal)
                .add();
            return voltageLevel.getExtension(SlackTerminal.class);
        } else {
            return null;
        }
    }

    public static void writeTerminalRef(Terminal t, XMLStreamWriter writer) throws XMLStreamException {
        Connectable c = t.getConnectable();
        writer.writeAttribute("connectableId", c.getId());
        if (c.getTerminals().size() > 1) {
            if (c instanceof Injection) {
                // nothing to do
            } else if (c instanceof Branch) {
                Branch branch = (Branch) c;
                writer.writeAttribute("side", branch.getSide(t).name());
            } else if (c instanceof ThreeWindingsTransformer) {
                ThreeWindingsTransformer twt = (ThreeWindingsTransformer) c;
                writer.writeAttribute("side", twt.getSide(t).name());
            } else {
                throw new AssertionError("Unexpected Connectable instance: " + c.getClass());
            }
        }
    }

    public static Terminal readTerminalRef(Network network, XmlReaderContext context) {
        String connectableId = context.getReader().getAttributeValue(null, "connectableId");
        String side = context.getReader().getAttributeValue(null, "side");
        Identifiable identifiable = network.getIdentifiable(connectableId);
        if (identifiable instanceof Injection) {
            return ((Injection) identifiable).getTerminal();
        } else if (identifiable instanceof Branch) {
            return side.equals(Branch.Side.ONE.name()) ? ((Branch) identifiable).getTerminal1()
                : ((Branch) identifiable).getTerminal2();
        } else if (identifiable instanceof ThreeWindingsTransformer) {
            ThreeWindingsTransformer twt = (ThreeWindingsTransformer) identifiable;
            return twt.getTerminal(ThreeWindingsTransformer.Side.valueOf(side));
        } else {
            throw new AssertionError("Unexpected Identifiable instance: " + identifiable.getClass());
        }
    }

}
