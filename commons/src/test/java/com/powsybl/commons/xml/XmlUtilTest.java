/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.xml;

import com.google.common.collect.ImmutableMap;
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

    @Test
    public void readUntilEndElementWithDepthTest() throws XMLStreamException {
        String xml = String.join(System.lineSeparator(),
                "<a>",
                "    <b>",
                "        <c/>",
                "    </b>",
                "    <d/>",
                "</a>");
        Map<String, Integer> depths = new HashMap<>();
        try (StringReader reader = new StringReader(xml)) {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
            try {
                XmlUtil.readUntilEndElementWithDepth("a", xmlReader, elementDepth -> depths.put(xmlReader.getLocalName(), elementDepth));
            } finally {
                xmlReader.close();
            }
        }
        assertEquals(ImmutableMap.of("a", 0, "b", 1, "c", 2, "d", 1), depths);
    }
}
