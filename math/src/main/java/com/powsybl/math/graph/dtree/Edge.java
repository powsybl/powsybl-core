/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph.dtree;

/**
 * An edge in a {@link DTGraph}.
 *
 * @author Valentin Carrez {@literal <valentin.carrez at rte-france.com>}
 */
public record Edge<V, E>(DTNode<V, E> nodeU, DTNode<V, E> nodeV, E edgeData) {

    public DTNode<V, E> opposite(DTNode<V, E> node) {
        if (nodeU == node) {
            return nodeV;
        } else {
            return nodeU;
        }
    }

    public boolean isTreeEdge() {
        return !nodeU.getNonTreeEdges().contains(this);
    }
}
