/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionXmlSerializer;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.commons.xml.XmlReaderContext;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ReferenceTerminal;
import com.powsybl.iidm.network.extensions.ReferenceTerminals;
import com.powsybl.iidm.network.extensions.ReferenceTerminalsAdder;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;
import com.powsybl.iidm.xml.TerminalRefXml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
@AutoService(ExtensionXmlSerializer.class)
public class ReferenceTerminalsXmlSerializer<C extends Connectable<C>> extends AbstractExtensionXmlSerializer<C,
        ReferenceTerminals<C>> {

    public ReferenceTerminalsXmlSerializer() {
        super("referenceTerminals", "network", ReferenceTerminals.class, true, "referenceTerminals.xsd",
                "http://www.powsybl.org/schema/iidm/ext/referenceTerminals/1_0", "reft");
    }

    @Override
    public void write(ReferenceTerminals<C> extension, XmlWriterContext context) throws XMLStreamException {
        XMLStreamWriter writer = context.getWriter();
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        for (ReferenceTerminal referenceTerminal : extension.getReferenceTerminals()) {
            writer.writeStartElement(getNamespaceUri(), "referenceTerminal"); // FIXME should writeEmptyElement instead
            XmlUtil.writeInt("priority", referenceTerminal.getPriority(), writer);
            TerminalRefXml.writeTerminalRefAttribute(referenceTerminal.getTerminal(), networkContext);
            writer.writeEndElement();
        }
    }

    @Override
    public ReferenceTerminals<C> read(C extendable, XmlReaderContext context) throws XMLStreamException {
        XMLStreamReader reader = context.getReader();
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        ReferenceTerminalsAdder<C> referenceTerminalsAdder = extendable.newExtension(ReferenceTerminalsAdder.class);
        ReferenceTerminals<C> referenceTerminals = referenceTerminalsAdder.add();
        XmlUtil.readUntilEndElement(getExtensionName(), reader, () -> {
            if (reader.getLocalName().equals("referenceTerminal")) {
                int priority = XmlUtil.readIntAttribute(reader, "priority");
                Terminal terminal = TerminalRefXml.readTerminal(networkContext, extendable.getNetwork());
                referenceTerminals.newReferenceTerminal()
                        .setPriority(priority)
                        .setTerminal(terminal)
                        .add();
            } else {
                throw new PowsyblException("Unexpected element: " + reader.getLocalName());
            }
        });
        return referenceTerminals;
    }

    @Override
    public boolean isSerializable(ReferenceTerminals<C> extension) {
        return !extension.getReferenceTerminals().isEmpty();
    }
}
