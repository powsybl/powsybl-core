/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph.dtree;

import com.powsybl.commons.PowsyblException;
import com.powsybl.math.graph.GraphConnectivity;
import com.powsybl.math.graph.GraphModification;
import com.powsybl.math.graph.dtree.StateMap.State;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Set;

/**
 * Contains modifications performed on the {@link DTGraph} between
 * the last call to {@link GraphConnectivity#startTemporaryChanges}
 * and the current instant. It stores a stack of {@link GraphModification}
 * and optionally the set of vertices and edges added to the
 * main component or removed from it.
 *
 * <p>
 * Topological comparisons are always computed relative to some main component
 * vertex, even when the user didn't specify one. When an edge is added in (or
 * removed from) a {@link DTGraph}, a DFS is performed to compute every vertex
 * that are now connected to (disconnected from) the main component vertex. It
 * is the responsibility of {@link DTGraph} to call the appropriate methods from
 * this class.
 * </p>
 *
 * <p>
 * Specifying a main component vertex is optional. In this case the main component
 * vertex is a vertex in the biggest component and is considered as fictitious.
 * It may lose its fictitious status when {@link #setMainComponentVertex(Object)}
 * is called. Over the time, this vertex may automatically change to another one
 * as the biggest main component can change.
 * </p>
 *
 * @see StateMap
 */
public class Modifications<V, E> implements Iterable<GraphModification<V, E>> {

    private final DTGraph<V, E> graph;

    private final Deque<GraphModification<V, E>> modificationsStack = new ArrayDeque<>();
    private final StateMap<V> verticesState;
    private final StateMap<E> edgesState;

    // true when the user didn't set the main component vertex
    // in this case, we set the main component vertex as a node
    // in the biggest component to avoid mainComponentVertex being
    // null and keep this class functional. However, it has an
    // impact on how edges/vertices removed from/added to are computed
    private boolean isMainComponentVertexFictitious;
    private DTNode<V, E> mainComponentNode;

    Modifications(DTGraph<V, E> graph, V mainComponentVertex, boolean computeComparisons) {
        this.graph = graph;

        if (mainComponentVertex == null) {
            mainComponentNode = graph.getBiggestRoot();
            isMainComponentVertexFictitious = true;
        } else {
            mainComponentNode = graph.getNodeOrThrow(mainComponentVertex);
            isMainComponentVertexFictitious = false;
        }

        if (computeComparisons) {
            verticesState = new StateMap<>();
            edgesState = new StateMap<>();
        } else {
            verticesState = null;
            edgesState = null;
        }
    }

    public void push(GraphModification<V, E> modification) {
        modificationsStack.push(modification);
    }

    /**
     * Called before a new edge is inserted in a component, in other words, inserting
     * {@code edge} won't merge two components. If the edge is inside the main component,
     * it is marked as added to the main component.
     *
     * @param treeRoot the root of the tree containing both endpoint of {@code edge}
     * @param edge the edge that is to be inserted
     */
    public void beforeInsertingEdgeInComponent(DTNode<V, E> treeRoot, Edge<V, E> edge) {
        if (topologicalComparisonsDisabled()) {
            return;
        }

        if (isInMainComponent(treeRoot)) {
            edgesState.markAsAdded(edge.edgeData());
        }
    }

    /**
     * Called before merging two trees using {@code edge}.
     * If one of the two endpoints is in the main component, then all vertices and edges
     * from the other tree are marked as added to the main component. {@code edge} is also
     * marked as added.
     *
     * @param rootU root of the tree containing {@code edge.nodeU()}
     * @param rootV root of the tree containing {@code edge.nodeV()}
     * @param edge the edge that will link the two trees
     */
    public void beforeInsertingTreeEdge(DTNode<V, E> rootU, DTNode<V, E> rootV, Edge<V, E> edge) {
        if (topologicalComparisonsDisabled()) {
            return;
        }

        if (isInMainComponent(rootV)) {
            edgesState.markAsAdded(edge.edgeData());
            markAll(rootU, State.ADDED);
        } else if (isInMainComponent(rootU)) {
            edgesState.markAsAdded(edge.edgeData());
            markAll(rootV, State.ADDED);
        }
    }

