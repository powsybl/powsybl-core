/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */

class BusbarSectionConversionTest extends AbstractSerDeTest {

    @Test
    void isolatedBusbarSectionWithoutTopologicalNodeInBusBreakerModelTest() {
        Network network = Network.read("bbs-busbreaker_EQ.xml", getClass().getResourceAsStream("/bbs-busbreaker_EQ.xml"));
        // The network can be imported without any issue
        assertNotNull(network);
        assertEquals(1, network.getSubstationCount());
        assertEquals(1, network.getSubstations().iterator().next().getVoltageLevelStream().count());
        // But it does not contain any busbar section
        assertEquals(0, network.getBusbarSectionCount());
        // And no bus has been created
        assertEquals(0, network.getBusBreakerView().getBusCount());
    }

    @Test
    void isolatedBusbarSectionWithTopologicalNodeInBusBreakerModelTest() {
        Network network = Network.read(
                new ResourceDataSource("bbs-busbreaker",
                new ResourceSet("/", "bbs-busbreaker_EQ.xml", "bbs-busbreaker_TP.xml")));
        // The network can be imported without any issue
        assertNotNull(network);
        assertEquals(1, network.getSubstationCount());
        assertEquals(1, network.getSubstations().iterator().next().getVoltageLevelStream().count());
        // It does not contain busbar sections
        assertEquals(0, network.getBusbarSectionCount());
        // But a bus has been created and the busbar section terminal has been kept as a property
        assertEquals(1, network.getBusBreakerView().getBusCount());
        assertEquals("bbs_t", network.getBusBreakerView().getBus("bbs_tn").getProperty("CGMES.busbarSectionTerminals"));
    }
}
