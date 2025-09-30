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

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class TieLineUpdateTest {

    private static final String DIR = "/update/tie-line/";

    // When there are more than two ACLineSegments connected to a boundary node, one DanglingLine is created for each ACLineSegment.
    // If the EQ and SSH files are imported together, a TieLine is created when there are exactly two ACLineSegments connected to the boundary node.
    // However, if EQ and SSH are imported separately, no TieLines are created in this configuration, because the connected status is only available
    // in the SSH file and no equipment is created during the update process.
    // In the tests, only the characteristics of the permanent TieLine are verified.
    private static void assertEqCount(Network network, int tieLines, int pairedDanglingLines) {
        assertEquals(tieLines, network.getTieLineCount());
        assertEquals(5, network.getDanglingLineCount());
        assertEquals(pairedDanglingLines, network.getDanglingLineStream().filter(DanglingLine::isPaired).count());
    }

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "tieLine_EQ.xml", "tieLine_EQ_BD.xml");
        assertEqCount(network, 1, 2);

        assertEq(network);
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "tieLine_EQ.xml", "tieLine_EQ_BD.xml", "tieLine_SSH.xml");
        assertEqCount(network, 2, 4);

        assertFirstSsh(network);
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "tieLine_EQ.xml", "tieLine_EQ_BD.xml");
        assertEqCount(network, 1, 2);

        assertEq(network);

        readCgmesResources(network, DIR, "tieLine_SSH.xml");
        assertFirstSsh(network);

        readCgmesResources(network, DIR, "tieLine_SSH_1.xml");
        assertSecondSsh(network);
    }

    @Test
    void importSvTogetherTest() {
        Network network = readCgmesResources(DIR, "tieLine_EQ.xml", "tieLine_EQ_BD.xml", "tieLine_SSH.xml", "tieLine_TP.xml", "tieLine_SV.xml");
        assertEqCount(network, 2, 4);

        TieLine tieLine = network.getTieLine("ACLineSegment-1 + ACLineSegment-2");
        assertSv(tieLine);
    }

    @Test
    void importSvSeparatelyTest() {
        Network network = readCgmesResources(DIR, "tieLine_EQ.xml", "tieLine_EQ_BD.xml", "tieLine_SSH.xml");
        assertEqCount(network, 2, 4);

        TieLine tieLine = network.getTieLine("ACLineSegment-1 + ACLineSegment-2");
        assertFlow(tieLine.getDanglingLine1(), Double.NaN, Double.NaN);
        assertFlow(tieLine.getDanglingLine2(), Double.NaN, Double.NaN);
        assertBusVoltage(tieLine.getDanglingLine1().getTerminal().getBusView().getBus(), Double.NaN, Double.NaN);
        assertBusVoltage(tieLine.getDanglingLine2().getTerminal().getBusView().getBus(), Double.NaN, Double.NaN);
        assertBoundaryBusVoltage(tieLine.getDanglingLine1(), Double.NaN, Double.NaN);
        assertBoundaryBusVoltage(tieLine.getDanglingLine2(), Double.NaN, Double.NaN);

        readCgmesResources(network, DIR, "tieLine_TP.xml", "tieLine_SV.xml");
        assertSv(tieLine);
    }

    private static void assertSv(TieLine tieLine) {
        assertFlow(tieLine.getDanglingLine1(), 275.1, 50.5);
        assertFlow(tieLine.getDanglingLine2(), -275.0, -50.0);
        assertBusVoltage(tieLine.getDanglingLine1().getTerminal().getBusView().getBus(), 400.5, -3.0);
        assertBusVoltage(tieLine.getDanglingLine2().getTerminal().getBusView().getBus(), 402.5, -5.0);
        assertBoundaryBusVoltage(tieLine.getDanglingLine1(), 401.5130326083143, -4.023034681728034);
        assertBoundaryBusVoltage(tieLine.getDanglingLine2(), 401.5130326083143, -4.023034681728034);
    }

    private static void assertEq(Network network) {
        assertEq(network.getTieLine("ACLineSegment-1 + ACLineSegment-2").getDanglingLine1(), new ActivePowerLimit(90.0, 108.0));
        assertEq(network.getTieLine("ACLineSegment-1 + ACLineSegment-2").getDanglingLine2(), new ActivePowerLimit(91.0, 109.0));
    }

    private static void assertFirstSsh(Network network) {
        assertSsh(network.getTieLine("ACLineSegment-1 + ACLineSegment-2").getDanglingLine1());
        assertSsh(network.getTieLine("ACLineSegment-1 + ACLineSegment-2").getDanglingLine2());
        assertDefinedActivePowerLimits(network.getTieLine("ACLineSegment-1 + ACLineSegment-2").getDanglingLine1(), new ActivePowerLimit(89.0, 107.0));
        assertDefinedActivePowerLimits(network.getTieLine("ACLineSegment-1 + ACLineSegment-2").getDanglingLine2(), new ActivePowerLimit(92.0, 110.0));
    }

    private static void assertSecondSsh(Network network) {
        assertSsh(network.getTieLine("ACLineSegment-1 + ACLineSegment-2").getDanglingLine1());
        assertSsh(network.getTieLine("ACLineSegment-1 + ACLineSegment-2").getDanglingLine2());
        assertDefinedActivePowerLimits(network.getTieLine("ACLineSegment-1 + ACLineSegment-2").getDanglingLine1(), new ActivePowerLimit(88.0, 106.0));
        assertDefinedActivePowerLimits(network.getTieLine("ACLineSegment-1 + ACLineSegment-2").getDanglingLine2(), new ActivePowerLimit(93.0, 111.0));
    }

    private static void assertEq(DanglingLine danglingLine, ActivePowerLimit activePowerLimit) {
        assertNotNull(danglingLine);
        assertTrue(Double.isNaN(danglingLine.getP0()));
        assertTrue(Double.isNaN(danglingLine.getQ0()));
        assertNull(danglingLine.getGeneration());
        assertDefinedActivePowerLimits(danglingLine, activePowerLimit);
        assertTrue(Double.isNaN(danglingLine.getTerminal().getP()));
        assertTrue(Double.isNaN(danglingLine.getTerminal().getQ()));
    }

    private static void assertSsh(DanglingLine danglingLine) {
        assertNotNull(danglingLine);
        double tol = 0.0000001;
        assertEquals(0.0, danglingLine.getP0(), tol);
        assertEquals(0.0, danglingLine.getQ0(), tol);
        assertNull(danglingLine.getGeneration());
    }

    private static void assertDefinedActivePowerLimits(DanglingLine danglingLine, ActivePowerLimit activePowerLimit) {
        assertNull(danglingLine.getCurrentLimits().orElse(null));
        assertNull(danglingLine.getApparentPowerLimits().orElse(null));

        assertEquals(1, danglingLine.getOperationalLimitsGroups().size());
        assertNotNull(danglingLine.getActivePowerLimits().orElse(null));
        if (danglingLine.getActivePowerLimits().isPresent()) {
            assertEquals(activePowerLimit.ptalValue, danglingLine.getActivePowerLimits().get().getPermanentLimit());
            assertEquals(1, danglingLine.getActivePowerLimits().get().getTemporaryLimits().size());
            assertEquals(900, danglingLine.getActivePowerLimits().get().getTemporaryLimits().iterator().next().getAcceptableDuration());
            assertEquals(activePowerLimit.tatlValue, danglingLine.getActivePowerLimits().get().getTemporaryLimits().iterator().next().getValue());
        }
    }

    private record ActivePowerLimit(double ptalValue, double tatlValue) {
    }

    private static void assertBusVoltage(Bus bus, double v, double angle) {
        double tol = 0.0000001;
        assertEquals(v, bus.getV(), tol);
        assertEquals(angle, bus.getAngle(), tol);
    }

    private static void assertFlow(DanglingLine danglingLine, double p, double q) {
        double tol = 0.0000001;
        assertEquals(p, danglingLine.getTerminal().getP(), tol);
        assertEquals(q, danglingLine.getTerminal().getQ(), tol);
    }

    private static void assertBoundaryBusVoltage(DanglingLine danglingLine, double v, double angle) {
        double tol = 0.0000001;
        double vActual = danglingLine.getProperty(CgmesNames.VOLTAGE) != null ? Double.parseDouble(danglingLine.getProperty(CgmesNames.VOLTAGE)) : Double.NaN;
        double angleActual = danglingLine.getProperty(CgmesNames.ANGLE) != null ? Double.parseDouble(danglingLine.getProperty(CgmesNames.ANGLE)) : Double.NaN;
        assertEquals(v, vActual, tol);
        assertEquals(angle, angleActual, tol);
    }
}
