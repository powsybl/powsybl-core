/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class VertexCountBugTest {

    private static Network createNetwork() {
        Network network = NetworkFactory.create("test", "test");
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setNominalV(100.0)
                .add();
        vl.getNodeBreakerView()
                .setNodeCount(3);

        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS")
                .setNode(0)
                .add();

        vl.getNodeBreakerView().newBreaker()
                .setId("SW2")
                .setNode1(0)
                .setNode2(2);

        vl.newGenerator()
                .setId("G")
                .setNode(1)
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(100.0)
                .setVoltageRegulatorOn(false)
                .setTargetQ(60.0)
                .add();

        vl.newLoad()
                .setId("L")
                .setNode(2)
                .setP0(100.0)
                .setQ0(60.0)
                .add();

        return network;
    }

    @Test
    public void test() {
        Network network = createNetwork();
        VoltageLevel vl = network.getVoltageLevel("VL");
        VoltageLevel.NodeBreakerView view = vl.getNodeBreakerView();
        assertEquals(3, view.getNodeCount());

        // Remove the generator and assert
        network.getGenerator("G").remove();
        assertEquals(3, view.getNodeCount());

        Network copy = NetworkXml.copy(network);
        vl = copy.getVoltageLevel("VL");
        view = vl.getNodeBreakerView();
        assertEquals(3, view.getNodeCount());
        assertArrayEquals(new int[]{0, 1, 2}, view.getNodes());

        vl.cleanTopology();
        assertEquals(2, view.getNodeCount());
        assertArrayEquals(new int[]{0, 2}, view.getNodes());
    }

    @Test
    public void reuseVertex() {
        Network network = createNetwork();
        network.getGenerator("G").remove();

        // Create the generator again and reuse old vertex
        VoltageLevel vl = network.getVoltageLevel("VL");
        vl.newGenerator()
                .setId("G")
                .setNode(1)
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(100.0)
                .setVoltageRegulatorOn(false)
                .setTargetQ(60.0)
                .add();

        assertEquals(3, vl.getNodeBreakerView().getNodeCount());
        assertArrayEquals(new int[]{0, 1, 2}, vl.getNodeBreakerView().getNodes());
    }
}
