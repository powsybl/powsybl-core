/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.xml;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
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
        return XmlUtil.readDoubleAttribute(reader, name, defaultValue);
    }

    @Override
    public Double readOptionalDoubleAttribute(String name) {
        return XmlUtil.readOptionalDoubleAttribute(reader, name);
    }

    @Override
    public float readFloatAttribute(String name, float defaultValue) {
        return XmlUtil.readFloatAttribute(reader, name, defaultValue);
    }

    @Override
    public String readStringAttribute(String name) {
        return reader.getAttributeValue(null, name);
    }

    @Override
    public int readIntAttribute(String name) {
        Integer value = XmlUtil.readIntegerAttribute(reader, name);
        if (value == null) {
            throw new PowsyblException("XML parsing: unknown required attribute '" + name + "'");
        }
        return value;
    }

    @Override
    public Integer readOptionalIntAttribute(String name) {
        return XmlUtil.readIntegerAttribute(reader, name);
    }

    @Override
    public int readIntAttribute(String name, int defaultValue) {
        return XmlUtil.readIntAttribute(reader, name, defaultValue);
    }

    @Override
    public Boolean readBooleanAttribute(String name) {
        return XmlUtil.readBooleanAttribute(reader, name);
    }

    @Override
    public boolean readBooleanAttribute(String name, boolean defaultValue) {
        return XmlUtil.readBooleanAttribute(reader, name, defaultValue);
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
        return Arrays.stream(readAndSplitStringArray(name))
                .map(Integer::parseInt)
                .toList();
    }

    @Override
    public List<String> readStringArrayAttribute(String name) {
        return Arrays.asList(readAndSplitStringArray(name));
    }

    private String[] readAndSplitStringArray(String name) {
        String arrayString = readStringAttribute(name);
        if (arrayString == null) {
            return new String[0];
        }
        return arrayString.split(",");
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
