/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.utils;

import org.jgrapht.graph.Pseudograph;

/**
 * @author Hugo Marcellin {@literal <hugo.marcelin at rte-france.com>}
 */
public class LineGraph<V, E> extends Pseudograph<V, E> {
    public LineGraph(Class<? extends E> edgeClass) {
        super(edgeClass);
    }

    public void addVerticesAndEdges(Iterable<V> vertices) {
        V previousVertex = null;
        for (V vertex : vertices) {
            if (!containsVertex(vertex)) {
                addVertex(vertex);
            }
            if (previousVertex != null) {
                addEdge(previousVertex, vertex);
            }
            previousVertex = vertex;
        }
    }
}

