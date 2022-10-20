/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraverseResult;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.powsybl.iidm.modification.topology.ModificationReports.notFoundConnectableReport;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class RemoveFeederBay extends AbstractNetworkModification {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveFeederBay.class);

    private final String connectableId;

    RemoveFeederBay(String connectableId) {
        this.connectableId = Objects.requireNonNull(connectableId);
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager, Reporter reporter) {
        Connectable<?> connectable = network.getConnectable(connectableId);
        // TODO: do not accept busbarSection ids
        // Note: if the connectable exists, topology of the associated voltage level is node/breaker
        if (connectable == null) {
            LOGGER.error("Connectable {} not found.", connectableId);
            notFoundConnectableReport(reporter, connectableId);
            if (throwException) {
                throw new PowsyblException("Connectable not found: " + connectableId);
            }
            return;
        }

        connectable.getTerminals().forEach(t -> {
            Graph<Integer, Object> graph = createGraphFromTerminal(t);
            int node = t.getNodeBreakerView().getNode();
            cleanGraph(t.getVoltageLevel().getNodeBreakerView(), graph, node);
        });
        connectable.remove();
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

    private void cleanGraph(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph, int node) {
        Set<Object> edges = graph.edgesOf(node);
        if (edges.size() == 1) {
            Object edge = edges.iterator().next();
            Integer oppositeNode = getOppositeNode(graph, node, edge);
            removeSwitchOrInternalConnection(nbv, graph, edge);
            cleanGraph(nbv, graph, oppositeNode);
        } else if (edges.size() > 1) {
            cleanFork(nbv, graph, node, edges);
        }
    }

    private void cleanFork(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph, int node, Set<Object> edges) {
        Set<Integer> visitedNodes = new HashSet<>();
        visitedNodes.add(node); // TODO: should we really with loop corner cases?

        List<Object> toBusesOnly = new ArrayList<>();
        List<Object> mixed = new ArrayList<>();
        for (Object edge : edges) {
            Integer oppositeNode = getOppositeNode(graph, node, edge);
            List<Connectable<?>> connectables = new ArrayList<>();
            searchConnectables(nbv, graph, oppositeNode, visitedNodes, connectables);
            boolean noBuses = connectables.stream().noneMatch(BusbarSection.class::isInstance);
            if (noBuses) {
                return;
            }
            boolean onlyBuses = connectables.stream().allMatch(BusbarSection.class::isInstance);
            if (onlyBuses) {
                toBusesOnly.add(edge);
            } else {
                mixed.add(edge);
            }
        }

        for (Object edge : toBusesOnly) {
            removeAllSwitchesAndInternalConnections(nbv, graph, node, edge);
        }

        if (mixed.size() == 1) {
            cleanGraph(nbv, graph, node);
        }
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

    private void removeAllSwitchesAndInternalConnections(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph, int originNode, Object edge) {
        Integer oppositeNode = getOppositeNode(graph, originNode, edge);
        removeSwitchOrInternalConnection(nbv, graph, edge);
        if (!isBusbarSection(nbv, oppositeNode)) {
            for (Object otherEdge : new ArrayList<>(graph.edgesOf(oppositeNode))) {
                removeAllSwitchesAndInternalConnections(nbv, graph, oppositeNode, otherEdge);
            }
        }
    }

    private static boolean isBusbarSection(VoltageLevel.NodeBreakerView nbv, Integer node) {
        Optional<Connectable<?>> c = nbv.getOptionalTerminal(node).map(Terminal::getConnectable);
        return c.isPresent() && c.get() instanceof BusbarSection;
    }

    private static void removeSwitchOrInternalConnection(VoltageLevel.NodeBreakerView nbv, Graph<Integer, Object> graph, Object edge) {
        if (edge instanceof Switch) {
            nbv.removeSwitch(((Switch) edge).getId());
        } else {
            Pair<Integer, Integer> ic = (Pair<Integer, Integer>) edge;
            nbv.removeInternalConnections(ic.getFirst(), ic.getSecond());
        }
        graph.removeEdge(edge);
    }

    private static Integer getOppositeNode(Graph<Integer, Object> graph, int node, Object e) {
        Integer edgeSource = graph.getEdgeSource(e);
        return edgeSource == node ? graph.getEdgeTarget(e) : edgeSource;
    }
}
