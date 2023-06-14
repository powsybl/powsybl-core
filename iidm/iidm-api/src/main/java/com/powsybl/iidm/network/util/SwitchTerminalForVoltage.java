/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.*;

/**
 * Utility class to compute the flow of the switches associated to a voltageLevel
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class SwitchTerminalForVoltage {
    private final Terminal terminal1;
    private final Terminal terminal2;

    public SwitchTerminalForVoltage(Switch sw) {

        Map<String, SwNode> swNodeKey = new HashMap<>();
        SimpleWeightedGraph<SwNode, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        buildGraph(sw, swNodeKey, graph);

        SwNode swNode1 = getSwNode1(swNodeKey, sw);
        SwNode swNode2 = getSwNode2(swNodeKey, sw);

        ConnectivityInspector<SwNode, DefaultWeightedEdge> ci = new ConnectivityInspector<>(graph);
        this.terminal1 = ci.connectedSetOf(swNode1).stream()
            .map(swNode -> terminalWithBus(swNode, sw.getVoltageLevel())).filter(Objects::nonNull)
            .findFirst().orElse(null);
        this.terminal2 = ci.connectedSetOf(swNode2).stream()
            .map(swNode -> terminalWithBus(swNode, sw.getVoltageLevel())).filter(Objects::nonNull)
            .findFirst().orElse(null);
    }

    public Optional<Terminal> getTerminal1() {
        return Optional.ofNullable(terminal1);
    }

    public Optional<Terminal> getTerminal2() {
        return Optional.ofNullable(terminal2);
    }

    private void buildGraph(Switch sw, Map<String, SwNode> swNodeKey, SimpleWeightedGraph<SwNode, DefaultWeightedEdge> graph) {
        if (sw.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            buildGraphFromNodeBreaker(sw, swNodeKey, graph);
        } else {
            buildGraphFromBusBreaker(sw, swNodeKey, graph);
        }
    }

    private static void buildGraphFromNodeBreaker(Switch swRef, Map<String, SwNode> swNodeKey,
        SimpleWeightedGraph<SwNode, DefaultWeightedEdge> graph) {
        swRef.getVoltageLevel().getNodeBreakerView().getSwitches().forEach(sw -> {
            if (swRef.equals(sw) || sw.isOpen()) {
                return;
            }
            SwNode swNode1 = addSwNode(swNodeKey, swRef.getVoltageLevel().getNodeBreakerView().getNode1(sw.getId()));
            SwNode swNode2 = addSwNode(swNodeKey, swRef.getVoltageLevel().getNodeBreakerView().getNode2(sw.getId()));
            // Discard loops
            if (swNode1 == swNode2) {
                return;
            }
            graph.addVertex(swNode1);
            graph.addVertex(swNode2);
            graph.addEdge(swNode1, swNode2);
        });

        swRef.getVoltageLevel().getNodeBreakerView().getInternalConnections().forEach(ic -> {
            SwNode swNode1 = addSwNode(swNodeKey, ic.getNode1());
            SwNode swNode2 = addSwNode(swNodeKey, ic.getNode2());
            // Discard loops
            if (swNode1 == swNode2) {
                return;
            }
            graph.addVertex(swNode1);
            graph.addVertex(swNode2);
            graph.addEdge(swNode1, swNode2);
        });
    }

    private static void buildGraphFromBusBreaker(Switch swRef, Map<String, SwNode> swNodeKey,
        SimpleWeightedGraph<SwNode, DefaultWeightedEdge> graph) {
        swRef.getVoltageLevel().getBusBreakerView().getSwitches().forEach(sw -> {
            if (swRef.equals(sw) || sw.isOpen()) {
                return;
            }
            SwNode swNode1 = addSwNode(swNodeKey, swRef.getVoltageLevel().getBusBreakerView().getBus1(sw.getId()));
            SwNode swNode2 = addSwNode(swNodeKey, swRef.getVoltageLevel().getBusBreakerView().getBus2(sw.getId()));
            // Discard loops
            if (swNode1 == swNode2) {
                return;
            }
            graph.addVertex(swNode1);
            graph.addVertex(swNode2);
            graph.addEdge(swNode1, swNode2);
        });
    }

    private static SwNode getSwNode1(Map<String, SwNode> swNodeKey, Switch sw) {
        if (sw.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            return addSwNode(swNodeKey, sw.getVoltageLevel().getNodeBreakerView().getNode1(sw.getId()));
        } else {
            return addSwNode(swNodeKey, sw.getVoltageLevel().getBusBreakerView().getBus1(sw.getId()));
        }
    }

    private static SwNode getSwNode2(Map<String, SwNode> swNodeKey, Switch sw) {
        if (sw.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            return addSwNode(swNodeKey, sw.getVoltageLevel().getNodeBreakerView().getNode2(sw.getId()));
        } else {
            return addSwNode(swNodeKey, sw.getVoltageLevel().getBusBreakerView().getBus2(sw.getId()));
        }
    }

    private static SwNode addSwNode(Map<String, SwNode> swNodeKey, int node) {
        if (swNodeKey.containsKey(getKey(node))) {
            return swNodeKey.get(getKey(node));
        }
        SwNode swNode = new SwNode(node);
        swNodeKey.put(getKey(node), swNode);
        return swNode;
    }

    private static SwNode addSwNode(Map<String, SwNode> swNodeKey, Bus bus) {
        if (swNodeKey.containsKey(getKey(bus))) {
            return swNodeKey.get(getKey(bus));
        }
        SwNode swNode = new SwNode(bus);
        swNodeKey.put(getKey(bus), swNode);
        return swNode;
    }

    private static Terminal terminalWithBus(SwNode swNode, VoltageLevel voltageLevel) {
        if (swNode.isNodeBreaker()) {
            return terminalWithBusNodeBreaker(swNode, voltageLevel);
        } else {
            return terminalWithBusBusBreaker(swNode);
        }
    }

    private static Terminal terminalWithBusNodeBreaker(SwNode swNode, VoltageLevel voltageLevel) {
        Terminal terminal = voltageLevel.getNodeBreakerView().getTerminal(swNode.node);
        if (terminal != null && terminal.getBusView().getBus() != null) {
            return terminal;
        }
        return null;
    }

    private static Terminal terminalWithBusBusBreaker(SwNode swNode) {
        return swNode.bus.getConnectedTerminalStream().findFirst().orElse(null);
    }

    private static String getKey(int node) {
        return String.format("N-%d", node);
    }

    private static String getKey(Bus bus) {
        return String.format("B-%s", bus);
    }

    private static final class SwNode {
        private final Integer node;
        private final Bus bus;

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
    }
}
