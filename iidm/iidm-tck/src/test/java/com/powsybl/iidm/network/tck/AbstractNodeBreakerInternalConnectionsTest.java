/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraverseResult;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static com.powsybl.iidm.network.VoltageLevel.NodeBreakerView.InternalConnection;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public abstract class AbstractNodeBreakerInternalConnectionsTest {

    private static final String S5_10K_V = "S5 10kV";

    @Test
    public void testTraversalInternalConnections() {
        Network network = Network.create("testTraversalInternalConnections", "test");
        InternalConnections all = new InternalConnections();
        createNetwork(network, all);
        VoltageLevel vl = network.getVoltageLevel(S5_10K_V);

        assertEquals(10, vl.getNodeBreakerView().getInternalConnectionCount());
        List<InternalConnection> internalConnections = vl.getNodeBreakerView().getInternalConnectionStream().toList();
        int[][] expectedIcNodes = new int[][]{{7, 0}, {6, 3}, {4, 3}, {5, 2}, {9, 2}, {8, 1}, {1, 10}, {3, 11}, {2, 12}, {2, 13}};
        for (int i = 0; i < 10; i++) {
            assertEquals(expectedIcNodes[i][0], internalConnections.get(i).getNode1());
            assertEquals(expectedIcNodes[i][1], internalConnections.get(i).getNode2());
        }

        assertEquals(List.of(0), vl.getNodeBreakerView().getNodesInternalConnectedTo(7));
        assertEquals(List.of(5, 9, 12, 13), vl.getNodeBreakerView().getNodesInternalConnectedTo(2));
        assertEquals(List.of(6, 4, 11), vl.getNodeBreakerView().getNodeInternalConnectedToStream(3).boxed().toList());

        assertEquals(new InternalConnections().add(0, 7), findFirstInternalConnections(vl));

        // Find the internal connections encountered before encountering a terminal, starting from every node
        // Only internal connections connecting two nodes having both a terminal are expected to be missing
        InternalConnections icConnectedToAtMostOneTerminal = findInternalConnectionsTraverseStoppingAtTerminals(vl);
        InternalConnections expected = new InternalConnections();
        expected.add(7, 0).add(6, 3).add(4, 3).add(5, 2).add(9, 2).add(8, 1);
        assertEquals(expected, icConnectedToAtMostOneTerminal);

        assertEquals(all, findInternalConnections(vl));

    }

    @Test
    public void testRemoveInternalConnections() {
        Network network = Network.create("testTraversalInternalConnections", "test");
        createNetwork(network, new InternalConnections());
        VoltageLevel vl = network.getVoltageLevel(S5_10K_V);

        // remove an existing internal connection
        assertTrue(vl.getNodeBreakerView().getInternalConnectionStream().anyMatch(ic -> ic.getNode1() == 7 && ic.getNode2() == 0));
        vl.getNodeBreakerView().removeInternalConnections(7, 0);
        assertTrue(vl.getNodeBreakerView().getInternalConnectionStream().noneMatch(ic -> ic.getNode1() == 7 && ic.getNode2() == 0));

        // remove a non-existing internal connection
        boolean thrownException = false;
        try {
            vl.getNodeBreakerView().removeInternalConnections(6, 0);
        } catch (PowsyblException e) {
            assertEquals("Internal connection not found between 6 and 0", e.getMessage());
            thrownException = true;
        }
        assertTrue(thrownException);

        // remove multiple internal connections
        vl.getNodeBreakerView().newInternalConnection()
                .setNode1(6)
                .setNode2(3)
                .add();
        assertEquals(2, vl.getNodeBreakerView().getInternalConnectionStream().filter(ic -> ic.getNode1() == 6 && ic.getNode2() == 3).count());
        vl.getNodeBreakerView().removeInternalConnections(6, 3);
        assertTrue(vl.getNodeBreakerView().getInternalConnectionStream().noneMatch(ic -> ic.getNode1() == 6 && ic.getNode2() == 3));
    }

    @Test
    public void testRemoveVoltageLevelWithInternalConnectionsIssue() {
        Network network = Network.create("testRemoveVoltageLevelWithInternalConnectionsIssue", "test");
        InternalConnections all = new InternalConnections();
        createNetwork(network, all);
        network.getLine("L6").remove(); // needed to be allowed to remove the voltage level
        // should not throw a null pointer exception anymore
        network.getVoltageLevel(S5_10K_V).remove();
        assertNull(network.getVoltageLevel(S5_10K_V));
    }

    private void createNetwork(Network network, InternalConnections internalConnections) {
        Substation s = network.newSubstation()
                .setId("S5")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId(S5_10K_V)
                .setNominalV(10.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("NODE13")
                .setNode(0)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("NODE14")
                .setNode(3)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("NODE15")
                .setNode(2)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("NODE16")
                .setNode(1)
                .add();
        vl.getNodeBreakerView().newSwitch()
                .setId("BREAKER4")
                .setNode1(4)
                .setNode2(5)
                .setKind(SwitchKind.BREAKER)
                .add();
        vl.getNodeBreakerView().newSwitch()
                .setId("DISCONNECTOR7")
                .setNode1(6)
                .setNode2(7)
                .setKind(SwitchKind.DISCONNECTOR)
                .add();
        vl.getNodeBreakerView().newSwitch()
                .setId("DISCONNECTOR8")
                .setNode1(8)
                .setNode2(9)
                .setKind(SwitchKind.DISCONNECTOR)
                .add();
        vl.newLoad()
                .setId("M3")
                .setNode(11)
                .setP0(5)
                .setQ0(3)
                .add();
        vl.newLoad()
                .setId("M2a")
                .setNode(12)
                .setP0(2)
                .setQ0(1)
                .add();
        vl.newLoad()
                .setId("M2b")
                .setNode(13)
                .setP0(2)
                .setQ0(1)
                .add();
        VoltageLevel.NodeBreakerView topo = vl.getNodeBreakerView();
        addInternalConnection(topo, internalConnections, 7, 0);
        addInternalConnection(topo, internalConnections, 6, 3);
        addInternalConnection(topo, internalConnections, 4, 3);
        addInternalConnection(topo, internalConnections, 5, 2);
        addInternalConnection(topo, internalConnections, 9, 2);
        addInternalConnection(topo, internalConnections, 8, 1);
        addInternalConnection(topo, internalConnections, 1, 10);
        addInternalConnection(topo, internalConnections, 3, 11);
        addInternalConnection(topo, internalConnections, 2, 12);
        addInternalConnection(topo, internalConnections, 2, 13);
        Substation s4 = network.newSubstation()
                .setId("S4")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s4.newVoltageLevel()
                .setId("S4 10kV")
                .setNominalV(10.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("NODE40")
                .setNode(1)
                .add();
        network.newLine()
                .setId("L6")
                .setVoltageLevel1("S4 10kV")
                .setNode1(0)
                .setVoltageLevel2(S5_10K_V)
                .setNode2(10)
                .setR(0.082)
                .setX(0.086)
                .setG1(0)
                .setB1(0)
                .setG2(0)
                .setB2(0)
                .add();
        vl2.getNodeBreakerView().newSwitch()
                .setId("DISCONNECTOR1")
                .setNode1(0)
                .setNode2(1)
                .setKind(SwitchKind.DISCONNECTOR)
                .add();
    }

    private void addInternalConnection(
            VoltageLevel.NodeBreakerView topo,
            InternalConnections internalConnections,
            int node1,
            int node2) {
        topo.newInternalConnection()
                .setNode1(node1)
                .setNode2(node2)
                .add();
        internalConnections.add(node1, node2);
    }

    static class InternalConnections extends HashSet<String> {
        InternalConnections add(int node1, int node2) {
            add(node1 + "-" + node2);
            add(node2 + "-" + node1);
            return this;
        }
    }

    private InternalConnections findInternalConnectionsTraverseStoppingAtTerminals(VoltageLevel vl) {
        InternalConnections cs = new InternalConnections();

        VoltageLevel.NodeBreakerView topo = vl.getNodeBreakerView();
        topo.traverse(topo.getNodes(), (n1, sw, n2) -> {
            if (topo.getTerminal(n2) == null) {
                if (sw == null) {
                    cs.add(n1, n2);
                }
                return TraverseResult.CONTINUE;
            } else {
                return TraverseResult.TERMINATE_PATH;
            }
        });

        return cs;
    }

    private InternalConnections findInternalConnections(VoltageLevel vl) {
        VoltageLevel.NodeBreakerView topo = vl.getNodeBreakerView();

        InternalConnections cs = new InternalConnections();
        topo.traverse(topo.getNodes(), (n1, sw, n2) -> {
            if (sw == null) {
                cs.add(n1, n2);
            }
            return TraverseResult.CONTINUE;
        });

        return cs;
    }

    private InternalConnections findFirstInternalConnections(VoltageLevel vl) {
        VoltageLevel.NodeBreakerView topo = vl.getNodeBreakerView();

        InternalConnections cs = new InternalConnections();
        topo.traverse(topo.getNodes(), (n1, sw, n2) -> {
            if (sw == null) {
                cs.add(n1, n2);
                return TraverseResult.TERMINATE_TRAVERSER;
            }
            return TraverseResult.CONTINUE;
        });

        return cs;
    }
}
