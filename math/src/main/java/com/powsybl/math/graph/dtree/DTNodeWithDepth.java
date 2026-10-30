/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph.dtree;

/**
 * A node with its depth in some unspecified spanning tree.
 *
 * @author Valentin Carrez {@literal <valentin.carrez at rte-france.com>}
 */
public record DTNodeWithDepth<V, E>(DTNode<V, E> node, int depth) {
}
