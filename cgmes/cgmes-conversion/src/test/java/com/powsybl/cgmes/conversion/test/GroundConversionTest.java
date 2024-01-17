/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.iidm.network.Ground;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */

class GroundConversionTest {

    @Test
    void groundConversionTest() {
        Network network = Network.read("groundTest.xml", getClass().getResourceAsStream("/groundTest.xml"));

        assertEquals(2, network.getGroundCount());

        assertNotNull(network.getGround("OU"));

        Ground groundCV = network.getGround("CV");
        assertNotNull(groundCV);
        assertEquals("CW", groundCV.getNameOrId());
        assertNotNull(groundCV.getTerminal());
        assertFalse(groundCV.getTerminal().isConnected());
        assertEquals("S", groundCV.getTerminal().getVoltageLevel().getId());
    }
}
