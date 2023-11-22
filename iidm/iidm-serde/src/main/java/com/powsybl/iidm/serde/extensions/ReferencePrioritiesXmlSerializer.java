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
import com.powsybl.commons.extensions.XmlReaderContext;
import com.powsybl.commons.extensions.XmlWriterContext;
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ReferencePriorities;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import com.powsybl.iidm.network.extensions.ReferencePrioritiesAdder;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;
import com.powsybl.iidm.xml.TerminalRefXml;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
@AutoService(ExtensionXmlSerializer.class)
public class ReferencePrioritiesXmlSerializer<C extends Connectable<C>> extends AbstractExtensionXmlSerializer<C,
        ReferencePriorities<C>> {

    public ReferencePrioritiesXmlSerializer() {
        super("referencePriorities", "network", ReferencePriorities.class, "referencePriorities.xsd",
                "http://www.powsybl.org/schema/iidm/ext/reference_priorities/1_0", "refpri");
    }

    @Override
    public void write(ReferencePriorities<C> extension, XmlWriterContext context) {
        TreeDataWriter writer = context.getWriter();
        writer.writeStartNodes("referencePriorities");
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        for (ReferencePriority referencePriority : extension.getReferencePriorities()) {
            writer.writeStartNode(getNamespaceUri(), "referencePriority");
            writer.writeIntAttribute("priority", referencePriority.getPriority());
            TerminalRefXml.writeTerminalRefAttribute(referencePriority.getTerminal(), networkContext);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    @Override
    public ReferencePriorities<C> read(C extendable, XmlReaderContext context) {
        TreeDataReader reader = context.getReader();
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        ReferencePrioritiesAdder<C> referencePrioritiesAdder = extendable.newExtension(ReferencePrioritiesAdder.class);
        ReferencePriorities<C> referencePriorities = referencePrioritiesAdder.add();
        reader.readChildNodes(elementName -> {
            if (elementName.equals("referencePriority")) {
                int priority = reader.readIntAttribute("priority");
                Terminal terminal = TerminalRefXml.readTerminal(networkContext, extendable.getNetwork());
                referencePriorities.newReferencePriority()
                        .setPriority(priority)
                        .setTerminal(terminal)
                        .add();
            } else {
                throw new PowsyblException("Unknown element name '" + elementName + "' in 'referencePriorities'");
            }
        });
        return referencePriorities;
    }

    @Override
    public boolean isSerializable(ReferencePriorities<C> extension) {
        return !extension.getReferencePriorities().isEmpty();
    }
}
