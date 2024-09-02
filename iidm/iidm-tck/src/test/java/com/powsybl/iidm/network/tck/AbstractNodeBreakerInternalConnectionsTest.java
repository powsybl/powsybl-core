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

import java.util.*;
import java.util.stream.Collectors;

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

        assertEquals(6, vl.getNodeBreakerView().getInternalConnectionCount());
        List<InternalConnection> internalConnections = vl.getNodeBreakerView().getInternalConnectionStream().toList();
        int[] expecteds1 = new int[]{7, 6, 4, 5, 9, 8};
        int[] expecteds2 = new int[]{0, 3, 3, 2, 2, 1};
        assertEquals(expecteds1.length, expecteds2.length);
        for (int i = 0; i < expecteds1.length; i++) {
            assertEquals(expecteds1[i], internalConnections.get(i).getNode1());
            assertEquals(expecteds2[i], internalConnections.get(i).getNode2());
        }

        Iterator<Integer> nodeIterator7 = vl.getNodeBreakerView().getNodesInternalConnectedTo(7).iterator();
        assertEquals(0, (int) nodeIterator7.next());
        assertFalse(nodeIterator7.hasNext());

        Iterator<Integer> nodeIterator2 = vl.getNodeBreakerView().getNodesInternalConnectedTo(2).iterator();
        assertEquals(5, (int) nodeIterator2.next());
        assertEquals(9, (int) nodeIterator2.next());
        assertFalse(nodeIterator2.hasNext());

        List<Integer> nodesInternallyConnectedTo3 = vl.getNodeBreakerView().getNodeInternalConnectedToStream(3).boxed().toList();
        assertEquals(Arrays.asList(6, 4), nodesInternallyConnectedTo3);

        // Find the first internal connection encountered
        InternalConnections firstInternalConnectionFound = findFirstInternalConnections(vl);
        assertEquals(new InternalConnections().add(0, 7), firstInternalConnectionFound);

        // Find the internal connections encountered before encountering a terminal
        InternalConnections foundStoppingAtTerminals = findInternalConnectionsTraverseStoppingAtTerminals(vl);
        // If we stop traversal at terminals
        // some internal connections are expected to be missing
        InternalConnections expectedMissing = new InternalConnections().add(6, 3).add(9, 2).add(4, 3);

        // Compute all missing connections
        Set<String> actualMissing = all.stream()
                .filter(c -> !foundStoppingAtTerminals.contains(c))
                .collect(Collectors.toSet());
        assertEquals(expectedMissing, actualMissing);

        InternalConnections actual = findInternalConnections(vl);
        InternalConnections expected = all;
        assertEquals(expected, actual);

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
        Substation s4 = network.newSubstation()
                .setId("S4")
                .setCountry(Country.FR)
                .add();
        s4.newVoltageLevel()
                .setId("S4 10kV")
                .setNominalV(10.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
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
