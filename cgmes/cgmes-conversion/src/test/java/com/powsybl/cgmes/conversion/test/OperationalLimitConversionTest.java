/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
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
        //   When it is set to true (default value), all limits group are exported, whether selected or not.
        Network network = readCgmesResources(DIR, "multiple_limitsets_on_same_terminal.xml");
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.EXPORT_ALL_LIMITS_GROUP, false);
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir, exportParams);

        // Only 1 OperationalLimitsGroup is selected, so only 1 is exported.
        assertEquals(1, getUniqueMatches(eqFile, OPERATIONAL_LIMIT_SET).size());
        assertEquals(3, getUniqueMatches(eqFile, OPERATIONAL_LIMIT_TYPE).size());
        assertEquals(0, getUniqueMatches(eqFile, ACTIVE_POWER_LIMIT).size());
        assertEquals(3, getUniqueMatches(eqFile, CURRENT_LIMIT).size());

        // Manually select one of the limits group on side 2 and check that 2 OperationalLimitsGroup are now exported.
        network.getLine("LN").setSelectedOperationalLimitsGroup2("OLS_2");
        eqFile = writeCgmesProfile(network, "EQ", tmpDir, exportParams);
        assertEquals(2, getUniqueMatches(eqFile, OPERATIONAL_LIMIT_SET).size());
        assertEquals(3, getUniqueMatches(eqFile, OPERATIONAL_LIMIT_TYPE).size());
        assertEquals(0, getUniqueMatches(eqFile, ACTIVE_POWER_LIMIT).size());
        assertEquals(6, getUniqueMatches(eqFile, CURRENT_LIMIT).size());

        // Export all 3 limits groups, regardless of selected (default value of the parameter).
        eqFile = writeCgmesProfile(network, "EQ", tmpDir);
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

    @Test
    void limitSetsAssociatedToTerminalsTest() {
        // CGMES network:
        //   OperationalLimitSet with CurrentLimit associated to the Terminal of:
        //   a DanglingLine DL, a Line ACL, a Switch SW, a TwoWindingTransformer PT2, a ThreeWindingTransformer PT3.
        // IIDM network:
        //   Limits associated to terminals of lines or transformers are imported,
        //   limits associated to terminals of switch are discarded.
        Network network = readCgmesResources(DIR, "limitsets_associated_to_terminals_EQ.xml",
                "limitsets_EQBD.xml", "limitsets_TPBD.xml");

        // OperationalLimitSet on dangling line terminal is imported smoothly.
        assertNotNull(network.getDanglingLine("DL"));
        assertTrue(network.getDanglingLine("DL").getCurrentLimits().isPresent());

        // OperationalLimitSet on ACLineSegment terminals are imported smoothly.
        assertNotNull(network.getLine("ACL"));
        assertTrue(network.getLine("ACL").getCurrentLimits1().isPresent());
        assertTrue(network.getLine("ACL").getCurrentLimits2().isPresent());

        // OperationalLimitSet on PowerTransformers terminals are imported smoothly.
        assertNotNull(network.getTwoWindingsTransformer("PT2"));
        assertTrue(network.getTwoWindingsTransformer("PT2").getCurrentLimits1().isPresent());
        assertTrue(network.getTwoWindingsTransformer("PT2").getCurrentLimits2().isPresent());

        assertNotNull(network.getThreeWindingsTransformer("PT3"));
        assertTrue(network.getThreeWindingsTransformer("PT3").getLeg1().getCurrentLimits().isPresent());
        assertTrue(network.getThreeWindingsTransformer("PT3").getLeg2().getCurrentLimits().isPresent());
        assertTrue(network.getThreeWindingsTransformer("PT3").getLeg3().getCurrentLimits().isPresent());

        // There can't be any limit associated to switches in IIDM, but check anyway that the switch has been imported.
        assertNotNull(network.getSwitch("SW"));
    }

    @Test
    void limitSetsAssociatedToEquipmentsTest() {
        // CGMES network:
        //   OperationalLimitSet with CurrentLimit associated to:
        //   a DanglingLine DL, a Line ACL, a Switch SW, a TwoWindingTransformer PT2, a ThreeWindingTransformer PT3.
        // IIDM network:
        //   Limits associated to lines are imported, limits associated to transformers or switches are discarded.
        Network network = readCgmesResources(DIR, "limitsets_associated_to_equipments_EQ.xml",
                "limitsets_EQBD.xml", "limitsets_TPBD.xml");

        // OperationalLimitSet on dangling line is imported on its single extremity.
        assertNotNull(network.getDanglingLine("DL"));
        assertTrue(network.getDanglingLine("DL").getCurrentLimits().isPresent());

        // OperationalLimitSet on ACLineSegment is imported on its two extremities.
        assertNotNull(network.getLine("ACL"));
        assertTrue(network.getLine("ACL").getCurrentLimits1().isPresent());
        assertTrue(network.getLine("ACL").getCurrentLimits2().isPresent());

        // OperationalLimitSet on PowerTransformers are discarded.
        assertNotNull(network.getTwoWindingsTransformer("PT2"));
        assertFalse(network.getTwoWindingsTransformer("PT2").getCurrentLimits1().isPresent());
        assertFalse(network.getTwoWindingsTransformer("PT2").getCurrentLimits2().isPresent());

        assertNotNull(network.getThreeWindingsTransformer("PT3"));
        assertFalse(network.getThreeWindingsTransformer("PT3").getLeg1().getCurrentLimits().isPresent());
        assertFalse(network.getThreeWindingsTransformer("PT3").getLeg2().getCurrentLimits().isPresent());
        assertFalse(network.getThreeWindingsTransformer("PT3").getLeg3().getCurrentLimits().isPresent());

        // There can't be any limit associated to switches in IIDM, but check anyway that the switch has been imported.
        assertNotNull(network.getSwitch("SW"));
    }

    @Test
    void loadingLimitTest() {
        // CGMES network:
        //   An ACLineSegment ACL with:
        //   - on side 1: CurrentLimit and ApparentPowerLimit (each time patl and tatl).
        //   - on side 2: CurrentLimit (2 patl and 2 tatl of same duration).
        // IIDM network:
        //   Limits are imported. In case of multiple limits with same terminal/kind/duration, the lowest value is kept.
        Network network = readCgmesResources(DIR, "loading_limits.xml");

        // Loading limits on side 1 have been imported smoothly.
        Line line = network.getLine("ACL");
        assertTrue(line.getCurrentLimits1().isPresent());
        assertEquals(100.0, line.getCurrentLimits1().get().getPermanentLimit());
        assertEquals(200.0, line.getCurrentLimits1().get().getTemporaryLimit(600).getValue());

        assertTrue(line.getActivePowerLimits1().isPresent());
        assertEquals(101.0, line.getActivePowerLimits1().get().getPermanentLimit());
        assertEquals(201.0, line.getActivePowerLimits1().get().getTemporaryLimit(600).getValue());

        assertTrue(line.getApparentPowerLimits1().isPresent());
        assertEquals(102.0, line.getApparentPowerLimits1().get().getPermanentLimit());
        assertEquals(202.0, line.getApparentPowerLimits1().get().getTemporaryLimit(600).getValue());

        // When several limits of same kind and duration are defined on the same terminal, only the lowest is kept.
        assertTrue(line.getCurrentLimits2().isPresent());
        assertEquals(99.0, line.getCurrentLimits2().get().getPermanentLimit());
        assertEquals(199.0, line.getCurrentLimits2().get().getTemporaryLimit(600).getValue());
    }

    @Test
    void voltageLimitTest() {
        // CGMES network:
        //   2 BusbarSection BBS_1, BBS_2 in 400 kV VoltageLevel VL_1, VL_2, with high/lowVoltageLimit 420/380 kV.
        //   BBS_1 has an OperationalLimitSet with 2 VoltageLimits 410/390 kV.
        //   BBS_2 has an OperationalLimitSet with 2 VoltageLimits 430/370 kV.
        // IIDM network:
        //   The IIDM VoltageLevel's limit is the most restrictive one between
        //   the CGMES VoltageLevel's limit and the CGMES OperationalLimit value.
        Network network = readCgmesResources(DIR, "voltage_limits.xml");

        // The most restrictive limits for VL_1 are the OperationalLimit (VoltageLimit) values.
        VoltageLevel vl1 = network.getVoltageLevel("VL_1");
        assertNotNull(vl1);
        assertEquals(410.0, vl1.getHighVoltageLimit());
        assertEquals(390.0, vl1.getLowVoltageLimit());

        // The most restrictive limits for VL_2 are the VoltageLevel's high/lowVoltageLimit.
        VoltageLevel vl2 = network.getVoltageLevel("VL_2");
        assertNotNull(vl2);
        assertEquals(420.0, vl2.getHighVoltageLimit());
        assertEquals(380.0, vl2.getLowVoltageLimit());
    }

    @Test
    void missingLimitsTest() {
        // CGMES network:
        //   An ACLineSegment ACL with 1 OperationalLimitSet on each side.
        //   On side 1, the limit set is missing the PATL. On side 2, the limit set is missing the TATL value for 1200s.
        // IIDM network:
        //   PATL are computed when missing as percentage * lowest tatl value.
        //   TATL are discarded when value is missing.
        Network network = readCgmesResources(DIR, "missing_limits.xml");

        // By default, if PATL is missing, it is set to the lowest TATL value.
        Line line = network.getLine("ACL");
        assertTrue(line.getCurrentLimits1().isPresent());
        assertEquals(125, line.getCurrentLimits1().get().getTemporaryLimits().iterator().next().getValue());
        assertEquals(125, line.getCurrentLimits1().get().getPermanentLimit());

        // It the parameter is set, the missing PATL is calculated as percentage (0.80) * lowest tatl value (125) = 100.
        Properties importParams = new Properties();
        importParams.setProperty(CgmesImport.MISSING_PERMANENT_LIMIT_PERCENTAGE, "80");
        network = readCgmesResources(importParams, DIR, "missing_limits.xml");
        line = network.getLine("ACL");
        assertTrue(line.getCurrentLimits1().isPresent());
        assertEquals(125, line.getCurrentLimits1().get().getTemporaryLimits().iterator().next().getValue());
        assertEquals(100, line.getCurrentLimits1().get().getPermanentLimit());

        // TATL for 1200s has no value, the limit is discarded.
        assertTrue(line.getCurrentLimits2().isPresent());
        assertNull(line.getCurrentLimits2().get().getTemporaryLimit(1200));
    }

    @Test
    void limitsCim100Test() {
        // CGMES network:
        //   An ACLineSegment ACL with CurrentLimit and ApparentPowerLimit (each time patl and tatl) on side 1.
        // IIDM network:
        //   Limits are imported smoothly.
        Network network = readCgmesResources(DIR, "limits_cim100.xml");

        // The difference between CIM16 and CIM100 limits lies in the naming of attributes: value and limitType.
        // It is therefore relevant to test that values and type (permanent vs temporary) are correctly converted
        // from the graph to the property bag. Everything else is common and doesn't need to be tested again.
        Line line = network.getLine("ACL");
        assertTrue(line.getCurrentLimits1().isPresent());
        assertEquals(100.0, line.getCurrentLimits1().get().getPermanentLimit());
        assertEquals(200.0, line.getCurrentLimits1().get().getTemporaryLimit(600).getValue());
        assertTrue(line.getApparentPowerLimits1().isPresent());
        assertEquals(102.0, line.getApparentPowerLimits1().get().getPermanentLimit());
        assertEquals(202.0, line.getApparentPowerLimits1().get().getTemporaryLimit(600).getValue());
    }

}
