/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0f. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0f/.
 * SPDX-License-Identifier: MPL-2.0f
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
class FloatArrayListHackTest {
    @Test
    void constructorTest() {
        FloatArrayListHack list1 = new FloatArrayListHack();
        assertTrue(list1.isEmpty());
        assertEquals(0, list1.getData().length);

        list1 = new FloatArrayListHack(1);
        assertTrue(list1.isEmpty());
        assertEquals(1, list1.getData().length);

        list1 = new FloatArrayListHack(1, 1.0f);
        assertFalse(list1.isEmpty());
        assertEquals(1, list1.size());
        assertEquals(1, list1.getData().length);
        assertEquals(1.0f, list1.getFloat(0));

        list1 = new FloatArrayListHack(new float[] {2.0f});
        assertFalse(list1.isEmpty());
        assertEquals(1, list1.size());
        assertEquals(1, list1.getData().length);
        assertEquals(2.0f, list1.getFloat(0));

        FloatArrayListHack list2 = new FloatArrayListHack(list1);
        assertFalse(list2.isEmpty());
        assertEquals(1, list2.size());
        assertEquals(1, list2.getData().length);
        assertEquals(2.0f, list2.getFloat(0));
    }

    @Test
    void growAndFillTest() {
        // Initialize
        FloatArrayListHack list = new FloatArrayListHack(1, 1.0f);
        assertEquals(1, list.size());
        assertEquals(1, list.getData().length);

        // Grow and fill from the last element
        list.growAndFill(2, 2.0f);
        assertEquals(3, list.size());
        assertEquals(3, list.getData().length);
        assertEquals(new FloatArrayListHack(new float[] {1.0f, 2.0f, 2.0f}), list);

        // Grow and fill from a specific element
        list.growAndFill(2, 2, 3.0f);
        assertEquals(4, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new FloatArrayListHack(new float[] {1.0f, 2.0f, 3.0f, 3.0f}), list);

        // Grow and fill from a specific element, with already a size big enough
        list.growAndFill(2, 2, 4.0f);
        assertEquals(4, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new FloatArrayListHack(new float[] {1.0f, 2.0f, 4.0f, 4.0f}), list);
    }

    @Test
    void fillTest() {
        // Initialize
        FloatArrayListHack list = new FloatArrayListHack(new float[] {1.0f, 2.0f, 4.0f, 4.0f});

        // Fill
        list.fill(1, 3, 3.0f);
        assertEquals(4, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new FloatArrayListHack(new float[] {1.0f, 3.0f, 3.0f, 4.0f}), list);
    }

    @Test
    void removeElementsTest() {
        // Initialize
        FloatArrayListHack list = new FloatArrayListHack(new float[] {1.0f, 2.0f, 4.0f, 4.0f});

        // Remove some elements
        list.removeElements(2);
        assertEquals(2, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new FloatArrayListHack(new float[] {1.0f, 2.0f}), list);

        // Remove 0 elements
        list.removeElements(0);
        assertEquals(2, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new FloatArrayListHack(new float[] {1.0f, 2.0f}), list);

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
        FloatArrayListHack list = new FloatArrayListHack(new float[] {1.0f, 2.0f, 4.0f, 4.0f});

        // Reset the list
        list.reset();
        assertEquals(4, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new FloatArrayListHack(new float[] {0.0f, 0.0f, 0.0f, 0.0f}), list);
    }

    @Test
    void timesTest() {
        // Initialize
        FloatArrayListHack list = new FloatArrayListHack(new float[] {1.0f, 2.0f, 4.0f, 4.0f});

        // Multiply
        list.times(2);
        assertEquals(4, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new FloatArrayListHack(new float[] {2.0f, 4.0f, 8.0f, 8.0f}), list);
    }
}
