/**
 * Copyright (c) 2022-2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph;

import java.util.Objects;
import java.util.function.ToIntFunction;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NaiveGraphConnectivityFactory<V, E> implements GraphConnectivityFactory<V, E> {

    private final ToIntFunction<V> numGetter;

    public NaiveGraphConnectivityFactory(ToIntFunction<V> numGetter) {
        this.numGetter = Objects.requireNonNull(numGetter);
    }

    @Override
    public GraphConnectivity<V, E> create() {
        return new NaiveGraphConnectivity<>(numGetter);
    }
}
