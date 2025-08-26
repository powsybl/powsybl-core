/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class DcTopologyTest {

    @Test
    void testBasicBusTopology() {
        Network net1 = Network.create("n1", "test");
        DcNode n11 = net1.newDcNode().setId("n11").setNominalV(500.).add();
        assertDcBusesAre(net1, List.of("n11_dcBus"));
        assertSame(n11.getDcBus(), net1.getDcBus("n11_dcBus"));
        DcNode n12 = net1.newDcNode().setId("n12").setNominalV(500.).add();
        assertDcBusesAre(net1, List.of("n11_dcBus", "n12_dcBus"));
        DcSwitch s1112 = net1.newDcSwitch().setId("s11-12")
                .setKind(DcSwitchKind.BREAKER)
                .setOpen(true)
                .setDcNode1(n11.getId()).setDcNode2(n12.getId())
                .add();
        assertDcBusesAre(net1, List.of("n11_dcBus", "n12_dcBus"));
        s1112.setOpen(false);
        assertDcBusesAre(net1, List.of("n11_dcBus"));
        assertSame(n11.getDcBus(), net1.getDcBus("n11_dcBus"));
        assertSame(n12.getDcBus(), net1.getDcBus("n11_dcBus"));
        s1112.remove();
        assertDcBusesAre(net1, List.of("n11_dcBus", "n12_dcBus"));
        assertSame(n11.getDcBus(), net1.getDcBus("n11_dcBus"));
        assertSame(n12.getDcBus(), net1.getDcBus("n12_dcBus"));
        n11.remove();
        assertDcBusesAre(net1, List.of("n12_dcBus"));
    }

    private void assertDcBusesAre(Network network, List<String> expected) {
        assertEquals(
                expected.stream().sorted().toList(),
                network.getDcBusStream().map(Identifiable::getId).sorted().toList()
        );
    }

    @Test
    void testNetworkSubnetworkMergeDetach() {
        Network net1 = Network.create("n1", "test");
        DcNode n11 = net1.newDcNode().setId("n11").setNominalV(500.).add();
        DcNode n12 = net1.newDcNode().setId("n12").setNominalV(500.).add();
        assertDcBusesAre(net1, List.of("n11_dcBus", "n12_dcBus"));
        net1.newDcSwitch().setId("s11-12")
                .setKind(DcSwitchKind.BREAKER)
                .setOpen(false)
                .setDcNode1(n11.getId()).setDcNode2(n12.getId())
                .add();
        assertDcBusesAre(net1, List.of("n11_dcBus"));

        Network net2 = Network.create("n2", "test");
        DcNode n21 = net2.newDcNode().setId("n21").setNominalV(500.).add();
        DcNode n22 = net2.newDcNode().setId("n22").setNominalV(500.).add();
        assertDcBusesAre(net2, List.of("n21_dcBus", "n22_dcBus"));
        net2.newDcSwitch().setId("s21-22")
                .setKind(DcSwitchKind.BREAKER)
                .setOpen(true)
                .setDcNode1(n21.getId()).setDcNode2(n22.getId())
                .add();
        assertDcBusesAre(net2, List.of("n21_dcBus", "n22_dcBus"));

        // merge
        Network merged = Network.merge(net1, net2);
        net1 = merged.getSubnetwork("n1");
        net2 = merged.getSubnetwork("n2");
        assertDcBusesAre(merged, List.of("n11_dcBus", "n21_dcBus", "n22_dcBus"));
        assertDcBusesAre(net1, List.of("n11_dcBus"));
        assertDcBusesAre(net2, List.of("n21_dcBus", "n22_dcBus"));

        // detach
        net2 = net2.detach();
        assertDcBusesAre(merged, List.of("n11_dcBus"));
        assertDcBusesAre(net1, List.of("n11_dcBus"));
        assertDcBusesAre(net2, List.of("n21_dcBus", "n22_dcBus"));
        net1 = net1.detach();
        assertDcBusesAre(merged, List.of());
        assertDcBusesAre(net1, List.of("n11_dcBus"));
        assertDcBusesAre(net2, List.of("n21_dcBus", "n22_dcBus"));
    }

    @Test
    void testNetworkSubnetworkFlatten() {
        Network network = Network.create("network", "test");
        Network net1 = network.createSubnetwork("n1", "n1", "test");
        DcNode n11 = net1.newDcNode().setId("n11").setNominalV(500.).add();
        DcNode n12 = net1.newDcNode().setId("n12").setNominalV(500.).add();
        assertDcBusesAre(network, List.of("n11_dcBus", "n12_dcBus"));
        assertDcBusesAre(net1, List.of("n11_dcBus", "n12_dcBus"));
        net1.newDcSwitch().setId("s11-12")
                .setKind(DcSwitchKind.BREAKER)
                .setOpen(false)
                .setDcNode1(n11.getId()).setDcNode2(n12.getId())
                .add();
        assertDcBusesAre(network, List.of("n11_dcBus"));
        assertDcBusesAre(net1, List.of("n11_dcBus"));

        Network net2 = network.createSubnetwork("n2", "n2", "test");
        DcNode n21 = net2.newDcNode().setId("n21").setNominalV(500.).add();
        DcNode n22 = net2.newDcNode().setId("n22").setNominalV(500.).add();
        assertDcBusesAre(network, List.of("n11_dcBus", "n21_dcBus", "n22_dcBus"));
        assertDcBusesAre(net2, List.of("n21_dcBus", "n22_dcBus"));
        net2.newDcSwitch().setId("s21-22")
                .setKind(DcSwitchKind.BREAKER)
                .setOpen(true)
                .setDcNode1(n21.getId()).setDcNode2(n22.getId())
                .add();
        assertDcBusesAre(network, List.of("n11_dcBus", "n21_dcBus", "n22_dcBus"));
        assertDcBusesAre(net2, List.of("n21_dcBus", "n22_dcBus"));

        // flatten
        network.flatten();
        assertEquals(0, network.getSubnetworks().size());
        assertDcBusesAre(network, List.of("n11_dcBus", "n21_dcBus", "n22_dcBus"));
    }

}
