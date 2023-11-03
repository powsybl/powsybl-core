/*
 * Copyright (c) 2023. , RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractNodeBreakerDisconnectionDiamondPathBugTest {

    /**
     * <pre>
     *     L
     *     |
     *  ---1---
     *  |     |
     * BR1   BR2
     *  |     |
     *  ---0--- BBS1</pre>
     */
    private Network createNetwork() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
            .setId("S")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl = s.newVoltageLevel()
            .setId("VL")
            .setNominalV(400.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        vl.getNodeBreakerView().newBusbarSection()
            .setId("BBS1")
            .setNode(0)
            .add();
        vl.newLoad()
            .setId("L")
            .setNode(1)
            .setP0(1)
            .setQ0(1)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("BR1")
            .setNode1(1)
            .setNode2(0)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("BR2")
            .setNode1(1)
            .setNode2(0)
            .setOpen(false)
            .add();
        return network;
    }

    /**
     * <pre>
     *     L
     *     |
     *   2 |-------
     *     |      |
     *  -------   |
     *  |     |   D1
     * BR1   D2   |                     LA
     *  |     |   |                      |
     *  ---1---   |                      4
     *     |      |                      |
     *    BR2     |                     BR4
     *     |      |                      |
     *  ---0-------   -----BR3-----   ---3---
     *    BBS1                         BBS2</pre>
     */
    private Network createNetwork2() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
            .setId("S")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl = s.newVoltageLevel()
            .setId("VL")
            .setNominalV(400.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        vl.getNodeBreakerView().newBusbarSection()
            .setId("BBS1")
            .setNode(0)
            .add();
        vl.getNodeBreakerView().newBusbarSection()
            .setId("BBS2")
            .setNode(3)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("BR3")
            .setNode1(0)
            .setNode2(3)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("BR4")
            .setNode1(3)
            .setNode2(4)
            .setOpen(false)
            .add();
        vl.newLoad()
            .setId("LA")
            .setNode(4)
            .setP0(1)
            .setQ0(1)
            .add();
        vl.newLoad()
            .setId("L")
            .setNode(2)
            .setP0(1)
            .setQ0(1)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("BR1")
            .setNode1(1)
            .setNode2(2)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newDisconnector()
            .setId("D2")
            .setNode1(1)
            .setNode2(2)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("BR2")
            .setNode1(1)
            .setNode2(0)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newDisconnector()
            .setId("D1")
            .setNode1(0)
            .setNode2(2)
            .setOpen(false)
            .add();
        return network;
    }

    /**
     * <pre>
     *    L1                   L2
     *     |                    |
     *     1                    3
     *     |                    |
     *    D1                   D2
     *     |                    |
     *  ---0---  ----BR----  ---2---
     *    BBS1                BBS2</pre>
     */
    private Network createNetwork3() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
            .setId("S")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl = s.newVoltageLevel()
            .setId("VL")
            .setNominalV(400.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        vl.getNodeBreakerView().newBusbarSection()
            .setId("BBS1")
            .setNode(0)
            .add();
        vl.getNodeBreakerView().newBusbarSection()
            .setId("BBS2")
            .setNode(2)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("BR")
            .setNode1(0)
            .setNode2(2)
            .setOpen(false)
            .add();
        vl.newLoad()
            .setId("L1")
            .setNode(1)
            .setP0(1)
            .setQ0(1)
            .add();
        vl.newLoad()
            .setId("L2")
            .setNode(3)
            .setP0(1)
            .setQ0(1)
            .add();
        vl.getNodeBreakerView().newDisconnector()
            .setId("D1")
            .setNode1(0)
            .setNode2(1)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newDisconnector()
            .setId("D2")
            .setNode1(2)
            .setNode2(3)
            .setOpen(false)
            .add();
        return network;
    }

    /**
     * <pre>
     *     Load        Line    2WT
     *       |          |       |
     *   ----2---       3       4
     *   |      |       |       |
     *  BR2    BR3     BR4     BR5
     *   |      |       |       |
     *   -----------1------------
     *              |
     *             BR1
     *              |
     *   -----------0------------
     *            BBS1</pre>
     */
    private Network createNetwork4() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
            .setId("S")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl1 = s.newVoltageLevel()
            .setId("VL1")
            .setNominalV(400.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        vl1.getNodeBreakerView().newBusbarSection()
            .setId("BBS1")
            .setNode(0)
            .add();
        vl1.getNodeBreakerView().newBreaker()
            .setId("BR1")
            .setNode1(0)
            .setNode2(1)
            .setOpen(false)
            .add();
        vl1.getNodeBreakerView().newBreaker()
            .setId("BR2")
            .setNode1(1)
            .setNode2(2)
            .setOpen(false)
            .add();
        vl1.getNodeBreakerView().newBreaker()
            .setId("BR3")
            .setNode1(1)
            .setNode2(2)
            .setOpen(false)
            .add();
        vl1.getNodeBreakerView().newBreaker()
            .setId("BR4")
            .setNode1(1)
            .setNode2(3)
            .setOpen(false)
            .add();
        vl1.getNodeBreakerView().newBreaker()
            .setId("BR5")
            .setNode1(1)
            .setNode2(4)
            .setOpen(false)
            .add();
        vl1.newLoad()
            .setId("Load")
            .setNode(2)
            .setP0(1)
            .setQ0(1)
            .add();

        VoltageLevel vl2 = s.newVoltageLevel()
            .setId("VL2")
            .setNominalV(400.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();
        vl2.getNodeBreakerView().newBusbarSection()
            .setId("BBS2")
            .setNode(0)
            .add();
        vl2.getNodeBreakerView().newBreaker()
            .setId("BR6")
            .setNode1(0)
            .setNode2(1)
            .setOpen(false)
            .add();
        vl2.getNodeBreakerView().newBreaker()
            .setId("BR7")
            .setNode1(0)
            .setNode2(2)
            .setOpen(false)
            .add();

        s.newTwoWindingsTransformer()
            .setId("2WT")
            .setVoltageLevel1(vl1.getId())
            .setNode1(4)
            .setVoltageLevel2(vl2.getId())
            .setNode2(2)
            .setR(250)
            .setX(100)
            .setG(52)
            .setB(12)
            .setRatedU1(405)
            .setRatedU2(405)
            .add();
        network.newLine()
            .setId("Line")
            .setVoltageLevel1(vl1.getId())
            .setNode1(3)
            .setVoltageLevel2(vl2.getId())
            .setNode2(1)
            .setR(1.0)
            .setX(1.0)
            .setG1(0.0)
            .setB1(0.0)
            .setG2(0.0)
            .setB2(0.0)
            .add();
        return network;
    }

    /**
     * <pre>
     *     L
     *     |
     *  ---1---
     *  |     |
     * BR1   BR2
     *  |     |
     *  ---0--- BBS1</pre>
     */
    @Test
    public void testDisconnect() {
        Network network = createNetwork();
        Load l = network.getLoad("L");
        assertTrue(l.getTerminal().isConnected());
        assertTrue(l.getTerminal().disconnect());
        assertFalse(l.getTerminal().isConnected());
    }

    /**
     * <pre>
     *     L
     *     |
     *   2 |-------
     *     |      |
     *  -------   |
     *  |     |   D1
     * BR1   D2   |                     LA
     *  |     |   |                      |
     *  ---1---   |                      4
     *     |      |                      |
     *    BR2     |                     BR4
     *     |      |                      |
     *  ---0-------   -----BR3-----   ---3---
     *    BBS1                         BBS2</pre>
     */
    @Test
    public void testDisconnect2() {
        Network network = createNetwork2();
        Load l = network.getLoad("L");
        assertTrue(l.getTerminal().isConnected());
        assertFalse(l.getTerminal().disconnect());
        assertTrue(l.getTerminal().isConnected()); // because of D1 which is not openable when using the default predicate
    }

    /**
     * <pre>
     *    L1                   L2
     *     |                    |
     *     1                    3
     *     |                    |
     *    D1                   D2
     *     |                    |
     *  ---0---  ----BR----  ---2---
     *    BBS1                BBS2</pre>
     */
    @Test
    public void testDisconnect3() {
        Network network = createNetwork3();
        Switch s = network.getSwitch("BR");
        assertFalse(s.isOpen());
        Load l1 = network.getLoad("L1");
        assertTrue(l1.getTerminal().isConnected());
        assertFalse(l1.getTerminal().disconnect());
        assertFalse(s.isOpen());
        assertTrue(l1.getTerminal().isConnected()); // because of D1 which is not openable when using the default predicate
    }

    /**
     * <pre>
     *     Load        Line    2WT
     *       |          |       |
     *   ----2---       3       4
     *   |      |       |       |
     *  BR2    BR3     BR4     BR5
     *   |      |       |       |
     *   -----------1------------
     *              |
     *             BR1
     *              |
     *   -----------0------------
     *            BBS1</pre>
     */
    @Test
    public void testDisconnect4() {
        Network network = createNetwork4();
        Switch s1 = network.getSwitch("BR1");
        Switch s2 = network.getSwitch("BR2");
        Switch s3 = network.getSwitch("BR3");
        Switch s4 = network.getSwitch("BR4");
        Switch s5 = network.getSwitch("BR5");

        assertFalse(s1.isOpen());
        assertFalse(s2.isOpen());
        assertFalse(s3.isOpen());
        assertFalse(s4.isOpen());
        assertFalse(s5.isOpen());

        Load l1 = network.getLoad("Load");
        assertTrue(l1.getTerminal().isConnected());
        assertTrue(l1.getTerminal().disconnect());
        assertFalse(l1.getTerminal().isConnected());

        assertFalse(s1.isOpen());
        assertTrue(s2.isOpen());
        assertTrue(s3.isOpen());
        assertFalse(s4.isOpen());
        assertFalse(s5.isOpen());
    }
}
