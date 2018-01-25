/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.graph;

/**
 * Directed graph interface
 *
 * Directed graph is composed of vertices connected by oriented edges.
 * Each vertex and edge can be associated to an object.
 *
 * @author Sebastien MURGEY <sebastien.murgey at rte-france.com>
 */
public interface DirectedGraph<V, E> extends Graph<V, E> {
    int getEdgeTail(int e);

    int getEdgeHead(int e);

    boolean isCyclic();
}
