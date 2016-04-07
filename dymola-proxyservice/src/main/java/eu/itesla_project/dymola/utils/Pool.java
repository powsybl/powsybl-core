/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Semaphore;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class Pool<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pool.class);

    final Semaphore available;
    final ArrayList<T> items;
    final ArrayList<Boolean> used;

    public Pool(ArrayList<T> array) {
        items = new ArrayList<>();
        items.addAll(array);
        used = new ArrayList<>(Collections.nCopies(array.size(), false));
        available = new Semaphore(array.size(), true);
    }

    public T getItem() throws InterruptedException {
        available.acquire();
        return getNextAvailableItem();
    }

    public void putItem(T x) {
        if (markAsUnused(x))
            available.release();
    }


    protected synchronized T getNextAvailableItem() {
        for (int i = 0; i < items.size(); ++i) {
            if (!used.get(i)) {
                used.set(i, true);
                return items.get(i);
            }
        }
        return null;
    }

    protected synchronized boolean markAsUnused(T item) {
        for (int i = 0; i < items.size(); ++i) {
            if (item.equals(items.get(i))) {
                if (used.get(i)==true) {
                    used.set(i, false);
                    return true;
                } else
                    return false;
            }
        }
        return false;
    }

}