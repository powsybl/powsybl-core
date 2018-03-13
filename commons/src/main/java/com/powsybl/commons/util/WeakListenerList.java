/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.util;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class WeakListenerList<L> {

    private final WeakHashMap<Object, List<L>> listeners = new WeakHashMap<>();

    private final Lock lock = new ReentrantLock();

    public void add(Object target, L l) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(l);
        lock.lock();
        try {
            listeners.computeIfAbsent(target, k -> new ArrayList<>()).add(l);
        } finally {
            lock.unlock();
        }
    }

    public void remove(Object target, L l) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(l);
        lock.lock();
        try {
            listeners.computeIfAbsent(target, k -> new ArrayList<>()).remove(l);
        } finally {
            lock.unlock();
        }
    }

    public void removeAll(Object target) {
        Objects.requireNonNull(target);
        lock.lock();
        try {
            listeners.remove(target);
        } finally {
            lock.unlock();
        }
    }

    public void notify(Consumer<L> notifier) {
        Objects.requireNonNull(notifier);
        lock.lock();
        try {
            new ArrayList<>(listeners.values()).stream().flatMap(List::stream).forEach(notifier);
        } finally {
            lock.unlock();
        }
    }

    public void notify(BiConsumer<Object, L> notifier) {
        Objects.requireNonNull(notifier);
        lock.lock();
        try {
            for (Map.Entry<Object, List<L>> e : listeners.entrySet()) {
                Object target = e.getKey();
                for (L listener : new ArrayList<>(e.getValue())) {
                    notifier.accept(target, listener);
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
