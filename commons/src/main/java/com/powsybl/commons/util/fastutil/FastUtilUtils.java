/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.util.fastutil;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntPredicate;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class FastUtilUtils {
    private FastUtilUtils() {
        // Empty constructor to prevent instantiation
    }

    /**
     * Returns a new list containing only the elements matching the predicate.
     * @param list the original IntArrayList
     * @param predicate IntPredicate used to filter the list
     * @return the filtered IntArrayList
     */
    public static IntArrayList grep(IntArrayList list, IntPredicate predicate) {
        IntArrayList result = new IntArrayList();
        for (int i = 0; i < list.size(); i++) {
            int v = list.getInt(i);
            if (predicate.test(v)) {
                result.add(v);
            }
        }
        return result;
    }

    static void checkSizeForRemoval(int numberOfElements, int size) {
        if (numberOfElements == 0) {
            return;
        }
        if (numberOfElements < 0) {
            throw new IllegalArgumentException("Cannot remove negative number of elements: " + numberOfElements);
        }
        if (numberOfElements > size) {
            throw new IllegalArgumentException("Cannot remove more elements than the list size: " + numberOfElements + " > " + size);
        }
    }
}
