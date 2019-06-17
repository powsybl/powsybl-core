/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.EventsStore;
import com.powsybl.computation.ComputationManager;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AppFileSystemProviderContext {

    private final ComputationManager computationManager;

    private final String token;

    private final EventsStore eventsStore;

    public AppFileSystemProviderContext(ComputationManager computationManager, String token, EventsStore eventsStore) {
        Objects.requireNonNull(eventsStore);
        this.computationManager = Objects.requireNonNull(computationManager);
        this.token = token;
        this.eventsStore = eventsStore;
    }

    public ComputationManager getComputationManager() {
        return computationManager;
    }

    public String getToken() {
        return token;
    }

    public EventsStore getEventsStore() {
        return eventsStore;
    }
}
