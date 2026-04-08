/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.scripting

import com.powsybl.iidm.network.*
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

/**
 * @author Miora VEDELAGO {@literal <miora.ralambotiana at rte-france.com>}
 */
class BatteryExtensionTest {

    @Test
    void test() {
        Network network = Network.create("test", "test")
        Substation substation = network.newSubstation()
                .setCountry(Country.AF)
                .setTso("tso")
                .setName("sub")
                .setId("subId")
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setId("bbVL")
                .setName("bbVL_name")
                .setNominalV(200.0f)
                .add()
        Bus bus = voltageLevel.getBusBreakerView()
                .newBus()
                .setName("Bus1")
                .setId("Bus1")
                .add()
        Battery b = voltageLevel.newBattery().setP0(10.0).setQ0(5.0).setId("B").setBus("Bus1").setMinP(-Double.MAX_VALUE).setMaxP(Double.MAX_VALUE).add()
        assertNotNull(b)
        assertEquals(10.0, b.p0, 0.0)
        assertEquals(10.0, b.getP0(), 0.0)
        assertEquals(5.0, b.q0, 0.0)
        assertEquals(5.0, b.getQ0(), 0.0)
    }
}
