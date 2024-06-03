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

import java.util.Optional;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public class AreaBoundarySerDe {

    static final AreaBoundarySerDe INSTANCE = new AreaBoundarySerDe();
    static final String ROOT_ELEMENT_NAME = "areaBoundary";
    static final String ARRAY_ELEMENT_NAME = "AreaBoundaries";

    protected void write(final Area holder, final NetworkSerializerContext context) {
        final TreeDataWriter writer = context.getWriter();
        writer.writeStartNodes();
        for (AreaBoundary boundary : holder.getAreaBoundaries()) {

            // TODO finir la serde, voir comment on gère le fait d'avoir les term et dangling lines en optionel
            writer.writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), ROOT_ELEMENT_NAME);
            boundary.getTerminal().ifPresent(terminal -> TerminalRefSerDe.writeTerminalRefAttribute(terminal, context));
            boundary.getDanglingLine().ifPresent(danglingLine -> writer.writeStringAttribute("danglingLine", danglingLine.getId()));
            writer.writeBooleanAttribute("ac", boundary.isAc());
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    protected void read(final Area boundaryTerminalHolder, final NetworkDeserializerContext context) {
        boolean ac = context.getReader().readBooleanAttribute("ac");
        Terminal terminal = TerminalRefSerDe.readTerminal(context, boundaryTerminalHolder.getNetwork());
        String danglingLineId = context.getReader().readStringAttribute("danglingLine");

    }
}
