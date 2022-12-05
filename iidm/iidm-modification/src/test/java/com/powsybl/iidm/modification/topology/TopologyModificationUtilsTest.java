/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.extensions.FourSubstationsNodeBreakerWithExtensionsFactory;
import com.powsybl.iidm.network.impl.extensions.BusbarSectionPositionImpl;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.apache.commons.lang3.Range;
import org.junit.Test;

import java.util.*;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.*;
import static org.junit.Assert.*;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TopologyModificationUtilsTest extends AbstractConverterTest {

    @Test
    public void testFeederOrders() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        Set<Integer> feederOrders = TopologyModificationUtils.getFeederPositions(network.getVoltageLevel("vl1"));
        assertEquals(13, feederOrders.size());
        assertTrue(feederOrders.contains(100));
        Set<Integer> feederOrders2 = TopologyModificationUtils.getFeederPositions(network.getVoltageLevel("vl2"));
        assertEquals(9, feederOrders2.size());
        Set<Integer> feederOrders3 = TopologyModificationUtils.getFeederPositions(network.getVoltageLevel("vlSubst2"));
        assertEquals(1, feederOrders3.size());
    }

    @Test
    public void testGetPositionsByConnectable() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        Map<String, List<Integer>> positionsByConnectable = getFeederPositionsByConnectable(network.getVoltageLevel("vl1"));
        assertTrue(positionsByConnectable.containsKey("load1"));
        assertEquals(List.of(70), positionsByConnectable.get("line1"));
        assertEquals(13, positionsByConnectable.size());
    }

    @Test
    public void testGetPositionsByConnectableWithInternalLine() {
        Network network = Network.read("network-node-breaker-with-new-internal-line.xml", getClass().getResourceAsStream("/network-node-breaker-with-new-internal-line.xml"));
        Map<String, List<Integer>> positionsByConnectable = getFeederPositionsByConnectable(network.getVoltageLevel("vl1"));
        assertEquals(List.of(14, 105), positionsByConnectable.get("lineTest"));
        assertEquals(14, positionsByConnectable.size());
    }

    @Test
    public void testGetFeedersByConnectable() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        Map<String, List<ConnectablePosition.Feeder>> feeders = getFeedersByConnectable(network.getVoltageLevel("vl1"));
        assertFalse(feeders.isEmpty());
        assertEquals(13, feeders.size());
        assertTrue(feeders.containsKey("trf5"));
        List<ConnectablePosition.Feeder> feeder = feeders.get("trf6");
        assertEquals(1, feeder.size());
        assertEquals("trf61", feeder.get(0).getName());
        assertEquals(Optional.of(50), feeder.get(0).getOrder());
    }

    @Test
    public void testGetFeedersByConnectableWithInternalLine() {
        Network network = Network.read("network-node-breaker-with-new-internal-line.xml", getClass().getResourceAsStream("/network-node-breaker-with-new-internal-line.xml"));
        Map<String, List<ConnectablePosition.Feeder>> feeders = getFeedersByConnectable(network.getVoltageLevel("vl1"));
        assertEquals(2, feeders.get("lineTest").size());
        assertEquals(14, feeders.size());
        List<ConnectablePosition.Feeder> feedersLineTest = feeders.get("lineTest");
        List<Integer> ordersLineTest = new ArrayList<>();
        feedersLineTest.forEach(feeder -> ordersLineTest.add(feeder.getOrder().orElse(0)));
        Collections.sort(ordersLineTest);
        assertEquals(List.of(14, 105), ordersLineTest);
    }

    @Test
    public void testGetUnusedPositionsWithEmptyVoltageLevel() {
        Network network = Network.create("n", "test");
        VoltageLevel vl1 = network.newVoltageLevel().setId("vl1").setNominalV(400).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        BusbarSection bbs = vl1.getNodeBreakerView().newBusbarSection().setId("b1").setNode(2).add();
        bbs.addExtension(BusbarSectionPosition.class, new BusbarSectionPositionImpl(bbs, 0, 0));

        Optional<Range<Integer>> unusedOrderPositionsAfter = TopologyModificationUtils.getUnusedOrderPositionsAfter(bbs);
        assertTrue(unusedOrderPositionsAfter.isPresent());
        assertEquals(0, (int) unusedOrderPositionsAfter.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter.get().getMaximum());

        Optional<Range<Integer>> unusedOrderPositionsBefore = TopologyModificationUtils.getUnusedOrderPositionsAfter(bbs);
        assertTrue(unusedOrderPositionsBefore.isPresent());
        assertEquals(0, (int) unusedOrderPositionsBefore.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsBefore.get().getMaximum());
    }

    @Test
    public void testNoConnectablePositionExt() {
        Network network = Network.read("network-nb-no-connectable-position.xiidm", getClass().getResourceAsStream("/network-nb-no-connectable-position.xiidm"));
        Optional<Range<Integer>> unusedOrderPositionsBefore = TopologyModificationUtils.getUnusedOrderPositionsBefore(network.getBusbarSection("vl_test_1_1"));
        Optional<Range<Integer>> unusedOrderPositionsAfter = TopologyModificationUtils.getUnusedOrderPositionsAfter(network.getBusbarSection("vl_test_1_1"));
        assertEquals(0, (int) unusedOrderPositionsBefore.map(Range::getMinimum).orElse(-1));
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsBefore.map(Range::getMaximum).orElse(-1));
        assertEquals(0, (int) unusedOrderPositionsAfter.map(Range::getMinimum).orElse(-1));
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter.map(Range::getMaximum).orElse(-1));
    }

    @Test
    public void testInvalidFeederReturnsNoPosition() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        network.getLoad("LD1").newExtension(ConnectablePositionAdder.class)
                .newFeeder1()
                .withName("LD1")
                .withOrder(0)
                .withDirection(ConnectablePosition.Direction.TOP)
                .add();
        Set<Integer> feederOrders = TopologyModificationUtils.getFeederPositions(network.getVoltageLevel("S1VL1"));
        assertEquals(0, feederOrders.size());
    }

    @Test
    public void testGetFeederPositionsWithInternalLine() {
        Network network = Network.read("network-node-breaker-with-new-internal-line.xml", getClass().getResourceAsStream("/network-node-breaker-with-new-internal-line.xml"));
        Set<Integer> feederOrders = TopologyModificationUtils.getFeederPositions(network.getVoltageLevel("vl1"));
        assertEquals(15, feederOrders.size());

    }

    @Test
    public void testGetFirstBbs() {
        Network network = FourSubstationsNodeBreakerWithExtensionsFactory.create();
        BusbarSection firstBbs = getFirstBusbarSection(network.getVoltageLevel("S1VL2"));
        assertEquals("S1VL2_BBS1", firstBbs.getId());
        Network networkWithoutExtensions = FourSubstationsNodeBreakerFactory.create();
        BusbarSection firstBbsWithoutExtensions = getFirstBusbarSection(network.getVoltageLevel("S1VL2"));
        assertEquals("S1VL2_BBS1", firstBbs.getId());
        network.newVoltageLevel().setId("VLTEST").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        assertThrows(PowsyblException.class, () -> getFirstBusbarSection(network.getVoltageLevel("VLTEST")));
    }

    @Test
    public void testGetFeedersByConnectableReturnEmptyListIfEmptyVoltageLevel() {
        Network network = FourSubstationsNodeBreakerWithExtensionsFactory.create();
        network.newVoltageLevel().setId("VLTEST").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        Map<String, List<ConnectablePosition.Feeder>> feedersByConnectable = getFeedersByConnectable(network.getVoltageLevel("VLTEST"));
        assertEquals(0, feedersByConnectable.size());
    }
}
