/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph;

import java.util.Collection;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface UndirectedGraphListener<V, E> {

    void vertexAdded(int v);

    void vertexObjectSet(int v, V obj);

    void vertexRemoved(int v, V obj);

    void allVerticesRemoved();

    void edgeAdded(int e, E obj);

    default void edgeBeforeRemoval(int e, E obj) {
        // Do nothing
    }

    void edgeRemoved(int e, E obj);

    void allEdgesBeforeRemoval(Collection<E> obj);

    default void allEdgesRemoved(Collection<E> obj) {
        // Do nothing
    }
}
