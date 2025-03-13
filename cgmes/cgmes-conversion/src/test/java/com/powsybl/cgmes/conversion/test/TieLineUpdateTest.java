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

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "tieLine_EQ.xml", "tieLine_EQ_BD.xml");
        assertEquals(1, network.getTieLineCount());

        TieLine tieLine = network.getTieLine("ACLineSegment-1 + ACLineSegment-2");
        assertTrue(checkEq(tieLine.getDanglingLine1(), new ActivePowerLimit(90.0, 900, 108.0)));
        assertTrue(checkEq(tieLine.getDanglingLine2(), new ActivePowerLimit(91.0, 901, 109.0)));
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "tieLine_EQ.xml", "tieLine_EQ_BD.xml", "tieLine_SSH.xml");
        assertEquals(1, network.getTieLineCount());

        TieLine tieLine = network.getTieLine("ACLineSegment-1 + ACLineSegment-2");
        assertTrue(checkSsh(tieLine.getDanglingLine1()));
        assertTrue(checkSsh(tieLine.getDanglingLine2()));
        assertTrue(checkDefinedActivePowerLimits(tieLine.getDanglingLine1(), new ActivePowerLimit(89.0, 900, 107.0)));
        assertTrue(checkDefinedActivePowerLimits(tieLine.getDanglingLine2(), new ActivePowerLimit(92.0, 901, 110.0)));
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "tieLine_EQ.xml", "tieLine_EQ_BD.xml");
        assertEquals(1, network.getTieLineCount());

        TieLine tieLine = network.getTieLine("ACLineSegment-1 + ACLineSegment-2");
        assertTrue(checkEq(tieLine.getDanglingLine1(), new ActivePowerLimit(90.0, 900, 108.0)));
        assertTrue(checkEq(tieLine.getDanglingLine2(), new ActivePowerLimit(91.0, 901, 109.0)));

        readCgmesResources(network, DIR, "tieLine_SSH.xml");
        assertTrue(checkSsh(tieLine.getDanglingLine1()));
        assertTrue(checkSsh(tieLine.getDanglingLine2()));
        assertTrue(checkDefinedActivePowerLimits(tieLine.getDanglingLine1(), new ActivePowerLimit(89.0, 900, 107.0)));
        assertTrue(checkDefinedActivePowerLimits(tieLine.getDanglingLine2(), new ActivePowerLimit(92.0, 901, 110.0)));

        readCgmesResources(network, DIR, "tieLine_SSH_1.xml");
        assertTrue(checkSsh(tieLine.getDanglingLine1()));
        assertTrue(checkSsh(tieLine.getDanglingLine2()));
        assertTrue(checkDefinedActivePowerLimits(tieLine.getDanglingLine1(), new ActivePowerLimit(88.0, 900, 106.0)));
        assertTrue(checkDefinedActivePowerLimits(tieLine.getDanglingLine2(), new ActivePowerLimit(93.0, 901, 111.0)));
    }

    @Test
    void importSvTogetherTest() {
        Network network = readCgmesResources(DIR, "tieLine_EQ.xml", "tieLine_EQ_BD.xml", "tieLine_SSH.xml", "tieLine_TP.xml", "tieLine_SV.xml");

        assertEquals(1, network.getTieLineCount());

        TieLine tieLine = network.getTieLine("ACLineSegment-1 + ACLineSegment-2");
        assertTrue(checkFlow(tieLine.getDanglingLine1(), 275.1, 50.5));
        assertTrue(checkFlow(tieLine.getDanglingLine2(), -275.0, -50.0));
        assertTrue(checkBusVoltage(tieLine.getDanglingLine1().getTerminal().getBusView().getBus(), 400.5, -3.0));
        assertTrue(checkBusVoltage(tieLine.getDanglingLine2().getTerminal().getBusView().getBus(), 402.5, -5.0));
        assertTrue(checkBoundaryBusVoltage(tieLine.getDanglingLine1(), 401.5130326083143, -4.023034681728034));
        assertTrue(checkBoundaryBusVoltage(tieLine.getDanglingLine2(), 401.5130326083143, -4.023034681728034));
    }

    @Test
    void importSvSeparatelyTest() {
        Network network = readCgmesResources(DIR, "tieLine_EQ.xml", "tieLine_EQ_BD.xml", "tieLine_SSH.xml");
        assertEquals(1, network.getTieLineCount());

        TieLine tieLine = network.getTieLine("ACLineSegment-1 + ACLineSegment-2");
        assertTrue(checkFlow(tieLine.getDanglingLine1(), Double.NaN, Double.NaN));
        assertTrue(checkFlow(tieLine.getDanglingLine2(), Double.NaN, Double.NaN));
        assertTrue(checkBusVoltage(tieLine.getDanglingLine1().getTerminal().getBusView().getBus(), Double.NaN, Double.NaN));
        assertTrue(checkBusVoltage(tieLine.getDanglingLine2().getTerminal().getBusView().getBus(), Double.NaN, Double.NaN));
        assertTrue(checkBoundaryBusVoltage(tieLine.getDanglingLine1(), Double.NaN, Double.NaN));
        assertTrue(checkBoundaryBusVoltage(tieLine.getDanglingLine2(), Double.NaN, Double.NaN));

        readCgmesResources(network, DIR, "tieLine_TP.xml", "tieLine_SV.xml");

        assertTrue(checkFlow(tieLine.getDanglingLine1(), 275.1, 50.5));
        assertTrue(checkFlow(tieLine.getDanglingLine2(), -275.0, -50.0));
        assertTrue(checkBusVoltage(tieLine.getDanglingLine1().getTerminal().getBusView().getBus(), 400.5, -3.0));
        assertTrue(checkBusVoltage(tieLine.getDanglingLine2().getTerminal().getBusView().getBus(), 402.5, -5.0));
        assertTrue(checkBoundaryBusVoltage(tieLine.getDanglingLine1(), 401.5130326083143, -4.023034681728034));
        assertTrue(checkBoundaryBusVoltage(tieLine.getDanglingLine2(), 401.5130326083143, -4.023034681728034));
    }

    private static boolean checkEq(DanglingLine danglingLine, ActivePowerLimit activePowerLimit) {
        assertNotNull(danglingLine);
        assertTrue(Double.isNaN(danglingLine.getP0()));
        assertTrue(Double.isNaN(danglingLine.getQ0()));
        assertNull(danglingLine.getGeneration());
        assertTrue(checkDefinedActivePowerLimits(danglingLine, activePowerLimit));
        assertTrue(Double.isNaN(danglingLine.getTerminal().getP()));
        assertTrue(Double.isNaN(danglingLine.getTerminal().getQ()));
        return true;
    }

    private static boolean checkSsh(DanglingLine danglingLine) {
        assertNotNull(danglingLine);
        double tol = 0.0000001;
        assertEquals(0.0, danglingLine.getP0(), tol);
        assertEquals(0.0, danglingLine.getQ0(), tol);
        assertNull(danglingLine.getGeneration());
        return true;
    }

    private static boolean checkDefinedActivePowerLimits(DanglingLine danglingLine, ActivePowerLimit activePowerLimit) {
        assertNull(danglingLine.getCurrentLimits().orElse(null));
        assertNull(danglingLine.getApparentPowerLimits().orElse(null));

        assertEquals(1, danglingLine.getOperationalLimitsGroups().size());
        assertNotNull(danglingLine.getActivePowerLimits().orElse(null));
        if (danglingLine.getActivePowerLimits().isPresent()) {
            assertEquals(activePowerLimit.ptalValue, danglingLine.getActivePowerLimits().get().getPermanentLimit());
            assertEquals(1, danglingLine.getActivePowerLimits().get().getTemporaryLimits().size());
            assertEquals(activePowerLimit.tatlDuration, danglingLine.getActivePowerLimits().get().getTemporaryLimits().iterator().next().getAcceptableDuration());
            assertEquals(activePowerLimit.tatlValue, danglingLine.getActivePowerLimits().get().getTemporaryLimits().iterator().next().getValue());
            return true;
        }
        return false;
    }

    private record ActivePowerLimit(double ptalValue, int tatlDuration, double tatlValue) {
    }

    private static boolean checkBusVoltage(Bus bus, double v, double angle) {
        double tol = 0.0000001;
        assertEquals(v, bus.getV(), tol);
        assertEquals(angle, bus.getAngle(), tol);
        return true;
    }

    private static boolean checkFlow(DanglingLine danglingLine, double p, double q) {
        double tol = 0.0000001;
        assertEquals(p, danglingLine.getTerminal().getP(), tol);
        assertEquals(q, danglingLine.getTerminal().getQ(), tol);
        return true;
    }

    private static boolean checkBoundaryBusVoltage(DanglingLine danglingLine, double v, double angle) {
        double tol = 0.0000001;
        double vActual = danglingLine.getProperty(CgmesNames.VOLTAGE) != null ? Double.parseDouble(danglingLine.getProperty(CgmesNames.VOLTAGE)) : Double.NaN;
        double angleActual = danglingLine.getProperty(CgmesNames.ANGLE) != null ? Double.parseDouble(danglingLine.getProperty(CgmesNames.ANGLE)) : Double.NaN;
        assertEquals(v, vActual, tol);
        assertEquals(angle, angleActual, tol);
        return true;
    }
}
