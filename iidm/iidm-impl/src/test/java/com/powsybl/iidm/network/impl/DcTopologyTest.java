/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
        assertEquals(IdentifiableType.DC_BUS, net1.getDcBus("n11_dcBus").getType());
        assertSame(n11.getDcBus(), net1.getDcBus("n11_dcBus"));
        assertDcBusContainsDcNodes(net1.getDcBus("n11_dcBus"), List.of(n11));

        DcNode n12 = net1.newDcNode().setId("n12").setNominalV(500.).add();
        assertDcBusesAre(net1, List.of("n11_dcBus", "n12_dcBus"));
        assertDcBusContainsDcNodes(net1.getDcBus("n11_dcBus"), List.of(n11));
        assertDcBusContainsDcNodes(net1.getDcBus("n12_dcBus"), List.of(n12));

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
        assertDcBusContainsDcNodes(net1.getDcBus("n11_dcBus"), List.of(n11, n12));

        s1112.remove();
        assertDcBusesAre(net1, List.of("n11_dcBus", "n12_dcBus"));
        assertSame(n11.getDcBus(), net1.getDcBus("n11_dcBus"));
        assertSame(n12.getDcBus(), net1.getDcBus("n12_dcBus"));

        n11.remove();
        assertDcBusesAre(net1, List.of("n12_dcBus"));
    }

    @Test
    void testMultiVariantDcBusV() {
        Network net1 = Network.create("n1", "test");
        var variantManager = net1.getVariantManager();
        DcNode n11 = net1.newDcNode().setId("n11").setNominalV(500.).add();
        DcNode n12 = net1.newDcNode().setId("n12").setNominalV(500.).add();
        DcSwitch s1112 = net1.newDcSwitch().setId("s11-12")
                .setKind(DcSwitchKind.BREAKER)
                .setOpen(true)
                .setDcNode1(n11.getId()).setDcNode2(n12.getId())
                .add();
        assertEquals(2, net1.getDcBusCount());
        DcBus n11DcBus = net1.getDcBus("n11_dcBus");
        DcBus n12DcBus = net1.getDcBus("n12_dcBus");
        assertDcBusContainsDcNodes(n11DcBus, List.of(n11));
        assertDcBusContainsDcNodes(n12DcBus, List.of(n12));
        assertTrue(Double.isNaN(n11DcBus.getV()));
        assertTrue(Double.isNaN(n11.getV()));
        assertTrue(Double.isNaN(n12DcBus.getV()));
        assertTrue(Double.isNaN(n12.getV()));
        n11DcBus.setV(501.);
        n12DcBus.setV(-502.);
        assertEquals(501., n11DcBus.getV(), 1e-3);
        assertEquals(501., n11.getV(), 1e-3);
        assertEquals(-502., n12DcBus.getV(), 1e-3);
        assertEquals(-502., n12.getV(), 1e-3);

        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "v1");
        variantManager.setWorkingVariant("v1");
        n11DcBus = net1.getDcBus("n11_dcBus");
        n12DcBus = net1.getDcBus("n12_dcBus");
        assertTrue(s1112.isOpen());
        assertEquals(501., n11DcBus.getV(), 1e-3);
        assertEquals(501., n11.getV(), 1e-3);
        assertEquals(-502., n12DcBus.getV(), 1e-3);
        assertEquals(-502., n12.getV(), 1e-3);

        s1112.setOpen(false);
        assertEquals(1, net1.getDcBusCount());
        n11DcBus = net1.getDcBus("n11_dcBus");
        n11DcBus.setV(504.);
        assertEquals(504., n11DcBus.getV(), 1e-3);
        assertEquals(504., n11.getV(), 1e-3);
        assertEquals(504., n12.getV(), 1e-3);

        // back to original variant
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        n11DcBus = net1.getDcBus("n11_dcBus");
        n12DcBus = net1.getDcBus("n12_dcBus");
        assertTrue(s1112.isOpen());
        assertEquals(501., n11DcBus.getV(), 1e-3);
        assertEquals(501., n11.getV(), 1e-3);
        assertEquals(-502., n12DcBus.getV(), 1e-3);
        assertEquals(-502., n12.getV(), 1e-3);
    }

    @Test
    void testDcBusInvalidation() {
        Network net1 = Network.create("n1", "test");

        DcNode n11 = net1.newDcNode().setId("n11").setNominalV(500.).add();
        DcNode n12 = net1.newDcNode().setId("n12").setNominalV(500.).add();
        DcSwitch s1112 = net1.newDcSwitch().setId("s11-12")
                .setKind(DcSwitchKind.BREAKER)
                .setOpen(true)
                .setDcNode1(n11.getId()).setDcNode2(n12.getId())
                .add();
        assertDcBusesAre(net1, List.of("n11_dcBus", "n12_dcBus"));
        var dcBus = net1.getDcBus("n11_dcBus");
        s1112.setOpen(false); // close the switch, this invalidates the DC bus
        assertBusInvalidated(dcBus, DcBus::getV);
        assertBusInvalidated(dcBus, DcBus::getConnectedComponent);
        assertBusInvalidated(dcBus, DcBus::getDcComponent);
        assertBusInvalidated(dcBus, DcBus::getDcNodes);
        assertBusInvalidated(dcBus, DcBus::getDcNodeStream);
        assertBusInvalidated(dcBus, DcBus::isInMainConnectedComponent);
        assertBusInvalidated(dcBus, b -> b.setV(1.23));
    }

    private static void assertBusInvalidated(DcBus dcBus, Consumer<DcBus> action) {
        PowsyblException e = assertThrows(PowsyblException.class, () -> action.accept(dcBus));
        assertEquals("DcBus has been invalidated", e.getMessage());
    }

    private static void assertDcBusesAre(Network network, List<String> expected) {
        assertEquals(
                expected.stream().sorted().toList(),
                network.getDcBusStream().map(Identifiable::getId).sorted().toList()
        );
        assertEquals(network.getDcBusCount(), expected.size());
    }

    private static void assertDcBusContainsDcNodes(DcBus dcBus, List<DcNode> dcNodes) {
        Objects.requireNonNull(dcBus);
        Objects.requireNonNull(dcNodes);
        var expected = dcNodes.stream().map(Identifiable::getId).sorted().toList();
        assertEquals(
                expected,
                dcBus.getDcNodeStream().map(Identifiable::getId).sorted().toList()
        );
        assertEquals(
                expected,
                StreamSupport.stream(dcBus.getDcNodes().spliterator(), false).map(Identifiable::getId).sorted().toList()
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
        assertEquals(3, merged.getDcBusCount());
        assertDcBusesAre(merged, List.of("n11_dcBus", "n21_dcBus", "n22_dcBus"));
        assertEquals(1, net1.getDcBusCount());
        assertDcBusesAre(net1, List.of("n11_dcBus"));
        assertEquals(2, net2.getDcBusCount());
        assertDcBusesAre(net2, List.of("n21_dcBus", "n22_dcBus"));
        assertSame(merged, merged.getDcBus("n11_dcBus").getNetwork());
        assertSame(net1, merged.getDcBus("n11_dcBus").getParentNetwork());

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
        assertEquals(3, network.getDcBusCount());
        assertDcBusesAre(network, List.of("n11_dcBus", "n21_dcBus", "n22_dcBus"));
    }

    @Test
    void testOneDcComponent() {
        Network network = DcDetailedNetworkFactory.createLccMonopoleGroundReturn();
        Collection<Component> dcs = network.getDcComponents();
        Collection<Component> ccs = network.getBusView().getConnectedComponents();
        List<Component> scs = List.copyOf(network.getBusView().getSynchronousComponents());
        assertEquals(1, dcs.size());
        assertEquals(1, ccs.size());
        assertEquals(2, scs.size());
        Component dc0 = dcs.iterator().next();
        Component cc0 = ccs.iterator().next();
        Component sc0 = scs.get(0);
        Component sc1 = scs.get(1);

        var sc0expectedAcBuses = List.of(
                "VL-FR_0",
                "VLDC-FR-xNodeDc1fr-150_0",
                "VLDC-FR-xNodeDc1fr-150_1",
                "VLDC-FR-xNodeDc1fr-400_0",
                "VLDC-FR-xNodeDc1fr-400_1");
        var sc1expectedAcBuses = List.of(
                "VL-GB_0",
                "VLDC-GB-xNodeDc1gb-150_0",
                "VLDC-GB-xNodeDc1gb-150_1",
                "VLDC-GB-xNodeDc1gb-400_0");
        var expectedDcBuses = List.of("dcNodeFrNeg_dcBus",
                "dcNodeFrPos_dcBus",
                "dcNodeGbNeg_dcBus",
                "dcNodeGbPos_dcBus");

        assertComponent(cc0, 0, Stream.concat(sc0expectedAcBuses.stream(), sc1expectedAcBuses.stream()).toList(), expectedDcBuses);
        assertComponent(dc0, 0, List.of(), expectedDcBuses);
        assertComponent(sc0, 0, sc0expectedAcBuses, List.of());
        assertComponent(sc1, 1, sc1expectedAcBuses, List.of());

        cc0.getDcBuses().forEach(b -> assertTrue(b.isInMainConnectedComponent()));
    }

    @Test
    void testTwoDcComponents() {
        Network network = DcDetailedNetworkFactory.createLccMonopoleGroundReturn();
        // run a first topology processing so that cache invalidation gets triggered
        network.getDcComponents();
        network.getBusView().getConnectedComponents();
        network.getBusView().getSynchronousComponents();

        var dcLine = network.getDcLine("dcLine1");
        dcLine.getDcTerminal1().setConnected(false);

        List<Component> dcs = List.copyOf(network.getDcComponents());
        List<Component> ccs = List.copyOf(network.getBusView().getConnectedComponents());
        List<Component> scs = List.copyOf(network.getBusView().getSynchronousComponents());
        assertEquals(2, dcs.size());
        assertEquals(2, ccs.size());
        assertEquals(2, scs.size());
        Component dc0 = dcs.get(0);
        Component dc1 = dcs.get(1);
        Component cc0 = ccs.get(0);
        Component cc1 = ccs.get(1);
        Component sc0 = scs.get(0);
        Component sc1 = scs.get(1);

        var sc0expectedAcBuses = List.of(
                "VL-FR_0",
                "VLDC-FR-xNodeDc1fr-150_0",
                "VLDC-FR-xNodeDc1fr-150_1",
                "VLDC-FR-xNodeDc1fr-400_0",
                "VLDC-FR-xNodeDc1fr-400_1");
        var sc1expectedAcBuses = List.of(
                "VL-GB_0",
                "VLDC-GB-xNodeDc1gb-150_0",
                "VLDC-GB-xNodeDc1gb-150_1",
                "VLDC-GB-xNodeDc1gb-400_0");
        var dc0expectedDcBuses = List.of("dcNodeFrNeg_dcBus",
                "dcNodeFrPos_dcBus");
        var dc1expectedDcBuses = List.of(
                "dcNodeGbNeg_dcBus",
                "dcNodeGbPos_dcBus");

        assertComponent(cc0, 0, sc0expectedAcBuses, dc0expectedDcBuses);
        assertComponent(cc1, 1, sc1expectedAcBuses, dc1expectedDcBuses);
        assertComponent(dc0, 0, List.of(), dc0expectedDcBuses);
        assertComponent(dc1, 1, List.of(), dc1expectedDcBuses);
        assertComponent(sc0, 0, sc0expectedAcBuses, List.of());
        assertComponent(sc1, 1, sc1expectedAcBuses, List.of());

        cc0.getDcBuses().forEach(b -> assertTrue(b.isInMainConnectedComponent()));
        cc1.getDcBuses().forEach(b -> assertFalse(b.isInMainConnectedComponent()));
    }

    private static void assertComponent(Component component, int expectedNum, List<String> expectedAcBuses, List<String> expectedDcBuses) {
        Objects.requireNonNull(component);
        Objects.requireNonNull(expectedAcBuses);
        Objects.requireNonNull(expectedDcBuses);
        assertEquals(expectedNum, component.getNum());
        assertEquals(expectedAcBuses.size() + expectedDcBuses.size(), component.getSize());
        assertEquals(expectedAcBuses.stream().sorted().toList(), component.getBusStream().map(Identifiable::getId).sorted().toList());
        assertEquals(expectedAcBuses.stream().sorted().toList(), StreamSupport.stream(component.getBuses().spliterator(), false).map(Identifiable::getId).sorted().toList());
        assertEquals(expectedDcBuses.stream().sorted().toList(), component.getDcBusStream().map(Identifiable::getId).sorted().toList());
        assertEquals(expectedDcBuses.stream().sorted().toList(), StreamSupport.stream(component.getDcBuses().spliterator(), false).map(Identifiable::getId).sorted().toList());
    }

}
