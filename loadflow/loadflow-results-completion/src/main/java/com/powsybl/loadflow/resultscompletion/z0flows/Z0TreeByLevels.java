/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.*;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;

import com.powsybl.loadflow.resultscompletion.z0flows.ControlTerminals.ControlType;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class Z0TreeByLevels {

    Z0TreeByLevels(Graph<Z0Vertex, Z0Edge> graph, SpanningTreeAlgorithm.SpanningTree<Z0Edge> tree, Z0Vertex seed, double distributeTolerance) {
        this.graph = graph;
        this.tree = tree;
        this.seed = seed;
        this.distributeTolerance = distributeTolerance;

        createTreeByLevels();
    }

    List<Z0Vertex> getProcessedZ0Vertices() {
        return processedZ0Vertices;
    }

    void completeFlows() {
        balanceWithImpedance();
        resetDescendentZ0Flow();
        boolean done = distributeBalanceBetweenContinuousControls();
        if (done) {
            balanceWithImpedance(); // update the balance
        }
        completeFlowsForEdgesInsideTree();
    }

    private void createTreeByLevels() {

        // set the root
        levels.add(new ArrayList<>(Collections.singleton(seed)));
        processedZ0Vertices.add(seed);

        int level = 0;
        while (level < levels.size()) {
            List<Z0Vertex> nextLevel = new ArrayList<>();

            for (Z0Vertex vertex : levels.get(level)) {
                List<Z0Edge> childrenEdges = graph.edgesOf(vertex).stream()
                    .filter(edge -> tree.getEdges().contains(edge) && !isParentEdge(parent, vertex, edge))
                    .collect(Collectors.toList());

                childrenEdges.forEach(branch -> {
                    Z0Vertex otherBus = otherVertex(branch, vertex);
                    nextLevel.add(otherBus);
                    parent.put(otherBus, branch);
                });
            }
            if (!nextLevel.isEmpty()) {
                levels.add(nextLevel);
                processedZ0Vertices.addAll(nextLevel);
            }
            level++;
        }
    }

    private static boolean isParentEdge(Map<Z0Vertex, Z0Edge> parent, Z0Vertex vertex, Z0Edge edge) {
        return parent.containsKey(vertex) && parent.get(vertex).equals(edge);
    }

    private static Z0Vertex otherVertex(Z0Edge edge, Z0Vertex vertex) {
        return edge.getVertex1().equals(vertex) ? edge.getVertex2() : edge.getVertex1();
    }

    private void balanceWithImpedance() {
        processedZ0Vertices.forEach(z0Vertex -> z0Vertex.calculateBalanceWithImpedance(graph.edgesOf(z0Vertex)));
    }

    private void resetDescendentZ0Flow() {
        processedZ0Vertices.forEach(Z0Vertex::resetDescendentZ0Flow);
    }

    private boolean distributeBalanceBetweenContinuousControls() {
        double balanceP = 0.0;
        double balanceQ = 0.0;
        for (Z0Vertex vertex : processedZ0Vertices) {
            balanceP += vertex.getBalanceP();
            balanceQ += vertex.getBalanceQ();
        }
        boolean done = false;
        if (Math.abs(balanceP) >= distributeTolerance) {
            new ControlTerminals(processedZ0Vertices, ControlType.ACTIVE).distribute(balanceP);
            done = true;
        }
        if (Math.abs(balanceQ) >= distributeTolerance) {
            new ControlTerminals(processedZ0Vertices, ControlType.REACTIVE).distribute(balanceQ);
            done = true;
        }
        return done;
    }

    private void completeFlowsForEdgesInsideTree() {
        // Traverse the tree from leaves to root
        // (The root itself does not need to be processed)
        int level = levels.size() - 1;

        // Flow coming from z0 links has already been reset
        // go up from the bottom level completing the flow of the z0 links
        while (level >= 1) {
            levels.get(level).forEach(z0Vertex -> {
                Z0Edge z0Edge = parent.get(z0Vertex);
                double pBalance = z0Vertex.getBalanceP();
                double qBalance = z0Vertex.getBalanceQ();

                z0Edge.assignFlowTo(z0Vertex, -pBalance, -qBalance);
                z0Edge.assignFlowTo(z0Edge.otherZ0Vertex(z0Vertex), pBalance, qBalance);
                z0Edge.otherZ0Vertex(z0Vertex).addDescendentZ0Flow(pBalance, qBalance);
            });
            level--;
        }
    }

    private final Z0Vertex seed;
    private final Graph<Z0Vertex, Z0Edge> graph;
    private final SpanningTreeAlgorithm.SpanningTree<Z0Edge> tree;
    private final double distributeTolerance;

    private final List<List<Z0Vertex>> levels = new ArrayList<>();
    private final Map<Z0Vertex, Z0Edge> parent = new HashMap<>();
    private final List<Z0Vertex> processedZ0Vertices = new ArrayList<>();
}
