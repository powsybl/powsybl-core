/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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

        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("T2W");
        assertTrue(checkEq(t2w));
        assertTrue(checkDefinedApparentPowerLimits(t2w,
                new ApparentPowerLimit(990.0, 900, 1000.0),
                new ApparentPowerLimit(991.0, 900, 1001.0)));
        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("T3W");
        assertTrue(checkEq(t3w));
        assertTrue(checkDefinedApparentPowerLimits(t3w,
                new ApparentPowerLimit(100.0, 900, 110.0),
                new ApparentPowerLimit(75.0, 900, 80.0),
                new ApparentPowerLimit(25.0, 900, 30.0)));
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "transformer_EQ.xml", "transformer_SSH.xml");
        assertEquals(1, network.getTwoWindingsTransformerCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());

        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("T2W");
        assertTrue(checkSsh(t2w, -2, 100.0, 0.2, true));
        assertTrue(checkDefinedApparentPowerLimits(t2w,
                new ApparentPowerLimit(995.0, 900, 1005.0),
                new ApparentPowerLimit(996.0, 900, 1006.0)));

        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("T3W");
        assertTrue(checkSsh(t3w, 8, 225.0, 2.0, true));
        assertTrue(checkDefinedApparentPowerLimits(t3w,
                new ApparentPowerLimit(102.0, 900, 112.0),
                new ApparentPowerLimit(76.0, 900, 81.0),
                new ApparentPowerLimit(26.0, 900, 31.0)));
    }

    @Test
    void importEqTwoSshsAndSvTest() {
        Network network = readCgmesResources(DIR, "transformer_EQ.xml");
        assertEquals(1, network.getTwoWindingsTransformerCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());

        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("T2W");
        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("T3W");
        assertTrue(checkEq(t2w, t3w));

        readCgmesResources(network, DIR, "transformer_SSH.xml");
        assertTrue(checkSsh(t2w, t3w));

        readCgmesResources(network, DIR, "transformer_SSH_1.xml");
        assertTrue(checkSsh1(t2w, t3w));

        readCgmesResources(network, DIR, "transformer_SV.xml");
        assertTrue(checkFlows(t2w.getTerminal1(), 100.0, -50.0));
        assertTrue(checkFlows(t2w.getTerminal2(), -100.1, 50.5));

        assertTrue(checkFlows(t3w.getLeg1().getTerminal(), 200.0, 50.0));
        assertTrue(checkFlows(t3w.getLeg2().getTerminal(), -150.0, -40.0));
        assertTrue(checkFlows(t3w.getLeg3().getTerminal(), -50.0, -10.0));
    }

    private static boolean checkEq(TwoWindingsTransformer t2w, ThreeWindingsTransformer t3w) {
        assertTrue(checkEq(t2w));
        assertTrue(checkDefinedApparentPowerLimits(t2w,
                new ApparentPowerLimit(990.0, 900, 1000.0),
                new ApparentPowerLimit(991.0, 900, 1001.0)));

        assertTrue(checkEq(t3w));
        assertTrue(checkDefinedApparentPowerLimits(t3w,
                new ApparentPowerLimit(100.0, 900, 110.0),
                new ApparentPowerLimit(75.0, 900, 80.0),
                new ApparentPowerLimit(25.0, 900, 30.0)));
        return true;
    }

    private static boolean checkSsh(TwoWindingsTransformer t2w, ThreeWindingsTransformer t3w) {
        assertTrue(checkSsh(t2w, -2, 100.0, 0.2, true));
        assertTrue(checkDefinedApparentPowerLimits(t2w,
                new ApparentPowerLimit(995.0, 900, 1005.0),
                new ApparentPowerLimit(996.0, 900, 1006.0)));
        assertTrue(checkSsh(t3w, 8, 225.0, 2.0, true));
        assertTrue(checkDefinedApparentPowerLimits(t3w,
                new ApparentPowerLimit(102.0, 900, 112.0),
                new ApparentPowerLimit(76.0, 900, 81.0),
                new ApparentPowerLimit(26.0, 900, 31.0)));
        return true;
    }

    private static boolean checkSsh1(TwoWindingsTransformer t2w, ThreeWindingsTransformer t3w) {
        assertTrue(checkSsh(t2w, -1, 110.0, 0.3, false));
        assertTrue(checkDefinedApparentPowerLimits(t2w,
                new ApparentPowerLimit(996.0, 900, 1006.0),
                new ApparentPowerLimit(997.0, 900, 1007.0)));
        assertTrue(checkSsh(t3w, 7, 220.0, 2.2, false));
        assertTrue(checkDefinedApparentPowerLimits(t3w,
                new ApparentPowerLimit(104.0, 900, 114.0),
                new ApparentPowerLimit(77.0, 900, 82.0),
                new ApparentPowerLimit(27.0, 900, 32.0)));
        return true;
    }

    private static boolean checkEq(TwoWindingsTransformer t2w) {
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
        return true;
    }

    private static boolean checkEq(ThreeWindingsTransformer t3w) {
        assertNotNull(t3w);
        String tapChangerId = t3w.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + 2).orElseThrow();
        int normalStep = getNormalStep(t3w, tapChangerId);
        assertEquals(normalStep, t3w.getLeg2().getRatioTapChanger().getTapPosition());

        assertTrue(Double.isNaN(t3w.getLeg2().getRatioTapChanger().getRegulationValue()));
        assertTrue(Double.isNaN(t3w.getLeg2().getRatioTapChanger().getTargetDeadband()));
        assertSame(RatioTapChanger.RegulationMode.VOLTAGE, t3w.getLeg2().getRatioTapChanger().getRegulationMode());
        assertNotNull(t3w.getLeg2().getRatioTapChanger().getRegulationTerminal());
        assertFalse(t3w.getLeg2().getRatioTapChanger().isRegulating());
        return true;
    }

    private static boolean checkSsh(TwoWindingsTransformer t2w, int tapPosition, double regulationValue, double targetDeadband, boolean isRegulating) {
        assertNotNull(t2w);
        assertEquals(tapPosition, t2w.getPhaseTapChanger().getTapPosition());

        assertEquals(regulationValue, t2w.getPhaseTapChanger().getRegulationValue());
        assertEquals(targetDeadband, t2w.getPhaseTapChanger().getTargetDeadband());
        assertEquals(isRegulating, t2w.getPhaseTapChanger().isRegulating());
        return true;
    }

    private static boolean checkSsh(ThreeWindingsTransformer t3w, int tapPosition, double regulationValue, double targetDeadband, boolean isRegulating) {
        assertNotNull(t3w);
        assertEquals(tapPosition, t3w.getLeg2().getRatioTapChanger().getTapPosition());

        assertEquals(regulationValue, t3w.getLeg2().getRatioTapChanger().getRegulationValue());
        assertEquals(targetDeadband, t3w.getLeg2().getRatioTapChanger().getTargetDeadband());
        assertEquals(isRegulating, t3w.getLeg2().getRatioTapChanger().isRegulating());
        return true;
    }

    private static <C extends Connectable<C>> int getNormalStep(Connectable<C> tw, String tapChangerId) {
        CgmesTapChangers<C> cgmesTcs = tw.getExtension(CgmesTapChangers.class);
        assertNotNull(cgmesTcs);
        CgmesTapChanger cgmesTc = cgmesTcs.getTapChanger(tapChangerId);
        assertNotNull(cgmesTcs);
        return cgmesTc.getStep().orElseThrow();
    }

    private static boolean checkDefinedApparentPowerLimits(TwoWindingsTransformer t2w, ApparentPowerLimit apparentPowerLimit1, ApparentPowerLimit apparentPowerLimit2) {

        assertEquals(1, t2w.getOperationalLimitsGroups1().size());
        assertTrue(checkDefinedApparentPowerLimitsSide(t2w, TwoSides.ONE, apparentPowerLimit1));
        assertEquals(1, t2w.getOperationalLimitsGroups2().size());
        assertTrue(checkDefinedApparentPowerLimitsSide(t2w, TwoSides.TWO, apparentPowerLimit2));

        assertNull(t2w.getCurrentLimits1().orElse(null));
        assertNull(t2w.getActivePowerLimits1().orElse(null));
        assertNull(t2w.getCurrentLimits2().orElse(null));
        assertNull(t2w.getActivePowerLimits2().orElse(null));
        return true;
    }

    private static boolean checkDefinedApparentPowerLimitsSide(TwoWindingsTransformer t2w, TwoSides side, ApparentPowerLimit apparentPowerLimit) {
        assertNotNull(t2w.getApparentPowerLimits(side).orElse(null));
        if (t2w.getApparentPowerLimits(side).isPresent()) {
            assertEquals(apparentPowerLimit.ptalValue, t2w.getApparentPowerLimits(side).get().getPermanentLimit());
            assertEquals(1, t2w.getApparentPowerLimits(side).get().getTemporaryLimits().size());
            assertEquals(apparentPowerLimit.tatlDuration, t2w.getApparentPowerLimits(side).get().getTemporaryLimits().iterator().next().getAcceptableDuration());
            assertEquals(apparentPowerLimit.tatlValue, t2w.getApparentPowerLimits(side).get().getTemporaryLimits().iterator().next().getValue());
            return true;
        }
        return false;
    }

    private static boolean checkDefinedApparentPowerLimits(ThreeWindingsTransformer t3w, ApparentPowerLimit apparentPowerLimit1, ApparentPowerLimit apparentPowerLimit2, ApparentPowerLimit apparentPowerLimit3) {

        assertTrue(checkDefinedApparentPowerLimitsSide(t3w.getLeg1(), apparentPowerLimit1));
        assertTrue(checkDefinedApparentPowerLimitsSide(t3w.getLeg2(), apparentPowerLimit2));
        assertTrue(checkDefinedApparentPowerLimitsSide(t3w.getLeg3(), apparentPowerLimit3));

        assertNull(t3w.getLeg1().getCurrentLimits().orElse(null));
        assertNull(t3w.getLeg1().getActivePowerLimits().orElse(null));
        assertNull(t3w.getLeg2().getCurrentLimits().orElse(null));
        assertNull(t3w.getLeg2().getActivePowerLimits().orElse(null));
        assertNull(t3w.getLeg3().getCurrentLimits().orElse(null));
        assertNull(t3w.getLeg3().getActivePowerLimits().orElse(null));
        return true;
    }

    private static boolean checkDefinedApparentPowerLimitsSide(ThreeWindingsTransformer.Leg leg, ApparentPowerLimit apparentPowerLimit) {
        assertEquals(1, leg.getOperationalLimitsGroups().size());
        assertNotNull(leg.getApparentPowerLimits().orElse(null));
        if (leg.getApparentPowerLimits().isPresent()) {
            assertEquals(apparentPowerLimit.ptalValue, leg.getApparentPowerLimits().get().getPermanentLimit());
            assertEquals(1, leg.getApparentPowerLimits().get().getTemporaryLimits().size());
            assertEquals(apparentPowerLimit.tatlDuration, leg.getApparentPowerLimits().get().getTemporaryLimits().iterator().next().getAcceptableDuration());
            assertEquals(apparentPowerLimit.tatlValue, leg.getApparentPowerLimits().get().getTemporaryLimits().iterator().next().getValue());
            return true;
        }
        return false;
    }

    private record ApparentPowerLimit(double ptalValue, int tatlDuration, double tatlValue) {
    }

    private static boolean checkFlows(Terminal terminal, double p, double q) {
        double tol = 0.0000001;
        assertEquals(p, terminal.getP(), tol);
        assertEquals(q, terminal.getQ(), tol);
        return true;
    }
}
