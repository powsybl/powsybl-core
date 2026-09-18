/**
 * Copyright (c) 2022-2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class VertexAdd<V, E> implements GraphModification<V, E> {
    protected final V v;

    public VertexAdd(V vertex) {
        this.v = vertex;
    }

    @Override
    public void apply(GraphModel<V, E> graph) {
        graph.addVertex(v);
    }

    @Override
    public void undo(GraphModel<V, E> graph) {
        graph.removeVertex(v);
    }
}
