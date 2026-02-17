/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static com.powsybl.cgmes.conversion.Conversion.PROPERTY_CGMES_ORIGINAL_CLASS;
import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class DanglingLineUpdateTest {

    private static final String DIR = "/update/dangling-line/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "danglingLine_EQ.xml", "danglingLine_EQ_BD.xml");
        assertEquals(4, network.getDanglingLineCount());

        assertEq(network);
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "danglingLine_EQ.xml", "danglingLine_EQ_BD.xml", "danglingLine_SSH.xml");
        assertEquals(4, network.getDanglingLineCount());

        assertFirstSsh(network);
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "danglingLine_EQ.xml", "danglingLine_EQ_BD.xml");
        assertEquals(4, network.getDanglingLineCount());

        assertEq(network);

        readCgmesResources(network, DIR, "danglingLine_SSH.xml");
        assertFirstSsh(network);

        readCgmesResources(network, DIR, "danglingLine_SSH_1.xml");
        assertSecondSsh(network);
    }

    @Test
    void importSvTogetherTest() {
        Network network = readCgmesResources(DIR, "danglingLine_EQ.xml", "danglingLine_EQ_BD.xml", "danglingLine_SSH.xml", "danglingLine_TP.xml", "danglingLine_SV.xml");

        assertEquals(4, network.getDanglingLineCount());
        assertSvTogether(network, 285.2495134203, -68.1683990331, 275.1, 50.5, 400.5, -3, 388.0868627936761, -5.9167013802728095);
    }

    @Test
    void importSvSeparatelyTest() {
        Network network = readCgmesResources(DIR, "danglingLine_EQ.xml", "danglingLine_EQ_BD.xml", "danglingLine_SSH.xml");

        assertEquals(4, network.getDanglingLineCount());

        double tol = 0.0000001;
        DanglingLine acLineSegment = network.getDanglingLine("ACLineSegment");
        assertTrue(Double.isNaN(acLineSegment.getTerminal().getP()));
        assertTrue(Double.isNaN(acLineSegment.getTerminal().getQ()));

        DanglingLine equivalentBranch = network.getDanglingLine("EquivalentBranch");
        assertTrue(Double.isNaN(equivalentBranch.getTerminal().getP()));
        assertTrue(Double.isNaN(equivalentBranch.getTerminal().getQ()));

        DanglingLine powerTransformer = network.getDanglingLine("PowerTransformer");
        assertTrue(Double.isNaN(powerTransformer.getTerminal().getP()));
        assertTrue(Double.isNaN(powerTransformer.getTerminal().getQ()));

        DanglingLine breaker = network.getDanglingLine("Breaker");
        assertEquals(10.0, breaker.getTerminal().getP(), tol);
        assertEquals(5.0, breaker.getTerminal().getQ(), tol);

        readCgmesResources(network, DIR, "danglingLine_TP.xml", "danglingLine_SV.xml");

        assertEquals(0.0503090159, acLineSegment.getTerminal().getP(), tol);
        assertEquals(-145.5845194744, acLineSegment.getTerminal().getQ(), tol);

        assertEquals(275.1, equivalentBranch.getTerminal().getP(), tol);
        assertEquals(50.5, equivalentBranch.getTerminal().getQ(), tol);
        assertBusVoltage(equivalentBranch.getTerminal().getBusView().getBus());
        assertTrue(checkBoundaryBusVoltage(equivalentBranch, 388.0868627936761, -5.9167013802728095));

        assertTrue(Double.isNaN(powerTransformer.getTerminal().getP()));
        assertTrue(Double.isNaN(powerTransformer.getTerminal().getQ()));
        assertEquals(0.0, breaker.getTerminal().getP(), tol);
        assertEquals(0.0, breaker.getTerminal().getQ(), tol);
    }

    @Test
    void usePreviousValuesTest() {
        Network network = readCgmesResources(DIR, "danglingLine_EQ.xml", "danglingLine_EQ_BD.xml", "danglingLine_SSH.xml", "danglingLine_TP.xml", "danglingLine_SV.xml");
        assertEquals(4, network.getDanglingLineCount());
        assertFirstSsh(network);

        // As the TP file is not read, voltage values cannot be assigned
        readCgmesResources(network, DIR, "danglingLine_SSH.xml", "danglingLine_SV.xml");
        assertFirstSsh(network);
        assertSvTogether(network, Double.NaN, Double.NaN, 275.1, 50.5, Double.NaN, Double.NaN, Double.NaN, Double.NaN);

        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.use-previous-values-during-update", "true");
        readCgmesResources(network, properties, DIR, "../empty_SSH.xml", "../empty_SV.xml");
        assertFirstSsh(network);
        assertSvTogether(network, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    @Test
    void removeAllPropertiesAndAliasesTest() {
        Network network = readCgmesResources(DIR, "danglingLine_EQ.xml", "danglingLine_EQ_BD.xml", "danglingLine_SSH.xml");
        assertPropertiesAndAliasesEmpty(network, false);

        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.remove-properties-and-aliases-after-import", "true");
        network = readCgmesResources(properties, DIR, "danglingLine_EQ.xml", "danglingLine_EQ_BD.xml", "danglingLine_SSH.xml");
        assertPropertiesAndAliasesEmpty(network, true);
    }

    private static void assertPropertiesAndAliasesEmpty(Network network, boolean expected) {
        assertEquals(expected, network.getSubstationStream().allMatch(substation -> substation.getPropertyNames().isEmpty()));
        assertTrue(network.getSubstationStream().allMatch(substation -> substation.getAliases().isEmpty()));

        assertEquals(expected, network.getDanglingLineStream().allMatch(dl -> dl.getPropertyNames().isEmpty()));
        assertEquals(expected, network.getDanglingLineStream().allMatch(dl -> dl.getAliases().isEmpty()));
        assertEquals(expected, network.getDanglingLineStream().allMatch(dl -> dl.getOperationalLimitsGroups().stream().allMatch(op -> op.getPropertyNames().isEmpty())));
    }

    private static void assertEq(Network network) {
        assertEq(network.getDanglingLine("ACLineSegment"));
        assertNotDefinedLimits(network.getDanglingLine("ACLineSegment"));

        assertEq(network.getDanglingLine("EquivalentBranch"));
        assertNotDefinedLimits(network.getDanglingLine("EquivalentBranch"));

        assertEq(network.getDanglingLine("PowerTransformer"));
        assertNotDefinedLimits(network.getDanglingLine("PowerTransformer"));

        assertEq(network.getDanglingLine("Breaker"));
        assertDefinedActivePowerLimits(network.getDanglingLine("Breaker"), new ActivePowerLimit(90.0, 900, 108.0));
    }

    private static void assertFirstSsh(Network network) {
        assertSsh(network.getDanglingLine("ACLineSegment"), 284.5, 70.5, false, Double.NaN, Double.NaN, Double.NaN, false);
        assertNotDefinedLimits(network.getDanglingLine("ACLineSegment"));

        assertSsh(network.getDanglingLine("EquivalentBranch"), 0.0, 0.0, true, -275.0, -50.0, 405.0, true);
        assertNotDefinedLimits(network.getDanglingLine("EquivalentBranch"));

        assertSsh(network.getDanglingLine("PowerTransformer"), 0.0, 0.0, true, -100.0, -25.0, 225.0, true);
        assertNotDefinedLimits(network.getDanglingLine("PowerTransformer"));

        assertSsh(network.getDanglingLine("Breaker"), 0.0, 0.0, true, -10.0, -5.0, 402.0, true);
        assertDefinedActivePowerLimits(network.getDanglingLine("Breaker"), new ActivePowerLimit(89.0, 900, 107.0));
    }

    private static void assertSecondSsh(Network network) {
        assertSsh(network.getDanglingLine("ACLineSegment"), 280.0, 70.0, false, Double.NaN, Double.NaN, Double.NaN, false);
        assertNotDefinedLimits(network.getDanglingLine("ACLineSegment"));

        assertSsh(network.getDanglingLine("EquivalentBranch"), 0.0, 0.0, true, -270.0, -55.0, 410.0, false);
        assertNotDefinedLimits(network.getDanglingLine("EquivalentBranch"));

        assertSsh(network.getDanglingLine("PowerTransformer"), 0.0, 0.0, true, -105.0, -20.0, 227.0, true);
        assertNotDefinedLimits(network.getDanglingLine("PowerTransformer"));

        assertSsh(network.getDanglingLine("Breaker"), 0.0, 0.0, true, -15.0, -3.0, 403.0, true);
        assertDefinedActivePowerLimits(network.getDanglingLine("Breaker"), new ActivePowerLimit(91.0, 900, 109.0));
    }

    private static void assertSvTogether(Network network, double acLineSegmentP, double acLineSegmentQ, double equivalentBranchP, double equivalentBranchQ, double v, double angle, double boundaryV, double boundaryAngle) {
        double tol = 0.0000001;
        DanglingLine acLineSegment = network.getDanglingLine("ACLineSegment");
        assertEquals(acLineSegmentP, acLineSegment.getTerminal().getP(), tol);
        assertEquals(acLineSegmentQ, acLineSegment.getTerminal().getQ(), tol);

        DanglingLine equivalentBranch = network.getDanglingLine("EquivalentBranch");
        assertEquals(equivalentBranchP, equivalentBranch.getTerminal().getP(), tol);
        assertEquals(equivalentBranchQ, equivalentBranch.getTerminal().getQ(), tol);
        assertBusVoltage(equivalentBranch.getTerminal().getBusView().getBus(), v, angle);
        assertTrue(checkBoundaryBusVoltage(equivalentBranch, boundaryV, boundaryAngle));

        DanglingLine powerTransformer = network.getDanglingLine("PowerTransformer");
        assertTrue(Double.isNaN(powerTransformer.getTerminal().getP()));
        assertTrue(Double.isNaN(powerTransformer.getTerminal().getQ()));

        DanglingLine breaker = network.getDanglingLine("Breaker");
        assertEquals(10.0, breaker.getTerminal().getP(), tol);
        assertEquals(5.0, breaker.getTerminal().getQ(), tol);
    }

    private static void assertEq(DanglingLine danglingLine) {
        assertNotNull(danglingLine);
        assertTrue(Double.isNaN(danglingLine.getP0()));
        assertTrue(Double.isNaN(danglingLine.getQ0()));
        if (danglingLine.getGeneration() != null) {
            assertTrue(Double.isNaN(danglingLine.getGeneration().getTargetV()));
            assertTrue(Double.isNaN(danglingLine.getGeneration().getTargetP()));
            assertTrue(Double.isNaN(danglingLine.getGeneration().getTargetQ()));
            assertFalse(danglingLine.getGeneration().isVoltageRegulationOn());
        }
        assertNotNull(danglingLine.getProperty(PROPERTY_CGMES_ORIGINAL_CLASS));
    }

    private static void assertSsh(DanglingLine danglingLine, double p0, double q0, boolean withGeneration, double targetP, double targetQ, double targetV, boolean isRegulatingOn) {
        assertNotNull(danglingLine);
        double tol = 0.0000001;
        assertEquals(p0, danglingLine.getP0(), tol);
        assertEquals(q0, danglingLine.getQ0(), tol);
        if (withGeneration) {
            assertNotNull(danglingLine.getGeneration());
            assertEquals(targetP, danglingLine.getGeneration().getTargetP(), tol);
            assertEquals(targetQ, danglingLine.getGeneration().getTargetQ(), tol);
            assertEquals(targetV, danglingLine.getGeneration().getTargetV(), tol);
            assertEquals(isRegulatingOn, danglingLine.getGeneration().isVoltageRegulationOn());
        } else {
            assertNull(danglingLine.getGeneration());
        }
    }

    private static void assertDefinedActivePowerLimits(DanglingLine danglingLine, ActivePowerLimit activePowerLimit) {
        assertNull(danglingLine.getCurrentLimits().orElse(null));
        assertNull(danglingLine.getApparentPowerLimits().orElse(null));

        assertEquals(1, danglingLine.getOperationalLimitsGroups().size());
        assertNotNull(danglingLine.getActivePowerLimits().orElse(null));
        if (danglingLine.getActivePowerLimits().isPresent()) {
            assertEquals(activePowerLimit.ptalValue, danglingLine.getActivePowerLimits().get().getPermanentLimit());
            assertEquals(1, danglingLine.getActivePowerLimits().get().getTemporaryLimits().size());
            assertEquals(activePowerLimit.tatlDuration, danglingLine.getActivePowerLimits().get().getTemporaryLimits().iterator().next().getAcceptableDuration());
            assertEquals(activePowerLimit.tatlValue, danglingLine.getActivePowerLimits().get().getTemporaryLimits().iterator().next().getValue());
        }
    }

    private static void assertNotDefinedLimits(DanglingLine danglingLine) {
        assertNull(danglingLine.getCurrentLimits().orElse(null));
        assertNull(danglingLine.getApparentPowerLimits().orElse(null));
        assertNull(danglingLine.getActivePowerLimits().orElse(null));
    }

    private record ActivePowerLimit(double ptalValue, int tatlDuration, double tatlValue) {
    }

    private static void assertBusVoltage(Bus bus) {
        double tol = 0.0000001;
        assertEquals(400.5, bus.getV(), tol);
        assertEquals(-3.0, bus.getAngle(), tol);
    }

    private static void assertBusVoltage(Bus bus, double v, double angle) {
        double tol = 0.0000001;
        assertEquals(v, bus.getV(), tol);
        assertEquals(angle, bus.getAngle(), tol);
    }

    private static boolean checkBoundaryBusVoltage(DanglingLine danglingLine, double v, double angle) {
        double tol = 0.0000001;
        String voltageProperty = danglingLine.getProperty(CgmesNames.VOLTAGE);
        String angleProperty = danglingLine.getProperty(CgmesNames.ANGLE);
        assertEquals(v, voltageProperty != null ? Double.parseDouble(voltageProperty) : Double.NaN, tol);
        assertEquals(angle, angleProperty != null ? Double.parseDouble(angleProperty) : Double.NaN, tol);
        return true;
    }
}
