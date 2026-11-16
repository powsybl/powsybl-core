/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nicolas Lhuillier {@literal <nicolas.lhuillier@rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public abstract class AbstractMainConnectedComponentWithSwitchTest {

    @Test
    public void test() {

        Network network = Network.create("test_mcc", "test");

        Substation s1 = network.newSubstation()
                .setId("A")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("B")
                .setNominalV(225.0)
                .setLowVoltageLimit(0.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl1.getNodeBreakerView().newBusbarSection()
                .setId("C")
                .setNode(0)
                .add();
        vl1.getNodeBreakerView().newSwitch()
                .setId("D")
                .setKind(SwitchKind.DISCONNECTOR)
                .setRetained(false)
                .setOpen(false)
                .setNode1(0)
                .setNode2(1)
                .add();
        vl1.getNodeBreakerView().newSwitch()
                .setId("E")
                .setKind(SwitchKind.BREAKER)
                .setRetained(false)
                .setOpen(false)
                .setNode1(1)
                .setNode2(2)
                .add();

        Substation s2 = network.newSubstation()
                .setId("F")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("G")
                .setNominalV(225.0)
                .setLowVoltageLimit(0.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("H")
                .setNode(0)
                .add();
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("I")
                .setNode(1)
                .add();
        vl2.getNodeBreakerView().newSwitch()
                .setId("J")
                .setKind(SwitchKind.DISCONNECTOR)
                .setRetained(true)
                .setOpen(false)
                .setNode1(0)
                .setNode2(2)
                .add();
        vl2.getNodeBreakerView().newSwitch()
                .setId("K")
                .setKind(SwitchKind.DISCONNECTOR)
                .setRetained(true)
                .setOpen(false)
                .setNode1(1)
                .setNode2(3)
                .add();
        vl2.getNodeBreakerView().newSwitch()
                .setId("L")
                .setKind(SwitchKind.BREAKER)
                .setRetained(true)
                .setOpen(false)
                .setNode1(2)
                .setNode2(3)
                .add();
        vl2.getNodeBreakerView().newSwitch()
                .setId("M")
                .setKind(SwitchKind.BREAKER)
                .setRetained(false)
                .setOpen(false)
                .setNode1(0)
                .setNode2(4)
                .add();

        network.newLine()
                .setId("N")
                .setR(0.001)
                .setX(0.1)
                .setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0)
                .setVoltageLevel1("B")
                .setNode1(2)
                .setVoltageLevel2("G")
                .setNode2(4)
                .add();

        network.getBusView().getBuses().forEach(b -> {
            if (b.getVoltageLevel() == vl1) {
                b.setV(230.0).setAngle(0.5);
            } else {
                b.setV(220.0).setAngle(0.7);
            }
        });

        assertEquals(2, network.getBusView().getBusStream().count());
        for (Bus b : network.getBusView().getBuses()) {
            assertTrue(b.isInMainConnectedComponent());
        }

        assertEquals(5, network.getBusBreakerView().getBusStream().count());
        for (Bus b : network.getBusBreakerView().getBuses()) {
            assertTrue(b.isInMainConnectedComponent());
            if (b.getVoltageLevel() == vl1) {
                assertEquals(230.0, b.getV(), 0.0);
                assertEquals(0.5, b.getAngle(), 0.0);
            } else {
                assertEquals(220.0, b.getV(), 0.0);
                assertEquals(0.7, b.getAngle(), 0.0);
            }
        }
    }
}
