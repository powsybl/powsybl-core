/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
class VoltageRegulationTest extends AbstractSerDeTest {

    private static final String DIR = "/issues/voltageRegulation/";

    @Test
    void generatorVoltageRegulationEq() {
        // EQ only import: regulation is created without targets and not enabled
        Network network = readCgmesResources(DIR, "generator_EQ.xml");

        // G0: no regulation (not CGMES compliant)
        Generator g0 = network.getGenerator("SM_0");
        assertLocalTargets(g0, Double.NaN, Double.NaN);
        VoltageRegulation reg0 = g0.getVoltageRegulation();
        assertNull(reg0);

        // G1: local voltage regulation
        Generator g1 = network.getGenerator("SM_1");
        assertLocalTargets(g1, Double.NaN, Double.NaN);
        VoltageRegulation reg1 = g1.getVoltageRegulation();
        assertVoltageRegulation(reg1, RegulationMode.VOLTAGE, null, Double.NaN, Double.NaN, false);

        // G2: remote voltage regulation
        Generator g2 = network.getGenerator("SM_2");
        assertLocalTargets(g2, Double.NaN, Double.NaN);
        VoltageRegulation reg2 = g2.getVoltageRegulation();
        assertVoltageRegulation(reg2, RegulationMode.VOLTAGE, "BBS", Double.NaN, Double.NaN, false);

        // G3: local reactive power regulation
        Generator g3 = network.getGenerator("SM_3");
        assertLocalTargets(g3, Double.NaN, Double.NaN);
        VoltageRegulation reg3 = g3.getVoltageRegulation();
        assertVoltageRegulation(reg3, RegulationMode.REACTIVE_POWER, "SM_3", Double.NaN, Double.NaN, false);

        // G4: remote reactive power regulation
        Generator g4 = network.getGenerator("SM_4");
        assertLocalTargets(g4, Double.NaN, Double.NaN);
        VoltageRegulation reg4 = g4.getVoltageRegulation();
        assertVoltageRegulation(reg4, RegulationMode.REACTIVE_POWER, "PT", Double.NaN, Double.NaN, false);
    }

    @Test
    void generatorVoltageRegulationEqAndSsh() {
        // Full import: regulation is created with correct targets and enabled.
        Network network = readCgmesResources(DIR, "generator_EQ.xml", "generator_SSH.xml");

        // G0: no regulation (not CGMES compliant)
        Generator g0 = network.getGenerator("SM_0");
        assertLocalTargets(g0, 10, Double.NaN);
        VoltageRegulation reg0 = g0.getVoltageRegulation();
        assertNull(reg0);

        // G1: local voltage regulation
        Generator g1 = network.getGenerator("SM_1");
        assertLocalTargets(g1, 10, 400);
        VoltageRegulation reg1 = g1.getVoltageRegulation();
        assertVoltageRegulation(reg1, RegulationMode.VOLTAGE, null, Double.NaN, Double.NaN, true);

        // G2: remote voltage regulation
        Generator g2 = network.getGenerator("SM_2");
        assertLocalTargets(g2, 10, Double.NaN);
        VoltageRegulation reg2 = g2.getVoltageRegulation();
        assertVoltageRegulation(reg2, RegulationMode.VOLTAGE, "BBS", 400, Double.NaN, true);

        // G3: local reactive power regulation
        Generator g3 = network.getGenerator("SM_3");
        assertLocalTargets(g3, 10, Double.NaN);
        VoltageRegulation reg3 = g3.getVoltageRegulation();
        assertVoltageRegulation(reg3, RegulationMode.REACTIVE_POWER, "SM_3", 10, Double.NaN, true);

        // G4: remote reactive power regulation
        Generator g4 = network.getGenerator("SM_4");
        assertLocalTargets(g4, 10, Double.NaN);
        VoltageRegulation reg4 = g4.getVoltageRegulation();
        assertVoltageRegulation(reg4, RegulationMode.REACTIVE_POWER, "PT", 20, Double.NaN, true);
    }

    @Test
    void shuntCompensatorVoltageRegulationEq() {
        // EQ only import: regulation is created without targets and not enabled
        Network network = readCgmesResources(DIR, "shuntCompensator_EQ.xml");

        // SC0: no regulation (not CGMES compliant)
        ShuntCompensator sc0 = network.getShuntCompensator("LSC_0");
        assertTrue(Double.isNaN(sc0.getLocalTargetV()));
        VoltageRegulation reg0 = sc0.getVoltageRegulation();
        assertNull(reg0);

        // SC1: local voltage regulation
        ShuntCompensator sc1 = network.getShuntCompensator("LSC_1");
        assertTrue(Double.isNaN(sc1.getLocalTargetV()));
        VoltageRegulation reg1 = sc1.getVoltageRegulation();
        assertVoltageRegulation(reg1, RegulationMode.VOLTAGE, null, Double.NaN, Double.NaN, false);

        // SC2: remote voltage regulation
        ShuntCompensator sc2 = network.getShuntCompensator("LSC_2");
        assertTrue(Double.isNaN(sc2.getLocalTargetV()));
        VoltageRegulation reg2 = sc2.getVoltageRegulation();
        assertVoltageRegulation(reg2, RegulationMode.VOLTAGE, "BBS", Double.NaN, Double.NaN, false);
    }

