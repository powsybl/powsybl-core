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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

class OperationalLimitsGroupTest extends AbstractSerDeTest {

    private static final Pattern OPERATIONAL_LIMIT_SET = Pattern.compile("<cim:OperationalLimitSet rdf:ID=\"(.*?)\">");
    private static final Pattern OPERATIONAL_LIMIT_TYPE = Pattern.compile("<cim:OperationalLimitType rdf:ID=\"(.*?)\">");
    private static final Pattern ACTIVE_POWER_LIMIT = Pattern.compile("<cim:ActivePowerLimit rdf:ID=\"(.*?)\">");
    private static final Pattern CURRENT_LIMIT = Pattern.compile("<cim:CurrentLimit rdf:ID=\"(.*?)\">");

    @Test
    void importMultipleLimitsGroupsOnSameLineEndTest() {
        // Retrieve line
        Network network = Network.read("OperationalLimits.xml", getClass().getResourceAsStream("/OperationalLimits.xml"));
        Line line = network.getLine("Line");

        // There are 4 CGMES OperationalLimitSets on side 1 merged into 3 IIDM OperationalLimitsGroup
        assertEquals(3, line.getOperationalLimitsGroups1().size());
        assertEquals(0, line.getOperationalLimitsGroups2().size());

        // The CGMES winter current and active power limits have been merged into the same limits group
        // since their OperationalLimitSet name are equals
        Optional<OperationalLimitsGroup> winterLimits = line.getOperationalLimitsGroup1("WINTER");
        assertTrue(winterLimits.isPresent());
        assertTrue(winterLimits.get().getCurrentLimits().isPresent());
        assertTrue(winterLimits.get().getActivePowerLimits().isPresent());

        // The CGMES spring current limits and summer active power limits have different limits group
        // since their OperationalLimitSet name are distinct
        Optional<OperationalLimitsGroup> springLimits = line.getOperationalLimitsGroup1("SPRING");
        assertTrue(springLimits.isPresent());
        assertTrue(springLimits.get().getCurrentLimits().isPresent());
        assertTrue(springLimits.get().getActivePowerLimits().isEmpty());

        Optional<OperationalLimitsGroup> summerLimits = line.getOperationalLimitsGroup1("SUMMER");
        assertTrue(summerLimits.isPresent());
        assertTrue(summerLimits.get().getCurrentLimits().isEmpty());
        assertTrue(summerLimits.get().getActivePowerLimits().isPresent());
    }

    @Test
    void exportSelectedLimitsGroupTest() throws IOException {
        // Import and export CGMES limits
        Network network = Network.read("OperationalLimits.xml", getClass().getResourceAsStream("/OperationalLimits.xml"));

        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.EXPORT_ALL_LIMITS_GROUP, false);
        exportParams.put(CgmesExport.PROFILES, List.of("EQ"));
        network.write("CGMES", exportParams, tmpDir.resolve("ExportSelectedLimitsGroup.xml"));
        String exportSelectedLimitsGroupXml = Files.readString(tmpDir.resolve("ExportSelectedLimitsGroup_EQ.xml"));

        // No OperationalLimitsGroup is exported
        assertEquals(0, getOccurrences(exportSelectedLimitsGroupXml, OPERATIONAL_LIMIT_SET).size());
        assertEquals(0, getOccurrences(exportSelectedLimitsGroupXml, OPERATIONAL_LIMIT_TYPE).size());
        assertEquals(0, getOccurrences(exportSelectedLimitsGroupXml, ACTIVE_POWER_LIMIT).size());
        assertEquals(0, getOccurrences(exportSelectedLimitsGroupXml, CURRENT_LIMIT).size());

        // Manually select one limits group
        Line line = network.getLine("Line");
        line.setSelectedOperationalLimitsGroup1("WINTER");
        network.write("CGMES", exportParams, tmpDir.resolve("ExportSelectedLimitsGroup.xml"));
        exportSelectedLimitsGroupXml = Files.readString(tmpDir.resolve("ExportSelectedLimitsGroup_EQ.xml"));

        // The selected limits group contains 2 sets of limits (current and active power)
        assertEquals(2, getOccurrences(exportSelectedLimitsGroupXml, OPERATIONAL_LIMIT_SET).size());
        assertEquals(3, getOccurrences(exportSelectedLimitsGroupXml, OPERATIONAL_LIMIT_TYPE).size());
        assertEquals(3, getOccurrences(exportSelectedLimitsGroupXml, ACTIVE_POWER_LIMIT).size());
        assertEquals(3, getOccurrences(exportSelectedLimitsGroupXml, CURRENT_LIMIT).size());
    }

    @Test
    void exportAllLimitsGroupTest() throws IOException {
        // Import and export CGMES limits
        Network network = Network.read("OperationalLimits.xml", getClass().getResourceAsStream("/OperationalLimits.xml"));

        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.EXPORT_ALL_LIMITS_GROUP, true);
        exportParams.put(CgmesExport.PROFILES, List.of("EQ"));
        network.write("CGMES", exportParams, tmpDir.resolve("ExportAllLimitsGroup.xml"));
        String exportAllLimitsGroupXml = Files.readString(tmpDir.resolve("ExportAllLimitsGroup_EQ.xml"));

        // All 4 OperationalLimitsGroup are exported
        assertEquals(4, getOccurrences(exportAllLimitsGroupXml, OPERATIONAL_LIMIT_SET).size());
        assertEquals(3, getOccurrences(exportAllLimitsGroupXml, OPERATIONAL_LIMIT_TYPE).size());
        assertEquals(6, getOccurrences(exportAllLimitsGroupXml, ACTIVE_POWER_LIMIT).size());
        assertEquals(6, getOccurrences(exportAllLimitsGroupXml, CURRENT_LIMIT).size());
    }

    private Set<String> getOccurrences(String xml, Pattern pattern) {
        Set<String> matches = new HashSet<>();
        Matcher matcher = pattern.matcher(xml);
        while (matcher.find()) {
            matches.add(matcher.group(1));
        }
        return matches;
    }

}
