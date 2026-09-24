/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.list;

import java.util.Comparator;
import java.util.List;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public interface ListChangeListener<E> {

    /**
     * Called after one or more elements were added, starting at {@code fromIndex}.
     */
    default void onAdded(List<E> added, int fromIndex) {
    }

    /**
     * Called after one or more elements were removed; {@code fromIndex} is where they were.
     */
    default void onRemoved(List<E> removed, int fromIndex) {
    }

    /**
     * Called after elements were replaced starting at {@code fromIndex}.
     */
    default void onSet(List<E> oldElements, List<E> newElements, int fromIndex) {
    }

    /**
     * Called after the list was sorted with the given comparator (which may be {@code null} for natural order).
     */
    default void onSorted(Comparator<? super E> comparator) {
    }

    /**
     * Called after the list was fully cleared.
     */
    default void onCleared() {
    }
}
