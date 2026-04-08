/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.SecurityAnalysisTestNetworkFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
class SecurityAnalysisTestNetworkFactoryTest {

    private final double threshold = 0.0001;
    private final String lineS1S2V11Str = "LINE_S1S2V1_1";
    private final String lineS1S2V12Str = "LINE_S1S2V1_2";
    private final String lineS1S2V2Str = "LINE_S1S2V2";
    private final String twtStr = "TWT";
    private final String twt2Str = "TWT2";

    @Test
    void testCreate() {
        Network network = SecurityAnalysisTestNetworkFactory.create();
        Line lineS1S2V11 = network.getLine(lineS1S2V11Str);
        assertEquals(0.01, lineS1S2V11.getR(), threshold);
        assertEquals(50, lineS1S2V11.getX(), threshold);
        assertEquals(0.0, lineS1S2V11.getG1(), threshold);
        assertEquals(0.0, lineS1S2V11.getG2(), threshold);
        assertEquals(0.0, lineS1S2V11.getB1(), threshold);
        assertEquals(0.0, lineS1S2V11.getB2(), threshold);
        assertEquals("S1VL1", lineS1S2V11.getTerminal1().getVoltageLevel().getId());
        assertEquals("S2VL1", lineS1S2V11.getTerminal2().getVoltageLevel().getId());

        Line lineS1S2V12 = network.getLine(lineS1S2V12Str);
        assertEquals(0.01, lineS1S2V12.getR(), threshold);
        assertEquals(50, lineS1S2V12.getX(), threshold);
        assertEquals(0.0, lineS1S2V12.getG1(), threshold);
        assertEquals(0.0, lineS1S2V12.getG2(), threshold);
        assertEquals(0.0, lineS1S2V12.getB1(), threshold);
        assertEquals(0.0, lineS1S2V12.getB2(), threshold);
        assertEquals("S1VL1", lineS1S2V11.getTerminal1().getVoltageLevel().getId());
        assertEquals("S2VL1", lineS1S2V11.getTerminal2().getVoltageLevel().getId());

        Line lineS1S2V2 = network.getLine(lineS1S2V2Str);
        assertEquals(0.01, lineS1S2V2.getR(), threshold);
        assertEquals(50, lineS1S2V2.getX(), threshold);
        assertEquals(0.0, lineS1S2V2.getG1(), threshold);
        assertEquals(0.0, lineS1S2V2.getG2(), threshold);
        assertEquals(0.0, lineS1S2V2.getB1(), threshold);
        assertEquals(0.0, lineS1S2V2.getB2(), threshold);
        assertEquals("S1VL2", lineS1S2V2.getTerminal1().getVoltageLevel().getId());
        assertEquals("S2VL2", lineS1S2V2.getTerminal2().getVoltageLevel().getId());

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer(twtStr);
        assertEquals(2.0, twt.getR(), threshold);
        assertEquals(25, twt.getX(), threshold);
        assertEquals(0.0, twt.getG(), threshold);
        assertEquals(3.2E-5, twt.getB(), threshold);
        assertEquals(400, twt.getRatedU1(), threshold);
        assertEquals(225, twt.getRatedU2(), threshold);

        TwoWindingsTransformer twt2 = network.getTwoWindingsTransformer(twt2Str);
        assertEquals(2.0, twt2.getR(), threshold);
        assertEquals(50, twt2.getX(), threshold);
        assertEquals(0.0, twt2.getG(), threshold);
        assertEquals(3.2E-5, twt2.getB(), threshold);
        assertEquals(400, twt2.getRatedU1(), threshold);
        assertEquals(225, twt2.getRatedU2(), threshold);

        Load load1 = network.getLoad("LD1");
        assertEquals(LoadType.UNDEFINED, load1.getLoadType());
        assertEquals(50, load1.getP0(), threshold);
        assertEquals(4, load1.getQ0(), threshold);

        Load load2 = network.getLoad("LD2");
        assertEquals(LoadType.UNDEFINED, load2.getLoadType());
        assertEquals(50, load2.getP0(), threshold);
        assertEquals(4, load2.getQ0(), threshold);

        Generator generator = network.getGenerator("GEN");
        assertEquals(EnergySource.OTHER, generator.getEnergySource());
        assertEquals(0, generator.getMinP(), threshold);
        assertEquals(150, generator.getMaxP(), threshold);
        assertTrue(generator.isVoltageRegulatorOn());
        assertEquals(400, generator.getTargetV(), threshold);
        assertEquals(100, generator.getTargetP(), threshold);
    }

