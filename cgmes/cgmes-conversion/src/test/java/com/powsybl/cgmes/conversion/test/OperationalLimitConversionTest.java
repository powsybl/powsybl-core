/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

class OperationalLimitConversionTest extends AbstractSerDeTest {

    private static final Pattern OPERATIONAL_LIMIT_SET = Pattern.compile("<cim:OperationalLimitSet rdf:ID=\"(.*?)\">");
    private static final Pattern OPERATIONAL_LIMIT_TYPE = Pattern.compile("<cim:OperationalLimitType rdf:ID=\"(.*?)\">");
    private static final Pattern ACTIVE_POWER_LIMIT = Pattern.compile("<cim:ActivePowerLimit rdf:ID=\"(.*?)\">");
    private static final Pattern CURRENT_LIMIT = Pattern.compile("<cim:CurrentLimit rdf:ID=\"(.*?)\">");

    private static final String DIR = "/issues/operational-limits/";

    @Test
    void importMultipleLimitsGroupsOnSameLineEndTest() {
        // CGMES network:
        //   An ACLineSegment LN with:
        //   - On side 1, 1 OperationalLimitSet OLS_1 (Spring).
        //   - On side 2, 2 OperationalLimitSet OLS_2 (Spring) and OLS_3 (Winter).
        //   All sets contain 3 CurrentLimit. Winter set has in addition 3 ActivePowerLimit.
        // IIDM network:
        //   All limits are imported.
        //   In case there is only 1 limit group on an extremity, it becomes the active set.
        Network network = readCgmesResources(DIR, "multiple_limitsets_on_same_terminal.xml");
        assertNotNull(network);

        // There is 1 set on side 1, 2 sets on side 2.
        Line line = network.getLine("LN");
        assertEquals(1, line.getOperationalLimitsGroups1().size());
        assertEquals(2, line.getOperationalLimitsGroups2().size());

        // The winter set (OLS_3) contains current limits and active power limits.
        Optional<OperationalLimitsGroup> winterLimits = line.getOperationalLimitsGroup2("OLS_3");
        assertTrue(winterLimits.isPresent());
        assertTrue(winterLimits.get().getCurrentLimits().isPresent());
        assertTrue(winterLimits.get().getActivePowerLimits().isPresent());

        // When an end has only 1 set, this set gets selected, otherwise none is.
        assertTrue(line.getSelectedOperationalLimitsGroup1().isPresent());
        assertFalse(line.getSelectedOperationalLimitsGroup2().isPresent());

        // The CGMES id/name have been preserved in a property.
        String propertyValue = """
                {"OLS_1":"Spring","OLS_2":"Spring","OLS_3":"Winter"}""";
        assertEquals(propertyValue, line.getProperty(Conversion.PROPERTY_OPERATIONAL_LIMIT_SET_IDENTIFIERS));
    }

    @Test
    void exportSelectedLimitsGroupTest() throws IOException {
        // IIDM network:
        //   A Line LN with:
        //   - On side 1, 1 (selected) OperationalLimitsGroup.
        //   - On side 2, 2 (not selected) OperationalLimitsGroup.
        // CGMES export:
        //   When the parameter to export all limits group is set to false, only the selected groups are exported.
        Network network = readCgmesResources(DIR, "multiple_limitsets_on_same_terminal.xml");
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.EXPORT_ALL_LIMITS_GROUP, false);
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir, exportParams);

        // Only 1 OperationalLimitsGroup is selected, so only 1 is exported.
        assertEquals(1, getUniqueMatches(eqFile, OPERATIONAL_LIMIT_SET).size());
        assertEquals(3, getUniqueMatches(eqFile, OPERATIONAL_LIMIT_TYPE).size());
        assertEquals(0, getUniqueMatches(eqFile, ACTIVE_POWER_LIMIT).size());
        assertEquals(3, getUniqueMatches(eqFile, CURRENT_LIMIT).size());

        // Manually select one of the limits group on side 2 and export again.
        network.getLine("LN").setSelectedOperationalLimitsGroup2("OLS_2");
        eqFile = writeCgmesProfile(network, "EQ", tmpDir, exportParams);

        // Now 2 OperationalLimitsGroup are exported since 2 are selected.
        assertEquals(2, getUniqueMatches(eqFile, OPERATIONAL_LIMIT_SET).size());
        assertEquals(3, getUniqueMatches(eqFile, OPERATIONAL_LIMIT_TYPE).size());
        assertEquals(0, getUniqueMatches(eqFile, ACTIVE_POWER_LIMIT).size());
        assertEquals(6, getUniqueMatches(eqFile, CURRENT_LIMIT).size());
    }

    @Test
    void exportAllLimitsGroupTest() throws IOException {
        // IIDM network:
        //   A Line LN with:
        //   - On side 1, 1 (selected) OperationalLimitsGroup.
        //   - On side 2, 2 (not selected) OperationalLimitsGroup.
        // CGMES export:
        //   When the parameter to export all limits group is set to true, all limits groups are exported, whether selected or not.
        Network network = readCgmesResources(DIR, "multiple_limitsets_on_same_terminal.xml");
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir);

        // All 3 OperationalLimitsGroup are exported, even though only 2 are selected.
        assertEquals(3, getUniqueMatches(eqFile, OPERATIONAL_LIMIT_SET).size());
        assertEquals(3, getUniqueMatches(eqFile, OPERATIONAL_LIMIT_TYPE).size());
        assertEquals(3, getUniqueMatches(eqFile, ACTIVE_POWER_LIMIT).size());
        assertEquals(9, getUniqueMatches(eqFile, CURRENT_LIMIT).size());

        // The CGMES id/name have been correctly exported.
        String regex = "<cim:OperationalLimitSet rdf:ID=\"_OLS_NUM\">.*?<cim:IdentifiedObject.name>(.*?)</cim:IdentifiedObject.name>";
        assertEquals("Spring", getFirstMatch(eqFile, Pattern.compile(regex.replace("NUM", "1"), Pattern.DOTALL)));
        assertEquals("Spring", getFirstMatch(eqFile, Pattern.compile(regex.replace("NUM", "2"), Pattern.DOTALL)));
        assertEquals("Winter", getFirstMatch(eqFile, Pattern.compile(regex.replace("NUM", "3"), Pattern.DOTALL)));
    }

}
