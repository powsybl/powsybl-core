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
class IntArrayListHackTest {
    @Test
    void constructorTest() {
        IntArrayListHack list1 = new IntArrayListHack();
        assertTrue(list1.isEmpty());
        assertEquals(0, list1.getData().length);

        list1 = new IntArrayListHack(1);
        assertTrue(list1.isEmpty());
        assertEquals(1, list1.getData().length);

        list1 = new IntArrayListHack(1, 1);
        assertFalse(list1.isEmpty());
        assertEquals(1, list1.size());
        assertEquals(1, list1.getData().length);
        assertEquals(1, list1.getInt(0));

        list1 = new IntArrayListHack(new int[] {2});
        assertFalse(list1.isEmpty());
        assertEquals(1, list1.size());
        assertEquals(1, list1.getData().length);
        assertEquals(2, list1.getInt(0));

        IntArrayListHack list2 = new IntArrayListHack(list1);
        assertFalse(list2.isEmpty());
        assertEquals(1, list2.size());
        assertEquals(1, list2.getData().length);
        assertEquals(2, list2.getInt(0));
    }

    @Test
    void growAndFillTest() {
        // Initialize
        IntArrayListHack list = new IntArrayListHack(1, 1);
        assertEquals(1, list.size());
        assertEquals(1, list.getData().length);

        // Grow and fill from the last element
        list.growAndFill(2, 2);
        assertEquals(3, list.size());
        assertEquals(3, list.getData().length);
        assertEquals(new IntArrayListHack(new int[] {1, 2, 2}), list);

        // Grow and fill from a specific element
        list.growAndFill(2, 2, 3);
        assertEquals(4, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new IntArrayListHack(new int[] {1, 2, 3, 3}), list);

        // Grow and fill from a specific element, with already a size big enough
        list.growAndFill(2, 2, 4);
        assertEquals(4, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new IntArrayListHack(new int[] {1, 2, 4, 4}), list);
    }

    @Test
    void fillTest() {
        // Initialize
        IntArrayListHack list = new IntArrayListHack(new int[] {1, 2, 4, 4});

        // Fill
        list.fill(1, 3, 3);
        assertEquals(4, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new IntArrayListHack(new int[] {1, 3, 3, 4}), list);
    }

    @Test
    void removeElementsTest() {
        // Initialize
        IntArrayListHack list = new IntArrayListHack(new int[] {1, 2, 4, 4});

        // Remove some elements
        list.removeElements(2);
        assertEquals(2, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new IntArrayListHack(new int[] {1, 2}), list);

        // Remove 0 elements
        list.removeElements(0);
        assertEquals(2, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new IntArrayListHack(new int[] {1, 2}), list);

        // Try to remove more elements than available
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> list.removeElements(10));
        assertEquals("Cannot remove more elements than the list size: 10 > 2", exception.getMessage());

        // Try to remove a negative number of elements
        exception = assertThrows(IllegalArgumentException.class, () -> list.removeElements(-2));
        assertEquals("Cannot remove negative number of elements: -2", exception.getMessage());
    }

    @Test
    void minTest() {
        // Initialize
        IntArrayListHack list = new IntArrayListHack(new int[] {1, 2, 0, 4});

        // Get the min
        assertEquals(0, list.min());

        // Exception for an empty list
        list = new IntArrayListHack();
        IllegalStateException exception = assertThrows(IllegalStateException.class, list::min);
        assertEquals("Cannot find minimum of an empty list", exception.getMessage());
    }
}
