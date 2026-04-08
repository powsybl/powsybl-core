/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.scripting

import com.powsybl.iidm.network.Country
import com.powsybl.iidm.network.Network
import com.powsybl.iidm.network.Substation
import com.powsybl.iidm.network.TopologyKind
import com.powsybl.iidm.network.VoltageLevel
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertSame

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class VoltageLevelExtensionTest {

    @Test
    void getSubstationTest() {
        Network network = Network.create("test", "test")
        Substation substation = network.newSubstation()
                .setCountry(Country.AF)
                .setTso("tso")
                .setName("sub")
                .setId("subId")
                .add()
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setId("bbVL")
                .setName("bbVL_name")
                .setNominalV(200.0f)
                .add()
        assertSame(substation, voltageLevel.substation)
    }

    @Test
    void getNullSubstationTest() {
        Network network = Network.create("test", "test")
        VoltageLevel voltageLevel = network.newVoltageLevel()
                .setId("VL")
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setNominalV(340.0).add()
        assertNull(voltageLevel.substation)
    }

    @Test
    void getNullSubstationNodeBreakerTest() {
        Network network = Network.create("test", "test")
        VoltageLevel voltageLevel = network.newVoltageLevel()
                .setId("VL")
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setNominalV(340.0).add()
        assertNull(voltageLevel.substation)
    }
}
