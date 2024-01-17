/*
 * Copyright (c) 2023. , RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.util;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.SwitchPredicates;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractSwitchPredicatesTest {

    private static Network network;

    @BeforeAll
    static void setUpClass() {
        network = createNetwork();
    }

    private static Network createNetwork() {
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation()
            .setId("S1")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl = s1.newVoltageLevel()
            .setId("VL1")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();

        // Busbar sections
        vl.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS11")
            .setNode(0)
            .add();
        vl.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS21")
            .setNode(1)
            .add();
        vl.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS12")
            .setNode(2)
            .add();
        vl.getNodeBreakerView()
            .newBusbarSection()
            .setId("BBS22")
            .setNode(3)
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
        vl.newLoad()
            .setId("L1")
            .setNode(4)
            .setP0(1)
            .setQ0(1)
            .add();
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
            .setOpen(true)
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
        vl.getNodeBreakerView().newSwitch()
            .setId("B4")
            .setNode1(8)
            .setNode2(9)
            .setOpen(false)
            .setKind(SwitchKind.LOAD_BREAK_SWITCH)
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

    @Test
    public void testNonFictionalClosedBreakers() {
        // Predicate to test
        Predicate<Switch> predicate = SwitchPredicates.IS_NONFICTIONAL_CLOSED_BREAKER;

        // Tests
        assertFalse(predicate.test(null));
        assertFalse(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("D_BBS11_BBS12")));
        assertFalse(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("B1")));
        assertFalse(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("B_L1_1")));
        assertTrue(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("B5")));
    }

    @Test
    public void testNonFictionalBreakers() {
        // Predicate to test
        Predicate<Switch> predicate = SwitchPredicates.IS_NONFICTIONAL_BREAKER;

        // Tests
        assertFalse(predicate.test(null));
        assertFalse(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("D_BBS11_BBS12")));
        assertFalse(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("B_L1_1")));
        assertTrue(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("B5")));
    }

    @Test
    public void testClosedBreakers() {
        // Predicate to test
        Predicate<Switch> predicate = SwitchPredicates.IS_CLOSED_BREAKER;

        // Tests
        assertFalse(predicate.test(null));
        assertFalse(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("D_BBS11_BBS12")));
        assertFalse(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("B1")));
        assertTrue(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("B5")));
    }

    @Test
    public void testBreakerOrDisconnector() {
        // Predicate to test
        Predicate<Switch> predicate = SwitchPredicates.IS_BREAKER_OR_DISCONNECTOR;

        // Tests
        assertFalse(predicate.test(null));
        assertFalse(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("B4")));
        assertTrue(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("D_BBS11_BBS12")));
        assertTrue(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("B5")));
    }

    @Test
    public void testOpenDisconnector() {
        // Predicate to test
        Predicate<Switch> predicate = SwitchPredicates.IS_OPEN_DISCONNECTOR;

        // Tests
        assertFalse(predicate.test(null));
        assertFalse(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("B4")));
        assertFalse(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("D4")));
        assertTrue(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("D5")));
    }

    @Test
    public void testBreakers() {
        // Predicate to test
        Predicate<Switch> predicate = SwitchPredicates.IS_BREAKER;

        // Tests
        assertFalse(predicate.test(null));
        assertFalse(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("D4")));
        assertTrue(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("B3")));
    }

    @Test
    public void testNonFictional() {
        // Predicate to test
        Predicate<Switch> predicate = SwitchPredicates.IS_NONFICTIONAL;

        // Tests
        assertFalse(predicate.test(null));
        assertFalse(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("B2")));
        assertTrue(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("B5")));
    }

    @Test
    public void testOpen() {
        // Predicate to test
        Predicate<Switch> predicate = SwitchPredicates.IS_OPEN;

        // Tests
        assertFalse(predicate.test(null));
        assertFalse(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("D4")));
        assertTrue(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("D5")));
    }

    @Test
    public void testNonNull() {
        // Predicate to test
        Predicate<Switch> predicate = SwitchPredicates.IS_NON_NULL;

        // Tests
        assertFalse(predicate.test(null));
        assertTrue(predicate.test(network.getVoltageLevel("VL1").getNodeBreakerView().getSwitch("D5")));
    }
}
