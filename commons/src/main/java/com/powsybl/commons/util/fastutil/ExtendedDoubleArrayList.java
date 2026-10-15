/*
 * Copyright (c) 2017-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.util.fastutil;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.io.Serial;
import java.util.Arrays;

import static com.powsybl.commons.util.fastutil.FastUtilUtils.checkSizeForRemoval;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ExtendedDoubleArrayList extends DoubleArrayList {

    @Serial
    private static final long serialVersionUID = 1L;

    public ExtendedDoubleArrayList() {
    }

    public ExtendedDoubleArrayList(int capacity) {
        super(capacity);
    }

    public ExtendedDoubleArrayList(int capacity, double value) {
        super(capacity);
        size(capacity);
        Arrays.fill(this.elements(), value);
    }

    public ExtendedDoubleArrayList(double[] values) {
        super(values);
    }

    public ExtendedDoubleArrayList(ExtendedDoubleArrayList existingList) {
        super(existingList);
    }

    public double[] getData() {
        return this.a;
    }

    public void fill(int fromIndex, int toIndex, double value) {
        Arrays.fill(this.elements(), fromIndex, toIndex, value);
    }

    /**
     * Ensures the underlying array of the list has the required capacity and fills it with a
     * specified value for the given length, starting from the current size of the list.
     *
     * @param length the number of elements to fill
     * @param value the value to fill the specified range with
     */
    public void growAndFill(int length, double value) {
        growAndFill(size(), length, value);
    }

    /**
     * Ensures the underlying array has the required size and fills it with a specified value
     * from the given starting index for a specified length.
     *
     * @param fromIndex the starting index where the filling begins
     * @param length the number of elements to fill
     * @param value the value to fill the specified range with
     */
    public void growAndFill(int fromIndex, int length, double value) {
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

    public void reset() {
        Arrays.fill(this.a, 0, this.size(), 0d);
    }

    public void times(double scalar) {
        double[] data = a;
        for (int i = 0; i < size(); i++) {
            data[i] *= scalar;
        }
    }
}
