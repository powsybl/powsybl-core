/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.scripting

import com.powsybl.iidm.network.Bus
import com.powsybl.iidm.network.Country
import com.powsybl.iidm.network.Network
import com.powsybl.iidm.network.ShuntCompensator
import com.powsybl.iidm.network.Substation
import com.powsybl.iidm.network.TopologyKind
import com.powsybl.iidm.network.VoltageLevel
import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 *
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class ShuntCompensatorExtensionTest {

    @Test
    void test() {
        ShuntCompensator shunt = createShuntCompensator()
        assertEquals(10, shunt.maximumSectionCount)
        shunt.maximumSectionCount = 11
        assertEquals(11, shunt.maximumSectionCount)
        assertEquals(5.0, shunt.bPerSection, 0.0f)
        shunt.bPerSection = 4.0
        assertEquals(4.0, shunt.bPerSection, 0.0f)
        assertEquals(44.0, shunt.maximumB, 0.0)
    }

    static ShuntCompensator createShuntCompensator() {
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
        network.getVoltageLevel("bbVL").newShuntCompensator().setId("SHUNT")
                .setBus("Bus1")
                .setConnectableBus("Bus1")
                .setCurrentSectionCount(6)
                .newLinearModel()
                    .setbPerSection(5.0)
                    .setMaximumSectionCount(10)
                    .add()
                .add()
    }
}