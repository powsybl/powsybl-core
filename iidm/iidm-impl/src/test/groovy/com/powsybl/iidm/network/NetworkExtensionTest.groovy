/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network

import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull

/**
 * @author Chamseddine BENHAMED <chamseddine.benhamed at rte-france.com>
 */
class NetworkExtensionTest {

    @Test
    void getShuntsTest() {
        Network network = NetworkFactory.create("test", "test")
        assertEquals(network.getShunts().size(),0)
        assertEquals(network.getShuntCount(),0)
    }

    @Test
    void getShuntStreamTest() {
        Network network = NetworkFactory.create("test", "test")
        assertNotNull(network.getShuntStream())
    }

    @Test
    void getShuntTest() {
        Network network = NetworkFactory.create("test", "test")
        assertNull(network.getShunt("shunt1"))
    }
}

