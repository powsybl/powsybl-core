/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.events;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * @author Chamseddine Benhamed <Chamseddine.Benhamed at rte-france.com>
 */
public class NodeEventContainerTest {

    @Test
    public void mainTest() {
        NodeEventContainer nodeEventContainer = new NodeEventContainer(new NodeCreated("id", "parentid"), "fs", "topic");
        assertEquals(nodeEventContainer.getFileSystemName(), "fs");
        assertEquals(nodeEventContainer.getTopic(), "topic");
        assertEquals(nodeEventContainer.getNodeEvent(), new NodeCreated("id", "parentid"));

        NodeEventContainer nodeEventContainer1 = new NodeEventContainer();
        assertNull(nodeEventContainer1.getNodeEvent());
        assertNull(nodeEventContainer1.getTopic());
        assertNull(nodeEventContainer1.getFileSystemName());
    }
}
