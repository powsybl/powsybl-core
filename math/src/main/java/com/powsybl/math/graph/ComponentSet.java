/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.graph;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Valentin Carrez {@literal <valentin.carrez at rte-france.com>}
 */
public record ComponentSet<V>(Set<V> set, int num) implements Component<V> {

    @Override
    public int getNum() {
        return num;
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean contains(V vertex) {
        return set.contains(vertex);
    }

    @Override
    public Iterator<V> iterator() {
        return set.iterator();
    }
}
