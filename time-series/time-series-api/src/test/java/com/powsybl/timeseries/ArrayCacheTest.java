/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class ArrayCacheTest {

    private final AtomicInteger computations = new AtomicInteger();
    private final ArrayCache<double[]> cache = new ArrayCache<>(double[]::clone);

    private double[] compute() {
        return new double[] {computations.incrementAndGet()};
    }

    @Test
    void computesOnlyOnceAndReturnsCopies() {
        double[] first = cache.get(this::compute);
        double[] second = cache.get(this::compute);
        assertEquals(1, computations.get());
        assertNotSame(first, second);
        assertArrayEquals(first, second);
    }

    @Test
    void mutatingReturnedArrayDoesNotAlterCache() {
        cache.get(this::compute)[0] = 42d;
        assertArrayEquals(new double[] {1d}, cache.get(this::compute));
        assertEquals(1, computations.get());
    }

    @Test
    void invalidateForcesRecomputation() {
        cache.get(this::compute);
        cache.invalidate();
        assertArrayEquals(new double[] {2d}, cache.get(this::compute));
    }

    @Test
    void invalidationDuringComputationPreventsCaching() {
        double[] first = cache.get(() -> {
            double[] value = compute();
            cache.invalidate();
            return value;
        });
        assertArrayEquals(new double[] {1d}, first);
        assertArrayEquals(new double[] {2d}, cache.get(this::compute));
        assertArrayEquals(new double[] {2d}, cache.get(this::compute));
        assertEquals(2, computations.get());
    }

    @Test
    void sourcesAreComparedByIdentity() {
        Set<String> a = Set.of("a");
        Set<String> sameContentAsA = Set.of("a");
        cache.get(List.of(a), this::compute);
        cache.get(List.of(a), this::compute);
        assertEquals(1, computations.get());
        cache.get(List.of(sameContentAsA), this::compute);
        assertEquals(2, computations.get());
        cache.get(List.of(a, sameContentAsA), this::compute);
        assertEquals(3, computations.get());
        cache.get(List.of(), this::compute);
        assertEquals(4, computations.get());
    }

    @Test
    void emptyArrayIsCached() {
        ArrayCache<double[]> emptyCache = new ArrayCache<>(double[]::clone);
        assertEquals(0, emptyCache.get(() -> new double[0]).length);
        assertEquals(0, emptyCache.get(() -> fail("Should be cached")).length);
    }
}
