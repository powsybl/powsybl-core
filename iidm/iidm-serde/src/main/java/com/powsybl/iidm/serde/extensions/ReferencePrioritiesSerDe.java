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
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ReferencePriorities;
import com.powsybl.iidm.network.extensions.ReferencePrioritiesAdder;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import com.powsybl.iidm.serde.NetworkDeserializerContext;
import com.powsybl.iidm.serde.NetworkSerializerContext;
import com.powsybl.iidm.serde.TerminalRefSerDe;

import java.util.Map;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
@AutoService(ExtensionSerDe.class)
public class ReferencePrioritiesSerDe<C extends Connectable<C>> extends AbstractExtensionSerDe<C,
        ReferencePriorities<C>> {

    public static final String REFERENCE_PRIORITY_ROOT_ELEMENT_NAME = "referencePriority";

    public ReferencePrioritiesSerDe() {
        super("referencePriorities", "network", ReferencePriorities.class, "referencePriorities.xsd",
                "http://www.powsybl.org/schema/iidm/ext/reference_priorities/1_0", "refpri");
    }

    @Override
    public Map<String, String> getArrayNameToSingleNameMap() {
        return Map.of("referencePriorities", REFERENCE_PRIORITY_ROOT_ELEMENT_NAME);
    }

    @Override
    public void write(ReferencePriorities<C> extension, SerializerContext context) {
        TreeDataWriter writer = context.getWriter();
        writer.writeStartNodes();
        NetworkSerializerContext networkContext = (NetworkSerializerContext) context;
        for (ReferencePriority referencePriority : extension.getReferencePriorities()) {
            writer.writeStartNode(getNamespaceUri(), REFERENCE_PRIORITY_ROOT_ELEMENT_NAME);
            writer.writeIntAttribute("priority", referencePriority.getPriority());
            TerminalRefSerDe.writeTerminalRefAttribute(referencePriority.getTerminal(), networkContext);
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    @Override
    public ReferencePriorities<C> read(C extendable, DeserializerContext context) {
        TreeDataReader reader = context.getReader();
        NetworkDeserializerContext networkContext = (NetworkDeserializerContext) context;
        ReferencePrioritiesAdder<C> referencePrioritiesAdder = extendable.newExtension(ReferencePrioritiesAdder.class);
        ReferencePriorities<C> referencePriorities = referencePrioritiesAdder.add();
        reader.readChildNodes(elementName -> {
            if (elementName.equals(REFERENCE_PRIORITY_ROOT_ELEMENT_NAME)) {
                int priority = reader.readIntAttribute("priority");
                Terminal terminal = TerminalRefSerDe.readTerminal(networkContext, extendable.getNetwork());
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
