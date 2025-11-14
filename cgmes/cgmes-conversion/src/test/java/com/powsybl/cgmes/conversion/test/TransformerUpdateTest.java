/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.extensions.CgmesTapChanger;
import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class TransformerUpdateTest {

    private static final String DIR = "/update/transformer/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "transformer_EQ.xml");
        assertEquals(1, network.getTwoWindingsTransformerCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());

        assertEq(network);
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "transformer_EQ.xml", "transformer_SSH.xml");
        assertEquals(1, network.getTwoWindingsTransformerCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());

        assertFirstSsh(network);
    }

    @Test
    void importEqTwoSshsAndSvTest() {
        Network network = readCgmesResources(DIR, "transformer_EQ.xml");
        assertEquals(1, network.getTwoWindingsTransformerCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());

        assertEq(network);

        readCgmesResources(network, DIR, "transformer_SSH.xml");
        assertFirstSsh(network);

        readCgmesResources(network, DIR, "transformer_SSH_1.xml");
        assertSecondSsh(network);

        assertUnassignedFlows(network);
        assertTapChangerStepsBeforeSv(network);
        readCgmesResources(network, DIR, "transformer_SV.xml");
        assertFlowsAfterSv(network);
        assertTapChangerStepsAfterSv(network);
    }

    @Test
    void starBusVoltageTogetherTest() {
        Network network = readCgmesResources(DIR, "transformer_EQ.xml", "transformer_SSH.xml", "transformer_TP.xml", "transformer_SV.xml");
        assertEquals(1, network.getTwoWindingsTransformerCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());

        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("T3W");
        assertBusVoltage(t3w.getLeg1().getTerminal().getBusView().getBus(), 402.5, 5.0);
        assertBusVoltage(t3w.getLeg2().getTerminal().getBusView().getBus(), 220.5, -3.0);
        assertBusVoltage(t3w.getLeg3().getTerminal().getBusView().getBus(), 111.5, 2.0);
        assertStarBusVoltage(t3w);
    }

    @Test
    void starBusVoltageSeparatelyTest() {
        Network network = readCgmesResources(DIR, "transformer_EQ.xml");
        assertEquals(1, network.getTwoWindingsTransformerCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());

        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("T3W");

        readCgmesResources(network, DIR, "transformer_SSH.xml", "transformer_TP.xml", "transformer_SV.xml");

        assertBusVoltage(t3w.getLeg1().getTerminal().getBusView().getBus(), 402.5, 5.0);
        assertBusVoltage(t3w.getLeg2().getTerminal().getBusView().getBus(), 220.5, -3.0);
        assertBusVoltage(t3w.getLeg3().getTerminal().getBusView().getBus(), 111.5, 2.0);
        assertStarBusVoltage(t3w);
    }

    @Test
    void usePreviousValuesTest() {
        Network network = readCgmesResources(DIR, "transformer_EQ.xml", "transformer_SSH.xml", "transformer_SV.xml");
        assertEquals(1, network.getTwoWindingsTransformerCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());
        assertFirstSsh(network);
        assertFlowsAfterSv(network);
        assertTapChangerStepsAfterSshAndSvTogether(network, 7, 13);

        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.use-previous-values-during-update", "true");
        readCgmesResources(network, properties, DIR, "../empty_SSH.xml", "../empty_SV.xml");
        assertFirstSsh(network);
        assertUnassignedFlows(network);
        assertTapChangerStepsAfterSshAndSvTogether(network, null, null);
    }

    private static void assertEq(Network network) {
        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("T2W");
        assertEq(t2w);
        assertDefinedApparentPowerLimits(t2w,
                new ApparentPowerLimit(990.0, 900, 1000.0),
                new ApparentPowerLimit(991.0, 900, 1001.0));

        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("T3W");
        assertEq(t3w);
        assertDefinedApparentPowerLimits(t3w,
                new ApparentPowerLimit(100.0, 900, 110.0),
                new ApparentPowerLimit(75.0, 900, 80.0),
                new ApparentPowerLimit(25.0, 900, 30.0));
    }

    private static void assertFirstSsh(Network network) {
        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("T2W");
        assertSsh(t2w, -2, 100.0, 0.2, true);
        assertDefinedApparentPowerLimits(t2w,
                new ApparentPowerLimit(995.0, 900, 1005.0),
                new ApparentPowerLimit(996.0, 900, 1006.0));

        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("T3W");
        assertSsh(t3w, 8, 225.0, 2.0, true);
        assertDefinedApparentPowerLimits(t3w,
                new ApparentPowerLimit(102.0, 900, 112.0),
                new ApparentPowerLimit(76.0, 900, 81.0),
                new ApparentPowerLimit(26.0, 900, 31.0));
    }

    private static void assertSecondSsh(Network network) {
        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("T2W");
        assertSsh(t2w, -1, 110.0, 0.3, false);
        assertDefinedApparentPowerLimits(t2w,
                new ApparentPowerLimit(996.0, 900, 1006.0),
                new ApparentPowerLimit(997.0, 900, 1007.0));

        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("T3W");
        assertSsh(t3w, 7, 220.0, 2.2, false);
        assertDefinedApparentPowerLimits(t3w,
                new ApparentPowerLimit(104.0, 900, 114.0),
                new ApparentPowerLimit(77.0, 900, 82.0),
                new ApparentPowerLimit(27.0, 900, 32.0));
    }

    private static void assertUnassignedFlows(Network network) {
        assertFlows(network, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    private static void assertFlowsAfterSv(Network network) {
        assertFlows(network, 100.0, -50.0, -100.1, 50.5, 200.0, 50.0, -150.0, -40.0, -50.0, -10.0);
    }

    private static void assertFlows(Network network, double t2wP1, double t2wQ1, double t2wP2, double t2wQ2, double t3wP1, double t3wQ1, double t3wP2, double t3wQ2, double t3wP3, double t3wQ3) {
        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("T2W");
        assertFlows(t2w.getTerminal1(), t2wP1, t2wQ1);
        assertFlows(t2w.getTerminal2(), t2wP2, t2wQ2);

        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("T3W");
        assertFlows(t3w.getLeg1().getTerminal(), t3wP1, t3wQ1);
        assertFlows(t3w.getLeg2().getTerminal(), t3wP2, t3wQ2);
        assertFlows(t3w.getLeg3().getTerminal(), t3wP3, t3wQ3);
    }

    private static void assertTapChangerStepsBeforeSv(Network network) {
        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("T2W");
        assertEquals(-1, t2w.getPhaseTapChanger().getTapPosition());
        assertFalse(t2w.getPhaseTapChanger().isRegulating());
        assertNull(t2w.getPhaseTapChanger().getSolvedTapPosition());

        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("T3W");
        assertEquals(7, t3w.getLeg2().getRatioTapChanger().getTapPosition());
        assertFalse(t3w.getLeg2().getRatioTapChanger().isRegulating());
        assertNull(t3w.getLeg2().getRatioTapChanger().getSolvedTapPosition());
    }

    private static void assertTapChangerStepsAfterSv(Network network) {
        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("T2W");
        assertEquals(4, t2w.getPhaseTapChanger().getTapPosition());
        assertFalse(t2w.getPhaseTapChanger().isRegulating());
        assertEquals(7, t2w.getPhaseTapChanger().getSolvedTapPosition());

        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("T3W");
        assertEquals(8, t3w.getLeg2().getRatioTapChanger().getTapPosition());
        assertFalse(t3w.getLeg2().getRatioTapChanger().isRegulating());
        assertEquals(13, t3w.getLeg2().getRatioTapChanger().getSolvedTapPosition());
    }

    private static void assertTapChangerStepsAfterSshAndSvTogether(Network network, Integer tw2SolvedTapPosition, Integer t3wSolvedTapPosition) {
        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("T2W");
        assertEquals(-2, t2w.getPhaseTapChanger().getTapPosition());
        assertTrue(t2w.getPhaseTapChanger().isRegulating());
        assertEquals(tw2SolvedTapPosition, t2w.getPhaseTapChanger().getSolvedTapPosition());

        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("T3W");
        assertEquals(8, t3w.getLeg2().getRatioTapChanger().getTapPosition());
        assertTrue(t3w.getLeg2().getRatioTapChanger().isRegulating());
        assertEquals(t3wSolvedTapPosition, t3w.getLeg2().getRatioTapChanger().getSolvedTapPosition());
    }

    private static void assertEq(TwoWindingsTransformer t2w) {
        assertNotNull(t2w);
        String tapChangerId = t2w.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 1).orElseThrow();
        int normalStep = getNormalStep(t2w, tapChangerId);
        assertNotNull(t2w.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL_SIGN));
        assertEquals(normalStep, t2w.getPhaseTapChanger().getTapPosition());

        assertTrue(Double.isNaN(t2w.getPhaseTapChanger().getRegulationValue()));
        assertTrue(Double.isNaN(t2w.getPhaseTapChanger().getTargetDeadband()));
        assertSame(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL, t2w.getPhaseTapChanger().getRegulationMode());
        assertNotNull(t2w.getPhaseTapChanger().getRegulationTerminal());
        assertFalse(t2w.getPhaseTapChanger().isRegulating());
    }

    private static void assertEq(ThreeWindingsTransformer t3w) {
        assertNotNull(t3w);
        String tapChangerId = t3w.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 2).orElseThrow();
        int normalStep = getNormalStep(t3w, tapChangerId);
        assertEquals(normalStep, t3w.getLeg2().getRatioTapChanger().getTapPosition());

        assertTrue(Double.isNaN(t3w.getLeg2().getRatioTapChanger().getRegulationValue()));
        assertTrue(Double.isNaN(t3w.getLeg2().getRatioTapChanger().getTargetDeadband()));
        assertSame(RatioTapChanger.RegulationMode.VOLTAGE, t3w.getLeg2().getRatioTapChanger().getRegulationMode());
        assertNotNull(t3w.getLeg2().getRatioTapChanger().getRegulationTerminal());
        assertFalse(t3w.getLeg2().getRatioTapChanger().isRegulating());
    }

    private static void assertSsh(TwoWindingsTransformer t2w, int tapPosition, double regulationValue, double targetDeadband, boolean isRegulating) {
        assertNotNull(t2w);
        assertEquals(tapPosition, t2w.getPhaseTapChanger().getTapPosition());

        assertEquals(regulationValue, t2w.getPhaseTapChanger().getRegulationValue());
        assertEquals(targetDeadband, t2w.getPhaseTapChanger().getTargetDeadband());
        assertEquals(isRegulating, t2w.getPhaseTapChanger().isRegulating());
    }

    private static void assertSsh(ThreeWindingsTransformer t3w, int tapPosition, double regulationValue, double targetDeadband, boolean isRegulating) {
        assertNotNull(t3w);
        assertEquals(tapPosition, t3w.getLeg2().getRatioTapChanger().getTapPosition());

        assertEquals(regulationValue, t3w.getLeg2().getRatioTapChanger().getRegulationValue());
        assertEquals(targetDeadband, t3w.getLeg2().getRatioTapChanger().getTargetDeadband());
        assertEquals(isRegulating, t3w.getLeg2().getRatioTapChanger().isRegulating());
    }

    private static <C extends Connectable<C>> int getNormalStep(Connectable<C> tw, String tapChangerId) {
        CgmesTapChangers<C> cgmesTcs = tw.getExtension(CgmesTapChangers.class);
        assertNotNull(cgmesTcs);
        CgmesTapChanger cgmesTc = cgmesTcs.getTapChanger(tapChangerId);
        assertNotNull(cgmesTcs);
        return cgmesTc.getStep().orElseThrow();
    }

    private static void assertDefinedApparentPowerLimits(TwoWindingsTransformer t2w, ApparentPowerLimit apparentPowerLimit1, ApparentPowerLimit apparentPowerLimit2) {

        assertEquals(1, t2w.getOperationalLimitsGroups1().size());
        assertDefinedApparentPowerLimitsSide(t2w, TwoSides.ONE, apparentPowerLimit1);
        assertEquals(1, t2w.getOperationalLimitsGroups2().size());
        assertDefinedApparentPowerLimitsSide(t2w, TwoSides.TWO, apparentPowerLimit2);

        assertNull(t2w.getCurrentLimits1().orElse(null));
        assertNull(t2w.getActivePowerLimits1().orElse(null));
        assertNull(t2w.getCurrentLimits2().orElse(null));
        assertNull(t2w.getActivePowerLimits2().orElse(null));
    }

    private static void assertDefinedApparentPowerLimitsSide(TwoWindingsTransformer t2w, TwoSides side, ApparentPowerLimit apparentPowerLimit) {
        assertNotNull(t2w.getApparentPowerLimits(side).orElse(null));
        if (t2w.getApparentPowerLimits(side).isPresent()) {
            assertEquals(apparentPowerLimit.ptalValue, t2w.getApparentPowerLimits(side).get().getPermanentLimit());
            assertEquals(1, t2w.getApparentPowerLimits(side).get().getTemporaryLimits().size());
            assertEquals(apparentPowerLimit.tatlDuration, t2w.getApparentPowerLimits(side).get().getTemporaryLimits().iterator().next().getAcceptableDuration());
            assertEquals(apparentPowerLimit.tatlValue, t2w.getApparentPowerLimits(side).get().getTemporaryLimits().iterator().next().getValue());
        }
    }

    private static void assertDefinedApparentPowerLimits(ThreeWindingsTransformer t3w, ApparentPowerLimit apparentPowerLimit1, ApparentPowerLimit apparentPowerLimit2, ApparentPowerLimit apparentPowerLimit3) {

        assertDefinedApparentPowerLimitsSide(t3w.getLeg1(), apparentPowerLimit1);
        assertDefinedApparentPowerLimitsSide(t3w.getLeg2(), apparentPowerLimit2);
        assertDefinedApparentPowerLimitsSide(t3w.getLeg3(), apparentPowerLimit3);

        assertNull(t3w.getLeg1().getCurrentLimits().orElse(null));
        assertNull(t3w.getLeg1().getActivePowerLimits().orElse(null));
        assertNull(t3w.getLeg2().getCurrentLimits().orElse(null));
        assertNull(t3w.getLeg2().getActivePowerLimits().orElse(null));
        assertNull(t3w.getLeg3().getCurrentLimits().orElse(null));
        assertNull(t3w.getLeg3().getActivePowerLimits().orElse(null));
    }

    private static void assertDefinedApparentPowerLimitsSide(ThreeWindingsTransformer.Leg leg, ApparentPowerLimit apparentPowerLimit) {
        assertEquals(1, leg.getOperationalLimitsGroups().size());
        assertNotNull(leg.getApparentPowerLimits().orElse(null));
        if (leg.getApparentPowerLimits().isPresent()) {
            assertEquals(apparentPowerLimit.ptalValue, leg.getApparentPowerLimits().get().getPermanentLimit());
            assertEquals(1, leg.getApparentPowerLimits().get().getTemporaryLimits().size());
            assertEquals(apparentPowerLimit.tatlDuration, leg.getApparentPowerLimits().get().getTemporaryLimits().iterator().next().getAcceptableDuration());
            assertEquals(apparentPowerLimit.tatlValue, leg.getApparentPowerLimits().get().getTemporaryLimits().iterator().next().getValue());
        }
    }

    private record ApparentPowerLimit(double ptalValue, int tatlDuration, double tatlValue) {
    }

    private static void assertFlows(Terminal terminal, double p, double q) {
        double tol = 0.0000001;
        assertEquals(p, terminal.getP(), tol);
        assertEquals(q, terminal.getQ(), tol);
    }

    private static void assertBusVoltage(Bus bus, double v, double angle) {
        double tol = 0.0000001;
        assertEquals(v, bus.getV(), tol);
        assertEquals(angle, bus.getAngle(), tol);
    }

    private static void assertStarBusVoltage(ThreeWindingsTransformer t3w) {
        double tol = 0.0000001;
        assertEquals(401.4610086017755, Double.parseDouble(t3w.getProperty(CgmesNames.VOLTAGE)), tol);
        assertEquals(-0.7578725292783544, Double.parseDouble(t3w.getProperty(CgmesNames.ANGLE)), tol);
    }
}
