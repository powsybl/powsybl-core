/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.afs.storage.events.AppStorageListener;
import com.powsybl.afs.storage.events.NodeEvent;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public interface EventsStore {
    void pushEvent(NodeEvent event, String topic);

    /**
     * Add a listener to the EventsStore.
     */
    void addListener(AppStorageListener l);

    /**
     * remove a listener from the EventsStore.
     */
    void removeListener(AppStorageListener l);

    /**
     * Remove all listeners from the EventsStore.
     */
    void removeListeners();

    /**
     * Flush any changes to underlying EventsStore.
     */
    void flush();
}
