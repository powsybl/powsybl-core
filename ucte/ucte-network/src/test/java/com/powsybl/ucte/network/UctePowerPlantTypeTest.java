/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import org.junit.Test;

import static com.powsybl.ucte.network.UctePowerPlantType.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class UctePowerPlantTypeTest {

    @Test
    public void test() {
        assertEquals(8, UctePowerPlantType.values().length);
        assertArrayEquals(new UctePowerPlantType[] {H, N, L, C, G, O, W, F}, UctePowerPlantType.values());
    }
}
