/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.scripting

import com.powsybl.iidm.network.*
import org.junit.Test

import static org.junit.Assert.assertNull
import static org.junit.Assert.assertSame

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class TwoWindingsTransformerExtensionTest {

    @Test
    void getSubstationTest() {
        Network network = Network.create("test", "test")
        Substation substation = network.newSubstation()
                .setId("sub")
                .add()
        VoltageLevel vlA = substation.newVoltageLevel()
                .setId("vlA")
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setNominalV(200)
                .add()
        vlA.getBusBreakerView().newBus().setId("busA").add()
        VoltageLevel vlB = substation.newVoltageLevel()
                .setId("vlB")
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setNominalV(340)
                .add()
        vlB.getBusBreakerView().newBus().setId("busB").add()
        TwoWindingsTransformer twt = substation.newTwoWindingsTransformer()
                .setId("NHV2_NLOAD")
                .setVoltageLevel1("vlA")
                .setBus1("busA")
                .setRatedU1(200)
                .setVoltageLevel2("vlB")
                .setConnectableBus2("busB")
                .setRatedU2(340)
                .setR(0.21 / 1000)
                .setX(Math.sqrt(18 * 18 - 0.21 * 0.21) / 1000)
                .setG(0.0)
                .setB(0.0)
                .add()
        assertSame(substation, twt.substation)
    }

    @Test
    void getNullSubstationTest() {
        Network network = Network.create("test", "test")
        VoltageLevel vlA = network.newVoltageLevel()
                .setId("vlA")
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setNominalV(200)
                .add()
        vlA.getBusBreakerView().newBus().setId("busA").add()
        VoltageLevel vlB = network.newVoltageLevel()
                .setId("vlB")
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setNominalV(340)
                .add()
        vlB.getBusBreakerView().newBus().setId("busB").add()
        TwoWindingsTransformer twt = network.newTwoWindingsTransformer()
                .setId("NHV2_NLOAD")
                .setVoltageLevel1("vlA")
                .setBus1("busA")
                .setRatedU1(200)
                .setVoltageLevel2("vlB")
                .setConnectableBus2("busB")
                .setRatedU2(340)
                .setR(0.21 / 1000)
                .setX(Math.sqrt(18 * 18 - 0.21 * 0.21) / 1000)
                .setG(0.0)
                .setB(0.0)
                .add()
        assertNull(twt.substation)
    }
}
