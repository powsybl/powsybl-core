/*
 * Copyright (c) 2017-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.util.fastutil;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.io.Serial;
import java.util.Arrays;

import static com.powsybl.commons.util.fastutil.FastUtilUtils.checkSizeForRemoval;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ExtendedIntArrayList extends IntArrayList {

    @Serial
    private static final long serialVersionUID = 1L;

    public ExtendedIntArrayList() {
    }

    public ExtendedIntArrayList(int capacity) {
        super(capacity);
    }

    public ExtendedIntArrayList(int capacity, int value) {
        super(capacity);
        size(capacity);
        Arrays.fill(this.elements(), value);
    }

    public ExtendedIntArrayList(int[] values) {
        super(values);
    }

    public ExtendedIntArrayList(ExtendedIntArrayList existingList) {
        super(existingList);
    }

    public int[] getData() {
        return this.elements();
    }

    public void fill(int fromIndex, int toIndex, int value) {
        Arrays.fill(this.elements(), fromIndex, toIndex, value);
    }

    /**
     * Ensures the underlying array of the list has the required capacity and fills it with a
     * specified value for the given length, starting from the current size of the list.
     *
     * @param length the number of elements to fill
     * @param value  the value to fill the specified range with
     */
    public void growAndFill(int length, int value) {
        growAndFill(size(), length, value);
    }

    /**
     * Ensures the underlying array has the required size and fills it with a specified value
     * from the given starting index for a specified length.
     *
     * @param fromIndex the starting index where the filling begins
     * @param length    the number of elements to fill
     * @param value     the value to fill the specified range with
     */
    public void growAndFill(int fromIndex, int length, int value) {
        // Last index to fill
        int toIndex = fromIndex + length;

        // Ensure the array has the required size
        if (toIndex > size()) {
            size(toIndex);
        }

        // Fill the array
        Arrays.fill(elements(), fromIndex, toIndex, value);
    }

    public int min() {
        if (size() == 0) {
            throw new IllegalStateException("Cannot find minimum of an empty list");
        }
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < size; i++) {
            if (a[i] < min) {
                min = a[i];
            }
        }
        return min;
    }

    /**
     * Removes the specified number of elements from the end of the list.
     * <br/>
     * <i>Warning: this method does not change the length of the underlying array.</i>
     *
     * @param numberOfElements the number of elements to be removed from the end of the list
     */
    public void removeElements(int numberOfElements) {
        checkSizeForRemoval(numberOfElements, size());
        removeElements(size() - numberOfElements, size());
    }
}
