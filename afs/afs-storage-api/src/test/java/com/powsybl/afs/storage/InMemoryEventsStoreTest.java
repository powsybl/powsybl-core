/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.afs.storage.events.NodeCreated;
import com.powsybl.afs.storage.events.NodeEvent;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class InMemoryEventsStoreTest {

    private InMemoryEventsStore eventsStore;

    @Before
    public void setUp() throws Exception {
        eventsStore = new InMemoryEventsStore();
    }

    @Test
    public void eventsStoreTest() throws IOException {
        NodeEvent nodeEvent = new NodeCreated("id", "parentId");
        eventsStore.pushEvent(nodeEvent, null, String.valueOf(nodeEvent.getType()));
        assertEquals(1, eventsStore.getTopics().size());
        assertNotNull(eventsStore.getTopics().get(String.valueOf(nodeEvent.getType())));
    }
}

