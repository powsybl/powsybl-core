/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.*;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public class AreaBoundarySerDe {

    static final AreaBoundarySerDe INSTANCE = new AreaBoundarySerDe();
    static final String ROOT_ELEMENT_NAME = "areaBoundary";
    static final String ARRAY_ELEMENT_NAME = "areaBoundaries";

    public static final String TERMINAL_REF = "terminalRef";

    protected void write(final Area holder, final NetworkSerializerContext context) {
        final TreeDataWriter writer = context.getWriter();
        writer.writeStartNodes();
        for (AreaBoundary boundary : holder.getAreaBoundaries()) {
            writer.writeStartNode(context.getVersion().getNamespaceURI(context.isValid()), ROOT_ELEMENT_NAME);
            writer.writeBooleanAttribute("ac", boundary.isAc());
            boundary.getTerminal().ifPresent(terminal -> TerminalRefSerDe.writeTerminalRef(terminal, context, TERMINAL_REF));
            boundary.getBoundary().ifPresent(danglingLineBoundary -> BoundaryRefSerDe.writeBoundaryRef(danglingLineBoundary, context));
            writer.writeEndNode();
        }
        writer.writeEndNodes();
    }

    protected void read(final Area holder, final NetworkDeserializerContext context) {
        boolean ac = context.getReader().readBooleanAttribute("ac");
        AreaBoundaryAdder adder = holder.newAreaBoundary().setAc(ac);
        context.getReader().readChildNodes(elementName -> {
            switch (elementName) {
                case TERMINAL_REF -> TerminalRefSerDe.readTerminalRef(context, holder.getNetwork(), adder::setTerminal);
                case BoundaryRefSerDe.ROOT_ELEMENT_NAME -> BoundaryRefSerDe.readBoundaryRef(context, holder.getNetwork(), adder::setBoundary);
                default -> throw new PowsyblException("Unexpected element for AreaBoundary: " + elementName + ". Should be " + BoundaryRefSerDe.ROOT_ELEMENT_NAME + " or " + TERMINAL_REF);
            }
        });
        context.getEndTasks().add(adder::add);
    }
}
