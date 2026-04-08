/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.scripting

import com.powsybl.iidm.network.Network
import com.powsybl.iidm.network.Substation
import com.powsybl.iidm.network.ThreeWindingsTransformer
import com.powsybl.iidm.network.TopologyKind
import com.powsybl.iidm.network.VoltageLevel
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertSame

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class ThreeWindingsTransformerExtensionTest {

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
        vlB.getBusBreakerView().newBus().setId("busC").add()
        ThreeWindingsTransformer twt = substation.newThreeWindingsTransformer()
                .setId("3WT")
                .setRatedU0(132.0)
                .newLeg1()
                .setR(17.424)
                .setX(1.7424)
                .setG(0.00573921028466483)
                .setB(0.000573921028466483)
                .setRatedU(200.0)
                .setVoltageLevel("vlA")
                .setBus("busA")
                .add()
                .newLeg2()
                .setR(1.089)
                .setX(0.1089)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(320.0)
                .setVoltageLevel("vlB")
                .setBus("busB")
                .add()
                .newLeg3()
                .setR(0.121)
                .setX(0.0121)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(340.0)
                .setVoltageLevel("vlB")
                .setBus("busC")
                .add()
                .add()
        assertSame(substation, twt.substation)
    }
}
