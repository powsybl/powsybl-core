/*
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Soft-referenced cache of a computed array. The cached array is never exposed: callers always get a copy.
 * An invalidation happening while the array is being computed prevents it from being cached.
 * Optionally, the cached array is bound to a list of source objects and is only reused for the very same instances.
 *
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
final class ArrayCache<A> {

    private final UnaryOperator<A> copier;

    private SoftReference<A> reference = new SoftReference<>(null);

    private List<?> sources = List.of();

    private long version;

    ArrayCache(UnaryOperator<A> copier) {
        this.copier = Objects.requireNonNull(copier);
    }

    A get(Supplier<A> computer) {
        return get(List.of(), computer);
    }

    A get(List<?> currentSources, Supplier<A> computer) {
        long expectedVersion;
        synchronized (this) {
            A cached = reference.get();
            if (cached != null && sameInstances(sources, currentSources)) {
                return copier.apply(cached);
            }
            expectedVersion = version;
        }
        A computed = computer.get();
        synchronized (this) {
            if (version == expectedVersion) {
                reference = new SoftReference<>(computed);
                sources = List.copyOf(currentSources);
                return copier.apply(computed);
            }
        }
        return computed;
    }

    private static boolean sameInstances(List<?> l1, List<?> l2) {
        if (l1.size() != l2.size()) {
            return false;
        }
        for (int i = 0; i < l1.size(); i++) {
            if (l1.get(i) != l2.get(i)) {
                return false;
            }
        }
        return true;
    }

    synchronized void invalidate() {
        version++;
        reference = new SoftReference<>(null);
        sources = List.of();
    }
}
