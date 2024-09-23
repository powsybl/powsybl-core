/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraverseResult;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.powsybl.iidm.modification.util.ModificationReports.*;

/**
 * This modification removes the whole feeder bay related to a given feeder connectable.
 * This means that it removes all the dangling switches and internal connections which remain once the connectable is removed.
 * Note that determining the bay which corresponds to a connectable needs some computation and graph traversals.
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class RemoveFeederBay extends AbstractNetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveFeederBay.class);

    private final String connectableId;

    /**
     * Constructor
     * @param connectableId non-null id of the connectable whose feeder bay will be removed (busbar section are not accepted)
     */
    public RemoveFeederBay(String connectableId) {
        this.connectableId = Objects.requireNonNull(connectableId);
    }

    @Override
    public String getName() {
        return "RemoveFeederBay";
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager, ReportNode reportNode) {
        Connectable<?> connectable = network.getConnectable(connectableId);
        if (!checkConnectable(throwException, reportNode, connectable)) {
            return;
        }

        for (Terminal t : connectable.getTerminals()) {
            if (t.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
                Graph<Integer, Object> graph = createGraphFromTerminal(t);
                int node = t.getNodeBreakerView().getNode();
                cleanTopology(t.getVoltageLevel().getNodeBreakerView(), graph, node, reportNode);
            }
        }
        connectable.remove();
        removedConnectableReport(reportNode, connectableId);
        LOGGER.info("Connectable {} removed", connectableId);
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        Connectable<?> connectable = network.getConnectable(connectableId);
        if (connectable == null || connectable instanceof BusbarSection) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        }
        return impact;
    }

    private Graph<Integer, Object> createGraphFromTerminal(Terminal terminal) {
        Graph<Integer, Object> graph = new Pseudograph<>(Object.class);
        int node = terminal.getNodeBreakerView().getNode();
        VoltageLevel.NodeBreakerView vlNbv = terminal.getVoltageLevel().getNodeBreakerView();
        graph.addVertex(node);
        vlNbv.traverse(node, (node1, sw, node2) -> {
            TraverseResult result = vlNbv.getOptionalTerminal(node2)
                    .map(Terminal::getConnectable)
                    .filter(BusbarSection.class::isInstance)
                    .map(c -> TraverseResult.TERMINATE_PATH)
                    .orElse(TraverseResult.CONTINUE);
            graph.addVertex(node2);
            graph.addEdge(node1, node2, sw != null ? sw : Pair.of(node1, node2));
            return result;
        });
        return graph;
    }

    /**
     * Starting from the given node, traverse the graph and remove all the switches and/or internal connections until a
     * fork node is encountered, for which special care is needed to clean the topology.
     */
    private void cleanTopology(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph, int node, ReportNode reportNode) {
        Set<Object> edges = graph.edgesOf(node);
        if (edges.size() == 1) {
            Object edge = edges.iterator().next();
            Integer oppositeNode = getOppositeNode(graph, node, edge);
            removeSwitchOrInternalConnection(nbv, graph, edge, reportNode);
            cleanTopology(nbv, graph, oppositeNode, reportNode);
        } else if (edges.size() > 1) {
            cleanFork(nbv, graph, node, edges, reportNode);
        }
    }

    /**
     * Starting from the given node, traverse the graph and remove all the switches and/or internal connections until a
     * fork node is encountered or a node on which a connectable is connected
     */
    private void cleanMixedTopology(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph, int node, ReportNode reportNode) {
        // Get the next edge and the opposite node
        Set<Object> edges = graph.edgesOf(node);
        Object edge = edges.iterator().next();
        Integer oppositeNode = getOppositeNode(graph, node, edge);

        // Remove the switch or internal connection on the current edge
        removeSwitchOrInternalConnection(nbv, graph, edge, reportNode);

        // List the connectables connected to the opposite node
        List<Connectable<?>> connectables = new ArrayList<>();
        nbv.getOptionalTerminal(oppositeNode).map(Terminal::getConnectable).ifPresent(connectables::add);

        // If there is only one edge on the opposite node and no connectable, continue to remove the elements
        if (graph.edgesOf(oppositeNode).size() == 1 && connectables.isEmpty()) {
            cleanMixedTopology(nbv, graph, oppositeNode, reportNode);
        }
    }

    /**
     * Try to remove all edges of the given fork node
     */
    private void cleanFork(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph, int node, Set<Object> edges, ReportNode reportNode) {
        List<Object> toBusesOnly = new ArrayList<>();
        List<Object> mixed = new ArrayList<>();
        for (Object edge : edges) {
            List<Connectable<?>> connectables = getLinkedConnectables(nbv, graph, node, edge);
            if (connectables.stream().allMatch(BusbarSection.class::isInstance)) {
                // the edge is only linked to busbarSections, or to no connectables, hence it's a good candidate for removal
                toBusesOnly.add(edge);
            } else if (connectables.stream().noneMatch(BusbarSection.class::isInstance)) {
                // the edge is only linked to other non-busbarSection connectables, no further cleaning can be done
                // Note that connectables cannot be empty because of previous if
                String otherConnectableId = connectables.stream().map(Connectable::getId).findFirst().orElse("none");
                removeFeederBayAborted(reportNode, connectableId, node, otherConnectableId);
                LOGGER.info("Remove feeder bay of {} cannot go further node {}, as it is connected to {}", connectableId, node, otherConnectableId);
                return;
            } else {
                // the edge is linked to busbarSections and non-busbarSection connectables, some further cleaning can be done if there's only one edge of that type
                mixed.add(edge);
            }
        }

        // We now know there are only edges which are
        // - either only linked to busbarSections and no other connectables
        // - or linked to busbarSections and connectables
        // The former ones can be removed:
        for (Object edge : toBusesOnly) {
            removeAllSwitchesAndInternalConnections(nbv, graph, node, edge, reportNode);
        }
        // We don't remove the latter ones if more than one, as this would break the connection between them
        if (mixed.size() == 1) {
            // If only one, we're cleaning the dangling switches and/or internal connections
            cleanMixedTopology(nbv, graph, node, reportNode);
        }
    }

    private List<Connectable<?>> getLinkedConnectables(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph, Integer node, Object edge) {
        Set<Integer> visitedNodes = new HashSet<>();
        visitedNodes.add(node);
        List<Connectable<?>> connectables = new ArrayList<>();
        searchConnectables(nbv, graph, getOppositeNode(graph, node, edge), visitedNodes, connectables);
        return connectables;
    }

    private void searchConnectables(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph, Integer node,
                                    Set<Integer> visitedNodes, List<Connectable<?>> connectables) {
        if (visitedNodes.contains(node)) {
            return;
        }
        nbv.getOptionalTerminal(node).map(Terminal::getConnectable).ifPresent(connectables::add);
        if (!isBusbarSection(nbv, node)) {
            visitedNodes.add(node);
            for (Object e : graph.edgesOf(node)) {
                searchConnectables(nbv, graph, getOppositeNode(graph, node, e), visitedNodes, connectables);
            }
        }
    }

    /**
     * Traverse the graph and remove all switches and internal connections until encountering a {@link BusbarSection}.
     */
    private void removeAllSwitchesAndInternalConnections(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph,
                                                         int originNode, Object edge, ReportNode reportNode) {
        Integer oppositeNode = getOppositeNode(graph, originNode, edge);
        removeSwitchOrInternalConnection(nbv, graph, edge, reportNode);
        if (!isBusbarSection(nbv, oppositeNode)) {
            for (Object otherEdge : new ArrayList<>(graph.edgesOf(oppositeNode))) {
                removeAllSwitchesAndInternalConnections(nbv, graph, oppositeNode, otherEdge, reportNode);
            }
        }
    }

    private static boolean isBusbarSection(VoltageLevel.NodeBreakerView nbv, Integer node) {
        Optional<Connectable<?>> c = nbv.getOptionalTerminal(node).map(Terminal::getConnectable);
        return c.isPresent() && c.get() instanceof BusbarSection;
    }

    private static void removeSwitchOrInternalConnection(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph,
                                                         Object edge, ReportNode reportNode) {
        if (edge instanceof Switch sw) {
            String switchId = sw.getId();
            nbv.removeSwitch(switchId);
            removedSwitchReport(reportNode, switchId);
            LOGGER.info("Switch {} removed", switchId);
        } else {
            Pair<Integer, Integer> ic = (Pair<Integer, Integer>) edge;
            nbv.removeInternalConnections(ic.getFirst(), ic.getSecond());
            removedInternalConnectionReport(reportNode, ic.getFirst(), ic.getSecond());
            LOGGER.info("Internal connection between {} and {} removed", ic.getFirst(), ic.getSecond());
        }
        graph.removeEdge(edge);
    }

    private static Integer getOppositeNode(Graph<Integer, Object> graph, int node, Object e) {
        Integer edgeSource = graph.getEdgeSource(e);
        return edgeSource == node ? graph.getEdgeTarget(e) : edgeSource;
    }

    private boolean checkConnectable(boolean throwException, ReportNode reportNode, Connectable<?> connectable) {
        if (connectable instanceof BusbarSection) {
            LOGGER.error("BusbarSection connectables are not allowed as RemoveFeederBay input: {}", connectableId);
            removeFeederBayBusbarSectionReport(reportNode, connectableId);
            if (throwException) {
                throw new PowsyblException("BusbarSection connectables are not allowed as RemoveFeederBay input: " + connectableId);
            }
            return false;
        }
        if (connectable == null) {
            LOGGER.error("Connectable {} not found", connectableId);
            notFoundConnectableReport(reportNode, connectableId);
            if (throwException) {
                throw new PowsyblException("Connectable not found: " + connectableId);
            }
            return false;
        }
        return true;
    }
}