    @Test
    void testCurrentLimits() {
        Network network = SecurityAnalysisTestNetworkFactory.createWithFixedCurrentLimits();
        assertTrue(network.getLine(lineS1S2V11Str).getCurrentLimits1().isPresent());
        assertEquals(75, network.getLine(lineS1S2V11Str).getCurrentLimits1().get().getPermanentLimit(), threshold);
        assertEquals(3, network.getLine(lineS1S2V11Str).getCurrentLimits1().get().getTemporaryLimits().size(), threshold);
        List<LoadingLimits.TemporaryLimit> temporaryLimits = new ArrayList<>(network.getLine(lineS1S2V11Str).getCurrentLimits1().get().getTemporaryLimits());
        assertEquals("10'", temporaryLimits.get(0).getName());
        assertEquals(600, temporaryLimits.get(0).getAcceptableDuration(), threshold);
        assertEquals(80, temporaryLimits.get(0).getValue(), threshold);
        assertEquals("1'", temporaryLimits.get(1).getName());
        assertEquals(60, temporaryLimits.get(1).getAcceptableDuration(), threshold);
        assertEquals(85, temporaryLimits.get(1).getValue(), threshold);
        assertEquals("Undefined", temporaryLimits.get(2).getName());
        assertEquals(0, temporaryLimits.get(2).getAcceptableDuration(), threshold);
        assertEquals(Double.MAX_VALUE, temporaryLimits.get(2).getValue(), threshold);

        assertTrue(network.getLine(lineS1S2V11Str).getCurrentLimits2().isPresent());
        assertEquals(75, network.getLine(lineS1S2V11Str).getCurrentLimits2().get().getPermanentLimit(), threshold);

        assertTrue(network.getLine(lineS1S2V12Str).getCurrentLimits1().isPresent());
        assertEquals(75, network.getLine(lineS1S2V12Str).getCurrentLimits1().get().getPermanentLimit(), threshold);
        assertEquals(3, network.getLine(lineS1S2V12Str).getCurrentLimits1().get().getTemporaryLimits().size(), threshold);
        temporaryLimits = new ArrayList<>(network.getLine(lineS1S2V12Str).getCurrentLimits1().get().getTemporaryLimits());
        assertEquals("10'", temporaryLimits.get(0).getName());
        assertEquals(600, temporaryLimits.get(0).getAcceptableDuration(), threshold);
        assertEquals(80, temporaryLimits.get(0).getValue(), threshold);
        assertEquals("1'", temporaryLimits.get(1).getName());
        assertEquals(60, temporaryLimits.get(1).getAcceptableDuration(), threshold);
        assertEquals(85, temporaryLimits.get(1).getValue(), threshold);
        assertEquals("Undefined", temporaryLimits.get(2).getName());
        assertEquals(0, temporaryLimits.get(2).getAcceptableDuration(), threshold);
        assertEquals(Double.MAX_VALUE, temporaryLimits.get(2).getValue(), threshold);

        assertTrue(network.getLine(lineS1S2V12Str).getCurrentLimits2().isPresent());
        assertEquals(75, network.getLine(lineS1S2V12Str).getCurrentLimits2().get().getPermanentLimit(), threshold);

        assertTrue(network.getLine(lineS1S2V2Str).getCurrentLimits1().isPresent());
        assertEquals(60, network.getLine(lineS1S2V2Str).getCurrentLimits1().get().getPermanentLimit(), threshold);
        assertEquals(1, network.getLine(lineS1S2V2Str).getCurrentLimits1().get().getTemporaryLimits().size(), threshold);
        temporaryLimits = new ArrayList<>(network.getLine(lineS1S2V2Str).getCurrentLimits1().get().getTemporaryLimits());
        assertEquals("10'", temporaryLimits.get(0).getName());
        assertEquals(600, temporaryLimits.get(0).getAcceptableDuration(), threshold);
        assertEquals(80, temporaryLimits.get(0).getValue(), threshold);

        assertTrue(network.getTwoWindingsTransformer(twtStr).getCurrentLimits1().isPresent());
        assertEquals(92, network.getTwoWindingsTransformer(twtStr).getCurrentLimits1().get().getPermanentLimit(), threshold);
        assertEquals(2, network.getTwoWindingsTransformer(twtStr).getCurrentLimits1().get().getTemporaryLimits().size(), threshold);
        temporaryLimits = new ArrayList<>(network.getTwoWindingsTransformer(twtStr).getCurrentLimits1().get().getTemporaryLimits());
        assertEquals("10'", temporaryLimits.get(0).getName());
        assertEquals(600, temporaryLimits.get(0).getAcceptableDuration(), threshold);
        assertEquals(100, temporaryLimits.get(0).getValue(), threshold);
        assertEquals("1'", temporaryLimits.get(1).getName());
        assertEquals(60, temporaryLimits.get(1).getAcceptableDuration(), threshold);
        assertEquals(110, temporaryLimits.get(1).getValue(), threshold);

        assertTrue(network.getTwoWindingsTransformer(twt2Str).getCurrentLimits1().isPresent());
        assertEquals(90, network.getTwoWindingsTransformer(twt2Str).getCurrentLimits1().get().getPermanentLimit(), threshold);
        assertEquals(2, network.getTwoWindingsTransformer(twt2Str).getCurrentLimits1().get().getTemporaryLimits().size(), threshold);
        temporaryLimits = new ArrayList<>(network.getTwoWindingsTransformer(twt2Str).getCurrentLimits1().get().getTemporaryLimits());
        assertEquals("10'", temporaryLimits.get(0).getName());
        assertEquals(600, temporaryLimits.get(0).getAcceptableDuration(), threshold);
        assertEquals(100, temporaryLimits.get(0).getValue(), threshold);
        assertEquals("1'", temporaryLimits.get(1).getName());
        assertEquals(60, temporaryLimits.get(1).getAcceptableDuration(), threshold);
        assertEquals(110, temporaryLimits.get(1).getValue(), threshold);
    }

