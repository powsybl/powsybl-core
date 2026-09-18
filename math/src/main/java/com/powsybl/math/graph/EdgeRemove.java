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
public class EdgeRemove<V, E> extends AbstractEdgeModification<V, E> {

    public EdgeRemove(V vertex1, V vertex2, E e) {
        super(vertex1, vertex2, e);
    }

    @Override
    public void apply(GraphModel<V, E> graph) {
        graph.removeEdge(e);
    }

    @Override
    public void undo(GraphModel<V, E> graph) {
        graph.addEdge(v1, v2, e);
    }
}

