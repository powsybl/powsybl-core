/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.xml;

import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.PowsyblException;
import org.junit.jupiter.api.Test;

import javax.xml.stream.*;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class XmlUtilTest {

    private static final String XML = String.join(System.lineSeparator(),
            "<a>",
            "    <b attrBool=\"true\" attrInt=\"34\" attrDbl=\"2e-65\" attrFlt=\"0.054864\">",
            "        <c/>",
            "    </b>",
            "    <d/>",
            "</a>");

    @Test
    void readAttributes() throws XMLStreamException {
        AtomicReference<Boolean> attrBoolBoxed = new AtomicReference<>(false);
        AtomicBoolean attrBool = new AtomicBoolean(false);
        AtomicReference<Integer> attrInteger = new AtomicReference<>(-1);
        AtomicInteger attrInt = new AtomicInteger(-1);
        AtomicReference<Double> attrDbl = new AtomicReference<>(0d);
        AtomicReference<Float> attrFloat = new AtomicReference<>(0f);
        try (StringReader reader = new StringReader(XML)) {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
            xmlReader.next();
            try {
                XmlUtil.readSubElements(xmlReader, elementName -> {
                    if ("b".equals(elementName)) {
                        attrBoolBoxed.set(XmlUtil.readBooleanAttribute(xmlReader, "attrBool"));
                        attrBool.set(XmlUtil.readBooleanAttribute(xmlReader, "attrBool", false));
                        attrInteger.set(XmlUtil.readIntegerAttribute(xmlReader, "attrInt"));
                        attrInt.set(XmlUtil.readIntAttribute(xmlReader, "attrInt", -1));
                        attrDbl.set(XmlUtil.readDoubleAttribute(xmlReader, "attrDbl", 0));
                        attrFloat.set(XmlUtil.readFloatAttribute(xmlReader, "attrFlt", 0));
                    }
                });
            } finally {
                xmlReader.close();
            }
        }

        assertTrue(attrBoolBoxed::get);
        assertTrue(attrBool.get());
        assertEquals(34, attrInteger.get());
        assertEquals(34, attrInt.get());
        assertEquals(2e-65, attrDbl.get(), 1e-80);
        assertEquals(0.054864f, attrFloat.get(), 1e-15);
    }

    @Test
    void readUntilEndElementWithDepthTest() throws XMLStreamException {
        Map<String, Integer> depths = new HashMap<>();
        try (StringReader reader = new StringReader(XML)) {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
            xmlReader.next();
            try {
                XmlUtil.readSubElements(xmlReader, elementName -> {
                    depths.put(elementName, 0);
                    XmlUtil.readSubElements(xmlReader, elementName1 -> {
                        depths.put(elementName1, 1);
                        XmlUtil.skipSubElements(xmlReader);
                    });
                });
            } finally {
                xmlReader.close();
            }
        }
        assertEquals(ImmutableMap.of("b", 0, "c", 1, "d", 0), depths);
    }

    @Test
    void nestedReadUntilEndElementWithDepthTest() throws XMLStreamException {
        Map<String, Integer> depths = new HashMap<>();
        try (StringReader reader = new StringReader(XML)) {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
            try {
                xmlReader.next();
                XmlUtil.readSubElements(xmlReader, elementName -> {
                    depths.put(elementName, 0);
                    // consume b and c
                    if (elementName.equals("b")) {
                        XmlUtil.readSubElements(xmlReader, elementName1 -> {
                            depths.put(elementName1, 1);
                            XmlUtil.skipSubElements(xmlReader);
                        });
                    }
                });
            } finally {
                xmlReader.close();
            }
        }
        assertEquals(ImmutableMap.of("b", 0, "c", 1, "d", 0), depths);
    }

    @Test
    void readUntilStartElementTest() throws XMLStreamException {
        readUntilStartElementTest("/a", "a");
        readUntilStartElementTest("/a/b/c", "c");
        readUntilStartElementTest("/a/d", "d");

        readUntilStartElementNotFoundTest("/a/e", "a");
        readUntilStartElementNotFoundTest("/a/b/a", "b");

        try {
            readUntilStartElementTest("/b", null);
        } catch (PowsyblException e) {
            assertEquals("Unable to find b: end of document has been reached", e.getMessage());
        }
    }

    private void readUntilStartElementTest(String path, String expected) throws XMLStreamException {
        try (StringReader reader = new StringReader(XML)) {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
            try {
                XmlUtil.readUntilStartElement(path, xmlReader, elementName -> assertEquals(expected, xmlReader.getLocalName()));
            } finally {
                xmlReader.close();
            }
        }
    }

    private void readUntilStartElementNotFoundTest(String path, String parent) throws XMLStreamException {
        try {
            readUntilStartElementTest(path, null);
        } catch (PowsyblException e) {
            assertEquals("Unable to find " + path + ": parent element " + parent + " has been closed", e.getMessage());
        }
    }

    @Test
    void readTextTest() throws XMLStreamException {
        String xml = "<a>hello</a>";
        try (StringReader reader = new StringReader(xml)) {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
            try {
                String text = null;
                while (xmlReader.hasNext()) {
                    int next = xmlReader.next();
                    if (next == XMLStreamConstants.START_ELEMENT && xmlReader.getLocalName().equals("a")) {
                        text = XmlUtil.readText(xmlReader);
                    }
                }
                assertEquals("hello", text);
            } finally {
                xmlReader.close();
            }
        }
    }

    @Test
    void initializeWriterDefault() throws XMLStreamException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter writer = XmlUtil.initializeWriter(true, " ", baos);
        writer.close();
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", baos.toString());
    }

    @Test
    void initializeWriter() throws XMLStreamException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter writer = XmlUtil.initializeWriter(false, " ", baos, StandardCharsets.ISO_8859_1);
        writer.close();
        assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>", baos.toString());
    }
}
