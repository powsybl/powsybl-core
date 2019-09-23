/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.xml;

import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
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

}
