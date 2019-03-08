/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.afs.storage.events.AppStorageListener;

/**
 *
 * A listenable {@link AppStorage}. Listeners will be notified of {@linkplain com.powsybl.afs.storage.events.NodeEvent NodeEvents} happening on the storage:
 * node creation, removal, etc.
 *
 * WARNING: you will need to keep a reference to your listeners,
 *          otherwise they may be garbage collected.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ListenableAppStorage extends AppStorage {

    /**
     * Add an {@link AppStorageListener} which will be notified of events happening on this storage.
     *
     * WARNING: you will need to keep a reference to your listeners,
     *          otherwise they may be garbage collected.
     */
    void addListener(AppStorageListener l);

    void removeListener(AppStorageListener l);

    void removeListeners();
}
