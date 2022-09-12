/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.impl.extensions.BusbarSectionPositionImpl;
import com.powsybl.iidm.xml.AbstractXmlConverterTest;
import org.apache.commons.lang3.Range;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.getFeederPositionsByConnectable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TopologyModificationUtilsTest extends AbstractXmlConverterTest  {

    @Test
    public void testFeederOrders() {
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
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
        Network network = Importers.loadNetwork("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        Map<String, List<Integer>> positionsByConnectable = getFeederPositionsByConnectable(network.getVoltageLevel("vl1"));
        assertTrue(positionsByConnectable.containsKey("load1"));
        assertEquals(List.of(70), positionsByConnectable.get("line1"));
        assertEquals(13, positionsByConnectable.size());
    }

    @Test
    public void testGetPositionsByConnectableWithInternalLine() {
        Network network = Importers.loadNetwork("network-node-breaker-with-new-internal-line.xml", getClass().getResourceAsStream("/network-node-breaker-with-new-internal-line.xml"));
        Map<String, List<Integer>> positionsByConnectable = getFeederPositionsByConnectable(network.getVoltageLevel("vl1"));
        assertEquals(List.of(14, 105), positionsByConnectable.get("lineTest"));
        assertEquals(14, positionsByConnectable.size());
    }

    @Test
    public void testGetUnusedPositionsWithEmptyVoltageLevel() {
        Network network = Network.create("n", "test");
        VoltageLevel vl1 = network.newVoltageLevel().setId("vl1").setNominalV(400).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        BusbarSection bbs = vl1.getNodeBreakerView().newBusbarSection().setId("b1").setNode(2).add();
        bbs.addExtension(BusbarSectionPosition.class, new BusbarSectionPositionImpl(bbs, 0, 0));

        Optional<Range<Integer>> unusedOrderPositionsAfter = TopologyModificationUtils.getUnusedOrderPositionsAfter(bbs);
        assertTrue(unusedOrderPositionsAfter.isPresent());
        assertEquals(Integer.MIN_VALUE, (int) unusedOrderPositionsAfter.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsAfter.get().getMaximum());

        Optional<Range<Integer>> unusedOrderPositionsBefore = TopologyModificationUtils.getUnusedOrderPositionsAfter(bbs);
        assertTrue(unusedOrderPositionsBefore.isPresent());
        assertEquals(Integer.MIN_VALUE, (int) unusedOrderPositionsBefore.get().getMinimum());
        assertEquals(Integer.MAX_VALUE, (int) unusedOrderPositionsBefore.get().getMaximum());
    }
}
