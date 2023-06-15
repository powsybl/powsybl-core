/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Anne Tilloy <anne.tilloy at rte-france.com>
 */
class EqOnlyConversionTest {

    @Test
    void testTeePointBusBranch() {
        Network network = Network.read("t-line.xml", getClass().getResourceAsStream("/t-line.xml"));
        assertEquals(4, network.getVoltageLevelCount());
        assertEquals(3, network.getSubstationCount());
        assertTrue(network.getVoltageLevel("Line1TPointBus_VL").getSubstation().isEmpty()); // tee point
    }
}
