/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph.dtree;

import com.powsybl.commons.PowsyblException;
import com.powsybl.math.graph.*;

import java.util.*;

/**
 * D-Tree implementation from <cite>Qing Chen, Oded Lachish, Sven Helmer, and Michael H. Böhlen. Dynamic
 * Spanning Trees for Connectivity Queries on Fully-dynamic Undirected
 * Graphs. PVLDB, 15(11): 3263 - 3276, 2022.
 * doi:10.14778/3551793.3551868</cite>. An extended version is available at
 * <a href="https://arxiv.org/pdf/2207.06887">https://arxiv.org/pdf/2207.06887</a>
 *
 * <p>
 * This implementation differs from the paper in the following ways:
 * <ul>
 *     <li>Instead of searching the best replacement edge, we select the first one encountered.
 *     It increases the sum of distances but improves performance of {@link #removeEdge(Object)}.</li>
 *     <li>In queries, the centroid heuristic isn't used. It doesn't decrease the sum of distances that much
 *     and deteriorate performances.</li>
 *     <li>When inserting a non-tree edge whose endpoints have a depth difference delta >= 2, we disconnect
 *     the ancestor at distance delta / 2 − 1 from the deeper endpoint from its parent, rather than disconnecting
 *     the ancestor at distance delta − 2 as in the original algorithm. It improves the sum of distances and
 *     performances.</li>
 * </ul>
 *
 * The last two points were proposed by: <cite>Lantian Xu, Dong Wen, Lu Qin, Ronghua Li, Ying Zhang, and
 * Xuemin Lin. 2024. Constant-time Connectivity Querying in Dynamic Graphs. Proc. ACM Manag. Data 2, 6
 * (SIGMOD), Article 230 (December 2024), 23 pages. <a href="https://doi.org/10.1145/3698805">https://doi.org/10.1145/3698805</a></cite>.
 * </p>
 *
 * @author Valentin Carrez {@literal <valentin.carrez at rte-france.com>}
 */
public class DTreeGraphConnectivity<V, E> implements GraphConnectivity<V, E> {

    private final DTGraph<V, E> graph = new DTGraph<>(this);

    private final Deque<Modifications<V, E>> modificationsStack = new ArrayDeque<>();
    private V defaultMainComponentVertex;

    private List<Component<V>> components;

    @Override
    public boolean addVertex(V vertex) {
        Objects.requireNonNull(vertex);

        if (graph.addVertex(vertex)) {
            // keep track of modifications
            Modifications<V, E> modifications = modificationsStack.peek();

            if (modifications != null) {
                modifications.push(new VertexAdd<>(vertex));
            }

            components = null;
            return true;
        }

        return false;
    }

    @Override
    public boolean removeVertex(V vertex) {
        return false;
    }

    @Override
    public boolean addEdge(V vertex1, V vertex2, E edge) {
        Objects.requireNonNull(vertex1);
        Objects.requireNonNull(vertex2);
        Objects.requireNonNull(edge);

        if (graph.addEdge(vertex1, vertex2, edge)) {
            // keep track of modifications
            Modifications<V, E> modifications = modificationsStack.peek();

            if (modifications != null) {
                modifications.push(new EdgeAdd<>(vertex1, vertex2, edge));
            }

            components = null;
            return true;
        }
        return false;
    }

    @Override
    public boolean removeEdge(E edge) {
        Objects.requireNonNull(edge);

        Edge<V, E> e = graph.removeEdge(edge);
        if (e != null) {
            // keep track of modifications
            Modifications<V, E> modifications = modificationsStack.peek();

            if (modifications != null) {
                modifications.push(new EdgeRemove<>(e.nodeU().getVertex(), e.nodeV().getVertex(), e.edgeData()));
            }

            components = null;
            return true;
        }
        return false;
    }

    @Override
    public boolean connected(V v1, V v2) {
        DTNode<V, E> node1 = graph.rootOf(v1);
        DTNode<V, E> node2 = graph.rootOf(v2);
        return node1 != null && node1 == node2;
    }

    @Override
    public boolean supportTemporaryChangesNesting() {
        return true;
    }

    @Override
    public void startTemporaryChanges(boolean computeComparisons) {
        modificationsStack.push(new Modifications<>(graph, defaultMainComponentVertex, computeComparisons));
        graph.setCurrentModificationsContext(modificationsStack.peek());
    }

    @Override
    public void undoTemporaryChanges() {
        if (modificationsStack.isEmpty()) {
            throw new PowsyblException("Cannot reset, no remaining saved connectivity");
        }

        Modifications<V, E> modifications = modificationsStack.peek();
        graph.setCurrentModificationsContext(null);

        for (GraphModification<V, E> gm : modifications) {
            switch (gm) {
                case EdgeAdd<V, E> edgeAdd -> graph.removeEdge(edgeAdd.e());
                case EdgeRemove<V, E> edgeRemove -> graph.addEdge(edgeRemove.v1(), edgeRemove.v2(), edgeRemove.e());
                case VertexAdd<V, E> vertexAdd -> graph.removeVertex(vertexAdd.v());
                default -> throw new IllegalStateException("Unexpected value: " + gm);
            }
        }

        modificationsStack.pop();
        graph.setCurrentModificationsContext(modificationsStack.peek());
    }

    public int getComponentNumber(V vertex) {
        DTNode<V, E> root = graph.rootOf(vertex);
        if (root == null) {
            return -1;
        }

        updateComponents();
        return root.getIndex();
    }

    @Override
    public void setMainComponentVertex(V mainComponentVertex) {
        if (!modificationsStack.isEmpty()) {
            Modifications<V, E> modifications = modificationsStack.peek();
            modifications.setMainComponentVertex(mainComponentVertex);
        }
        defaultMainComponentVertex = mainComponentVertex;
    }

    @Override
    public int getNbConnectedComponents() {
        return graph.getNbConnectedComponent();
    }

    @Override
    public Component<V> getConnectedComponent(V vertex) {
        return graph.componentView(vertex);
    }

    @Override
    public Component<V> getLargestConnectedComponent() {
        if (components == null) {
            return getGraph().getBiggestRoot().componentView();
        } else {
            return components.getFirst();
        }
    }

    @Override
    public List<Component<V>> getConnectedComponents() {
        updateComponents();
        return components;
    }

    @Override
    public Set<V> getVerticesRemovedFromMainComponent() {
        return checkSavedContext().getVerticesRemovedFromMainComponent();
    }

    @Override
    public Set<E> getEdgesRemovedFromMainComponent() {
        return checkSavedContext().getEdgesRemovedFromMainComponent();
    }

    @Override
    public Set<V> getVerticesAddedToMainComponent() {
        return checkSavedContext().getVerticesAddedToMainComponent();
    }

    @Override
    public Set<E> getEdgesAddedToMainComponent() {
        return checkSavedContext().getEdgesAddedToMainComponent();
    }

    private void updateComponents() {
        if (components == null) {
            components = graph.allComponents();
        }
    }

    private Modifications<V, E> checkSavedContext() {
        if (modificationsStack.isEmpty()) {
            throw new PowsyblException("Cannot compute connectivity without a saved state, please call GraphConnectivity::startTemporaryChanges at least once beforehand");
        }
        return modificationsStack.peek();
    }

    public DTGraph<V, E> getGraph() {
        return graph;
    }
}
