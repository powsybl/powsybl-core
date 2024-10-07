/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

class NodeContainerMappingTest extends AbstractSerDeTest {

    @Test
    void nodeContainersConnectedBySwitchesTest() {
        // CGMES network:
        //   Switch SW_AB connects nodes in VoltageLevels VL_1A, VL_1B of the same Substation ST_1.
        //   Switch SW_12 connects nodes in VoltageLevels VL_1B, VL_2 of Substations ST_1, ST_2.
        //   Switch SW_23 connects node in VoltageLevel VL_2 of Substation ST_2 with node in Substation ST_3.
        //   Switch SW_3L connects node in Substation ST_3 with node in Line LN_L.
        // IIDM network:
        //   Ends of switches must be in the same VoltageLevel. So none of the situation above is allowed.
        //   In such cases, a representative Substation and VoltageLevel are determined and gather all the elements.
        ReadOnlyDataSource ds = new ResourceDataSource("Node containers connected by switches",
                new ResourceSet("/issues/node-containers/", "containers_connected_by_switches.xml"));
        Network network = Network.read(ds, new Properties());
        assertNotNull(network);

        // All Substations and VoltageLevels are adjacent. Only 1 of each is kept.
        assertEquals(1, network.getSubstationCount());
        assertEquals(1, network.getVoltageLevelCount());

        // Representatives are ST_1 and VL_1A (min in alphabetical order).
        assertNotNull(network.getSubstation("ST_1"));
        assertNotNull(network.getVoltageLevel("VL_1A"));

        // Merged Substations and VoltageLevels are saved in aliases.
        assertEquals(Set.of("ST_2", "ST_3"), network.getSubstation("ST_1").getAliases());
        assertEquals(Set.of("VL_1B", "VL_2", "CN_3_VL", "CN_L_VL"), network.getVoltageLevel("VL_1A").getAliases());
    }

    @Test
    void nodeOfTJunctionInLineContainerTest() {
        // CGMES network:
        //   3 VoltageLevels VL_1, VL_2, VL_3 are connected together through line segments in a T-junction.
        //   Node CN_A of T-junction is directly in Line Container LN_A.
        // IIDM network:
        //   Nodes must be within a VoltageLevel.
        //   If that is not the case and no real representative can be found, a fictitious one is created.
        ReadOnlyDataSource ds = new ResourceDataSource("Node of T-junction in line container",
                new ResourceSet("/issues/node-containers/", "line_with_t-junction.xml"));
        Network network = Network.read(ds, new Properties());
        assertNotNull(network);

        // There is no real representative VoltageLevel for node CN_A, so a fictitious one has been created.
        assertNotNull(network.getVoltageLevel("CN_A_VL"));
        assertEquals("LN_A", network.getVoltageLevel("CN_A_VL").getProperty("CGMES.LineContainerId"));
    }

    @Test
    void substationsConnectedByTransformerTest() {
        // CGMES network:
        //   3-winding transformer connects nodes in VoltageLevels VL_1, VL_2, VL_3 of Substations ST_1, ST_2, ST_3.
        // IIDM network:
        //   Ends of transformers need to be in the same Substation. So the situation above is not allowed.
        //   In such a case, a representative Substation is determined and gathers all the elements.
        ReadOnlyDataSource ds = new ResourceDataSource("Substations connected by transformer",
                new ResourceSet("/issues/node-containers/", "substations_connected_by_transformer.xml"));
        Network network = Network.read(ds, new Properties());
        assertNotNull(network);

        // All Substations are adjacent, only 1 is kept.
        assertEquals(1, network.getSubstationCount());

        // Representative is ST_1 (min in alphabetical order).
        assertNotNull(network.getSubstation("ST_1"));

        // Merged Substations are saved in aliases.
        assertEquals(Set.of("ST_2", "ST_3"), network.getSubstation("ST_1").getAliases());
    }

    @Test
    void voltageLevelsConnectedByOpenSwitchTest() {
        // CGMES network:
        //   VoltageLevel VL_1 and VL_2 in Substation ST are connected by Switch SW_12 which is open in SSH.
        // IIDM network:
        //   Ends of switches must be in the same VoltageLevel regardless the switch status in SSH.
        ReadOnlyDataSource ds = new ResourceDataSource("Lines connected by switch",
                new ResourceSet("/issues/node-containers/",
                        "voltage_levels_connected_by_open_switch_EQ.xml",
                        "voltage_levels_connected_by_open_switch_SSH.xml"));
        Network network = Network.read(ds, new Properties());
        assertNotNull(network);

        // The Switch is indeed open.
        assertTrue(network.getSwitch("SW_12").isOpen());

        // Still there is only one VoltageLevel.
        assertEquals(1, network.getVoltageLevelCount());
        assertNotNull(network.getVoltageLevel("VL_1"));
    }
}
