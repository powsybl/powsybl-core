/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.model;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.xml.XmlUtil;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
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
                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    root = true;
                    for (int k = 0; k < xmlsr.getNamespaceCount(); k++) {
                        found.add(xmlsr.getNamespaceURI(k));
                    }
                }
            }
        } finally {
            xmlsr.close();
            XmlUtil.gcXmlInputFactory(XML_INPUT_FACTORY_SUPPLIER.get());
        }
        return found;
    }

    public static String base(InputStream is) {
        XMLStreamReader xmlsr;
        try {
            xmlsr = XML_INPUT_FACTORY_SUPPLIER.get().createXMLStreamReader(is);
            try {
                while (xmlsr.hasNext()) {
                    int eventType = xmlsr.next();
                    if (eventType == XMLStreamConstants.START_ELEMENT) {
                        return xmlsr.getAttributeValue(null, "base");
                    }
                }
            } finally {
                xmlsr.close();
                XmlUtil.gcXmlInputFactory(XML_INPUT_FACTORY_SUPPLIER.get());
            }
            return null;
        } catch (XMLStreamException e) {
            throw new CgmesModelException("base", e);
        }
    }

    private static final Supplier<XMLInputFactory> XML_INPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLInputFactory::newInstance);
}
