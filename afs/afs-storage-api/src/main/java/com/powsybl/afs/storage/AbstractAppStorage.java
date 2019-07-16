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

    protected static final String  NODE_CREATED = "NODE_CREATED";
    protected static final String  NODE_REMOVED = "NODE_REMOVED";
    protected static final String  NODE_DESCRIPTION_UPDATED = "NODE_DESCRIPTION_UPDATED";
    protected static final String  NODE_NAME_UPDATED = "NODE_NAME_UPDATED";
    protected static final String  NODE_DATA_UPDATED = "NODE_DATA_UPDATED";
    protected static final String  NODE_DATA_REMOVED = "NODE_DATA_REMOVED";
    protected static final String  NODE_CONSISTENT = "NODE_CONSISTENT";
    protected static final String  PARENT_CHANGED = "PARENT_CHANGED";
    protected static final String  DEPENDENCY_ADDED = "DEPENDENCY_ADDED";
    protected static final String  DEPENDENCY_REMOVED = "DEPENDENCY_REMOVED";
    protected static final String  BACKWARD_DEPENDENCY_ADDED = "BACKWARD_DEPENDENCY_ADDED";
    protected static final String  BACKWARD_DEPENDENCY_REMOVED = "BACKWARD_DEPENDENCY_REMOVED";
    protected static final String  TIME_SERIES_CREATED = "TIME_SERIES_CREATED";
    protected static final String  TIME_SERIES_DATA_UPDATED = "TIME_SERIES_DATA_UPDATED";
    protected static final String  TIME_SERIES_CLEARED = "TIME_SERIES_CLEARED";

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
