/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.xml;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.AbstractTreeDataReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class XmlReader extends AbstractTreeDataReader {

    private static final Supplier<XMLInputFactory> XML_INPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLInputFactory::newInstance);

    private final XMLStreamReader reader;
    private final Map<String, String> namespaceVersionsMap;
    private final Collection<ExtensionSerDe> extensionProviders;

    public XmlReader(InputStream is, Map<String, String> namespaceVersionMap, Collection<ExtensionSerDe> extensionProviders) throws XMLStreamException {
        this.reader = XML_INPUT_FACTORY_SUPPLIER.get().createXMLStreamReader(Objects.requireNonNull(is));
        int state = reader.next();
        while (state == XMLStreamConstants.COMMENT) {
            state = reader.next();
        }
        this.namespaceVersionsMap = Objects.requireNonNull(namespaceVersionMap);
        this.extensionProviders = Objects.requireNonNull(extensionProviders);
    }

    @Override
    public String readRootVersion() {
        return namespaceVersionsMap.get(reader.getNamespaceURI());
    }

    @Override
    public Map<String, String> readVersions() {
        Map<String, String> versions = new HashMap<>();
        for (ExtensionSerDe<?, ?> e : extensionProviders) {
            String namespaceUri = reader.getNamespaceURI(e.getNamespacePrefix());
            if (namespaceUri != null) {
                versions.put(e.getExtensionName(), e.getVersion(namespaceUri));
            }
        }
        return versions;
    }

    @Override
    public double readDoubleAttribute(String name, double defaultValue) {
        String attributeValue = reader.getAttributeValue(null, name);
        return attributeValue != null ? Double.parseDouble(attributeValue) : defaultValue;
    }

    @Override
    public float readFloatAttribute(String name, float defaultValue) {
        String attributeValue = reader.getAttributeValue(null, name);
        return attributeValue != null ? Float.parseFloat(attributeValue) : defaultValue;
    }

    @Override
    public String readStringAttribute(String name) {
        return reader.getAttributeValue(null, name);
    }

    @Override
    public Integer readIntAttribute(String name) {
        String attributeValue = reader.getAttributeValue(null, name);
        return attributeValue != null ? Integer.valueOf(attributeValue) : null;
    }

    @Override
    public int readIntAttribute(String name, int defaultValue) {
        String attributeValue = reader.getAttributeValue(null, name);
        return attributeValue != null ? Integer.parseInt(attributeValue) : defaultValue;
    }

    @Override
    public Boolean readBooleanAttribute(String name) {
        String attributeValue = reader.getAttributeValue(null, name);
        return attributeValue != null ? Boolean.valueOf(attributeValue) : null;
    }

    @Override
    public boolean readBooleanAttribute(String name, boolean defaultValue) {
        String attributeValue = reader.getAttributeValue(null, name);
        return attributeValue != null ? Boolean.parseBoolean(attributeValue) : defaultValue;
    }

    @Override
    public String readContent() {
        try {
            return XmlUtil.readText(reader);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public List<Integer> readIntArrayAttribute(String name) {
        String arrayString = readStringAttribute(name);
        return Arrays.stream(arrayString.split(","))
                .map(Integer::parseInt)
                .toList();
    }

    @Override
    public void readChildNodes(ChildNodeReader childNodeReader) {
        XmlUtil.readSubElements(reader, childNodeReader);
    }

    @Override
    public void readEndNode() {
        try {
            XmlUtil.readEndElementOrThrow(reader);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public void close() {
        try {
            reader.close();
            XmlUtil.gcXmlInputFactory(XML_INPUT_FACTORY_SUPPLIER.get());
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }
}
