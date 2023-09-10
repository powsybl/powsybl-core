/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
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
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControlAdder;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;
import com.powsybl.iidm.xml.TerminalRefXml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

@AutoService(ExtensionXmlSerializer.class)
public class RemoteReactivePowerControlXmlSerializer extends AbstractExtensionXmlSerializer<Generator, RemoteReactivePowerControl> {

    public RemoteReactivePowerControlXmlSerializer() {
        super(RemoteReactivePowerControl.NAME, "network", RemoteReactivePowerControl.class, false,
                "remoteReactivePowerControl_V1_0.xsd", "http://www.powsybl.org/schema/iidm/ext/remote_reactive_power_control/1_0", "rrpc");
    }

    @Override
    public void write(RemoteReactivePowerControl extension, XmlWriterContext context) throws XMLStreamException {
        XMLStreamWriter writer = context.getWriter();
        writer.writeAttribute("enabled", Boolean.toString(extension.isEnabled()));
        XmlUtil.writeDouble("targetQ", extension.getTargetQ(), writer);
        TerminalRefXml.writeTerminalRefAttribute(extension.getRegulatingTerminal(), (NetworkXmlWriterContext) context);
    }

    @Override
    public RemoteReactivePowerControl read(Generator extendable, XmlReaderContext context) throws XMLStreamException {
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        boolean enabled = XmlUtil.readBoolAttribute(context.getReader(), "enabled");
        double targetQ = XmlUtil.readDoubleAttribute(context.getReader(), "targetQ");
        String id = networkContext.getAnonymizer().deanonymizeString(networkContext.getReader().getAttributeValue(null, "id"));
        String side = networkContext.getReader().getAttributeValue(null, "side");
        Terminal terminal = TerminalRefXml.readTerminalRef(extendable.getNetwork(), id, side);
        return extendable.newExtension(RemoteReactivePowerControlAdder.class)
                .withEnabled(enabled)
                .withTargetQ(targetQ)
                .withRegulatingTerminal(terminal)
                .add();
    }
}
