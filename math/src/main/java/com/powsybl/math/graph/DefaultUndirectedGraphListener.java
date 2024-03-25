/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
public class DefaultUndirectedGraphListener<V, E> implements UndirectedGraphListener<V, E> {

    @Override
    public void vertexAdded(int v) {
        // nothing to do
    }

    @Override
    public void vertexObjectSet(int v, V obj) {
        // nothing to do
    }

    @Override
    public void vertexRemoved(int v, V obj) {
        // nothing to do
    }

    @Override
    public void allVerticesRemoved() {
        // nothing to do
    }

    @Override
    public void edgeAdded(int e, E obj) {
        // nothing to do
    }

    @Override
    public void edgeRemoved(int e, E obj) {
        // nothing to do
    }

    @Override
    public void edgeBeforeRemoval(int e, E obj) {
        // nothing to do
    }

    @Override
    public void allEdgesBeforeRemoval(Collection<E> obj) {
        // nothing to do
    }

    @Override
    public void allEdgesRemoved(Collection<E> obj) {
        // nothing to do
    }
}
