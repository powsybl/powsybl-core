/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
class EqOnlyConversionTest {

    @Test
    void testTeePointBusBranch() {
        Network network = Network.read("t-line.xml", getClass().getResourceAsStream("/t-line.xml"));
        assertEquals(4, network.getVoltageLevelCount());
        assertEquals(3, network.getSubstationCount());
        assertTrue(network.getVoltageLevel("Line1TPointBus_VL").getSubstation().isEmpty()); // tee point
    }

    @Test
    void testTeePointNodeBreaker() {
        Network network = Network.read("t-line-node-breaker.xml", getClass().getResourceAsStream("/t-line-node-breaker.xml"));
        assertEquals(4, network.getVoltageLevelCount());
        assertEquals(2, network.getSubstationCount());
        assertEquals(5, network.getLineCount());
        VoltageLevel line1TPoint1Vl = network.getVoltageLevel("Line1TPoint1_VL"); // tee point 1 fictitious voltage level
        VoltageLevel line1TPoint2Vl = network.getVoltageLevel("Line1TPoint2_VL"); // tee point 2 fictitious voltage level
        assertEquals("Line1", line1TPoint1Vl.getOptionalName().orElseThrow());
        assertEquals("Line1", line1TPoint2Vl.getOptionalName().orElseThrow());
        assertTrue(line1TPoint1Vl.isFictitious());
        assertTrue(line1TPoint2Vl.isFictitious());
        assertTrue(line1TPoint1Vl.getSubstation().isEmpty());
        assertTrue(line1TPoint2Vl.getSubstation().isEmpty());
    }
}