    /**
     * Called after merging two trees.
     * If the main component vertex is fictitious, that is, the user
     * didn't specify a main component vertex, then the main component
     * may change. In particular, {@code mergedTree} is a new candidate
     * to become the main component.
     *
     * @param mergedTree the tree resulting from a merge
     */
    public void afterInsertingTreeEdge(DTNode<V, E> mergedTree) {
        if (topologicalComparisonsDisabled()) {
            return;
        }

        if (isMainComponentVertexFictitious) {
            maybeBiggestTreeChanged(mergedTree);
        }
    }

    /**
     * Called after removing an edge that didn't create two components.
     * If the edge was inside the main component, it is marked as removed from the main component.
     *
     * @param treeRoot the root of the tree containing the endpoint of {@code edge}
     * @param edge the edge that was removed
     */
    public void afterRemovingNonBreakingConnectivityEdge(DTNode<V, E> treeRoot, Edge<V, E> edge) {
        if (topologicalComparisonsDisabled()) {
            return;
        }

        if (isInMainComponent(treeRoot)) {
            edgesState.markAsRemoved(edge.edgeData());
        }
    }

    /**
     * Called after removing an edge that split a tree in two.
     * If one of the two trees contains the main component vertex
     * then all vertices and edges of the other one are marked as removed.
     * {@code edge} is also marked as removed.
     *
     * <p>
     * If the main component vertex is fictitious, that is, the user
     * didn't specify a main component vertex, then the main component
     * may change. In particular, there might be a new biggest component
     * that need to be calculated.
     * </p>
     *
     * @param smallRoot the root of one of the two created tree.
     *                  Its size is assumed to be smaller than largeRoot's size.
     * @param largeRoot the root of the other created tree.
     *                  Its size is assumed to be greater than smallRoot's size.
     * @param edge the edge that was removed
     */
    public void afterRemovingEdgeBreakingConnectivity(DTNode<V, E> smallRoot, DTNode<V, E> largeRoot, Edge<V, E> edge) {
        if (topologicalComparisonsDisabled()) {
            return;
        }

        // compute the main component root once to answer two connectivity queries faster.
        DTNode<V, E> mainComponentRoot = mainComponentNode.findRoot();
        boolean smallInMainComponent = mainComponentRoot == smallRoot;
        boolean largeInMainComponent = mainComponentRoot == largeRoot;

        if (smallInMainComponent || largeInMainComponent) {
            edgesState.markAsRemoved(edge.edgeData());
        }

        if (largeInMainComponent) {
            markAll(smallRoot, State.REMOVED);
        } else if (smallInMainComponent) {
            markAll(largeRoot, State.REMOVED);
        }

        // handle unspecified main component vertex
        if (isMainComponentVertexFictitious) {
            maybeBiggestTreeChanged(graph.getBiggestRoot());
        }
    }

    /**
     * Change the main component vertex to the specified one.
     * If the new main component vertex isn't in the actual main component,
     * we need to mark every element in the new main component as added
     * and every element in the old main component as removed.
     *
     * @param mainComponentVertex new vertex identifying the main component.
     */
    public void setMainComponentVertex(V mainComponentVertex) {
        if (topologicalComparisonsDisabled()) {
            return;
        }

        if (this.mainComponentNode.getVertex() != mainComponentVertex) {
            // two things to do:
            // 1. check if the new main component vertex was in the main component before temporary changes.
            // 2. if the main component vertex isn't in the current main component, we need to
            //    update state of edges and vertices

            DTNode<V, E> oldComponentRoot = this.mainComponentNode.findRoot();
            DTNode<V, E> newMainComponentNode = graph.getNodeOrThrow(mainComponentVertex);
            DTNode<V, E> newComponentRoot = newMainComponentNode.findRoot();

            if (oldComponentRoot != newComponentRoot) {
                // the new main component vertex isn't in the current main component.
                // But that doesn't mean it wasn't in the main component before starting temporary changes,
                // it may have been removed.
                if (verticesState.getState(mainComponentVertex) != State.REMOVED) {
                    throw new PowsyblException("Cannot take the given vertex as main component vertex! This vertex was outside the main component before starting temporary changes");
                }

                // last thing to do is update state of vertices and edges in the two trees.
                markAll(oldComponentRoot, State.REMOVED);
                markAll(newComponentRoot, State.ADDED);
            }

            this.mainComponentNode = newMainComponentNode;
        }

        isMainComponentVertexFictitious = false;
    }

