/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.util.fastutil;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;

import java.io.Serial;
import java.util.Arrays;

import static com.powsybl.commons.util.fastutil.FastUtilUtils.checkSizeForRemoval;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class BooleanArrayListHack extends BooleanArrayList {

    @Serial
    private static final long serialVersionUID = 1L;

    public BooleanArrayListHack() {
    }

    public BooleanArrayListHack(int capacity) {
        super(capacity);
    }

    public BooleanArrayListHack(int capacity, boolean value) {
        super(capacity);
        size(capacity);
        Arrays.fill(this.elements(), value);
    }

    public BooleanArrayListHack(boolean[] values) {
        super(values);
    }

    public BooleanArrayListHack(BooleanArrayListHack existingList) {
        super(existingList);
    }

    public boolean[] getData() {
        return elements();
    }

    /**
     * Ensures the underlying array of the list has the required capacity and fills it with a
     * specified value for the given length, starting from the current size of the list.
     *
     * @param length the number of elements to fill
     * @param value  the value to fill the specified range with
     */
    public void growAndFill(int length, boolean value) {
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
    public void growAndFill(int fromIndex, int length, boolean value) {
        // Last index to fill
        int toIndex = fromIndex + length;

        // Ensure the array has the required size
        if (toIndex > size()) {
            size(toIndex);
        }

        // Fill the array
        Arrays.fill(elements(), fromIndex, toIndex, value);
    }

    /**
     * Fills a portion of the underlying boolean array with the specified value.
     *
     * @param fromIndex the starting index of the range (inclusive) to be filled
     * @param toIndex   the ending index of the range (exclusive) to be filled
     * @param value     the boolean value to fill the specified range with
     */
    public void fill(int fromIndex, int toIndex, boolean value) {
        Arrays.fill(elements(), fromIndex, toIndex, value);
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

    /**
     * Resets the state of the boolean array list by setting all elements in the list to {@code false}.
     * The method modifies all elements from the beginning of the array up to the current size of the list.
     */
    public void reset() {
        Arrays.fill(elements(), 0, this.size(), false);
    }
}
