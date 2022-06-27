/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.xml;

import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class XmlUtilTest {

    private static final String XML = String.join(System.lineSeparator(),
            "<a>",
            "    <b>",
            "        <c/>",
            "    </b>",
            "    <d/>",
            "</a>");

    @Test
    public void readUntilEndElementWithDepthTest() throws XMLStreamException {
        Map<String, Integer> depths = new HashMap<>();
        try (StringReader reader = new StringReader(XML)) {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
            try {
                XmlUtil.readUntilEndElementWithDepth("a", xmlReader, elementDepth -> depths.put(xmlReader.getLocalName(), elementDepth));
            } finally {
                xmlReader.close();
            }
        }
        assertEquals(ImmutableMap.of("a", 0, "b", 1, "c", 2, "d", 1), depths);
    }

    @Test
    public void nestedReadUntilEndElementWithDepthTest() throws XMLStreamException {
        Map<String, Integer> depths = new HashMap<>();
        try (StringReader reader = new StringReader(XML)) {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
            try {
                XmlUtil.readUntilEndElementWithDepth("a", xmlReader, elementDepth -> {
                    depths.put(xmlReader.getLocalName(), elementDepth);
                    // consume b and c
                    if (xmlReader.getLocalName().equals("b")) {
                        try {
                            XmlUtil.readUntilEndElement("b", xmlReader, () -> {
                            });
                        } catch (XMLStreamException e) {
                            throw new UncheckedXmlStreamException(e);
                        }
                    }
                });
            } finally {
                xmlReader.close();
            }
        }
        assertEquals(ImmutableMap.of("a", 0, "b", 1, "d", 1), depths);
    }

    @Test
    public void readUntilStartElementTest() throws XMLStreamException {
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
                XmlUtil.readUntilStartElement(path, xmlReader, () -> assertEquals(expected, xmlReader.getLocalName()));
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
    public void readTextTest() throws XMLStreamException {
        String xml = "<a>hello</a>";
        try (StringReader reader = new StringReader(xml)) {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
            try {
                assertEquals("hello", XmlUtil.readText("a", xmlReader));
            } finally {
                xmlReader.close();
            }
        }
    }

    @Test
    public void initializeWriterDefault() throws XMLStreamException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter writer = XmlUtil.initializeWriter(true, " ", baos);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", baos.toString());
        writer.close();
    }

    @Test
    public void initializeWriter() throws XMLStreamException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter writer = XmlUtil.initializeWriter(false, " ", baos, StandardCharsets.ISO_8859_1);
        writer.flush();
        assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>", baos.toString());
        writer.close();
    }
}
