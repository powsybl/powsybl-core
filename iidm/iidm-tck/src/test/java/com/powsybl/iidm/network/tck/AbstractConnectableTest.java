/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class AbstractConnectableTest {

    private Network createNetwork() {
        Network network = Network.create("test", "test");
        // Substations
        Substation s1 = network.newSubstation()
            .setId("S1")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl = s1.newVoltageLevel()
            .setId("VL1")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        VoltageLevel vl2 = s1.newVoltageLevel()
            .setId("VL2")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();

        // Busbar sections
        BusbarSection bbs11 = vl.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS11")
            .setNode(0)
            .add();
        bbs11.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(1)
            .add();
        BusbarSection bbs21 = vl.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS21")
            .setNode(1)
            .add();
        bbs21.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(2)
            .withSectionIndex(1)
            .add();
        BusbarSection bbs12 = vl.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS12")
            .setNode(2)
            .add();
        bbs12.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(1)
            .withSectionIndex(2)
            .add();
        BusbarSection bbs22 = vl.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS22")
            .setNode(3)
            .add();
        bbs22.newExtension(BusbarSectionPositionAdder.class)
            .withBusbarIndex(2)
            .withSectionIndex(2)
            .add();
        vl2.getBusBreakerView()
            .newBus()
            .setId("busB")
            .add();

        // Disconnectors for coupling
        vl.getNodeBreakerView().newDisconnector()
            .setId("D_BBS11_BBS12")
            .setNode1(0)
            .setNode2(2)
            .setOpen(true)
            .add();
        vl.getNodeBreakerView().newDisconnector()
            .setId("D_BBS21_BBS22")
            .setNode1(1)
            .setNode2(3)
            .setOpen(false)
            .add();

        // Generators and loads
        vl.newGenerator()
            .setId("G1")
            .setNode(5)
            .setMaxP(100)
            .setMinP(50)
            .setTargetP(100)
            .setTargetV(400)
            .setVoltageRegulatorOn(true)
            .add();
        vl.newGenerator()
            .setId("G2")
            .setNode(6)
            .setMaxP(100)
            .setMinP(50)
            .setTargetP(100)
            .setTargetV(400)
            .setVoltageRegulatorOn(true)
            .add();
        network.newLine()
            .setId("L1")
            .setName("LINE")
            .setR(1.0)
            .setX(2.0)
            .setG1(3.0)
            .setG2(3.5)
            .setB1(4.0)
            .setB2(4.5)
            .setVoltageLevel1("vl")
            .setVoltageLevel2("vl2")
            .setNode1(4)
            .setBus2("busB")
            .setConnectableBus2("busB")
            .add();

        // Breakers
        vl.getNodeBreakerView().newBreaker()
            .setId("B_L1_1")
            .setNode1(4)
            .setNode2(7)
            .setOpen(false)
            .setFictitious(true)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B_L1_2")
            .setNode1(4)
            .setNode2(7)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B_G1")
            .setNode1(5)
            .setNode2(8)
            .setOpen(true)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B_G2")
            .setNode1(6)
            .setNode2(9)
            .setOpen(false)
            .setFictitious(true)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B0")
            .setNode1(7)
            .setNode2(17)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B1")
            .setNode1(8)
            .setNode2(11)
            .setOpen(true)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B2")
            .setNode1(9)
            .setNode2(12)
            .setOpen(false)
            .setFictitious(true)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B3")
            .setNode1(7)
            .setNode2(8)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B4")
            .setNode1(8)
            .setNode2(9)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B5")
            .setNode1(17)
            .setNode2(10)
            .setOpen(false)
            .add();

        // Disconnectors
        vl.getNodeBreakerView().newDisconnector()
            .setId("D0")
            .setNode1(0)
            .setNode2(10)
            .setOpen(true)
            .add();
        vl.getNodeBreakerView().newDisconnector()
            .setId("D1")
            .setNode1(1)
            .setNode2(10)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newDisconnector()
            .setId("D2")
            .setNode1(0)
            .setNode2(11)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newDisconnector()
            .setId("D3")
            .setNode1(1)
            .setNode2(11)
            .setOpen(true)
            .add();
        vl.getNodeBreakerView().newDisconnector()
            .setId("D4")
            .setNode1(2)
            .setNode2(12)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newDisconnector()
            .setId("D5")
            .setNode1(3)
            .setNode2(12)
            .setOpen(true)
            .add();
        return network;
    }
}
