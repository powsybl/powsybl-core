/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.list;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class ObservableArrayListTest {

    private ObservableArrayList<Integer> list;
    private ListChangeListener<Integer> chunkListChangeListener;
    private boolean firedAdd = false;
    private boolean firedRemove = false;
    private boolean firedSet = false;
    private boolean firedSort = false;
    private boolean firedClear = false;

    @BeforeEach
    void setUp() {
        list = new ObservableArrayList<>(List.of(1, 2, 3));
        chunkListChangeListener = new ListChangeListener<>() {
            @Override
            public void onAdded(List<Integer> added, int fromIndex) {
                firedAdd = true;
            }

            @Override
            public void onRemoved(List<Integer> removed, int fromIndex) {
                firedRemove = true;
            }

            @Override
            public void onSet(List<Integer> oldElements, List<Integer> newElements, int fromIndex) {
                firedSet = true;
            }

            @Override
            public void onSorted(Comparator<? super Integer> comparator) {
                firedSort = true;
            }

            @Override
            public void onCleared() {
                firedClear = true;
            }
        };
        list.addListener(chunkListChangeListener);
    }

    @Test
    void testRemoveListener() {
        list.removeListener(chunkListChangeListener);
        list.add(4);
        assertFalse(firedAdd);
    }

    @Test
    void testAdd() {
        list.add(4);
        assertTrue(firedAdd);
    }

    @Test
    void testAddByIndex() {
        list.add(1, 5);
        assertTrue(firedAdd);
    }

    @Test
    void testAddAll() {
        list.addAll(List.of(6, 7));
        assertTrue(firedAdd);
    }

    @Test
    void testAddFirst() {
        list.addFirst(8);
        assertTrue(firedAdd);
    }

    @Test
    void testAddAllByIndex() {
        list.addAll(3, List.of(9, 10));
        assertTrue(firedAdd);
    }

    @Test
    void testRemove() {
        list.remove(1);
        assertTrue(firedRemove);
    }

    @Test
    void testRemoveFirst() {
        list.removeFirst();
        assertTrue(firedRemove);
    }

    @Test
    void testRemoveLast() {
        list.removeLast();
        assertTrue(firedRemove);
    }

    @Test
    void testRemoveAllNok() {
        // Try to remove elements that are not in the list
        list.removeAll(List.of(6, 7));
        assertFalse(firedRemove);
    }

    @Test
    void testRemoveAllOk() {
        // Try to remove elements that are in the list
        list.removeAll(List.of(0, 1));
        assertTrue(firedRemove);
    }

    @Test
    void testRemoveIfNok() {
        list.removeIf(i -> i > 5);
        assertFalse(firedRemove);
    }

    @Test
    void testRemoveIfOk() {
        list.removeIf(i -> i % 2 == 0);
        assertTrue(firedRemove);
    }

    @Test
    void testRemoveRange() {
        list.removeRange(1, 2);
        assertTrue(firedRemove);
    }

    @Test
    void testRetainAllNok() {
        list.retainAll(List.of(1, 2, 3));
        assertFalse(firedRemove);
    }

    @Test
    void testRetainAllOk() {
        list.retainAll(List.of(1, 2));
        assertTrue(firedRemove);
    }

    @Test
    void testSet() {
        list.set(1, 4);
        assertTrue(firedSet);
    }

    @Test
    void testReplaceAll() {
        list.replaceAll(i -> i * 2);
        assertTrue(firedSet);
    }

    @Test
    void testSort() {
        list.sort(Comparator.naturalOrder());
        assertTrue(firedSort);
    }

    @Test
    void testClear() {
        list.clear();
        assertTrue(firedClear);
    }

    @Test
    void testEquals() {
        ObservableArrayList<Integer> newList = new ObservableArrayList<>();
        assertNotEquals(list, newList);

        newList.addAll(List.of(1, 2, 3));
        assertEquals(list, newList);
    }
}
