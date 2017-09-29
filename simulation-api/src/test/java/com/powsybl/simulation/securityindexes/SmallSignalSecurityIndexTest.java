/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation.securityindexes;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SmallSignalSecurityIndexTest {

    public SmallSignalSecurityIndexTest() {
    }

    @Test
    public void testXml() throws IOException, XMLStreamException {
        String xml = "<?xml version=\"1.0\" ?><index name=\"smallsignal\"><matrix name=\"gmi\"><m><r>0.5</r></m></matrix><matrix name=\"ami\"><m><r>1.0 2.0</r></m></matrix><matrix name=\"smi\"><m><r>3.0 4.0</r><r>5.0 6.0</r></m></matrix></index>";
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        SmallSignalSecurityIndex index;
        try (Reader reader = new StringReader(xml)) {
            XMLStreamReader xmlReader = xmlif.createXMLStreamReader(reader);
            try {
                index = SmallSignalSecurityIndex.fromXml("c1", xmlReader);
            } finally {
                xmlReader.close();
            }
        }
        assertTrue(index.getGmi() == 0.5d);
        assertTrue(Arrays.equals(index.getAmi(), new double[] {1, 2}));
        assertTrue(Arrays.deepEquals(index.getSmi(), new double[][] {new double[] {3, 4}, new double[] {5, 6}}));
        assertEquals(xml, index.toXml());
    }

}
