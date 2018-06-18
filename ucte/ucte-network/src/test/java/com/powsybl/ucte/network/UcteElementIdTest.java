/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class UcteElementIdTest {

    @Test
    public void test() {
        UcteNodeCode node1 = new UcteNodeCode(UcteCountryCode.FR, "AAAAA", UcteVoltageLevelCode.VL_380, '1');
        UcteNodeCode node2 = new UcteNodeCode(UcteCountryCode.BE, "BBBBB", UcteVoltageLevelCode.VL_380, '2');
        UcteElementId id = new UcteElementId(node1, node2, '3');

        assertEquals(node1, id.getNodeCode1());
        assertEquals(node2, id.getNodeCode2());

        assertEquals('3', id.getOrderCode());
        id.setOrderCode('4');
        assertEquals('4', id.getOrderCode());

        assertEquals("FAAAAA11 BBBBBB12 4", id.toString());
    }

    @Test
    public void testHashCode() {
        UcteNodeCode node1 = new UcteNodeCode(UcteCountryCode.FR, "AAAAA", UcteVoltageLevelCode.VL_380, '1');
        UcteNodeCode node2 = new UcteNodeCode(UcteCountryCode.BE, "BBBBB", UcteVoltageLevelCode.VL_380, '2');
        UcteElementId id1 = new UcteElementId(node1, node2, '3');
        UcteElementId id2 = new UcteElementId(node2, node1, '3');
        UcteElementId id3 = new UcteElementId(node1, node2, '2');

        assertFalse(id1.equals(null));

        new EqualsTester()
                .addEqualityGroup(id1)
                .addEqualityGroup(id2)
                .addEqualityGroup(id3)
                .testEquals();

        id3.setOrderCode('3');
        new EqualsTester()
                .addEqualityGroup(id1, id3)
                .addEqualityGroup(id2)
                .testEquals();
    }
}
