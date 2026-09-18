/**
 * Copyright (c) 2022-2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph;

import com.powsybl.commons.PowsyblException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractGraphConnectivity<V, E, G extends GraphModel<V, E>> implements GraphConnectivity<V, E> {

    private final G graph;

    private final Deque<ModificationsContext<V, E>> modificationsContexts = new ArrayDeque<>();

    protected List<? extends Component<V>> componentSets;

    protected V defaultMainComponentVertex;

    protected abstract void updateConnectivity(EdgeRemove<V, E> edgeRemove);

    protected abstract void updateConnectivity(EdgeAdd<V, E> edgeAdd);

    protected abstract void updateConnectivity(VertexAdd<V, E> vertexAdd);

    protected abstract void resetConnectivity(Deque<GraphModification<V, E>> m);

    protected abstract void updateComponents();

    protected AbstractGraphConnectivity(G graph) {
        this.graph = Objects.requireNonNull(graph);
    }

    @Override
    public boolean addVertex(V vertex) {
        Objects.requireNonNull(vertex);
        if (graph.containsVertex(vertex)) {
            return false;
        }
        VertexAdd<V, E> vertexAdd = new VertexAdd<>(vertex);
        vertexAdd.apply(graph);
        if (!modificationsContexts.isEmpty()) {
            ModificationsContext<V, E> modificationsContext = modificationsContexts.peekLast();
            modificationsContext.add(vertexAdd);
            updateConnectivity(vertexAdd);
        }
        return true;
    }

    @Override
    public boolean removeVertex(V vertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addEdge(V vertex1, V vertex2, E edge) {
        Objects.requireNonNull(vertex1);
        Objects.requireNonNull(vertex2);
        Objects.requireNonNull(edge);
        if (graph.containsEdge(edge)) {
            return false;
        }
        EdgeAdd<V, E> edgeAdd = new EdgeAdd<>(vertex1, vertex2, edge);
        edgeAdd.apply(graph);
        if (!modificationsContexts.isEmpty()) {
            ModificationsContext<V, E> modificationsContext = modificationsContexts.peekLast();
            modificationsContext.add(edgeAdd);
            updateConnectivity(edgeAdd);
        }
        return true;
    }

    @Override
    public boolean removeEdge(E edge) {
        Objects.requireNonNull(edge);
        if (!graph.containsEdge(edge)) {
            return false;
        }
        V vertex1 = graph.getEdgeSource(edge);
        V vertex2 = graph.getEdgeTarget(edge);
        EdgeRemove<V, E> edgeRemove = new EdgeRemove<>(vertex1, vertex2, edge);
        edgeRemove.apply(graph);
        if (!modificationsContexts.isEmpty()) {
            ModificationsContext<V, E> modificationsContext = modificationsContexts.peekLast();
            modificationsContext.add(edgeRemove);
            updateConnectivity(edgeRemove);
        }
        return true;
    }

    @Override
    public void startTemporaryChanges(boolean computeComparisons) {
        ModificationsContext<V, E> modificationsContext = new ModificationsContext<>(computeComparisons, this::getVerticesNotInMainComponent, defaultMainComponentVertex);
        modificationsContexts.add(modificationsContext);
        modificationsContext.computeVerticesNotInMainComponentBefore();
    }

    @Override
    public void undoTemporaryChanges() {
        if (modificationsContexts.isEmpty()) {
            throw new PowsyblException("Cannot reset, no remaining saved connectivity");
        }
        ModificationsContext<V, E> m = modificationsContexts.pollLast();
        Deque<GraphModification<V, E>> modifications = m.getModifications();
        resetConnectivity(modifications);
        modifications.descendingIterator().forEachRemaining(gm -> gm.undo(graph));
    }

    public int getComponentNumber(V vertex) {
        checkSavedContext();
        checkVertex(vertex);
        updateComponents();
        return getQuickComponentNumber(vertex);
    }

    protected abstract int getQuickComponentNumber(V vertex);

    @Override
    public boolean connected(V v1, V v2) {
        checkSavedContext();
        checkVertex(v1);
        checkVertex(v2);
        updateComponents();
        return getQuickComponentNumber(v1) == getQuickComponentNumber(v2);
    }

    @Override
    public int getNbConnectedComponents() {
        checkSavedContext();
        updateComponents();
        return componentSets.size();
    }

    protected Collection<? extends Component<V>> getSmallComponents() {
        checkSavedContext();
        updateComponents();
        return componentSets.subList(1, componentSets.size());
    }

    @Override
    public Component<V> getConnectedComponent(V vertex) {
        int componentNumber = getComponentNumber(vertex);
        return componentSets.get(componentNumber);
    }

    @Override
    public Component<V> getLargestConnectedComponent() {
        checkSavedContext();
        updateComponents();
        return componentSets.getFirst();
    }

    public List<Component<V>> getConnectedComponents() {
        checkSavedContext();
        updateComponents();
        return Collections.unmodifiableList(componentSets);
    }

    protected Set<V> getNonConnectedVertices(V vertex) {
        Component<V> connectedComponent = getConnectedComponent(vertex);
        return componentSets.stream()
                .filter(component -> component != connectedComponent)
                .flatMap(Component::stream).collect(Collectors.toSet());
    }

    public G getGraph() {
        return graph;
    }

    protected Deque<ModificationsContext<V, E>> getModificationsContexts() {
        return modificationsContexts;
    }

    protected ModificationsContext<V, E> checkSavedContext() {
        if (modificationsContexts.isEmpty()) {
            throw new PowsyblException("Cannot compute connectivity without a saved state, please call GraphConnectivity::startTemporaryChanges at least once beforehand");
        }
        return modificationsContexts.peekLast();
    }

    protected void checkVertex(V vertex) {
        if (!graph.containsVertex(vertex)) {
            throw new IllegalArgumentException("given vertex " + vertex + " is not in the graph");
        }
    }

    @Override
    public Set<V> getVerticesAddedToMainComponent() {
        return checkSavedContext().getVerticesAddedToMainComponent();
    }

    @Override
    public Set<E> getEdgesAddedToMainComponent() {
        return checkSavedContext().getEdgesAddedToMainComponent(graph);
    }

    @Override
    public Set<V> getVerticesRemovedFromMainComponent() {
        return checkSavedContext().getVerticesRemovedFromMainComponent();
    }

    @Override
    public Set<E> getEdgesRemovedFromMainComponent() {
        return checkSavedContext().getEdgesRemovedFromMainComponent(graph);
    }

    protected Set<V> getVerticesNotInMainComponent(V mainComponentVertex) {
        if (mainComponentVertex != null) {
            return getNonConnectedVertices(mainComponentVertex);
        } else {
            return getSmallComponents().stream().flatMap(Component::stream).collect(Collectors.toSet());
        }
    }

    public void setMainComponentVertex(V mainComponentVertex) {
        if (!modificationsContexts.isEmpty()) {
            var modificationsContext = modificationsContexts.peekLast();
            modificationsContext.setMainComponentVertex(mainComponentVertex);
        }
        defaultMainComponentVertex = mainComponentVertex;
    }
}
