/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.commons.io.TreeDataReader;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ReferenceTerminals;
import com.powsybl.iidm.network.extensions.ReferenceTerminalsAdder;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;
import com.powsybl.iidm.serde.TerminalRefSerDe;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
@AutoService(ExtensionSerDe.class)
public class ReferenceTerminalsSerDe extends AbstractExtensionSerDe<Network,
        ReferenceTerminals> {

    public static final String REFERENCE_TERMINAL_ROOT_ELEMENT_NAME = "referenceTerminal";

    public ReferenceTerminalsSerDe() {
        super("referenceTerminals", "network", ReferenceTerminals.class, "referenceTerminals.xsd",
                "http://www.powsybl.org/schema/iidm/ext/reference_terminals/1_0", "reft");
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of("referenceTerminals", REFERENCE_TERMINAL_ROOT_ELEMENT_NAME);
    }

    @Override
    public void write(ReferenceTerminals extension, SerializerContext context) {
        TreeDataWriter writer = context.getWriter();
        writer.writeStartNodes();
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        for (Terminal terminal : extension.getReferenceTerminals()) {
            writer.writeStartNode(getNamespaceUri(), REFERENCE_TERMINAL_ROOT_ELEMENT_NAME);
            TerminalRefSerDe.writeTerminalRefAttribute(terminal, networkContext);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    @Override
    public ReferenceTerminals read(Network extendable, DeserializerContext context) {
        TreeDataReader reader = context.getReader();
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
        Set<Terminal> terminals = new LinkedHashSet<>();
        reader.readChildNodes(elementName -> {
            if (elementName.equals(REFERENCE_TERMINAL_ROOT_ELEMENT_NAME)) {
                Terminal terminal = TerminalRefSerDe.readTerminal(networkContext, extendable.getNetwork());
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
