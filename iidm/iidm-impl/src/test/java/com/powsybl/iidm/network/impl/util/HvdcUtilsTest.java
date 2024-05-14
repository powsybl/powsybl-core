/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.util;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.util.HvdcUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
class HvdcUtilsTest {

    private static double EPS = 0.001;

    @Test
    void testLcc() {
        Network network = HvdcTestNetwork.createLcc();
        LccConverterStation c1 = network.getLccConverterStation("C1");
        LccConverterStation c2 = network.getLccConverterStation("C2");

        assertFalse(HvdcUtils.isRectifier(c1));
        assertTrue(HvdcUtils.isRectifier(c2));

        assertEquals(-273.399, HvdcUtils.getConverterStationTargetP(c1), EPS);
        assertEquals(280.0, HvdcUtils.getConverterStationTargetP(c2), EPS);

        assertEquals(473.542, HvdcUtils.getLccConverterStationLoadTargetQ(c1), EPS);
        assertEquals(373.333, HvdcUtils.getLccConverterStationLoadTargetQ(c2), EPS);
    }

    @Test
    void testVsc() {
        Network network = HvdcTestNetwork.createVsc();
        VscConverterStation c1 = network.getVscConverterStation("C1");
        VscConverterStation c2 = network.getVscConverterStation("C2");

        assertFalse(HvdcUtils.isRectifier(c1));
        assertTrue(HvdcUtils.isRectifier(c2));

        assertEquals(273.399, HvdcUtils.getConverterStationTargetP(c1), EPS);
        assertEquals(-280.0, HvdcUtils.getConverterStationTargetP(c2), EPS);
    }
}
