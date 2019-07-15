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
public class InMemoryEventsBusTest {

    private InMemoryEventsBus eventsBus;

    @Before
    public void setUp() throws Exception {
        eventsBus = new InMemoryEventsBus();
    }

    @Test
    public void eventsStoreTest() throws IOException {
        NodeEvent nodeEvent = new NodeCreated("id", "parentId");
        eventsBus.pushEvent(nodeEvent, String.valueOf(nodeEvent.getType()));
        assertEquals(1, eventsBus.getTopics().size());
        assertNotNull(eventsBus.getTopics().get(String.valueOf(nodeEvent.getType())));
    }
}

