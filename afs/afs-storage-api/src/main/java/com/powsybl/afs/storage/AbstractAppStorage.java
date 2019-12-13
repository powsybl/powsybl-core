/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.afs.storage.events.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public abstract class AbstractAppStorage implements AppStorage {

    protected static final String APPSTORAGE_NODE_TOPIC = "APPSTORAGE_NODE_TOPIC";
    protected static final String APPSTORAGE_DEPENDENCY_TOPIC = "APPSTORAGE_DEPENDENCY_TOPIC";
    protected static final String APPSTORAGE_TIMESERIES_TOPIC = "APPSTORAGE_TIMESERIES_TOPIC";

    protected EventsBus eventsBus;

    private  final Logger logger = LoggerFactory.getLogger(AbstractAppStorage.class);

    protected void pushEvent(NodeEvent event, String topic) {
        if (eventsBus == null) {
            logger.warn("Event can't be pushed : No EventStore instance is available.");
            return;
        }
        eventsBus.pushEvent(event, topic);
    }

    @Override
    public EventsBus getEventsBus() {
        return eventsBus;
    }
}
