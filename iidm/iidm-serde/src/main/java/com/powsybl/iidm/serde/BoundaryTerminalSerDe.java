/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.*;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public class BoundaryTerminalSerDe {

    static final BoundaryTerminalSerDe INSTANCE = new BoundaryTerminalSerDe();
    static final String ROOT_ELEMENT_NAME = "boundaryTerminal";
    static final String ARRAY_ELEMENT_NAME = "boundaryTerminals";

    protected void write(final Area holder, final NetworkSerializerContext context) {
        final TreeDataWriter writer = context.getWriter();
        writer.writeStartNodes();
        for (Area.BoundaryTerminal boundaryTerminal : holder.getBoundaryTerminals()) {
            writer.writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), ROOT_ELEMENT_NAME);
            writer.writeBooleanAttribute("ac", boundaryTerminal.ac());
            TerminalRefSerDe.writeTerminalRef(boundaryTerminal.terminal(), context, "terminalRef");
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    protected void read(final Area boundaryTerminalHolder, final NetworkDeserializerContext context) {
        boolean ac = context.getReader().readBooleanAttribute("ac");
        context.getReader().readChildNodes(elementName -> {
            Terminal terminal = TerminalRefSerDe.readTerminal(context, boundaryTerminalHolder.getNetwork());
            boundaryTerminalHolder.addBoundaryTerminal(terminal, ac);
        });
    }
}
