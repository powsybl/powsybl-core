/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UuidNodeIdTest {

    @Test
    public void test() {
        UuidNodeId nodeId = UuidNodeId.generate();
        assertNotNull(nodeId.getUuid());
        assertEquals(nodeId.getUuid().toString(), nodeId.toString());
        UuidNodeId nodeId2 = new UuidNodeId(nodeId.getUuid());
        UuidNodeId nodeId3 = UuidNodeId.generate();
        UuidNodeId nodeId4 = new UuidNodeId(nodeId3.getUuid());
        new EqualsTester()
                .addEqualityGroup(nodeId, nodeId2)
                .addEqualityGroup(nodeId3, nodeId4)
                .testEquals();
    }
}
