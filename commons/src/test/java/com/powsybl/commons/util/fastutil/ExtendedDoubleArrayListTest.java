/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.util.fastutil;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class ExtendedDoubleArrayListTest {
    @Test
    void constructorTest() {
        ExtendedDoubleArrayList list1 = new ExtendedDoubleArrayList();
        assertTrue(list1.isEmpty());
        assertEquals(0, list1.getData().length);

        list1 = new ExtendedDoubleArrayList(1);
        assertTrue(list1.isEmpty());
        assertEquals(1, list1.getData().length);

        list1 = new ExtendedDoubleArrayList(1, 1.0);
        assertFalse(list1.isEmpty());
        assertEquals(1, list1.size());
        assertEquals(1, list1.getData().length);
        assertEquals(1.0, list1.getDouble(0));

        list1 = new ExtendedDoubleArrayList(new double[] {2.0});
        assertFalse(list1.isEmpty());
        assertEquals(1, list1.size());
        assertEquals(1, list1.getData().length);
        assertEquals(2.0, list1.getDouble(0));

        ExtendedDoubleArrayList list2 = new ExtendedDoubleArrayList(list1);
        assertFalse(list2.isEmpty());
        assertEquals(1, list2.size());
        assertEquals(1, list2.getData().length);
        assertEquals(2.0, list2.getDouble(0));
    }

    @Test
    void growAndFillTest() {
        // Initialize
        ExtendedDoubleArrayList list = new ExtendedDoubleArrayList(1, 1.0);
        assertEquals(1, list.size());
        assertEquals(1, list.getData().length);

        // Grow and fill from the last element
        list.growAndFill(2, 2.0);
        assertEquals(3, list.size());
        assertEquals(3, list.getData().length);
        assertEquals(new ExtendedDoubleArrayList(new double[] {1.0, 2.0, 2.0}), list);

        // Grow and fill from a specific element
        list.growAndFill(2, 2, 3.0);
        assertEquals(4, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new ExtendedDoubleArrayList(new double[] {1.0, 2.0, 3.0, 3.0}), list);

        // Grow and fill from a specific element, with already a size big enough
        list.growAndFill(2, 2, 4.0);
        assertEquals(4, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new ExtendedDoubleArrayList(new double[] {1.0, 2.0, 4.0, 4.0}), list);
    }

    @Test
    void fillTest() {
        // Initialize
        ExtendedDoubleArrayList list = new ExtendedDoubleArrayList(new double[] {1.0, 2.0, 4.0, 4.0});

        // Fill
        list.fill(1, 3, 3.0);
        assertEquals(4, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new ExtendedDoubleArrayList(new double[] {1.0, 3.0, 3.0, 4.0}), list);
    }

    @Test
    void removeElementsTest() {
        // Initialize
        ExtendedDoubleArrayList list = new ExtendedDoubleArrayList(new double[] {1.0, 2.0, 4.0, 4.0});

        // Remove some elements
        list.removeElements(2);
        assertEquals(2, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new ExtendedDoubleArrayList(new double[] {1.0, 2.0}), list);

        // Remove 0 elements
        list.removeElements(0);
        assertEquals(2, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new ExtendedDoubleArrayList(new double[] {1.0, 2.0}), list);

        // Try to remove more elements than available
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> list.removeElements(10));
        assertEquals("Cannot remove more elements than the list size: 10 > 2", exception.getMessage());

        // Try to remove a negative number of elements
        exception = assertThrows(IllegalArgumentException.class, () -> list.removeElements(-2));
        assertEquals("Cannot remove negative number of elements: -2", exception.getMessage());
    }

    @Test
    void resetTest() {
        // Initialize
        ExtendedDoubleArrayList list = new ExtendedDoubleArrayList(new double[] {1.0, 2.0, 4.0, 4.0});

        // Reset the list
        list.reset();
        assertEquals(4, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new ExtendedDoubleArrayList(new double[] {0.0, 0.0, 0.0, 0.0}), list);
    }

    @Test
    void timesTest() {
        // Initialize
        ExtendedDoubleArrayList list = new ExtendedDoubleArrayList(new double[] {1.0, 2.0, 4.0, 4.0});

        // Multiply
        list.times(2);
        assertEquals(4, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new ExtendedDoubleArrayList(new double[] {2.0, 4.0, 8.0, 8.0}), list);
    }
}
