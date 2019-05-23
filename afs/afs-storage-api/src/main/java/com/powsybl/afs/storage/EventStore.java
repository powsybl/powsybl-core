package com.powsybl.afs.storage; /**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
import com.powsybl.afs.storage.events.NodeEvent;

/**
 * An event store
 *
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public interface EventStore {
    public void pushEvent(NodeEvent event, String fileSystem);

    public void addTopic();
}
