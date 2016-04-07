/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.histo;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class HistoDbAttributeIdParserTest {

    public HistoDbAttributeIdParserTest() {
    }

    @Test
    public void testParseMetaAttr() {
        HistoDbAttributeId attrId = HistoDbAttributeIdParser.parse("cimName");
        assertTrue(attrId instanceof HistoDbMetaAttributeId);
    }

    @Test
    public void testParseBranchAttr() {
        HistoDbAttributeId attrId = HistoDbAttributeIdParser.parse("LINE1__TO__SUB1_P");
        assertTrue(attrId instanceof HistoDbNetworkAttributeId);
        assertTrue("LINE1".equals(((HistoDbNetworkAttributeId) attrId).getEquipmentId()));
        assertTrue("SUB1".equals(((HistoDbNetworkAttributeId) attrId).getSide()));
        assertTrue(((HistoDbNetworkAttributeId) attrId).getAttributeType() == HistoDbAttr.P);
    }

    @Test
    public void testParseGeneratorAttr() {
        HistoDbAttributeId attrId = HistoDbAttributeIdParser.parse("GEN1_V");
        assertTrue(attrId instanceof HistoDbNetworkAttributeId);
        assertTrue("GEN1".equals(((HistoDbNetworkAttributeId) attrId).getEquipmentId()));
        assertTrue(((HistoDbNetworkAttributeId) attrId).getSide() == null);
        assertTrue(((HistoDbNetworkAttributeId) attrId).getAttributeType() == HistoDbAttr.V);
    }

}
