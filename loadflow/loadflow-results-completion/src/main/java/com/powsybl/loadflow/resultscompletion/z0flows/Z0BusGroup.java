/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.*;

import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm.SpanningTree;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Terminal;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class Z0BusGroup {

    public Z0BusGroup(Bus bus, Z0LineChecker z0checker) {
        this.seed = bus;
        this.z0checker = z0checker;
    }

    public boolean contains(Bus bus) {
        return buses.contains(bus);
    }

    public boolean valid() {
        return buses.size() > 1 || loops != null && !loops.isEmpty();
    }

    public void exploreZ0(Set<Bus> processed) {
        Objects.requireNonNull(processed);

        buses.add(seed);
        processed.add(seed);
        int k = 0;
        while (k < buses.size()) {
            Bus b = buses.get(k);
            b.getLineStream().forEach(line -> {
                Bus other = other(line, b);
                if (other != null && z0checker.isZ0(line)) {
                    if (b == other) {
                        addLoop(line);
                    } else {
                        addToGraph(line, b, other);
                        if (!buses.contains(other)) {
                            buses.add(other);
                            processed.add(other);
                        }
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
        if (loops != null) {
            assignZeroFlowToLoops();
        }
        if (graph != null) {
            computeTreeFromGraph();
            assignZeroFlowToEdgesOutsideTree();
            completeFlowsForEdgesInsideTree();
        }
    }

    private void computeTreeFromGraph() {
        Objects.requireNonNull(graph);
        tree = new KruskalMinimumSpanningTree<>(graph).getSpanningTree();
        levels = new ArrayList<>();
        parent = new HashMap<>();
        Set<Line> processed = new HashSet<>();
        Map<Bus, List<Z0Edge>> linesInBus = new HashMap<>();

        // Map to iterate once over the edges of the bus
        tree.getEdges().forEach(e -> {
            Bus b1 = e.getLine().getTerminal1().getBusView().getBus();
            Bus b2 = e.getLine().getTerminal2().getBusView().getBus();

            linesInBus.computeIfAbsent(b1, b -> new ArrayList<>()).add(e);
            linesInBus.computeIfAbsent(b2, b -> new ArrayList<>()).add(e);
        });

        // Add root level
        Bus root = buses.get(0);
        levels.add(new ArrayList<>(Collections.singleton(root)));

        // Build levels of the tree
        int level = 0;
        while (level < levels.size()) {
            List<Bus> nextLevel = new ArrayList<>();
            levels.get(level).forEach(bus -> linesInBus.get(bus).forEach(e -> {
                Bus other = other(e.getLine(), bus);
                if (other == null) {
                    return;
                }
                if (processed.contains(e.getLine())) {
                    return;
                }
                nextLevel.add(other);
                parent.put(other, e.getLine());
                processed.add(e.getLine());
            }));
            if (!nextLevel.isEmpty()) {
                levels.add(nextLevel);
            }
            level++;
        }
    }

    private void assignZeroFlowToEdgesOutsideTree() {
        Objects.requireNonNull(graph);
        graph.edgeSet().forEach(e -> {
            if (!tree.getEdges().contains(e)) {
                assignZeroFlowTo(e.getLine());
            }
        });
    }

    private void assignZeroFlowToLoops() {
        Objects.requireNonNull(loops);
        loops.forEach(this::assignZeroFlowTo);
    }

    private void assignZeroFlowTo(Line line) {
        Objects.requireNonNull(line);
        line.getTerminal1().setP(0.0);
        line.getTerminal1().setQ(0.0);
        line.getTerminal2().setP(0.0);
        line.getTerminal2().setQ(0.0);
        if (line.getB1() != 0.0 || line.getB2() != 0.0
                || line.getG1() != 0.0 || line.getG2() != 0.0) {
            LOG.error("Z0 line {} has B1, G1, B2, G2 != 0", line);
        }
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

    private static Bus other(Line line, Bus bus) {
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

    private void addLoop(Line line) {
        if (loops == null) {
            loops = new ArrayList<>();
        }
        loops.add(line);
    }

    private final Bus seed;
    private final Z0LineChecker z0checker;
    private final List<Bus> buses = new ArrayList<>();

    private SimpleWeightedGraph<Bus, Z0Edge> graph;
    private SpanningTree<Z0Edge> tree;
    private List<List<Bus>> levels;
    private Map<Bus, Line> parent;

    // Lines with same bus at both ends
    private List<Line> loops;

    private static final Logger LOG = LoggerFactory.getLogger(Z0BusGroup.class);
}