    @Test
    void shuntCompensatorVoltageRegulationEqAndSsh() {
        // Full import: regulation is created with correct targets and enabled.
        Network network = readCgmesResources(DIR, "shuntCompensator_EQ.xml", "shuntCompensator_SSH.xml");

        // SC0: no regulation (not CGMES compliant)
        ShuntCompensator sc0 = network.getShuntCompensator("LSC_0");
        assertTrue(Double.isNaN(sc0.getLocalTargetV()));
        VoltageRegulation reg0 = sc0.getVoltageRegulation();
        assertNull(reg0);

        // SC1: local voltage regulation
        ShuntCompensator sc1 = network.getShuntCompensator("LSC_1");
        assertEquals(400, sc1.getLocalTargetV());
        VoltageRegulation reg1 = sc1.getVoltageRegulation();
        assertVoltageRegulation(reg1, RegulationMode.VOLTAGE, null, Double.NaN, 2.0, true);

        // SC2: remote voltage regulation
        ShuntCompensator sc2 = network.getShuntCompensator("LSC_2");
        assertTrue(Double.isNaN(sc2.getLocalTargetV()));
        VoltageRegulation reg2 = sc2.getVoltageRegulation();
        assertVoltageRegulation(reg2, RegulationMode.VOLTAGE, "BBS", 400, 2.0, true);
    }

    @Test
    void staticVarCompensatorVoltageRegulationEq() {
        // EQ only import: regulation is created without targets and not enabled
        Network network = readCgmesResources(DIR, "staticVarCompensator_EQ.xml");

        // SVC1: local voltage regulation
        StaticVarCompensator svc1 = network.getStaticVarCompensator("SVC_1");
        assertLocalTargets(svc1, Double.NaN, Double.NaN);
        VoltageRegulation reg1 = svc1.getVoltageRegulation();
        assertVoltageRegulation(reg1, RegulationMode.VOLTAGE, null, Double.NaN, Double.NaN, false);

        // SVC2: remote voltage regulation
        StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC_2");
        assertLocalTargets(svc2, Double.NaN, Double.NaN);
        VoltageRegulation reg2 = svc2.getVoltageRegulation();
        assertVoltageRegulation(reg2, RegulationMode.VOLTAGE, "BBS", Double.NaN, Double.NaN, false);

        // SVC3: local reactive power regulation
        StaticVarCompensator svc3 = network.getStaticVarCompensator("SVC_3");
        assertLocalTargets(svc3, Double.NaN, Double.NaN);
        VoltageRegulation reg3 = svc3.getVoltageRegulation();
        assertNotNull(reg3);
        assertVoltageRegulation(reg3, RegulationMode.REACTIVE_POWER, "SVC_3", Double.NaN, Double.NaN, false);

        // SVC4: remote reactive power regulation
        StaticVarCompensator svc4 = network.getStaticVarCompensator("SVC_4");
        assertLocalTargets(svc4, Double.NaN, Double.NaN);
        VoltageRegulation reg4 = svc4.getVoltageRegulation();
        assertNotNull(reg4);
        assertVoltageRegulation(reg4, RegulationMode.REACTIVE_POWER, "PT", Double.NaN, Double.NaN, false);
    }

    @Test
    void staticVarCompensatorVoltageRegulationEqAndSsh() {
        // Full import: regulation is created with correct targets and enabled.
        Network network = readCgmesResources(DIR, "staticVarCompensator_EQ.xml", "staticVarCompensator_SSH.xml");

        // SVC1: local voltage regulation
        StaticVarCompensator svc1 = network.getStaticVarCompensator("SVC_1");
        assertLocalTargets(svc1, 10, 400);
        VoltageRegulation reg1 = svc1.getVoltageRegulation();
        assertVoltageRegulation(reg1, RegulationMode.VOLTAGE, null, Double.NaN, Double.NaN, true);

        // SVC2: remote voltage regulation
        StaticVarCompensator svc2 = network.getStaticVarCompensator("SVC_2");
        assertLocalTargets(svc2, 10, Double.NaN);
        VoltageRegulation reg2 = svc2.getVoltageRegulation();
        assertVoltageRegulation(reg2, RegulationMode.VOLTAGE, "BBS", 400, Double.NaN, true);

        // SVC3: local reactive power regulation
        StaticVarCompensator svc3 = network.getStaticVarCompensator("SVC_3");
        assertLocalTargets(svc3, 10, Double.NaN);
        VoltageRegulation reg3 = svc3.getVoltageRegulation();
        assertVoltageRegulation(reg3, RegulationMode.REACTIVE_POWER, "SVC_3", 10, Double.NaN, true);

        // SVC4: remote reactive power regulation
        StaticVarCompensator svc4 = network.getStaticVarCompensator("SVC_4");
        assertLocalTargets(svc4, 10, Double.NaN);
        VoltageRegulation reg4 = svc4.getVoltageRegulation();
        assertVoltageRegulation(reg4, RegulationMode.REACTIVE_POWER, "PT", 20, Double.NaN, true);
    }