    /**
     * A node is in the main component if it is in the same tree as the main component vertex.
     *
     * @param node the node to test if it is in the main component
     * @return {@code true} if {@code node} is in the main component
     */
    private boolean isInMainComponent(DTNode<V, E> node) {
        return mainComponentNode.findRoot() == node.findRoot();
    }

    /**
     * Update the state of every vertex and edge in the tree rooted at {@code root}.
     *
     * @param root root of the tree whose elements' state will be updated.
     * @param newState the state in which elements are relative to the main component.
     */
    private void markAll(DTNode<V, E> root, State newState) {
        for (DFSIterator<V, E> it = new DFSIterator<>(root); it.hasNext();) {
            V vertex = it.next();
            verticesState.updateState(vertex, newState);

            DTNode<V, E> node = it.node();
            if (node.getParentEdge() != null) {
                edgesState.updateState(node.getParentEdge().edgeData(), newState);
            }

            for (Edge<V, E> nte : node.getNonTreeEdges()) {
                if (nte.nodeU() == it.node()) { // only if current node is edge source
                    edgesState.updateState(nte.edgeData(), newState);
                }
            }

            // we don't mark child tree edges as removed
            // because for each child tree edge, there is a parentEdge
            // so if we mark a parent edge as removed, we also mark
            // the corresponding child tree edge as removed
        }
    }

    /**
     * Potentially update the main component if the main component node
     * isn't in the biggest tree.
     *
     * @param currentBiggestRoot the root of the biggest tree
     */
    private void maybeBiggestTreeChanged(DTNode<V, E> currentBiggestRoot) {
        DTNode<V, E> mainComponentVertexTree = mainComponentNode.findRoot();
        if (currentBiggestRoot.size() > mainComponentVertexTree.size()) {
            // there is a new biggest main component
            markAll(mainComponentVertexTree, State.REMOVED);
            markAll(currentBiggestRoot, State.ADDED);
            mainComponentNode = currentBiggestRoot;
        }
    }

    public Set<V> getVerticesRemovedFromMainComponent() {
        if (topologicalComparisonsDisabled()) {
            throw new PowsyblException("Topological comparisons are disabled for the current temporary changes context!");
        }
        return verticesState.getRemoved();
    }

    public Set<E> getEdgesRemovedFromMainComponent() {
        if (topologicalComparisonsDisabled()) {
            throw new PowsyblException("Topological comparisons are disabled for the current temporary changes context!");
        }
        return edgesState.getRemoved();
    }

    public Set<V> getVerticesAddedToMainComponent() {
        if (topologicalComparisonsDisabled()) {
            throw new PowsyblException("Topological comparisons are disabled for the current temporary changes context!");
        }
        return verticesState.getAdded();
    }

    public Set<E> getEdgesAddedToMainComponent() {
        if (topologicalComparisonsDisabled()) {
            throw new PowsyblException("Topological comparisons are disabled for the current temporary changes context!");
        }
        return edgesState.getAdded();
    }

    private boolean topologicalComparisonsDisabled() {
        return verticesState == null || edgesState == null;
    }

    @Override
    public Iterator<GraphModification<V, E>> iterator() {
        return modificationsStack.iterator();
    }
}
