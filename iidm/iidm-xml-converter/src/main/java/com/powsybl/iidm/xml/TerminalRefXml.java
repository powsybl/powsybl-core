/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.xml.TreeDataWriter;
import com.powsybl.iidm.network.*;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class TerminalRefXml {

    public static void writeTerminalRef(Terminal t, NetworkXmlWriterContext context, String elementName) {
        writeTerminalRef(t, context, context.getVersion().getNamespaceURI(context.isValid()), elementName);
    }

    public static void writeTerminalRef(Terminal t, NetworkXmlWriterContext context, String namespace, String elementName) {
        writeTerminalRef(t, context, namespace, elementName, context.getWriter());
    }

    public static void writeTerminalRef(Terminal t, NetworkXmlWriterContext context, String namespace, String elementName, TreeDataWriter writer) {
        writer.writeEmptyNode(namespace, elementName);
        writeTerminalRefAttribute(t, context, writer);
    }

    public static void writeTerminalRefAttribute(Terminal t, NetworkXmlWriterContext context) {
        writeTerminalRefAttribute(t, context, context.getWriter());
    }

    public static void writeTerminalRefAttribute(Terminal t, NetworkXmlWriterContext context, TreeDataWriter writer) {
        Connectable c = t.getConnectable();
        if (!context.getFilter().test(c)) {
            throw new PowsyblException("Oups, terminal ref point to a filtered equipment " + c.getId());
        }
        if (t.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER
                && context.getOptions().getTopologyLevel() != TopologyLevel.NODE_BREAKER
                && t.getConnectable() instanceof BusbarSection) {
            throw new PowsyblException(String.format("Terminal ref should not point to a busbar section (here %s). Try to export in node-breaker or delete this terminal ref.",
                    t.getConnectable().getId()));
        }
        writer.writeStringAttribute("id", context.getAnonymizer().anonymizeString(c.getId()));
        if (c.getTerminals().size() > 1) {
            if (c instanceof Injection) {
                // nothing to do
            } else if (c instanceof Branch) {
                Branch branch = (Branch) c;
                writer.writeStringAttribute("side", branch.getSide(t).name());
            } else if (c instanceof ThreeWindingsTransformer) {
                ThreeWindingsTransformer twt = (ThreeWindingsTransformer) c;
                writer.writeStringAttribute("side", twt.getSide(t).name());
            } else {
                throw new AssertionError("Unexpected Connectable instance: " + c.getClass());
            }
        }
    }

    public static Terminal readTerminalRef(Network network, String id, String side) {
        Identifiable identifiable = network.getIdentifiable(id);
        if (identifiable == null) {
            throw new PowsyblException("Terminal reference identifiable not found: '" + id + "'");
        }
        if (identifiable instanceof Injection) {
            return ((Injection) identifiable).getTerminal();
        } else if (identifiable instanceof Branch) {
            return side.equals(Branch.Side.ONE.name()) ? ((Branch) identifiable).getTerminal1()
                    : ((Branch) identifiable).getTerminal2();
        } else if (identifiable instanceof ThreeWindingsTransformer) {
            ThreeWindingsTransformer twt = (ThreeWindingsTransformer) identifiable;
            return twt.getTerminal(ThreeWindingsTransformer.Side.valueOf(side));
        } else {
            throw new PowsyblException("Unexpected terminal reference identifiable instance: " + identifiable.getClass());
        }
    }

    private TerminalRefXml() {
    }
}
