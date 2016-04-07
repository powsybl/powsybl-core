/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.histo;

import org.junit.Test;

import java.util.Iterator;
import java.util.TreeSet;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class HistoDbAttributeIdTest {

    @Test
    public void testCompareTo() throws Exception {
        TreeSet<HistoDbAttributeId> attributeIds = new TreeSet<>();
        attributeIds.add(HistoDbMetaAttributeId.daytime);
        HistoDbNetworkAttributeId attrId1 = new HistoDbNetworkAttributeId("eq1", HistoDbAttr.P);
        attributeIds.add(attrId1);
        attributeIds.add(HistoDbMetaAttributeId.cimName);
        HistoDbNetworkAttributeId attrId2 = new HistoDbNetworkAttributeId("eq2", "s1", HistoDbAttr.Q);
        attributeIds.add(attrId2);
        Iterator<HistoDbAttributeId> it = attributeIds.iterator();
        assertEquals(it.next(), HistoDbMetaAttributeId.cimName);
        assertEquals(it.next(), HistoDbMetaAttributeId.daytime);
        assertEquals(it.next(), attrId1);
        assertEquals(it.next(), attrId2);
    }
}