    @Test
    void transformerVoltageRegulationEq() {
        // EQ only import: regulation is created without targets and not enabled
        Network network = readCgmesResources(DIR, "transformer_EQ.xml");

        // RTC1: 2w transformer voltage regulation
        RatioTapChanger rtc1 = network.getTwoWindingsTransformer("PT2_1").getRatioTapChanger();
        assertVoltageRegulation(rtc1.getVoltageRegulation(), RegulationMode.VOLTAGE, "BBS_2", Double.NaN, Double.NaN, false);

        // RTC2: 2w transformer reactive power regulation
        RatioTapChanger rtc2 = network.getTwoWindingsTransformer("PT2_2").getRatioTapChanger();
        assertVoltageRegulation(rtc2.getVoltageRegulation(), RegulationMode.REACTIVE_POWER, "PT2_2", Double.NaN, Double.NaN, false);

        // RTC3: 3w transformer voltage regulation
        RatioTapChanger rtc3 = network.getThreeWindingsTransformer("PT3_1").getLeg2().getRatioTapChanger();
        assertVoltageRegulation(rtc3.getVoltageRegulation(), RegulationMode.VOLTAGE, "BBS_2", Double.NaN, Double.NaN, false);

        // RTC4: 3w transformer reactive power regulation
        RatioTapChanger rtc4 = network.getThreeWindingsTransformer("PT3_2").getLeg2().getRatioTapChanger();
        assertVoltageRegulation(rtc4.getVoltageRegulation(), RegulationMode.REACTIVE_POWER, "PT3_2", Double.NaN, Double.NaN, false);
    }

    @Test
    void transformerVoltageRegulationEqAndSsh() {
        // Full import: regulation is created with correct targets and enabled.
        Network network = readCgmesResources(DIR, "transformer_EQ.xml", "transformer_SSH.xml");

        // RTC1: 2w transformer voltage regulation
        RatioTapChanger rtc1 = network.getTwoWindingsTransformer("PT2_1").getRatioTapChanger();
        assertVoltageRegulation(rtc1.getVoltageRegulation(), RegulationMode.VOLTAGE, "BBS_2", 200.0, 1.0, true);

        // RTC2: 2w transformer reactive power regulation
        RatioTapChanger rtc2 = network.getTwoWindingsTransformer("PT2_2").getRatioTapChanger();
        assertVoltageRegulation(rtc2.getVoltageRegulation(), RegulationMode.REACTIVE_POWER, "PT2_2", 50.0, 2.0, true);

        // RTC3: 3w transformer voltage regulation
        RatioTapChanger rtc3 = network.getThreeWindingsTransformer("PT3_1").getLeg2().getRatioTapChanger();
        assertVoltageRegulation(rtc3.getVoltageRegulation(), RegulationMode.VOLTAGE, "BBS_2", 200.0, 1.0, true);

        // RTC4: 3w transformer reactive power regulation
        RatioTapChanger rtc4 = network.getThreeWindingsTransformer("PT3_2").getLeg2().getRatioTapChanger();
        assertVoltageRegulation(rtc4.getVoltageRegulation(), RegulationMode.REACTIVE_POWER, "PT3_2", 50.0, 2.0, true);
    }

    private void assertLocalTargets(Generator gen, double localTargetQ, double localTargetV) {
        assertNotNull(gen);
        assertLocalTargets(gen.getLocalTargetQ(), gen.getLocalTargetV(), localTargetQ, localTargetV);
    }

    private void assertLocalTargets(StaticVarCompensator svc, double localTargetQ, double localTargetV) {
        assertNotNull(svc);
        assertLocalTargets(svc.getLocalTargetQ(), svc.getLocalTargetV(), localTargetQ, localTargetV);
    }

    private void assertLocalTargets(double actualTargetQ, double actualTargetV, double expectedTargetQ, double expectedTargetV) {
        assertEquals(expectedTargetQ, actualTargetQ);
        assertEquals(expectedTargetV, actualTargetV);
    }

    private void assertVoltageRegulation(VoltageRegulation reg, RegulationMode mode, String terminalId,
                                         double targetValue, double targetDeadband, boolean isRegulating) {
        assertNotNull(reg);
        assertEquals(mode, reg.getMode());
        if (reg.getTerminal() == null) {
            assertNull(terminalId);
        } else {
            assertEquals(terminalId, reg.getTerminal().getConnectable().getId());
        }
        assertEquals(targetValue, reg.getTargetValue());
        assertEquals(targetDeadband, reg.getTargetDeadband());
        assertEquals(isRegulating, reg.isRegulating());
    }

}
