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
import com.powsybl.iidm.serde.util.IidmSerDeUtil;
import com.powsybl.iidm.serde.util.TopologyLevelUtil;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public final class TerminalRefSerDe {

    private static final String ID = "id";
    private static final String SIDE = "side";
    private static final String NUMBER = "number";

    public record TerminalData(String id, ThreeSides side, TerminalNumber number) { }

    public static void writeTerminalRef(Terminal t, NetworkSerializerContext context, String elementName) {
        writeTerminalRef(t, context, context.getVersion().getNamespaceURI(context.isValid()), elementName);
    }

    public static void writeTerminalRef(Terminal t, NetworkSerializerContext context, String namespace, String elementName) {
        writeTerminalRef(t, context, namespace, elementName, context.getWriter());
    }

    public static void writeTerminalRef(Terminal t, NetworkSerializerContext context, String namespace, String elementName, TreeDataWriter writer) {
        if (t != null) {
            writer.writeStartNode(namespace, elementName);
            writeTerminalRefAttribute(t, context, elementName, writer);
            writer.writeEndNode();
        }
    }

    @Deprecated(since = "7.2.0")
    public static void writeTerminalRefAttribute(Terminal t, NetworkSerializerContext context) {
        writeTerminalRefAttribute(t, context, context.getWriter());
    }

    public static void writeTerminalRefAttribute(Terminal t, NetworkSerializerContext context, String parentElementName) {
        writeTerminalRefAttribute(t, context, parentElementName, context.getWriter());
    }

    @Deprecated(since = "7.2.0")
    public static void writeTerminalRefAttribute(Terminal terminal, NetworkSerializerContext context, TreeDataWriter writer) {
        writeTerminalRefAttribute(terminal, context, null, writer);
    }

    public static void writeTerminalRefAttribute(Terminal terminal, NetworkSerializerContext context, String parentElementName, TreeDataWriter writer) {

        String connectableId = Optional.ofNullable(terminal)
                .map(t -> {
                    checkTerminal(t, context);
                    return context.getAnonymizer().anonymizeString(t.getConnectable().getId());
                })
                .orElse(null);
        ThreeSides tSide = Optional.ofNullable(terminal)
                .flatMap(Terminal::getConnectableSide)
                .orElse(null);
        TerminalNumber tNumber = Optional.ofNullable(terminal)
                .flatMap(Terminal::getConnectableTerminalNumber)
                .orElse(null);

        writer.writeStringAttribute(ID, connectableId);
        writer.writeEnumAttribute(SIDE, tSide);
        if (context.getVersion().compareTo(IidmVersion.V_1_15) >= 0 || tNumber != null) {
            //TODO This allow to export the terminal number when present in IIDM versions < 1.15 with
            // the "iidmVersionIncompatibilityBehavior" export option set to LOG_ERROR.
            // But this may cause problem in this specific case with BIIDM format
            // (the number is set in the file but won't be read => invalid file)
            IidmSerDeUtil.assertMinimumVersion(parentElementName != null ? parentElementName : "terminalRef", NUMBER,
                    IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_15, context);
            writer.writeEnumAttribute(NUMBER, tNumber);
        }
    }

    private static void checkTerminal(Terminal t, NetworkSerializerContext context) {
        Connectable<?> c = t.getConnectable();
        if (!context.getFilter().test(c)) {
            throw new PowsyblException("Oups, terminal ref point to a filtered equipment " + c.getId());
        }
        if (t.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER
                && TopologyLevelUtil.determineTopologyLevel(t.getVoltageLevel(), context) != TopologyLevel.NODE_BREAKER
                && t.getConnectable() instanceof BusbarSection) {
            throw new PowsyblException(String.format("Terminal ref should not point to a busbar section (here %s). Try to export in node-breaker or delete this terminal ref.",
                    t.getConnectable().getId()));
        }
    }

    public static @NonNull TerminalData readTerminalData(NetworkDeserializerContext context) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().readStringAttribute(ID));
        ThreeSides side = context.getReader().readEnumAttribute(SIDE, ThreeSides.class);
        TerminalNumber[] number = {null};
        IidmSerDeUtil.runFromMinimumVersion(IidmVersion.V_1_15, context, () -> number[0] = context.getReader().readEnumAttribute(NUMBER, TerminalNumber.class));
        context.getReader().readEndNode();
        return new TerminalData(id, side, number[0]);
    }

    public static Terminal readTerminal(NetworkDeserializerContext context, Network n) {
        TerminalData data = readTerminalData(context);
        return TerminalRefSerDe.resolve(data.id(), data.side(), data.number(), n);
    }

    public static void readTerminalRef(NetworkDeserializerContext context, Network network, Consumer<Terminal> endTaskTerminalConsumer) {
        TerminalData data = readTerminalData(context);
        context.addEndTask(DeserializationEndTask.Step.AFTER_EXTENSIONS, () -> {
            Terminal t = resolve(data.id(), data.side(), data.number(), network);
            endTaskTerminalConsumer.accept(t);
        });
    }

    public static Terminal resolve(String id, ThreeSides side, TerminalNumber number, Network network) {
        Identifiable<?> identifiable = network.getIdentifiable(id);
        if (identifiable == null) {
            throw new PowsyblException("Terminal reference identifiable not found: '" + id + "'");
        }
        if (side != null && number != null) {
            throw new PowsyblException("Terminal reference specifies both terminal side and terminal number: '" + id + "'");
        }
        if (number != null) {
            return Terminal.getTerminal(identifiable, number);
        }
        return Terminal.getTerminal(identifiable, side != null ? side : ThreeSides.ONE);
    }

    private TerminalRefSerDe() {
    }
}
