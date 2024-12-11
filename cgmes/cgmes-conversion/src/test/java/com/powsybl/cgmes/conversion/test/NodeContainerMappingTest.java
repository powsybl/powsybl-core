/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.NetworkFactoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

class NodeContainerMappingTest extends AbstractSerDeTest {

    private static final String DIR = "/issues/node-containers/";

    @Test
    void nodeContainersConnectedBySwitchesTest() {
        // CGMES network:
        //   Switch SW_AB connects nodes in VoltageLevels VL_1A, VL_1B of the same Substation ST_1.
        //   Switch SW_12 connects nodes in VoltageLevels VL_1B, VL_2 of Substations ST_1, ST_2.
        //   Switch SW_23 connects node in VoltageLevel VL_2 of Substation ST_2 with node in Substation ST_3.
        //   Switch SW_3L connects node in Substation ST_3 with node in Line LN_L.
        //   There is a BusbarSection attached to the node in VL_2 and a Load attached to the node in ST_3
        // IIDM network:
        //   Ends of switches must be in the same VoltageLevel. So none of the situation above is allowed.
        //   In such cases, a representative Substation and VoltageLevel are determined and gather all the elements.
        Network network = readCgmesResources(DIR, "containers_connected_by_switches.xml");
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

        // Verify that the Load has been imported and has a Bus
        assertNotNull(network.getLoad("LD"));
        assertNotNull(network.getLoad("LD").getTerminal().getBusView().getBus());
    }

    @Test
    void nodeOfTJunctionInLineContainerTest() {
        // CGMES network:
        //   3 VoltageLevels VL_1, VL_2, VL_3 are connected together through line segments in a T-junction.
        //   ACLineSegments ACL_1A, ACL_2A are in Line LN_A, but ACL_3A has no EquipmentContainer.
        //   Node CN_A of T-junction is directly in Line Container LN_A.
        // IIDM network:
        //   Nodes must be within a VoltageLevel.
        //   If that is not the case and no real representative can be found, a fictitious one is created.
        Network network = readCgmesResources(DIR, "line_with_t-junction.xml");
        assertNotNull(network);

        // There is no real representative VoltageLevel for node CN_A, so a fictitious one has been created.
        VoltageLevel innerVL = network.getVoltageLevel("CN_A_VL");
        assertNotNull(innerVL);
        assertNull(innerVL.getNullableSubstation());
        assertEquals("LN_A", innerVL.getProperty("CGMES.LineContainerId"));

        // All line segments have been imported (even if not associated to an EquipmentContainer) and connected together
        assertEquals(3, network.getLineCount());
        assertEquals(3, innerVL.getBusBreakerView().getBuses().iterator().next().getConnectedTerminalCount());
    }

    @Test
    void chainedLineSegmentsTest() {
        // CGMES network:
        //   2 Substations ST_1, ST_2 connected by 3 ACLineSegments in a row: ACL_1A, ACL_AB, ACL_B2.
        //   The 3 ACLineSegments are in the Line container LN_12.
        //   Extremity nodes CN_A and CN_B of ACL_AB are in LN_12, nodes CN_1, CN_2 are in VL_1, VL_2 of ST_1, ST_2.
        // IIDM network:
        //   Nodes must be within a VoltageLevel. In case of multiple nodes in the same non-VoltageLevel container,
        //   a parameter allows to create a fictitious VoltageLevel for every node or for that container
        Properties importParams = new Properties();
        importParams.put(CgmesImport.CREATE_FICTITIOUS_VOLTAGE_LEVEL_FOR_EVERY_NODE, "false");
        Network network = readCgmesResources(importParams, DIR, "chained_line_segments.xml");
        assertNotNull(network);
        assertEquals(Set.of("VL_1", "VL_2", "LN_12_VL"),
                network.getVoltageLevelStream().map(Identifiable::getId).collect(Collectors.toSet()));
        assertTrue(network.getVoltageLevel("LN_12_VL").isFictitious());

        importParams.put(CgmesImport.CREATE_FICTITIOUS_VOLTAGE_LEVEL_FOR_EVERY_NODE, "true");
        network = readCgmesResources(importParams, DIR, "chained_line_segments.xml");
        assertNotNull(network);
        assertEquals(Set.of("VL_1", "VL_2", "CN_A_VL", "CN_B_VL"),
                network.getVoltageLevelStream().map(Identifiable::getId).collect(Collectors.toSet()));
        assertTrue(network.getVoltageLevel("CN_A_VL").isFictitious());
        assertTrue(network.getVoltageLevel("CN_B_VL").isFictitious());
    }

    @Test
    void substationsConnectedByTransformerTest() {
        // CGMES network:
        //   3-winding transformer connects nodes in VoltageLevels VL_1, VL_2, VL_3 of Substations ST_1, ST_2, ST_3.
        // IIDM network:
        //   Ends of transformers need to be in the same Substation. So the situation above is not allowed.
        //   In such a case, a representative Substation is determined and gathers all the elements.
        Network network = readCgmesResources(DIR, "substations_connected_by_transformer.xml");
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

        Network network = readCgmesResources(DIR,
                "voltage_levels_connected_by_open_switch_EQ.xml", "voltage_levels_connected_by_open_switch_SSH.xml");
        assertNotNull(network);

        // The Switch is indeed open.
        assertTrue(network.getSwitch("SW_12").isOpen());

        // Still there is only one VoltageLevel.
        assertEquals(1, network.getVoltageLevelCount());
        assertNotNull(network.getVoltageLevel("VL_1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"CIM14", "CIM16"})
    void voltageLevelWithoutName(String version) {
        // CGMES network:
        //   Voltage level of ID "VoltageLevel1" has no name
        // IIDM network:
        //   The voltage level imported without name, and can be retrieved via its id

        Network network = switch (version) {
            case "CIM14" -> {
                ReadOnlyDataSource ds = new ResourceDataSource("vl_without_name_14",
                    new ResourceSet(DIR, "vl_without_name_14.xml"));
                yield new CgmesImport().importData(ds, new NetworkFactoryImpl(), new Properties());
            }
            case "CIM16" -> readCgmesResources(DIR, "vl_without_name_16.xml");
            default -> throw new IllegalStateException("Unexpected version: " + version);
        };

        assertNotNull(network);
        assertEquals(2, network.getVoltageLevelCount());
        assertNotNull(network.getVoltageLevel("VoltageLevel1"));
    }
}
