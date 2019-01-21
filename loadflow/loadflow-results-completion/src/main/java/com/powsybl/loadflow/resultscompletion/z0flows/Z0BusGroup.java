/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm.SpanningTree;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Terminal;

public class Z0BusGroup {

    public Z0BusGroup(Bus bus) {
        this.seed = bus;
        buses = new ArrayList<>();
    }

    public boolean contains(Bus bus) {
        return buses.contains(bus);
    }

    public boolean valid() {
        return buses.size() > 1;
    }

    public void exploreZ0() {
        buses.add(seed);
        int k = 0;
        while (k < buses.size()) {
            Bus b = buses.get(k);
            b.getLineStream().forEach(line -> {
                Bus other = other(line, b);
                if (other != null && isZ0(line, b, other)) {
                    addToGraph(line, b, other);
                    if (!buses.contains(other)) {
                        buses.add(other);
                    }
                }
            });
            k++;
        }
    }

    public void complete() {
        if (!valid()) {
            LOG.warn("Z0 flow group not valid for seed bus {}", seed);
            return;
        }
        computeTreeFromGraph();
        assignZeroFlowToEdgesOutsideTree();
        completeFlowsForEdgesInsideTree();
    }

    private void computeTreeFromGraph() {
        tree = new KruskalMinimumSpanningTree<>(graph).getSpanningTree();
        levels = new ArrayList<>();
        parent = new HashMap<>();

        // Add root level
        Bus root = buses.get(0);
        levels.add(new ArrayList<>(Arrays.asList(root)));

        // Build levels of the tree
        int level = 0;
        while (level < levels.size()) {
            List<Bus> nextLevel = new ArrayList<>();
            // FIXME(Luma) Iterating over all edges of the tree for each bus
            levels.get(level).forEach(bus -> tree.getEdges().forEach(e -> {
                Bus other = other(e.getLine(), bus);
                if (other == null) {
                    return;
                }
                // FIXME(Luma) containsValue is O(n) in a HashMap
                if (parent.containsValue(e.getLine())) {
                    return;
                }
                nextLevel.add(other);
                parent.put(other, e.getLine());
            }));
            if (!nextLevel.isEmpty()) {
                levels.add(nextLevel);
            }
            level++;
        }
    }

    private void assignZeroFlowToEdgesOutsideTree() {
        graph.edgeSet().forEach(e -> {
            if (!tree.getEdges().contains(e)) {
                Line line = e.getLine();
                line.getTerminal1().setP(0.0);
                line.getTerminal1().setQ(0.0);
                line.getTerminal2().setP(0.0);
                line.getTerminal2().setQ(0.0);
                if (line.getB1() != 0.0 || line.getB2() != 0.0 || line.getG1() != 0.0 || line.getG2() != 0.0) {
                    LOG.error("Z0 line {} has B1, G1, B2, G2 != 0", line);
                }
            }
        });
    }

    private void completeFlowsForEdgesInsideTree() {
        // Traverse the tree from leaves to root
        // (The root itself does not need to be processed)
        int level = levels.size() - 1;
        while (level >= 1) {
            levels.get(level).forEach(bus -> {
                Line line = parent.get(bus);
                new Z0FlowFromBusBalance(bus, line).complete();
            });
            level--;
        }
    }

    // FIXME(Luma) accept the Z0 criteria as a parameter to Z0FlowsCompletion
    private static boolean isZ0(Line line, Bus b1, Bus b2) {
        if (LOG.isTraceEnabled()) {
            LOG.trace(String.format("isZ0 %s; bus1 %6.2f, %6.2f; bus2 %6.2f, %6.2f",
                    line,
                    b1.getV(), b1.getAngle(),
                    b2.getV(), b2.getAngle()));
        }
        return b1.getV() == b2.getV() && b1.getAngle() == b2.getAngle();
    }

    private Bus other(Line line, Bus bus) {
        Terminal t = BranchTerminal.ofOtherBus(line, bus);
        if (t == null) {
            return null;
        }
        return t.getBusView().getBus();
    }

    private void addToGraph(Line line, Bus b1, Bus b2) {
        if (graph == null) {
            // Lazy creation of graph
            graph = new SimpleWeightedGraph<>(Z0Edge.class);
        }
        graph.addVertex(b1);
        graph.addVertex(b2);
        graph.addEdge(b1, b2, new Z0Edge(line));
    }

    private final Bus seed;
    private final List<Bus> buses;
    private SimpleWeightedGraph<Bus, Z0Edge> graph;
    private SpanningTree<Z0Edge> tree;
    private List<List<Bus>> levels;
    private Map<Bus, Line> parent;

    private static final Logger LOG = LoggerFactory.getLogger(Z0BusGroup.class);
}
