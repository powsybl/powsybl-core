/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.scripting

import com.powsybl.iidm.network.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

/**
 * @author Chamseddine BENHAMED {@literal <chamseddine.benhamed at rte-france.com>}
 */
class NetworkExtensionTest {

    private Network network

    @BeforeEach
    void prepareNetwork(){
        network = Network.create("test", "test")
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
                .setSectionCount(6)
                .newLinearModel()
                    .setBPerSection(5.0)
                    .setMaximumSectionCount(10)
                    .add()
                .add()
    }

    @Test
    void getShuntsTest() {
        assertEquals(1, network.getShunts().size())
        assertEquals(1, network.getShuntCount())
    }

    @Test
    void getShuntStreamTest() {
        assertEquals(1, network.getShuntStream().count())
    }

    @Test
    void getShuntTest() {
        assertNotNull(network.getShunt("SHUNT"))
        assertEquals(6, network.getShunt("SHUNT").getSectionCount())
    }
}
