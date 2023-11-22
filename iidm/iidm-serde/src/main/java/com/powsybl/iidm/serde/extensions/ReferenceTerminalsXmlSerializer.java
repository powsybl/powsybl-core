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
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.xml.NetworkXmlReaderContext;
import com.powsybl.iidm.xml.NetworkXmlWriterContext;
import com.powsybl.iidm.xml.TerminalRefXml;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
@AutoService(ExtensionXmlSerializer.class)
public class ReferenceTerminalsXmlSerializer extends AbstractExtensionXmlSerializer<Network,
        ReferenceTerminals> {

    public ReferenceTerminalsXmlSerializer() {
        super("referenceTerminals", "network", ReferenceTerminals.class, "referenceTerminals.xsd",
                "http://www.powsybl.org/schema/iidm/ext/reference_terminals/1_0", "reft");
    }

    @Override
    public void write(ReferenceTerminals extension, XmlWriterContext context) {
        TreeDataWriter writer = context.getWriter();
        writer.writeStartNodes("referenceTerminals");
        NetworkXmlWriterContext networkContext = (NetworkXmlWriterContext) context;
        for (Terminal terminal : extension.getReferenceTerminals()) {
            writer.writeStartNode(getNamespaceUri(), "referenceTerminal");
            TerminalRefXml.writeTerminalRefAttribute(terminal, networkContext);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    @Override
    public ReferenceTerminals read(Network extendable, XmlReaderContext context) {
        TreeDataReader reader = context.getReader();
        NetworkXmlReaderContext networkContext = (NetworkXmlReaderContext) context;
        Set<Terminal> terminals = new LinkedHashSet<>();
        reader.readChildNodes(elementName -> {
            if (elementName.equals("referenceTerminal")) {
                Terminal terminal = TerminalRefXml.readTerminal(networkContext, extendable.getNetwork());
                terminals.add(terminal);
            } else {
                throw new PowsyblException("Unknown element name '" + elementName + "' in 'referenceTerminals'");
            }
        });
        ReferenceTerminalsAdder referenceTerminalsAdder = extendable.newExtension(ReferenceTerminalsAdder.class)
                .withTerminals(terminals);
        return referenceTerminalsAdder.add();
    }

    @Override
    public boolean isSerializable(ReferenceTerminals extension) {
        return !extension.getReferenceTerminals().isEmpty();
    }
}
