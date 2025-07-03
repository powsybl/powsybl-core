/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static com.powsybl.iidm.network.HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER;
import static com.powsybl.iidm.network.HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class HvdcUpdateTest {

    private static final String DIR = "/update/hvdc/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "hvdc_EQ.xml");
        assertEquals(2, network.getHvdcLineCount());

        assertEq(network);
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "hvdc_EQ.xml", "hvdc_SSH.xml");
        assertEquals(2, network.getHvdcLineCount());

        assertFirstSsh(network);
    }

    @Test
    void importEqTwoSshsAndSvTest() {
        Network network = readCgmesResources(DIR, "hvdc_EQ.xml");
        assertEquals(2, network.getHvdcLineCount());

        assertEq(network);

        readCgmesResources(network, DIR, "hvdc_SSH.xml");
        assertFirstSsh(network);

        readCgmesResources(network, DIR, "hvdc_SSH_1.xml");
        assertSecondSsh(network);

        assertFlowsBeforeSv(network);
        readCgmesResources(network, DIR, "hvdc_SV.xml");
        assertFlowsAfterSv(network);
    }

    private static void assertEq(Network network) {
        assertEqLcc(network.getHvdcLine("DCLineSegment-Lcc"));
        assertEqVsc(network.getHvdcLine("DCLineSegment-Vsc"));
    }

    private static void assertFirstSsh(Network network) {
        assertSshLcc(network.getHvdcLine("DCLineSegment-Lcc"), 360.0, SIDE_1_INVERTER_SIDE_2_RECTIFIER,
                -0.9152494668960571, 0.0,
                0.9340579509735107, 0.0);
        assertSshVsc(network.getHvdcLine("DCLineSegment-Vsc"), 597.24, SIDE_1_INVERTER_SIDE_2_RECTIFIER,
                0.0, 392.54, 0.0, true,
                0.0, 392.54, 0.0, true);
    }

    private static void assertSecondSsh(Network network) {
        assertSshLcc(network.getHvdcLine("DCLineSegment-Lcc"), 420.0, SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                0.9503694176673889, 0.0,
                -0.9194843769073486, 0.0);
        assertSshVsc(network.getHvdcLine("DCLineSegment-Vsc"), 596.4, SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                0.0, 396.54, 0.0, true,
                0.0, 0.0, 30.0, false);
    }

    private static void assertFlowsBeforeSv(Network network) {
        assertFlows(network.getHvdcLine("DCLineSegment-Lcc").getConverterStation1().getTerminal(), Double.NaN, Double.NaN,
                network.getHvdcLine("DCLineSegment-Lcc").getConverterStation2().getTerminal(), Double.NaN, Double.NaN);
        assertFlows(network.getHvdcLine("DCLineSegment-Vsc").getConverterStation1().getTerminal(), Double.NaN, Double.NaN,
                network.getHvdcLine("DCLineSegment-Vsc").getConverterStation2().getTerminal(), Double.NaN, Double.NaN);
    }

    private static void assertFlowsAfterSv(Network network) {
        assertFlows(network.getHvdcLine("DCLineSegment-Lcc").getConverterStation1().getTerminal(), 200.0, 50.0,
                network.getHvdcLine("DCLineSegment-Lcc").getConverterStation2().getTerminal(), -200.1, -50.1);
        assertFlows(network.getHvdcLine("DCLineSegment-Vsc").getConverterStation1().getTerminal(), 100.0, 25.0,
                network.getHvdcLine("DCLineSegment-Vsc").getConverterStation2().getTerminal(), -100.1, -25.1);
    }

    private static void assertEqLcc(HvdcLine hvdcLine) {
        assertNotNull(hvdcLine);
        assertEquals(0.0, hvdcLine.getMaxP());
        assertEquals(SIDE_1_RECTIFIER_SIDE_2_INVERTER, hvdcLine.getConvertersMode());

        assertEquals(HvdcConverterStation.HvdcType.LCC, hvdcLine.getConverterStation1().getHvdcType());
        assertEqLccConverter((LccConverterStation) hvdcLine.getConverterStation1());
        assertEqLccConverter((LccConverterStation) hvdcLine.getConverterStation2());
    }

    private static void assertEqLccConverter(LccConverterStation lccConverterStation) {
        double tol = 0.0000001;
        assertEquals(0.8, lccConverterStation.getPowerFactor(), tol);
        assertEquals(0.0, lccConverterStation.getLossFactor(), tol);
    }

    private static void assertEqVsc(HvdcLine hvdcLine) {
        assertNotNull(hvdcLine);
        assertEquals(0.0, hvdcLine.getMaxP());
        assertEquals(SIDE_1_RECTIFIER_SIDE_2_INVERTER, hvdcLine.getConvertersMode());

        assertEquals(HvdcConverterStation.HvdcType.VSC, hvdcLine.getConverterStation1().getHvdcType());
        assertEqVscConverter((VscConverterStation) hvdcLine.getConverterStation1());
        assertEqVscConverter((VscConverterStation) hvdcLine.getConverterStation2());
    }

    private static void assertEqVscConverter(VscConverterStation vscConverterStation) {
        double tol = 0.0000001;
        assertEquals(0.0, vscConverterStation.getLossFactor(), tol);
        assertNotNull(vscConverterStation.getRegulatingTerminal());
        assertEquals(0.0, vscConverterStation.getReactivePowerSetpoint(), tol);
        assertTrue(Double.isNaN(vscConverterStation.getVoltageSetpoint()));
        assertFalse(vscConverterStation.isVoltageRegulatorOn());
    }

    private static void assertSshLcc(HvdcLine hvdcLine, double maxP, HvdcLine.ConvertersMode convertersMode,
                                     double powerFactor1, double lossFactor1, double powerFactor2, double lossFactor2) {
        assertNotNull(hvdcLine);
        assertEquals(maxP, hvdcLine.getMaxP());
        assertEquals(convertersMode, hvdcLine.getConvertersMode());

        assertEquals(HvdcConverterStation.HvdcType.LCC, hvdcLine.getConverterStation1().getHvdcType());
        assertSshLccConverter((LccConverterStation) hvdcLine.getConverterStation1(), powerFactor1, lossFactor1);
        assertSshLccConverter((LccConverterStation) hvdcLine.getConverterStation2(), powerFactor2, lossFactor2);
    }

    private static void assertSshLccConverter(LccConverterStation lccConverterStation, double powerFactor, double lossFactor) {
        double tol = 0.0000001;
        assertEquals(powerFactor, lccConverterStation.getPowerFactor(), tol);
        assertEquals(lossFactor, lccConverterStation.getLossFactor(), tol);
    }

    private static void assertSshVsc(HvdcLine hvdcLine, double maxP, HvdcLine.ConvertersMode convertersMode,
                                     double lossFactor1, double targetV1, double targetQ1, boolean voltageRegulatorOn1,
                                     double lossFactor2, double targetV2, double targetQ2, boolean voltageRegulatorOn2) {
        assertNotNull(hvdcLine);
        assertEquals(maxP, hvdcLine.getMaxP());
        assertEquals(convertersMode, hvdcLine.getConvertersMode());

        assertEquals(HvdcConverterStation.HvdcType.VSC, hvdcLine.getConverterStation1().getHvdcType());
        assertSshVscConverter((VscConverterStation) hvdcLine.getConverterStation1(), lossFactor1, targetV1, targetQ1, voltageRegulatorOn1);
        assertSshVscConverter((VscConverterStation) hvdcLine.getConverterStation2(), lossFactor2, targetV2, targetQ2, voltageRegulatorOn2);
    }

    private static void assertSshVscConverter(VscConverterStation vscConverterStation, double lossFactor, double targetV, double targetQ, boolean voltageRegulatorOn) {
        double tol = 0.0000001;
        assertEquals(lossFactor, vscConverterStation.getLossFactor(), tol);
        assertEquals(targetV, vscConverterStation.getVoltageSetpoint(), tol);
        assertEquals(targetQ, vscConverterStation.getReactivePowerSetpoint(), tol);
        assertEquals(voltageRegulatorOn, vscConverterStation.isVoltageRegulatorOn());
    }

    private static void assertFlows(Terminal terminal1, double p1, double q1, Terminal terminal2, double p2, double q2) {
        double tol = 0.0000001;
        assertEquals(p1, terminal1.getP(), tol);
        assertEquals(q1, terminal1.getQ(), tol);
        assertEquals(p2, terminal2.getP(), tol);
        assertEquals(q2, terminal2.getQ(), tol);
    }
}
