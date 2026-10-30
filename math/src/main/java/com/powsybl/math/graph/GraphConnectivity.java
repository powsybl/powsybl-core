/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph;

import java.util.List;
import java.util.Set;

/**
 * @author Valentin Carrez {@literal <valentin.carrez at rte-france.com>}
 */
public interface GraphConnectivity<V, E> {

    boolean addVertex(V vertex);

    boolean removeVertex(V vertex);

    boolean addEdge(V src, V dest, E edge);

    boolean removeEdge(E edge);

    boolean connected(V v1, V v2);

    /**
     * Return the number of connected components
     */
    int getNbConnectedComponents();

    /**
     * Return the connected component set of given vertex
     */
    Component<V> getConnectedComponent(V vertex);

    /**
     * Return the largest connected component
     */
    Component<V> getLargestConnectedComponent();

    List<Component<V>> getConnectedComponents();

    boolean supportTemporaryChangesNesting();

    /**
     * Start recording topological changes to undo them later by a {@link #undoTemporaryChanges} call.
     * Comparisons will be computed.
     *
     * @see #startTemporaryChanges(boolean)
     */
    default void startTemporaryChanges() {
        startTemporaryChanges(true);
    }

    /**
     * Start recording topological changes to undo them later by a {@link #undoTemporaryChanges} call.
     * When {@code computeComparisons} is {@code true}, it becomes possible to call the following methods:
     * <ul>
     *     <li>{@link #getVerticesRemovedFromMainComponent()}</li>
     *     <li>{@link #getEdgesRemovedFromMainComponent()}</li>
     *     <li>{@link #getVerticesAddedToMainComponent()}</li>
     *     <li>{@link #getEdgesAddedToMainComponent()}</li>
     * </ul>
     * When these methods aren't needed, it is advised to set {@code computeComparisons} to {@code false}
     * for performance reasons.
     *
     * @param computeComparisons {@code true} to enable computation of comparisons.
     */
    void startTemporaryChanges(boolean computeComparisons);

    /**
     * Undo all the connectivity changes (possibly none) since last call to {@link #startTemporaryChanges}.
     */
    void undoTemporaryChanges();

    /**
     * Set the main component with given vertex.
     * The connected component relative to this vertex is considered as being the main component.
     * If not set, the main component is considered to be the biggest component.
     * This main component cannot be changed if any temporary changes are ongoing.
     * @param mainComponentVertex vertex defining main component
     */
    void setMainComponentVertex(V mainComponentVertex);

    /**
     * Return the vertices which were removed from main component by last temporary changes.
     * The main component is set by calling setMainComponentVertex, or if not set it is the biggest connected component.
     */
    Set<V> getVerticesRemovedFromMainComponent();

    /**
     * Return the edges which were removed from main component by last temporary changes.
     * The main component is set by calling setMainComponentVertex, or if not set it is the biggest connected component.
     */
    Set<E> getEdgesRemovedFromMainComponent();

    /**
     * Return the vertices which were added to main component by last temporary changes.
     * The main component is set by calling setMainComponentVertex, or if not set it is the biggest connected component.
     */
    Set<V> getVerticesAddedToMainComponent();

    /**
     * Return the edges which were added to main component by last temporary changes.
     * The main component is set by calling setMainComponentVertex, or if not set it is the biggest connected component.
     */
    Set<E> getEdgesAddedToMainComponent();
}
