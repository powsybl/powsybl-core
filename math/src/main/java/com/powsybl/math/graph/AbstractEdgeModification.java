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
public abstract class AbstractEdgeModification<V, E> implements GraphModification<V, E> {
    protected final E e;
    protected final V v1;
    protected final V v2;

    protected AbstractEdgeModification(V vertex1, V vertex2, E e) {
        this.v1 = vertex1;
        this.v2 = vertex2;
        this.e = e;
    }

    public E e() {
        return e;
    }

    public V v1() {
        return v1;
    }

    public V v2() {
        return v2;
    }
}

