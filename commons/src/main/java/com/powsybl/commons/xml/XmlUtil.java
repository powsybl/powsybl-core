/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.xml;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class XmlUtil {

    private XmlUtil() {
    }

    public interface XmlEventHandler {

        void onStartElement() throws XMLStreamException;

    }

    public static String readUntilEndElement(String endElementName, XMLStreamReader reader, XmlEventHandler eventHandler) throws XMLStreamException {
        String text = null;
        int event;
        while (!((event = reader.next()) == XMLStreamConstants.END_ELEMENT
                && reader.getLocalName().equals(endElementName))) {
            text = null;
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (eventHandler != null) {
                        eventHandler.onStartElement();
                    }
                    break;

                case XMLStreamConstants.CHARACTERS:
                    text = reader.getText();
                    break;

                default:
                    break;
            }
        }
        return text;
    }

    public static void writeOptionalBoolean(String name, boolean value, boolean absentValue, XMLStreamWriter writer) throws XMLStreamException {
        if (value != absentValue) {
            writer.writeAttribute(name, Boolean.toString(value));
        }
    }

    public static void writeDouble(String name, double value, XMLStreamWriter writer) throws XMLStreamException {
        if (!Double.isNaN(value)) {
            writer.writeAttribute(name, Double.toString(value));
        }
    }

    public static void writeOptionalDouble(String name, double value, double absentValue, XMLStreamWriter writer) throws XMLStreamException {
        if (!Double.isNaN(value) && value != absentValue) {
            writer.writeAttribute(name, Double.toString(value));
        }
    }

    public static void writeFloat(String name, float value, XMLStreamWriter writer) throws XMLStreamException {
        if (!Float.isNaN(value)) {
            writer.writeAttribute(name, Float.toString(value));
        }
    }

    public static void writeOptionalFloat(String name, float value, float absentValue, XMLStreamWriter writer) throws XMLStreamException {
        if (!Float.isNaN(value) && value != absentValue) {
            writer.writeAttribute(name, Float.toString(value));
        }
    }

    public static void writeInt(String name, int value, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeAttribute(name, Integer.toString(value));
    }

    public static void writeOptionalInt(String name, int value, int absentValue, XMLStreamWriter writer) throws XMLStreamException {
        if (value != absentValue) {
            writer.writeAttribute(name, Integer.toString(value));
        }
    }

    public static int readIntAttribute(XMLStreamReader reader, String attributeName) {
        return Integer.parseInt(reader.getAttributeValue(null, attributeName));
    }

    public static boolean readBoolAttribute(XMLStreamReader reader, String attributeName) {
        return Boolean.valueOf(reader.getAttributeValue(null, attributeName));
    }

    public static boolean readOptionalBoolAttribute(XMLStreamReader reader, String attributeName, boolean defaultValue) {
        String attributeValue = reader.getAttributeValue(null, attributeName);
        return attributeValue != null ? Boolean.valueOf(attributeValue) : defaultValue;
    }

    public static double readDoubleAttribute(XMLStreamReader reader, String attributeName) {
        return Double.valueOf(reader.getAttributeValue(null, attributeName));
    }

    public static double readOptionalDoubleAttribute(XMLStreamReader reader, String attributeName) {
        return readOptionalDoubleAttribute(reader, attributeName, Double.NaN);
    }

    public static double readOptionalDoubleAttribute(XMLStreamReader reader, String attributeName, double defaultValue) {
        String attributeValue = reader.getAttributeValue(null, attributeName);
        return attributeValue != null ? Double.valueOf(attributeValue) : defaultValue;
    }

    public static Integer readOptionalIntegerAttribute(XMLStreamReader reader, String attributeName) {
        String attributeValue = reader.getAttributeValue(null, attributeName);
        return attributeValue != null ? Integer.valueOf(attributeValue) : null;
    }

    public static int readOptionalIntegerAttribute(XMLStreamReader reader, String attributeName, int defaultValue) {
        String attributeValue = reader.getAttributeValue(null, attributeName);
        return attributeValue != null ? Integer.parseInt(attributeValue) : defaultValue;
    }

    public static float readFloatAttribute(XMLStreamReader reader, String attributeName) {
        return Float.valueOf(reader.getAttributeValue(null, attributeName));
    }

    public static float readOptionalFloatAttribute(XMLStreamReader reader, String attributeName) {
        return readOptionalFloatAttribute(reader, attributeName, Float.NaN);
    }

    public static float readOptionalFloatAttribute(XMLStreamReader reader, String attributeName, float defaultValue) {
        String attributeValue = reader.getAttributeValue(null, attributeName);
        return attributeValue != null ? Float.valueOf(attributeValue) : defaultValue;
    }
}
