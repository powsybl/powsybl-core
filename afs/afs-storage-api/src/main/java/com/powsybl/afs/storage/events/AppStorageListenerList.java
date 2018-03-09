/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AppStorageListenerList {

    private final WeakHashMap<Object, List<AppStorageListener>> listeners = new WeakHashMap<>();

    private final Lock lock = new ReentrantLock();

    public void add(Object target, AppStorageListener l) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(l);
        lock.lock();
        try {
            listeners.computeIfAbsent(target, k -> new ArrayList<>()).add(l);
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

    public void notify(NodeEventList eventList) {
        lock.lock();
        try {
            new ArrayList<>(listeners.values()).stream().flatMap(List::stream).forEach(l -> l.onEvents(eventList));
        } finally {
            lock.unlock();
        }
    }
}
