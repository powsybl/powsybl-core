/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.xml;

import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.io.TreeDataReader;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class XmlReader implements TreeDataReader {

    private final XMLStreamReader reader;

    public XmlReader(XMLStreamReader reader) {
        this.reader = Objects.requireNonNull(reader);
    }

    @Override
    public double readDoubleAttribute(String name) {
        return readDoubleAttribute(name, Double.NaN);
    }

    @Override
    public double readDoubleAttribute(String name, double defaultValue) {
        String attributeValue = reader.getAttributeValue(null, name);
        return attributeValue != null ? Double.parseDouble(attributeValue) : defaultValue;
    }

    @Override
    public float readFloatAttribute(String name) {
        return readFloatAttribute(name, Float.NaN);
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
    public <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz) {
        return readEnumAttribute(name, clazz, null);
    }

    @Override
    public <T extends Enum<T>> T readEnumAttribute(String name, Class<T> clazz, T defaultValue) {
        String attributeValue = reader.getAttributeValue(null, name);
        return attributeValue != null ? Enum.valueOf(clazz, attributeValue) : defaultValue;
    }

    @Override
    public String getNodeName() {
        return reader.getLocalName();
    }

    @Override
    public String readContent() {
        try {
            return reader.getElementText();
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public String readUntilEndNode(String endNodeName, EventHandler eventHandler) {
        try {
            return XmlUtil.readUntilEndElement(endNodeName, reader, eventHandler);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    @Override
    public String readUntilEndNodeWithDepth(String endNodeName, EventHandlerWithDepth eventHandler) {
        try {
            return XmlUtil.readUntilEndElementWithDepth(endNodeName, reader, eventHandler);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }
}
