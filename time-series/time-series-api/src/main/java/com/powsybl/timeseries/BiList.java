/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class BiList<E> {

    private final List<E> list = new ArrayList<>();

    private final TObjectIntMap<E> reverse = new TObjectIntHashMap<>();

    private final Lock lock = new ReentrantLock();

    public int add(E e) {
        Objects.requireNonNull(e);
        lock.lock();
        try {
            int i = list.size();
            list.add(e);
            reverse.put(e, i);
            return i;
        } finally {
            lock.unlock();
        }
    }

    public int addIfNotAlreadyExist(E e) {
        lock.lock();
        try {
            int i = indexOf(e);
            if (i == -1) {
                i = add(e);
            }
            return i;
        } finally {
            lock.unlock();
        }
    }

    public E get(int index) {
        lock.lock();
        try {
            return list.get(index);
        } finally {
            lock.unlock();
        }
    }

    public int indexOf(E e) {
        Objects.requireNonNull(e);
        lock.lock();
        try {
            if (!reverse.containsKey(e)) {
                return -1;
            }
            return reverse.get(e);
        } finally {
            lock.unlock();
        }
    }

    int size() {
        lock.lock();
        try {
            return list.size();
        } finally {
            lock.unlock();
        }
    }

    void clear() {
        lock.lock();
        try {
            list.clear();
            reverse.clear();
        } finally {
            lock.unlock();
        }
    }
}
