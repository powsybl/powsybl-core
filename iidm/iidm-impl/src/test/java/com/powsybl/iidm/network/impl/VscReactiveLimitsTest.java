/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VscReactiveLimitsTest {

    @Test
    void testReactiveLimits() {
        Network network = HvdcTestNetwork.createVsc();
        VscConverterStation cs1 = network.getVscConverterStation("C1");
        cs1.newMinMaxReactiveLimits()
                .setMinQ(10.0)
                .setMaxQ(100.0)
            .add();
        assertEquals(100.0, cs1.getReactiveLimits().getMaxQ(2.0), 0.0);
        try {
            cs1.getReactiveLimits(ReactiveCapabilityCurveImpl.class);
            fail();
        } catch (Exception ignored) {
        }
        cs1.getReactiveLimits(MinMaxReactiveLimitsImpl.class);
    }

}
