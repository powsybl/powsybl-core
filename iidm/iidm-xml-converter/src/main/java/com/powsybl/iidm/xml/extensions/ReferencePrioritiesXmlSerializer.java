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
import com.powsybl.iidm.network.extensions.ReferencePriorities;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import com.powsybl.iidm.network.extensions.ReferencePrioritiesAdder;
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
public class ReferencePrioritiesXmlSerializer<C extends Connectable<C>> extends AbstractExtensionXmlSerializer<C,
        ReferencePriorities<C>> {

    public ReferencePrioritiesXmlSerializer() {
        super("referencePriorities", "network", ReferencePriorities.class, true, "referencePriorities.xsd",
                "http://www.powsybl.org/schema/iidm/ext/reference_priorities/1_0", "refpri");
    }

    @Override
    public void write(ReferencePriorities<C> extension, XmlWriterContext context) throws XMLStreamException {
        XMLStreamWriter writer = context.getWriter();
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        for (ReferencePriority referencePriority : extension.getReferencePriorities()) {
            writer.writeEmptyElement(getNamespaceUri(), "referencePriority");
            XmlUtil.writeInt("priority", referencePriority.getPriority(), writer);
            TerminalRefXml.writeTerminalRefAttribute(referencePriority.getTerminal(), networkContext);
        }
    }

    @Override
    public ReferencePriorities<C> read(C extendable, XmlReaderContext context) throws XMLStreamException {
        XMLStreamReader reader = context.getReader();
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        ReferencePrioritiesAdder<C> referencePrioritiesAdder = extendable.newExtension(ReferencePrioritiesAdder.class);
        ReferencePriorities<C> referencePriorities = referencePrioritiesAdder.add();
        XmlUtil.readUntilEndElement(getExtensionName(), reader, () -> {
            if (reader.getLocalName().equals("referencePriority")) {
                int priority = XmlUtil.readIntAttribute(reader, "priority");
                Terminal terminal = TerminalRefXml.readTerminal(networkContext, extendable.getNetwork());
                referencePriorities.newReferencePriority()
                        .setPriority(priority)
                        .setTerminal(terminal)
                        .add();
            } else {
                throw new PowsyblException("Unexpected element: " + reader.getLocalName());
            }
        });
        return referencePriorities;
    }

    @Override
    public boolean isSerializable(ReferencePriorities<C> extension) {
        return !extension.getReferencePriorities().isEmpty();
    }
}
