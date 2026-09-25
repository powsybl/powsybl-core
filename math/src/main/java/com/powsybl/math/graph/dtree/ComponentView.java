/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph.dtree;

import com.powsybl.math.graph.Component;

import java.util.Iterator;
import java.util.Objects;

/**
 * A set view of a connected component in a graph. A component view is
 * represented by a single {@link DTNode} in a spanning tree. The nodes in this
 * tree are exactly the vertices of the connected component. A component view
 * is guaranteed to contain {@link #node} and every {@link DTNode} connected to
 * {@link #node}.
 *
 * @author Valentin Carrez {@literal <valentin.carrez at rte-france.com>}
 */
public class ComponentView<V, E> implements Component<V> {

    private final DTNode<V, E> node;

    ComponentView(DTNode<V, E> node) {
        this.node = node;
    }

    @Override
    public int getNum() {
        DTGraph<V, E> graph = node.getGraph();
        return graph.connectivity.getComponentNumber(node.getVertex());
    }

    @Override
    public Iterator<V> iterator() {
        return new DFSIterator<>(node.findRoot());
    }

    @Override
    public boolean contains(Object o) {
        if (o != null) {
            // node might not be the root anymore, so need to use findRoot on node.
            return node.getGraph().rootOf((V) o) == node.findRoot();
        }

        return false;
    }

    @Override
    public int size() {
        return node.findRoot().size();
    }

    void setIndex(int index) {
        node.setIndex(index);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComponentView<?, ?> that = (ComponentView<?, ?>) o;
        return Objects.equals(node.findRoot(), that.node.findRoot());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(node.findRoot());
    }
}
