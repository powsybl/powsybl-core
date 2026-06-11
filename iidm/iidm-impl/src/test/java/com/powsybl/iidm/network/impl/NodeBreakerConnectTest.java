/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class NodeBreakerConnectTest {

    /**
     * <pre>
     *           LD        G
     *           |    B1   |
     *           |---[+]---|
     *       B2 [-]       [+] B3
     *           |    C    |
     *  BBS1 --------[-]-------- BBS2
     * </pre>
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
        vl.getNodeBreakerView().newBusbarSection()
            .setId("BBS2")
            .setNode(1)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("C")
            .setNode1(1)
            .setNode2(0)
            .setOpen(true)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B2")
            .setNode1(0)
            .setNode2(2)
            .setOpen(true)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B1")
            .setNode1(2)
            .setNode2(3)
            .setOpen(false)
            .add();
        vl.getNodeBreakerView().newBreaker()
            .setId("B3")
            .setNode1(3)
            .setNode2(1)
            .setOpen(false)
            .add();
        vl.newLoad()
            .setId("LD")
            .setNode(2)
            .setP0(1)
            .setQ0(1)
            .add();
        vl.newGenerator()
            .setId("G")
            .setNode(3)
            .setMinP(-9999.99)
            .setMaxP(9999.99)
            .setVoltageRegulatorOn(true)
            .setTargetV(400)
            .setTargetP(1)
            .setTargetQ(0)
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
     *  ---0--- BBS1
     * </pre>
     */
    private Network createDiamondNetwork() {
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

    @Test
    void testNodeBreakerConnectConnectedLoad() {
        Network network = createNetwork();
        Load l = network.getLoad("LD");
        assertTrue(l.getTerminal().isConnected());
        assertTrue(network.getSwitch("B2").isOpen());

        l.getTerminal().connect();
        assertTrue(network.getSwitch("B2").isOpen());
        assertTrue(l.getTerminal().isConnected());
    }

    @Test
    void testNodeBreakerConnectViaVoltageLevelConnectedLoad() {
        Network network = createNetwork();
        Load l = network.getLoad("LD");
        assertTrue(l.getTerminal().isConnected());
        assertTrue(network.getSwitch("B2").isOpen());

        if (l.getTerminal() instanceof TerminalExt terminal) {
            NodeBreakerTopologyModel topologyModel = (NodeBreakerTopologyModel) ((VoltageLevelImpl) network.getVoltageLevel("VL")).getTopologyModel();
            topologyModel.connect(terminal);
        }
        assertTrue(network.getSwitch("B2").isOpen());
        assertTrue(l.getTerminal().isConnected());
    }

    @Test
    void testNodeBreakerDisconnectDisconnectedLoad() {
        Network network = createNetwork();
        network.getSwitch("B3").setOpen(true);
        Load l = network.getLoad("LD");
        assertFalse(l.getTerminal().isConnected());

        l.getTerminal().disconnect();
        assertFalse(network.getSwitch("B1").isOpen());
        assertFalse(l.getTerminal().isConnected());
    }

    @Test
    void testNodeBreakerDisconnectionDiamond() {
        Network network = createDiamondNetwork();
        Load l = network.getLoad("L");
        assertTrue(l.getTerminal().isConnected());
        if (l.getTerminal() instanceof TerminalExt terminal) {
            NodeBreakerTopologyModel topologyModel = (NodeBreakerTopologyModel) ((VoltageLevelImpl) network.getVoltageLevel("VL")).getTopologyModel();
            topologyModel.disconnect(terminal);
        }
        assertFalse(l.getTerminal().isConnected());
    }
}
