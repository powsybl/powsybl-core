package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.powsybl.iidm.network.VoltageLevel.NodeBreakerView.InternalConnection;
import static org.junit.Assert.assertEquals;

public class NodeBreakerInternalConnectionsTest {

    @Test
    public void testTraversalInternalConnections() {
        Network network = NetworkFactory.create("testTraversalInternalConnections", "test");
        InternalConnections all = new InternalConnections();
        createNetwork(network, all);
        VoltageLevel vl = network.getVoltageLevel("S5 10kV");

        assertEquals(6, vl.getNodeBreakerView().getInternalConnectionCount());
        List<InternalConnection> internalConnections = vl.getNodeBreakerView().getInternalConnectionStream().collect(Collectors.toList());
        assertEquals(7, internalConnections.get(0).getNode1());
        assertEquals(0, internalConnections.get(0).getNode2());
        assertEquals(6, internalConnections.get(1).getNode1());
        assertEquals(3, internalConnections.get(1).getNode2());
        assertEquals(4, internalConnections.get(2).getNode1());
        assertEquals(3, internalConnections.get(2).getNode2());
        assertEquals(5, internalConnections.get(3).getNode1());
        assertEquals(2, internalConnections.get(3).getNode2());
        assertEquals(9, internalConnections.get(4).getNode1());
        assertEquals(2, internalConnections.get(4).getNode2());
        assertEquals(8, internalConnections.get(5).getNode1());
        assertEquals(1, internalConnections.get(5).getNode2());

        InternalConnections foundStoppingAtTerminals = findInternalConnectionsTraverseStoppingAtTerminals(vl);
        // If we stop traversal at terminals
        // some internal connections are expected to be missing
        InternalConnections expectedMissing = new InternalConnections();
        expectedMissing.add(5, 2);
        expectedMissing.add(4, 3);
        // Compute all missing connections
        Set<String> actualMissing = all.stream()
                .filter(c -> !foundStoppingAtTerminals.contains(c))
                .collect(Collectors.toSet());
        assertEquals(expectedMissing, actualMissing);

        InternalConnections actual = findInternalConnections(vl);
        InternalConnections expected = all;
        assertEquals(expected, actual);
    }

    private void createNetwork(Network network, InternalConnections internalConnections) {
        Substation s = network.newSubstation()
                .setId("S5")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("S5 10kV")
                .setNominalV(10.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().setNodeCount(14);
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
        VoltageLevel vl4 = s4.newVoltageLevel()
                .setId("S4 10kV")
                .setNominalV(10.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl4.getNodeBreakerView().setNodeCount(1);
        network.newLine()
                .setId("L6")
                .setVoltageLevel1("S4 10kV")
                .setNode1(0)
                .setVoltageLevel2("S5 10kV")
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
        void add(int node1, int node2) {
            add(node1 + "-" + node2);
            add(node2 + "-" + node1);
        }
    }

    private InternalConnections findInternalConnectionsTraverseStoppingAtTerminals(VoltageLevel vl) {
        InternalConnections cs = new InternalConnections();

        VoltageLevel.NodeBreakerView topo = vl.getNodeBreakerView();
        int[] nodes = topo.getNodes();
        final TIntSet explored = new TIntHashSet();
        for (int n : nodes) {
            if (explored.contains(n) || topo.getTerminal(n) == null) {
                continue;
            }
            explored.add(n);
            topo.traverse(n, (n1, sw, n2) -> {
                explored.add(n2);
                if (sw == null) {
                    cs.add(n1, n2);
                }
                return topo.getTerminal(n2) == null;
            });
        }
        return cs;
    }

    private InternalConnections findInternalConnections(VoltageLevel vl) {
        InternalConnections cs = new InternalConnections();

        VoltageLevel.NodeBreakerView topo = vl.getNodeBreakerView();
        int[] nodes = topo.getNodes();
        final TIntSet explored = new TIntHashSet();
        for (int n : nodes) {
            if (explored.contains(n)) {
                continue;
            }
            explored.add(n);
            topo.traverse(n, (n1, sw, n2) -> {
                explored.add(n2);
                if (sw == null) {
                    cs.add(n1, n2);
                }
                return true;
            });
        }
        return cs;
    }
}
