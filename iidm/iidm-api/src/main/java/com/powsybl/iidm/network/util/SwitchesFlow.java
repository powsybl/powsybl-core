/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm.SpanningTree;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.*;

/**
 * Utility class to compute the flow of the switches associated to a voltageLevel
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class SwitchesFlow {
    private final VoltageLevel voltageLevel;
    private final Terminal slackTerminal;
    private final Map<String, SwFlow> switchesFlows;

    public SwitchesFlow(VoltageLevel voltageLevel) {
        this(voltageLevel, null);
    }

    public SwitchesFlow(VoltageLevel voltageLevel, Terminal slackTerminal) {
        this.voltageLevel = Objects.requireNonNull(voltageLevel);
        this.slackTerminal = slackTerminal;
        switchesFlows = new HashMap<>();

        compute();
    }

    // Compute switches flow
    private void compute() {

        Map<String, SwNode> swNodeInjection = new HashMap<>();
        SimpleWeightedGraph<SwNode, SwEdge> graph = new SimpleWeightedGraph<>(SwEdge.class);

        buildGraph(swNodeInjection, graph);
        calculateInjections(swNodeInjection);

        ConnectivityInspector<SwNode, SwEdge> ci = new ConnectivityInspector<>(graph);
        ci.connectedSets().forEach(connectedSet -> connectedComponentSwitchesFlow(swNodeInjection, graph, connectedSet));

        assignZeroFlowToEdgesOutsideAllTrees(graph);
    }

    public boolean isEmpty() {
        return switchesFlows.isEmpty();
    }

    public double getP1(String switchId) {
        if (switchesFlows.containsKey(switchId)) {
            return switchesFlows.get(switchId).p1;
        } else {
            return 0.0;
        }
    }

    public double getQ1(String switchId) {
        if (switchesFlows.containsKey(switchId)) {
            return switchesFlows.get(switchId).q1;
        } else {
            return 0.0;
        }
    }

    public double getP2(String switchId) {
        if (switchesFlows.containsKey(switchId)) {
            return switchesFlows.get(switchId).p2;
        } else {
            return 0.0;
        }
    }

    public double getQ2(String switchId) {
        if (switchesFlows.containsKey(switchId)) {
            return switchesFlows.get(switchId).q2;
        } else {
            return 0.0;
        }
    }

    private void buildGraph(Map<String, SwNode> swNodeInjection, SimpleWeightedGraph<SwNode, SwEdge> graph) {
        if (voltageLevel.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            buildGraphFromNodeBreaker(swNodeInjection, graph);
        } else {
            buildGraphFromBusBreaker(swNodeInjection, graph);
        }
    }

    private void buildGraphFromNodeBreaker(Map<String, SwNode> swNodeInjection, SimpleWeightedGraph<SwNode, SwEdge> graph) {
        voltageLevel.getNodeBreakerView().getSwitches().forEach(sw -> {
            if (sw.isOpen()) {
                return;
            }
            SwNode swNode1 = addSwNode(swNodeInjection, voltageLevel.getNodeBreakerView().getNode1(sw.getId()));
            SwNode swNode2 = addSwNode(swNodeInjection, voltageLevel.getNodeBreakerView().getNode2(sw.getId()));
            // Discard loops
            if (swNode1 == swNode2) {
                return;
            }
            graph.addVertex(swNode1);
            graph.addVertex(swNode2);
            graph.addEdge(swNode1, swNode2, new SwEdge(sw, swNode1, swNode2));
        });

        voltageLevel.getNodeBreakerView().getInternalConnections().forEach(ic -> {
            SwNode swNode1 = addSwNode(swNodeInjection, ic.getNode1());
            SwNode swNode2 = addSwNode(swNodeInjection, ic.getNode2());
            SwEdge swEdge = new SwEdge(swNode1, swNode2);
            // Discard loops
            if (swNode1 == swNode2) {
                return;
            }
            graph.addVertex(swNode1);
            graph.addVertex(swNode2);
            graph.addEdge(swNode1, swNode2, swEdge);
        });
    }

    private void buildGraphFromBusBreaker(Map<String, SwNode> swNodeInjection, SimpleWeightedGraph<SwNode, SwEdge> graph) {
        voltageLevel.getBusBreakerView().getSwitches().forEach(sw -> {
            if (sw.isOpen()) {
                return;
            }
            SwNode swNode1 = addSwNode(swNodeInjection, voltageLevel.getBusBreakerView().getBus1(sw.getId()));
            SwNode swNode2 = addSwNode(swNodeInjection, voltageLevel.getBusBreakerView().getBus2(sw.getId()));
            // Discard loops
            if (swNode1 == swNode2) {
                return;
            }
            graph.addVertex(swNode1);
            graph.addVertex(swNode2);
            graph.addEdge(swNode1, swNode2, new SwEdge(sw, swNode1, swNode2));
        });
    }

    private static SwNode addSwNode(Map<String, SwNode> swNodeInjection, int node) {
        if (swNodeInjection.containsKey(getKey(node))) {
            return swNodeInjection.get(getKey(node));
        }
        SwNode swNode = new SwNode(node);
        swNodeInjection.put(getKey(node), swNode);
        return swNode;
    }

    private static SwNode addSwNode(Map<String, SwNode> swNodeInjection, Bus bus) {
        if (swNodeInjection.containsKey(getKey(bus))) {
            return swNodeInjection.get(getKey(bus));
        }
        SwNode swNode = new SwNode(bus);
        swNodeInjection.put(getKey(bus), swNode);
        return swNode;
    }

    private void calculateInjections(Map<String, SwNode> swNodeInjection) {
        if (voltageLevel.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            calculateInjectionsNodeBreaker(voltageLevel, swNodeInjection);
        } else {
            calculateInjectionsBusBreaker(voltageLevel, swNodeInjection);
        }
    }

    private static void calculateInjectionsNodeBreaker(VoltageLevel voltageLevel, Map<String, SwNode> swNodeInjection) {
        int[] nodes = voltageLevel.getNodeBreakerView().getNodes();
        for (int node : nodes) {
            Terminal terminal = voltageLevel.getNodeBreakerView().getTerminal(node);
            if (terminal != null) {
                double p = getTerminalP(terminal);
                double q = getTerminalQ(terminal);
                swNodeInjection.computeIfPresent(getKey(node), (key, value) -> value.addPQ(p, q));
            }
        }
    }

    private static void calculateInjectionsBusBreaker(VoltageLevel voltageLevel, Map<String, SwNode> swNodeInjection) {
        voltageLevel.getBusBreakerView().getBuses().forEach(bus ->
            bus.getConnectedTerminals().forEach(terminal -> {
                double p = getTerminalP(terminal);
                double q = getTerminalQ(terminal);
                swNodeInjection.computeIfPresent(getKey(bus), (key, value) -> value.addPQ(p, q));
            }));
    }

    private boolean isSlack(SwNode swNode) {
        if (voltageLevel.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            return slackTerminal != null && swNode.node == slackTerminal.getNodeBreakerView().getNode();
        } else {
            return slackTerminal != null && swNode.bus.equals(slackTerminal.getBusBreakerView().getBus());
        }
    }

    private void connectedComponentSwitchesFlow(Map<String, SwNode> swNodeInjection,
        SimpleWeightedGraph<SwNode, SwEdge> graph, Set<SwNode> connectedSet) {
        // Discard isolated nodes
        if (connectedSet.size() <= 1) {
            return;
        }
        // We chose the slack bus (if there is one) as the root of the tree,
        // This way, the potential mismatch of the whole busview bus will be assigned entirely
        // to the bus/breaker view that is labeled as slack
        // We always look in the sorted nodes list to ensure a deterministic selection of the root
        List<SwNode> sortedNodes = connectedSet.stream().sorted(Comparator.comparing(SwitchesFlow::getKey)).toList();
        Optional<SwNode> swRoot = sortedNodes.stream()
                .filter(this::isSlack)
                .findFirst()
                .or(() -> sortedNodes.stream().findFirst());
        if (swRoot.isEmpty()) {
            return;
        }

        AsSubgraph<SwNode, SwEdge> subGraph = new AsSubgraph<>(graph, connectedSet);
        SpanningTree<SwEdge> tree = new KruskalMinimumSpanningTree<>(subGraph).getSpanningTree();

        List<List<SwNode>> levels = new ArrayList<>();
        Map<SwNode, SwEdge> parent = new HashMap<>();
        buildLevels(tree, swRoot.get(), parent, levels);

        completeFlowsForEdgesInsideTree(swNodeInjection, parent, levels);
    }

    private static void buildLevels(SpanningTree<SwEdge> tree, SwNode swRoot, Map<SwNode, SwEdge> parent,
        List<List<SwNode>> levels) {
        Set<SwEdge> processed = new HashSet<>();
        Map<SwNode, List<SwEdge>> swEdgesInNode = new HashMap<>();

        // Map to iterate once over the edges of the bus
        tree.getEdges().forEach(e -> {
            swEdgesInNode.computeIfAbsent(e.swNode1, b -> new ArrayList<>()).add(e);
            swEdgesInNode.computeIfAbsent(e.swNode2, b -> new ArrayList<>()).add(e);
        });

        // Add root level
        levels.add(new ArrayList<>(Collections.singleton(swRoot)));

        // Build levels of the tree
        int level = 0;
        while (level < levels.size()) {
            List<SwNode> nextLevel = new ArrayList<>();
            levels.get(level).forEach(swNode -> swEdgesInNode.get(swNode).forEach(e -> {
                SwNode other = otherSwNode(e, swNode);
                if (other == null) {
                    return;
                }
                if (processed.contains(e)) {
                    return;
                }
                nextLevel.add(other);
                parent.put(other, e);
                processed.add(e);
            }));
            if (!nextLevel.isEmpty()) {
                levels.add(nextLevel);
            }
            level++;
        }
    }

    private void completeFlowsForEdgesInsideTree(Map<String, SwNode> swNodeInjection, Map<SwNode, SwEdge> parent,
        List<List<SwNode>> levels) {
        // Traverse the tree from leaves to root
        // (The root itself does not need to be processed)
        int level = levels.size() - 1;
        while (level >= 1) {
            levels.get(level).forEach(swNode -> {
                double p = swNode.pflow + swNode.p;
                double q = swNode.qflow + swNode.q;
                SwEdge swEdge = parent.get(swNode);
                addFlowToParentSwNode(swNodeInjection, otherSwNode(swEdge, swNode), p, q);
                if (swEdge.isSwitch()) {
                    switchesFlows.computeIfAbsent(swEdge.getSwitchId(), k -> calculateSwFlow(swEdge, swNode, p, q));
                }
            });
            level--;
        }
    }

    private static void addFlowToParentSwNode(Map<String, SwNode> swNodeInjection, SwNode swNode, double p, double q) {
        swNodeInjection.computeIfPresent(getKey(swNode), (key, value) -> value.addFlow(p, q));
    }

    private void assignZeroFlowToEdgesOutsideAllTrees(SimpleWeightedGraph<SwNode, SwEdge> graph) {
        graph.edgeSet().forEach(e -> {
            if (e.isSwitch() && !switchesFlows.containsKey(e.getSwitchId())) {
                switchesFlows.put(e.getSwitchId(), new SwFlow(0.0, 0.0, 0.0, 0.0));
            }
        });
    }

    private static SwFlow calculateSwFlow(SwEdge swEdge, SwNode swNode, double pOtherNode, double qOtherNode) {
        if (swEdge.swNode1 == swNode) {
            return new SwFlow(-pOtherNode, -qOtherNode, pOtherNode, qOtherNode);
        } else {
            return new SwFlow(pOtherNode, qOtherNode, -pOtherNode, -qOtherNode);
        }
    }

    private static SwNode otherSwNode(SwEdge swEdge, SwNode swNode) {
        if (swEdge.swNode1 == swNode) {
            return swEdge.swNode2;
        } else if (swEdge.swNode2 == swNode) {
            return swEdge.swNode1;
        } else {
            return null;
        }
    }

    private static String getKey(int node) {
        return String.format("N-%d", node);
    }

    private static String getKey(Bus bus) {
        return String.format("B-%s", bus.getId());
    }

    private static String getKey(SwNode swNode) {
        if (swNode.isNodeBreaker()) {
            return getKey(swNode.node);
        } else {
            return getKey(swNode.bus);
        }
    }

    private static double getTerminalP(Terminal terminal) {
        if (Double.isNaN(terminal.getP())) {
            return 0.0;
        } else {
            return terminal.getP();
        }
    }

    private static double getTerminalQ(Terminal terminal) {
        if (Double.isNaN(terminal.getQ())) {
            return 0.0;
        } else {
            return terminal.getQ();
        }
    }

    private static final class SwNode {
        private final Integer node;
        private final Bus bus;
        private double p;
        private double q;
        private double pflow;
        private double qflow;

        private SwNode(int node) {
            this.node = node;
            bus = null;
        }

        private SwNode(Bus bus) {
            node = null;
            this.bus = bus;
        }

        private boolean isNodeBreaker() {
            return node != null;
        }

        private SwNode addPQ(double p, double q) {
            this.p = this.p + p;
            this.q = this.q + q;

            return this;
        }

        private SwNode addFlow(double pFlow, double qFlow) {
            this.pflow = this.pflow + pFlow;
            this.qflow = this.qflow + qFlow;

            return this;
        }
    }

    private static final class SwEdge extends DefaultWeightedEdge {
        private final transient Switch sw;
        private final transient SwNode swNode1;
        private final transient SwNode swNode2;

        private SwEdge(Switch sw, SwNode swNode1, SwNode swNode2) {
            this.sw = sw;
            this.swNode1 = swNode1;
            this.swNode2 = swNode2;
        }

        private SwEdge(SwNode swNode1, SwNode swNode2) {
            sw = null;
            this.swNode1 = swNode1;
            this.swNode2 = swNode2;
        }

        private boolean isSwitch() {
            return sw != null;
        }

        private String getSwitchId() {
            if (sw != null) {
                return sw.getId();
            } else {
                return null;
            }
        }

        @Override
        protected double getWeight() {
            return 0.0;
        }
    }

    private static final class SwFlow {
        private final double p1;
        private final double q1;
        private final double p2;
        private final double q2;

        private SwFlow(double p1, double q1, double p2, double q2) {
            this.p1 = p1;
            this.q1 = q1;
            this.p2 = p2;
            this.q2 = q2;
        }
    }
}
