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

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AppStorageListenerList {

    private final WeakHashMap<Object, List<AppStorageListener>> listeners = new WeakHashMap<>();

    public void add(Object target, AppStorageListener l) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(l);
        listeners.computeIfAbsent(target, k -> new ArrayList<>()).add(l);
    }

    public void removeAll(Object target) {
        Objects.requireNonNull(target);
        listeners.remove(target);
    }

    public void notify(NodeEvent event) {
        listeners.values().stream().flatMap(List::stream).forEach(l -> l.onEvent(event));
    }
}
