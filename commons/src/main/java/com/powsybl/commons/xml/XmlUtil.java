/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.xml;

import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import javanet.staxutils.IndentingXMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class XmlUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtil.class);

    private static final Supplier<XMLOutputFactory> XML_OUTPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLOutputFactory::newFactory);

    private XmlUtil() {
    }

    public interface XmlEventHandler {

        void onStartElement() throws XMLStreamException;
    }

    /**
     * An richer event handler which give element depth with each start event.
     */
    public interface XmlEventHandlerWithDepth {

        void onStartElement(int elementDepth) throws XMLStreamException;
    }

    public static void readUntilStartElement(String path, XMLStreamReader reader, XmlEventHandler handler) throws XMLStreamException {
        Objects.requireNonNull(path);
        String[] elements = path.split("/");
        readUntilStartElement(elements, reader, handler);
    }

    public static void readUntilStartElement(String[] elements, XMLStreamReader reader, XmlEventHandler handler) throws XMLStreamException {
        Objects.requireNonNull(elements);
        if (elements.length == 0) {
            throw new PowsyblException("Empty element list");
        }
        StringBuilder currentPath = new StringBuilder();
        for (int i = 1; i < elements.length; ++i) {
            currentPath.append("/").append(elements[i]);
            if (!readUntilStartElement(elements[i], elements[i - 1], reader)) {
                throw new PowsyblException("Unable to find " + currentPath.toString() + ": parent element " + elements[i - 1] + " has been closed");
            }
        }
        if (handler != null) {
            handler.onStartElement();
        }
    }

    private static boolean readUntilStartElement(String startElement, String endElement, XMLStreamReader reader) throws XMLStreamException {
        int event;
        while ((event = reader.next()) != XMLStreamConstants.END_DOCUMENT) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (reader.getLocalName().equals(startElement)) {
                        return true;
                    } else {
                        // Skip the current element
                        readUntilEndElement(reader.getLocalName(), reader, null);
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (reader.getLocalName().equals(endElement)) {
                        return false;
                    }
                    break;

                default:
                    break;
            }
        }
        throw new PowsyblException("Unable to find " + startElement + ": end of document has been reached");
    }

    public static String readUntilEndElement(String endElementName, XMLStreamReader reader, XmlEventHandler eventHandler) throws XMLStreamException {
        return readUntilEndElementWithDepth(endElementName, reader, elementDepth -> {
            if (eventHandler != null) {
                eventHandler.onStartElement();
            }
        });
    }

    public static String readUntilEndElementWithDepth(String endElementName, XMLStreamReader reader, XmlEventHandlerWithDepth eventHandler) throws XMLStreamException {
        Objects.requireNonNull(endElementName);
        Objects.requireNonNull(reader);

        String text = null;
        int event;
        int depth = 0;
        while (!((event = reader.next()) == XMLStreamConstants.END_ELEMENT
                && reader.getLocalName().equals(endElementName))) {
            text = null;
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    if (eventHandler != null) {
                        String startLocalName = reader.getLocalName();
                        eventHandler.onStartElement(depth);
                        // if handler has already consumed end element we must decrease the depth
                        if (reader.getEventType() == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(startLocalName)) {
                            depth--;
                        }
                    }
                    depth++;
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    depth--;
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

    public static String readText(String endElementName, XMLStreamReader reader) throws XMLStreamException {
        return readUntilEndElement(endElementName, reader, () -> { });
    }

    public static void writeOptionalBoolean(String name, boolean value, boolean absentValue, XMLStreamWriter writer) throws XMLStreamException {
        if (value != absentValue) {
            writer.writeAttribute(name, Boolean.toString(value));
        }
    }

    public static void writeOptionalBoolean(String name, Optional<Boolean> value, XMLStreamWriter writer) throws XMLStreamException {
        if (value.isPresent()) {
            writer.writeAttribute(name, Boolean.toString(value.get()));
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

    public static void writeOptionalString(String name, String value, XMLStreamWriter writer) throws XMLStreamException {
        if (value != null) {
            writer.writeAttribute(name, value);
        }
    }

    public static <E extends Enum<E>> void writeOptionalEnum(String name, E value, XMLStreamWriter writer) throws XMLStreamException {
        if (value != null) {
            writer.writeAttribute(name, value.name());
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

    public static <E extends Enum<E>> E readOptionalEnum(XMLStreamReader reader, String attributeName, Class<E> enumClass) {
        String attributeValue = reader.getAttributeValue(null, attributeName);
        return attributeValue != null ? Enum.valueOf(enumClass, attributeValue) : null;
    }

    public static void consumeOptionalBoolAttribute(XMLStreamReader reader, String attributeName, Consumer<Boolean> consumer) {
        String attributeValue = reader.getAttributeValue(null, attributeName);
        if (attributeValue != null) {
            consumer.accept(Boolean.parseBoolean(attributeValue));
        }
    }

    public static void consumeOptionalIntAttribute(XMLStreamReader reader, String attributeName, IntConsumer consumer) {
        String attributeValue = reader.getAttributeValue(null, attributeName);
        if (attributeValue != null) {
            consumer.accept(Integer.parseInt(attributeValue));
        }
    }

    public static XMLStreamWriter initializeWriter(boolean indent, String indentString, OutputStream os) throws XMLStreamException {
        XMLStreamWriter writer = XML_OUTPUT_FACTORY_SUPPLIER.get().createXMLStreamWriter(os, StandardCharsets.UTF_8.toString());
        return initializeWriter(indent, indentString, writer);
    }

    public static XMLStreamWriter initializeWriter(boolean indent, String indentString, OutputStream os, Charset charset) throws XMLStreamException {
        XMLStreamWriter writer = XML_OUTPUT_FACTORY_SUPPLIER.get().createXMLStreamWriter(os, charset.name());
        return initializeWriter(indent, indentString, writer, charset);
    }

    public static XMLStreamWriter initializeWriter(boolean indent, String indentString, Writer writer) throws XMLStreamException {
        XMLStreamWriter xmlWriter = XML_OUTPUT_FACTORY_SUPPLIER.get().createXMLStreamWriter(writer);
        return initializeWriter(indent, indentString, xmlWriter);
    }

    private static XMLStreamWriter initializeWriter(boolean indent, String indentString, XMLStreamWriter initialXmlWriter) throws XMLStreamException {
        return initializeWriter(indent, indentString, initialXmlWriter, StandardCharsets.UTF_8);
    }

    private static XMLStreamWriter initializeWriter(boolean indent, String indentString, XMLStreamWriter initialXmlWriter, Charset charset) throws XMLStreamException {
        XMLStreamWriter xmlWriter;
        if (indent) {
            IndentingXMLStreamWriter indentingWriter = new IndentingXMLStreamWriter(initialXmlWriter);
            indentingWriter.setIndent(indentString);
            xmlWriter = indentingWriter;
        } else {
            xmlWriter = initialXmlWriter;
        }
        xmlWriter.writeStartDocument(charset.name(), "1.0");
        return xmlWriter;
    }

    public static void gcXmlInputFactory(XMLInputFactory xmlInputFactory) {
        // Workaround: Manually force XMLInputFactory and XmlStreamReader to clear the reference to the last inputstream.
        // jdk xerces XmlInputFactory keeps a ref to the last created XmlStreamReader (so that the factory
        // can optionally reuse it. But the XmlStreamReader keeps a ref to it's inputstream. There is
        // no public API in XmlStreamReader to clear the previous input stream, close doesn't do it).
        try (InputStream is = new ByteArrayInputStream(new byte[] {})) {
            XMLStreamReader xmlsr = xmlInputFactory.createXMLStreamReader(is);
            xmlsr.close();
        } catch (XMLStreamException | IOException e) {
            LOGGER.error(e.toString(), e);
        }
    }
}
