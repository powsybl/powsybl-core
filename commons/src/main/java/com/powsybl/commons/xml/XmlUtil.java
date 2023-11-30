/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.xml;

import com.google.common.base.Suppliers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.io.TreeDataReader;
import javanet.staxutils.IndentingXMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class XmlUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtil.class);

    private static final Supplier<XMLOutputFactory> XML_OUTPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLOutputFactory::newFactory);

    private XmlUtil() {
    }

    public static void readUntilStartElement(String path, XMLStreamReader reader, TreeDataReader.ChildNodeReader handler) throws XMLStreamException {
        Objects.requireNonNull(path);
        String[] elements = path.split("/");
        readUntilStartElement(elements, reader, handler);
    }

    public static void readUntilStartElement(String[] elements, XMLStreamReader reader, TreeDataReader.ChildNodeReader handler) throws XMLStreamException {
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
            handler.onStartNode(elements[elements.length - 1]);
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
                        skipSubElements(reader);
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

    public static void skipSubElements(XMLStreamReader reader) {
        readSubElements(reader, elementName -> skipSubElements(reader));
    }

    public static void readSubElements(XMLStreamReader reader, TreeDataReader.ChildNodeReader childNodeReader) {
        Objects.requireNonNull(reader);
        Objects.requireNonNull(childNodeReader);

        try {
            int event;
            while ((event = reader.next()) != XMLStreamConstants.END_ELEMENT) {
                if (event == XMLStreamConstants.START_ELEMENT) {
                    childNodeReader.onStartNode(reader.getLocalName());
                }
            }
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    public static String readText(XMLStreamReader reader) throws XMLStreamException {
        String text = reader.getElementText();
        return text.isEmpty() ? null : text;
    }

    public static Integer readIntegerAttribute(XMLStreamReader reader, String name) {
        String attributeValue = reader.getAttributeValue(null, name);
        return attributeValue != null ? Integer.valueOf(attributeValue) : null;
    }

    public static int readIntAttribute(XMLStreamReader reader, String name, int defaultValue) {
        String attributeValue = reader.getAttributeValue(null, name);
        return attributeValue != null ? Integer.parseInt(attributeValue) : defaultValue;
    }

    public static Boolean readBooleanAttribute(XMLStreamReader reader, String name) {
        String attributeValue = reader.getAttributeValue(null, name);
        return attributeValue != null ? Boolean.valueOf(attributeValue) : null;
    }

    public static boolean readBooleanAttribute(XMLStreamReader reader, String name, boolean defaultValue) {
        String attributeValue = reader.getAttributeValue(null, name);
        return attributeValue != null ? Boolean.parseBoolean(attributeValue) : defaultValue;
    }

    public static double readDoubleAttribute(XMLStreamReader reader, String name, double defaultValue) {
        String attributeValue = reader.getAttributeValue(null, name);
        return attributeValue != null ? Double.parseDouble(attributeValue) : defaultValue;
    }

    public static float readFloatAttribute(XMLStreamReader reader, String name, float defaultValue) {
        String attributeValue = reader.getAttributeValue(null, name);
        return attributeValue != null ? Float.parseFloat(attributeValue) : defaultValue;
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

    public static void readEndElementOrThrow(XMLStreamReader reader) throws XMLStreamException {
        if (reader.next() != XMLStreamConstants.END_ELEMENT) {
            throw new PowsyblException("XMLStreamConstants.END_ELEMENT expected but found another event (eventType = '" + reader.getEventType() + "')");
        }
    }
}
