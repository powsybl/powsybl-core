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
        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("T3W");
        assertTrue(checkEq(t3w));
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "transformer_EQ.xml", "transformer_SSH.xml");
        assertEquals(1, network.getTwoWindingsTransformerCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());

        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("T2W");
        assertTrue(checkSsh(t2w, -2, 100.0, 0.2, true));

        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("T3W");
        assertTrue(checkSsh(t3w, 8, 225.0, 2.0, true));
    }

    @Test
    void importEqAndSshSeparatelyTest() {
        Network network = readCgmesResources(DIR, "transformer_EQ.xml");
        assertEquals(1, network.getTwoWindingsTransformerCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());

        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("T2W");
        assertTrue(checkEq(t2w));
        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("T3W");
        assertTrue(checkEq(t3w));

        readCgmesResources(network, DIR, "transformer_SSH.xml");

        assertTrue(checkSsh(t2w, -2, 100.0, 0.2, true));
        assertTrue(checkSsh(t3w, 8, 225.0, 2.0, true));
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "transformer_EQ.xml");
        assertEquals(1, network.getTwoWindingsTransformerCount());
        assertEquals(1, network.getThreeWindingsTransformerCount());

        TwoWindingsTransformer t2w = network.getTwoWindingsTransformer("T2W");
        assertTrue(checkEq(t2w));
        ThreeWindingsTransformer t3w = network.getThreeWindingsTransformer("T3W");
        assertTrue(checkEq(t3w));

        readCgmesResources(network, DIR, "transformer_SSH.xml");
        assertTrue(checkSsh(t2w, -2, 100.0, 0.2, true));
        assertTrue(checkSsh(t3w, 8, 225.0, 2.0, true));

        readCgmesResources(network, DIR, "transformer_SSH_1.xml");
        assertTrue(checkSsh(t2w, -1, 110.0, 0.3, false));
        assertTrue(checkSsh(t3w, 7, 220.0, 2.2, false));
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
}
