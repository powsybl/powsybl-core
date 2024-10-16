/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

class OperationalLimitsGroupTest extends AbstractSerDeTest {

    private static final Pattern OPERATIONAL_LIMIT_SET = Pattern.compile("<cim:OperationalLimitSet rdf:ID=\"(.*?)\">");
    private static final Pattern OPERATIONAL_LIMIT_TYPE = Pattern.compile("<cim:OperationalLimitType rdf:ID=\"(.*?)\">");
    private static final Pattern ACTIVE_POWER_LIMIT = Pattern.compile("<cim:ActivePowerLimit rdf:ID=\"(.*?)\">");
    private static final Pattern CURRENT_LIMIT = Pattern.compile("<cim:CurrentLimit rdf:ID=\"(.*?)\">");

    private static final String DIR = "/issues/operational-limits/";

    @Test
    void importMultipleLimitsGroupsOnSameLineEndTest() {
        // Retrieve line
        Network network = readCgmesResources(DIR, "multiple_limitsets_on_same_terminal.xml");
        Line line = network.getLine("Line");

        // There is 1 set on side 1, 2 sets on side 2
        assertEquals(1, line.getOperationalLimitsGroups1().size());
        assertEquals(2, line.getOperationalLimitsGroups2().size());

        // The winter set (_OLS_3) contains current limits and active power limits
        Optional<OperationalLimitsGroup> winterLimits = line.getOperationalLimitsGroup2("OLS_3");
        assertTrue(winterLimits.isPresent());
        assertTrue(winterLimits.get().getCurrentLimits().isPresent());
        assertTrue(winterLimits.get().getActivePowerLimits().isPresent());

        // When an end has only 1 set, this set gets selected, otherwise none is
        assertTrue(line.getSelectedOperationalLimitsGroup1().isPresent());
        assertFalse(line.getSelectedOperationalLimitsGroup2().isPresent());
    }

    @Test
    void exportSelectedLimitsGroupTest() throws IOException {
        // Import and export CGMES limits
        Network network = readCgmesResources(DIR, "multiple_limitsets_on_same_terminal.xml");

        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.EXPORT_ALL_LIMITS_GROUP, false);
        exportParams.put(CgmesExport.PROFILES, List.of("EQ"));
        network.write("CGMES", exportParams, tmpDir.resolve("ExportSelectedLimitsGroup.xml"));
        String exportSelectedLimitsGroupXml = Files.readString(tmpDir.resolve("ExportSelectedLimitsGroup_EQ.xml"));

        // There is 1 set on side 1 which is selected, and there are 2 sets on side 2 but none of them is selected
        assertEquals(1, getUniqueMatches(exportSelectedLimitsGroupXml, OPERATIONAL_LIMIT_SET).size());
        assertEquals(3, getUniqueMatches(exportSelectedLimitsGroupXml, OPERATIONAL_LIMIT_TYPE).size());
        assertEquals(0, getUniqueMatches(exportSelectedLimitsGroupXml, ACTIVE_POWER_LIMIT).size());
        assertEquals(3, getUniqueMatches(exportSelectedLimitsGroupXml, CURRENT_LIMIT).size());

        // Manually select one of the limits group on side 2 and export again
        Line line = network.getLine("Line");
        line.setSelectedOperationalLimitsGroup2("OLS_2");
        network.write("CGMES", exportParams, tmpDir.resolve("ExportSelectedLimitsGroup.xml"));
        exportSelectedLimitsGroupXml = Files.readString(tmpDir.resolve("ExportSelectedLimitsGroup_EQ.xml"));

        // That makes 1 set selected on each side = 2 in total
        assertEquals(2, getUniqueMatches(exportSelectedLimitsGroupXml, OPERATIONAL_LIMIT_SET).size());
        assertEquals(3, getUniqueMatches(exportSelectedLimitsGroupXml, OPERATIONAL_LIMIT_TYPE).size());
        assertEquals(0, getUniqueMatches(exportSelectedLimitsGroupXml, ACTIVE_POWER_LIMIT).size());
        assertEquals(6, getUniqueMatches(exportSelectedLimitsGroupXml, CURRENT_LIMIT).size());
    }

    @Test
    void exportAllLimitsGroupTest() throws IOException {
        // Import and export CGMES limits
        Network network = readCgmesResources(DIR, "multiple_limitsets_on_same_terminal.xml");

        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.EXPORT_ALL_LIMITS_GROUP, true);
        exportParams.put(CgmesExport.PROFILES, List.of("EQ"));
        network.write("CGMES", exportParams, tmpDir.resolve("ExportAllLimitsGroup.xml"));
        String exportAllLimitsGroupXml = Files.readString(tmpDir.resolve("ExportAllLimitsGroup_EQ.xml"));

        // All 3 OperationalLimitsGroup are exported, even though only 2 are selected
        assertEquals(3, getUniqueMatches(exportAllLimitsGroupXml, OPERATIONAL_LIMIT_SET).size());
        assertEquals(3, getUniqueMatches(exportAllLimitsGroupXml, OPERATIONAL_LIMIT_TYPE).size());
        assertEquals(3, getUniqueMatches(exportAllLimitsGroupXml, ACTIVE_POWER_LIMIT).size());
        assertEquals(9, getUniqueMatches(exportAllLimitsGroupXml, CURRENT_LIMIT).size());
    }

    @Test
    void limitSetsAssociatedToEquipmentsTest() {
        // CGMES network:
        //   An OperationalLimitSet with a CurrentLimit associated to a boundary ACLineSegment (Dangling Line in IIDM).
        //   An OperationalLimitSet with a CurrentLimit associated to a normal ACLineSegment.
        //   An OperationalLimitSet with a CurrentLimit associated to a 2-windings PowerTransformer.
        //   An OperationalLimitSet with a CurrentLimit associated to a Switch.
        Network network = readCgmesResources(DIR, "limitsets_associated_to_equipments_EQ.xml",
                "limitsets_associated_to_equipments_EQBD.xml", "limitsets_associated_to_equipments_TPBD.xml");

        // OperationalLimitSet on dangling line is imported on its single extremity.
        assertNotNull(network.getDanglingLine("DL"));
        assertTrue(network.getDanglingLine("DL").getCurrentLimits().isPresent());

        // OperationalLimitSet on ACLineSegment is imported on its two extremities.
        assertNotNull(network.getLine("ACL"));
        assertTrue(network.getLine("ACL").getCurrentLimits1().isPresent());
        assertTrue(network.getLine("ACL").getCurrentLimits2().isPresent());

        // OperationalLimitSet on PowerTransformer is discarded.
        assertNotNull(network.getTwoWindingsTransformer("PT"));
        assertFalse(network.getTwoWindingsTransformer("PT").getCurrentLimits1().isPresent());
        assertFalse(network.getTwoWindingsTransformer("PT").getCurrentLimits2().isPresent());

        // There can't be any limit associated to switches in IIDM, but check anyway that the switch has been imported.
        assertNotNull(network.getSwitch("SW"));
    }

}
