/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.powsybl.iidm.xml.IidmXmlConstants.IIDM_URI;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class TerminalRefXml {

    public static void writeTerminalRef(Terminal t, NetworkXmlWriterContext context, String elementName) throws XMLStreamException {
        writeTerminalRef(t, context, IIDM_URI, elementName);
    }

    public static void writeTerminalRef(Terminal t, NetworkXmlWriterContext context, String namespace, String elementName) throws XMLStreamException {
        writeTerminalRef(t, context, namespace, elementName, context.getWriter());
    }

    public static void writeTerminalRef(Terminal t, NetworkXmlWriterContext context, String namespace, String elementName, XMLStreamWriter writer) throws XMLStreamException {
        Connectable c = t.getConnectable();
        if (!context.getFilter().test(c)) {
            throw new PowsyblException("Oups, terminal ref point to a filtered equipment " + c.getId());
        }
        writer.writeEmptyElement(namespace, elementName);
        writer.writeAttribute("id", context.getAnonymizer().anonymizeString(c.getId()));
        if (c.getTerminals().size() > 1) {
            if (c instanceof Injection) {
                // nothing to do
            } else if (c instanceof Branch) {
                Branch branch = (Branch) c;
                writer.writeAttribute("side", branch.getSide(t).name());
            } else if (c instanceof ThreeWindingsTransformer) {
                ThreeWindingsTransformer twt = (ThreeWindingsTransformer) c;
                writer.writeAttribute("side", twt.getSide(t).name());
            } else {
                throw new AssertionError("Unexpected Connectable instance: " + c.getClass());
            }
        }
    }

    public static Terminal readTerminalRef(Network network, String id, String side) {
        Identifiable identifiable = network.getIdentifiable(id);
        if (identifiable instanceof Injection) {
            return ((Injection) identifiable).getTerminal();
        } else if (identifiable instanceof Branch) {
            return side.equals(Branch.Side.ONE.name()) ? ((Branch) identifiable).getTerminal1()
                    : ((Branch) identifiable).getTerminal2();
        } else if (identifiable instanceof ThreeWindingsTransformer) {
            ThreeWindingsTransformer twt = (ThreeWindingsTransformer) identifiable;
            return twt.getTerminal(ThreeWindingsTransformer.Side.valueOf(side));
        } else {
            throw new AssertionError("Unexpected Identifiable instance: " + identifiable.getClass());
        }
    }

    private TerminalRefXml() {
    }
}
