/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public final class NamespaceReader {

    private NamespaceReader() {
    }

    public static Set<String> namespaces(InputStream is) {
        try {
            return namespaces1(is);
        } catch (XMLStreamException x) {
            throw new CgmesModelException("namespaces", x);
        }
    }

    public static Set<String> namespaces1(InputStream is) throws XMLStreamException {
        Set<String> found = new HashSet<>();
        XMLStreamReader xmlsr = XML_INPUT_FACTORY_SUPPLIER.get().createXMLStreamReader(is);
        try {
            boolean root = false;
            while (xmlsr.hasNext() && !root) {
                int eventType = xmlsr.next();
                if (eventType == XMLStreamReader.START_ELEMENT) {
                    root = true;
                    for (int k = 0; k < xmlsr.getNamespaceCount(); k++) {
                        found.add(xmlsr.getNamespaceURI(k));
                    }
                }
            }
        } finally {
            xmlsr.close();
        }
        return found;
    }

    private static final Supplier<XMLInputFactory> XML_INPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLInputFactory::newInstance);
}
