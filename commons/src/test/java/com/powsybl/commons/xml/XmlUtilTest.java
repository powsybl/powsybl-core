/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class XmlUtilTest {

    private static final String XML = String.join(System.lineSeparator(),
            "<a>",
            "    <b>",
            "        <c/>",
            "    </b>",
            "    <d/>",
            "</a>");

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
                        XmlUtil.readSubElements(xmlReader);
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
                            XmlUtil.readSubElements(xmlReader);
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
                XmlUtil.readUntilStartElement(path, xmlReader, (String elementName) -> assertEquals(expected, xmlReader.getLocalName()));
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
