/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph;

import java.util.Set;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface GraphModel<V, E> {

    void addEdge(V v1, V v2, E e);

    void removeEdge(E e);

    void addVertex(V v);

    void removeVertex(V v);

    boolean containsVertex(V vertex);

    boolean containsEdge(E edge);

    V getEdgeSource(E edge);

    V getEdgeTarget(E edge);

    Set<E> getNeighborEdgesOf(V v);
}