    @Test
    void testPowerLimits() {
        Network network = SecurityAnalysisTestNetworkFactory.createWithFixedPowerLimits();
        assertTrue(network.getLine(lineS1S2V11Str).getActivePowerLimits1().isPresent());
        assertEquals(55, network.getLine(lineS1S2V11Str).getActivePowerLimits1().get().getPermanentLimit(), threshold);
        assertTrue(network.getLine(lineS1S2V12Str).getActivePowerLimits1().isPresent());
        assertEquals(55, network.getLine(lineS1S2V12Str).getActivePowerLimits1().get().getPermanentLimit(), threshold);
        assertTrue(network.getLine(lineS1S2V2Str).getActivePowerLimits1().isPresent());
        assertEquals(30, network.getLine(lineS1S2V2Str).getActivePowerLimits1().get().getPermanentLimit(), threshold);
        assertTrue(network.getTwoWindingsTransformer(twtStr).getActivePowerLimits1().isPresent());
        assertEquals(71, network.getTwoWindingsTransformer(twtStr).getActivePowerLimits1().get().getPermanentLimit(), threshold);
        assertTrue(network.getTwoWindingsTransformer(twt2Str).getActivePowerLimits1().isPresent());
        assertEquals(55, network.getTwoWindingsTransformer(twt2Str).getActivePowerLimits1().get().getPermanentLimit(), threshold);
    }
}
