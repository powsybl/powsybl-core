/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BiList<E> {

    private final List<E> list = new ArrayList<>();

    private final TObjectIntMap<E> reverse = new TObjectIntHashMap<>();

    public int add(E e) {
        Objects.requireNonNull(e);
        int i = list.size();
        list.add(e);
        reverse.put(e, i);
        return i;
    }

    public E get(int index) {
        return list.get(index);
    }

    public int indexOf(E e) {
        Objects.requireNonNull(e);
        if (!reverse.containsKey(e)) {
            return -1;
        }
        return reverse.get(e);
    }

    int size() {
        return list.size();
    }

    void clear() {
        list.clear();
        reverse.clear();
    }
}
