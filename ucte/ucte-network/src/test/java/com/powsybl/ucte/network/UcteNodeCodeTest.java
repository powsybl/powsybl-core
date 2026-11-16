/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static com.powsybl.ucte.network.UcteNodeCode.isUcteNodeId;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class UcteNodeCodeTest {

    @Test
    void test() {
        UcteNodeCode node = new UcteNodeCode(UcteCountryCode.FR, "SUBST", UcteVoltageLevelCode.VL_380, '1');
        assertEquals("FSUBST11", node.toString());

        assertEquals(UcteCountryCode.FR, node.getUcteCountryCode());
        node.setUcteCountryCode(UcteCountryCode.BE);
        assertEquals(UcteCountryCode.BE, node.getUcteCountryCode());

        assertEquals("SUBST", node.getGeographicalSpot());
        node.setGeographicalSpot("subst");
        assertEquals("subst", node.getGeographicalSpot());

        assertEquals(UcteVoltageLevelCode.VL_380, node.getVoltageLevelCode());
        node.setVoltageLevelCode(UcteVoltageLevelCode.VL_220);
        assertEquals(UcteVoltageLevelCode.VL_220, node.getVoltageLevelCode());

        assertEquals(Character.valueOf('1'), node.getBusbar());
        node.setBusbar('A');
        assertEquals(Character.valueOf('A'), node.getBusbar());
        node.setBusbar(null);
        assertNull(node.getBusbar());

        assertEquals("Bsubst2 ", node.toString());
    }

    @Test
    void testEquals() {
        UcteNodeCode node1 = new UcteNodeCode(UcteCountryCode.FR, "SUBST", UcteVoltageLevelCode.VL_380, '1');
        UcteNodeCode node2 = new UcteNodeCode(UcteCountryCode.FR, "SUBST", UcteVoltageLevelCode.VL_380, '1');

        new EqualsTester()
                .addEqualityGroup(node1, node2)
                .testEquals();

        node2.setBusbar(null);
        new EqualsTester()
                .addEqualityGroup(node1)
                .addEqualityGroup(node2)
                .testEquals();
    }

    @Test
    void isUcteNodeIdTest() {
        assertTrue(isUcteNodeId("B_SU1_11"));
        assertTrue(isUcteNodeId("B_SU1_1 "));
        assertTrue(isUcteNodeId("7efG8411"));
        assertFalse(isUcteNodeId("        "));
        assertFalse(isUcteNodeId("B_SU1_"));
        assertFalse(isUcteNodeId("&ezrt874g"));
    }
}
