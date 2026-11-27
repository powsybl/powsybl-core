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
class ExtendedBooleanArrayListTest {

    @Test
    void constructorTest() {
        ExtendedBooleanArrayList list1 = new ExtendedBooleanArrayList();
        assertTrue(list1.isEmpty());
        assertEquals(0, list1.getData().length);

        list1 = new ExtendedBooleanArrayList(1);
        assertTrue(list1.isEmpty());
        assertEquals(1, list1.getData().length);

        list1 = new ExtendedBooleanArrayList(1, true);
        assertFalse(list1.isEmpty());
        assertEquals(1, list1.size());
        assertEquals(1, list1.getData().length);
        assertTrue(list1.getBoolean(0));

        list1 = new ExtendedBooleanArrayList(new boolean[] {true});
        assertFalse(list1.isEmpty());
        assertEquals(1, list1.size());
        assertEquals(1, list1.getData().length);
        assertTrue(list1.getBoolean(0));

        ExtendedBooleanArrayList list2 = new ExtendedBooleanArrayList(list1);
        assertFalse(list2.isEmpty());
        assertEquals(1, list2.size());
        assertEquals(1, list2.getData().length);
        assertTrue(list2.getBoolean(0));
    }

    @Test
    void growAndFillTest() {
        // Initialize
        ExtendedBooleanArrayList list = new ExtendedBooleanArrayList(1, true);
        assertEquals(1, list.size());
        assertEquals(1, list.getData().length);

        // Grow and fill from the last element
        list.growAndFill(2, false);
        assertEquals(3, list.size());
        assertEquals(3, list.getData().length);
        assertEquals(new ExtendedBooleanArrayList(new boolean[] {true, false, false}), list);

        // Grow and fill from a specific element
        list.growAndFill(2, 2, true);
        assertEquals(4, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new ExtendedBooleanArrayList(new boolean[] {true, false, true, true}), list);

        // Grow and fill from a specific element, with already a size big enough
        list.growAndFill(2, 2, false);
        assertEquals(4, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new ExtendedBooleanArrayList(new boolean[] {true, false, false, false}), list);
    }

    @Test
    void fillTest() {
        // Initialize
        ExtendedBooleanArrayList list = new ExtendedBooleanArrayList(new boolean[] {true, false, true, true});

        // Fill
        list.fill(1, 3, false);
        assertEquals(4, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new ExtendedBooleanArrayList(new boolean[] {true, false, false, true}), list);
    }

    @Test
    void removeElementsTest() {
        // Initialize
        ExtendedBooleanArrayList list = new ExtendedBooleanArrayList(new boolean[] {true, false, true, true});

        // Remove some elements
        list.removeElements(2);
        assertEquals(2, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new ExtendedBooleanArrayList(new boolean[] {true, false}), list);

        // Remove 0 elements
        list.removeElements(0);
        assertEquals(2, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new ExtendedBooleanArrayList(new boolean[] {true, false}), list);

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
        ExtendedBooleanArrayList list = new ExtendedBooleanArrayList(new boolean[] {true, false, true, true});

        // Fill
        list.reset();
        assertEquals(4, list.size());
        assertEquals(4, list.getData().length);
        assertEquals(new ExtendedBooleanArrayList(new boolean[] {false, false, false, false}), list);
    }
}
