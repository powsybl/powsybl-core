/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation.securityindexes;

import org.junit.Test;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TransientSecurityIndexTest {

    public TransientSecurityIndexTest() {
    }

    @Test
    public void testXml() throws IOException, XMLStreamException {
        String xml = "<?xml version=\"1.0\" ?><index name=\"transient\"><j>0.5</j></index>";
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        TransientSecurityIndex index;
        try (Reader reader = new StringReader(xml)) {
            XMLStreamReader xmlReader = xmlif.createXMLStreamReader(reader);
            try {
                index = TransientSecurityIndex.fromXml("c1", xmlReader);
            } finally {
                xmlReader.close();
            }
        }
        assertTrue(index.getJ() == 0.5d);
        assertEquals(xml, index.toXml());
    }

}
