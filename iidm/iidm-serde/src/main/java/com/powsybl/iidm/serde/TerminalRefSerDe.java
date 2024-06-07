/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.*;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public final class TerminalRefSerDe {

    private static final String ID = "id";
    private static final String SIDE = "side";

    public static void writeTerminalRef(Terminal t, NetworkSerializerContext context, String elementName) {
        writeTerminalRef(t, context, context.getVersion().getNamespaceURI(context.isValid()), elementName);
    }

    public static void writeTerminalRef(Terminal t, NetworkSerializerContext context, String namespace, String elementName) {
        writeTerminalRef(t, context, namespace, elementName, context.getWriter());
    }

    public static void writeTerminalRef(Terminal t, NetworkSerializerContext context, String namespace, String elementName, TreeDataWriter writer) {
        if (t != null) {
            writer.writeStartNode(namespace, elementName);
            writeTerminalRefAttribute(t, context, writer);
            writer.writeEndNode();
        }
    }

    public static void writeTerminalRefAttribute(Terminal t, NetworkSerializerContext context) {
        writeTerminalRefAttribute(t, context, context.getWriter());
    }

    public static void writeTerminalRefAttribute(Terminal terminal, NetworkSerializerContext context, TreeDataWriter writer) {

        String connectableId = Optional.ofNullable(terminal)
                .map(t -> {
                    checkTerminal(t, context);
                    return context.getAnonymizer().anonymizeString(t.getConnectable().getId());
                })
                .orElse(null);
        ThreeSides tSide = Optional.ofNullable(terminal)
                .flatMap(Terminal::getConnectableSide)
                .orElse(null);

        writer.writeStringAttribute("id", connectableId);
        writer.writeEnumAttribute("side", tSide);
    }

    private static void checkTerminal(Terminal t, NetworkSerializerContext context) {
        Connectable<?> c = t.getConnectable();
        if (!context.getFilter().test(c)) {
            throw new PowsyblException("Oups, terminal ref point to a filtered equipment " + c.getId());
        }
        if (t.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER
                && context.getOptions().getTopologyLevel() != TopologyLevel.NODE_BREAKER
                && t.getConnectable() instanceof BusbarSection) {
            throw new PowsyblException(String.format("Terminal ref should not point to a busbar section (here %s). Try to export in node-breaker or delete this terminal ref.",
                    t.getConnectable().getId()));
        }
    }

    public static Terminal readTerminal(NetworkDeserializerContext context, Network n) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute(ID));
        ThreeSides side = context.getReader().readEnumAttribute(SIDE, ThreeSides.class);
        context.getReader().readEndNode();
        return TerminalRefSerDe.resolve(id, side, n);
    }

    public static void readTerminalRefAttributes(NetworkDeserializerContext context, Network network, Consumer<Terminal> endTaskTerminalConsumer) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute(ID));
        ThreeSides side = context.getReader().readEnumAttribute(SIDE, ThreeSides.class);
        context.getEndTasks().add(() -> {
            Terminal t = resolve(id, side, network);
            endTaskTerminalConsumer.accept(t);
        });
    }

    public static void readTerminalRef(NetworkDeserializerContext context, Network network, Consumer<Terminal> endTaskTerminalConsumer) {
        readTerminalRefAttributes(context, network, endTaskTerminalConsumer);
        context.getReader().readEndNode();
    }

    public static Terminal resolve(String id, ThreeSides side, Network network) {
        Identifiable<?> identifiable = network.getIdentifiable(id);
        if (identifiable == null) {
            throw new PowsyblException("Terminal reference identifiable not found: '" + id + "'");
        }
        return Terminal.getTerminal(identifiable, side != null ? side : ThreeSides.ONE);
    }

    private TerminalRefSerDe() {
    }
}
