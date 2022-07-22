/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.*;

import org.jgrapht.alg.interfaces.SpanningTreeAlgorithm;
import org.jgrapht.alg.spanning.KruskalMinimumSpanningTree;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author Marcos De Miguel <demiguelm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class Z0FlowsCompletion {

    public Z0FlowsCompletion(Z0Checker z0checker, double distributeTolerance) {

        this.z0checker = Objects.requireNonNull(z0checker);
        this.distributeTolerance = distributeTolerance;
        this.tree = z0checker.getZ0Graph().vertexSet().isEmpty() ? null
            : new KruskalMinimumSpanningTree<>(z0checker.getZ0Graph()).getSpanningTree();
    }

    public void complete() {
        if (tree == null) {
            return;
        }
        Set<Z0Vertex> processed = new HashSet<>();

        z0checker.getZ0Graph().vertexSet().forEach(z0Vertex -> {
            if (processed.contains(z0Vertex)) {
                return;
            }
            Z0TreeByLevels treeByLevels = new Z0TreeByLevels(z0checker.getZ0Graph(), tree, z0Vertex, distributeTolerance);
            treeByLevels.completeFlows();
            processed.addAll(treeByLevels.getProcessedZ0Vertices());
        });

        // Zero to all flows outside tree
        z0checker.getZ0Graph().edgeSet().stream().filter(branch -> !tree.getEdges().contains(branch))
            .forEach(Z0Edge::assignZeroFlowTo);
    }

    private final Z0Checker z0checker;
    private final double distributeTolerance;
    private final SpanningTreeAlgorithm.SpanningTree<Z0Edge> tree;
}
