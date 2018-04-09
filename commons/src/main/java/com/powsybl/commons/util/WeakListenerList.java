/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class WeakListenerList<L> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeakListenerList.class);

    private final WeakHashMap<L, Object> listeners = new WeakHashMap<>();

    private final Lock lock = new ReentrantLock();

    public int size() {
        return listeners.size();
    }

    public void add(L l) {
        Objects.requireNonNull(l);
        lock.lock();
        try {
            listeners.put(l, new Object());
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(L l) {
        Objects.requireNonNull(l);
        lock.lock();
        try {
            return listeners.remove(l) != null;
        } finally {
            lock.unlock();
        }
    }

    public void removeAll() {
        lock.lock();
        try {
            listeners.clear();
        } finally {
            lock.unlock();
        }
    }

    public void notify(Consumer<L> notifier) {
        Objects.requireNonNull(notifier);
        lock.lock();
        try {
            for (L listener : new HashSet<>(listeners.keySet())) {
                notifier.accept(listener);
            }
        } finally {
            lock.unlock();
        }
    }

    public List<L> toList() {
        lock.lock();
        try {
            return new ArrayList<>(listeners.keySet());
        } finally {
            lock.unlock();
        }
    }

    public void log() {
        lock.lock();
        try {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Weak listener list status:{}{}",
                        System.lineSeparator(),
                        new HashSet<>(listeners.keySet())
                                .stream()
                                .collect(Collectors.groupingBy(L::getClass))
                                .entrySet()
                                .stream()
                                .map(e -> e.getKey().getSimpleName() + ": " + e.getValue().size())
                                .collect(Collectors.joining(System.lineSeparator())));
            }
        } finally {
            lock.unlock();
        }
    }
}
