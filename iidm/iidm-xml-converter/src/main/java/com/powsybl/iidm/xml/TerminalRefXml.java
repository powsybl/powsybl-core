/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.TerminalUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class TerminalRefXml {

    private static final String ID = "id";
    private static final String SIDE = "side";

    public static void writeTerminalRef(Terminal t, NetworkXmlWriterContext context, String elementName) throws XMLStreamException {
        writeTerminalRef(t, context, context.getVersion().getNamespaceURI(context.isValid()), elementName);
    }

    public static void writeTerminalRef(Terminal t, NetworkXmlWriterContext context, String namespace, String elementName) throws XMLStreamException {
        writeTerminalRef(t, context, namespace, elementName, context.getWriter());
    }

    public static void writeTerminalRef(Terminal t, NetworkXmlWriterContext context, String namespace, String elementName, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEmptyElement(namespace, elementName);
        writeTerminalRefAttribute(t, context, writer);
    }

    public static void writeTerminalRefAttribute(Terminal t, NetworkXmlWriterContext context) throws XMLStreamException {
        writeTerminalRefAttribute(t, context, context.getWriter());
    }

    public static void writeTerminalRefAttribute(Terminal t, NetworkXmlWriterContext context, XMLStreamWriter writer) throws XMLStreamException {
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
        writer.writeAttribute("id", context.getAnonymizer().anonymizeString(c.getId()));

        TerminalUtil.getConnectableSide(t).ifPresent(side -> {
            try {
                writer.writeAttribute("side", side.name());
            } catch (XMLStreamException e) {
                throw new UncheckedXmlStreamException(e);
            }
        });
    }

    // FIXME(Luma) write and read are not exactly symmetrical at this point
    // write terminal ref writes the name of the Branch.Side or ThreeWindingTransformer.Side,
    // but read is interpreting this name as TerminalRef.Side, that unifies both names
    public static Terminal readTerminal(NetworkXmlReaderContext context, Network n) {
        String id = context.getAnonymizer().deanonymizeString(context.getReader().getAttributeValue(null, ID));
        String side = context.getReader().getAttributeValue(null, SIDE);
        return TerminalRefXml.resolve(id, side, n);
    }

    public static Terminal resolve(String id, String sideText, Network network) {
        ThreeSides side = sideText == null ? ThreeSides.ONE : ThreeSides.valueOf(sideText);
        return TerminalRefXml.resolve(id, side, network);
    }

    public static Terminal resolve(String id, ThreeSides side, Network network) {
        Identifiable<?> identifiable = network.getIdentifiable(id);
        if (identifiable == null) {
            throw new PowsyblException("Terminal reference identifiable not found: '" + id + "'");
        }
        if (identifiable instanceof Connectable) {
            return getTerminal((Connectable<?>) identifiable, side);
        } else {
            throw new PowsyblException("Unexpected terminal reference identifiable instance: " + identifiable.getClass());
        }
    }

    private static Terminal getTerminal(Connectable<?> connectable, ThreeSides side) {
        if (connectable instanceof Injection) {
            return ((Injection<?>) connectable).getTerminal();
        } else if (connectable instanceof Branch) {
            if (side.equals(ThreeSides.ONE)) {
                return ((Branch<?>) connectable).getTerminal1();
            } else if (side.equals(ThreeSides.TWO)) {
                return ((Branch<?>) connectable).getTerminal2();
            } else {
                throw new IllegalStateException("Unexpected Branch side: " + side.name());
            }
        } else if (connectable instanceof ThreeWindingsTransformer) {
            if (side.equals(ThreeSides.ONE)) {
                return ((ThreeWindingsTransformer) connectable).getLeg1().getTerminal();
            } else if (side.equals(ThreeSides.TWO)) {
                return ((ThreeWindingsTransformer) connectable).getLeg2().getTerminal();
            } else {
                return ((ThreeWindingsTransformer) connectable).getLeg3().getTerminal();
            }
        } else {
            throw new PowsyblException("Unexpected terminal reference identifiable instance: " + connectable.getClass());
        }
    }

    private TerminalRefXml() {
    }
}
