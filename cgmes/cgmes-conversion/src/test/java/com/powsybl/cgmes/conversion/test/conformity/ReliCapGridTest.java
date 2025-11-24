/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test.conformity;

import com.powsybl.cgmes.conformity.ReliCapGridCatalog;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
class ReliCapGridTest {

    @Test
    void igmBelgoviaTest() {
        ReadOnlyDataSource ds = ReliCapGridCatalog.belgovia().dataSource();
        Network network = Network.read(ds, new Properties());

        assertNotNull(network);
        assertEquals(2, network.getSubstationCount());
    }

    @Test
    void igmBritheimTest() {
        ReadOnlyDataSource ds = ReliCapGridCatalog.britheim().dataSource();
        Network network = Network.read(ds, new Properties());

        assertNotNull(network);
        assertEquals(2, network.getSubstationCount());
    }

    @Test
    void igmEspheimTest() {
        ReadOnlyDataSource ds = ReliCapGridCatalog.espheim().dataSource();
        Network network = Network.read(ds, new Properties());

        assertNotNull(network);
        assertEquals(104, network.getSubstationCount());
    }

    @Test
    void igmGaliaTest() {
        ReadOnlyDataSource ds = ReliCapGridCatalog.galia().dataSource();
        Network network = Network.read(ds, new Properties());

        assertNotNull(network);
        assertEquals(1, network.getSubstationCount());
    }

    @Test
    void igmNordheimTest() {
        ReadOnlyDataSource ds = ReliCapGridCatalog.nordheim().dataSource();
        Network network = Network.read(ds, new Properties());

        assertNotNull(network);
        assertEquals(1, network.getSubstationCount());
    }

    @Test
    void igmSvedalaTest() {
        ReadOnlyDataSource ds = ReliCapGridCatalog.svedala().dataSource();
        Network network = Network.read(ds, new Properties());

        assertNotNull(network);
        assertEquals(56, network.getSubstationCount());
    }

    @Test
    void igmHvdcEspheimSvedalaTest() {
        ReadOnlyDataSource ds = ReliCapGridCatalog.hvdcEspheimSvedala().dataSource();
        Network network = Network.read(ds, new Properties());

        assertNotNull(network);
        assertEquals(1, network.getSubstationCount());
    }

    @Test
    void igmHvdcNordheimGaliaTest() {
        ReadOnlyDataSource ds = ReliCapGridCatalog.hvdcNordheimGalia().dataSource();
        Network network = Network.read(ds, new Properties());

        assertNotNull(network);
        assertEquals(1, network.getSubstationCount());
    }

    @Test
    void cgmNineRealmsTest() {
        ReadOnlyDataSource ds = ReliCapGridCatalog.nineRealms().dataSource();
        Network network = Network.read(ds, new Properties());

        assertNotNull(network);
        assertEquals(8, network.getSubnetworks().size());
        assertEquals(168, network.getSubstationCount());
    }
}
