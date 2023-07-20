/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.TerminalRef.Side;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
*
* @author Luma Zamarreño <zamarrenolm at aia.es>
* @author José Antonio Marqués <marquesja at aia.es>
*/
class VoltageAngleLimitTest {

    @Test
    void voltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        network.newVoltageAngleLimit()
        .setName("VOLTAGE_ANGLE_LIMIT_LINE_S2S3")
            .setReferenceTerminal(TerminalRef.create("LINE_S2S3", Side.ONE))
            .setOtherTerminal(TerminalRef.create("LINE_S2S3", Side.TWO))
            .setHighLimit(1.0)
            .add();

        network.newVoltageAngleLimit().setName("VOLTAGE_ANGLE_LIMIT_LD1_LD6")
            .setReferenceTerminal(TerminalRef.create("LD1"))
            .setOtherTerminal(TerminalRef.create("LD6"))
            .setLowLimit(1.0)
            .add();

        assertEquals(2, network.getVoltageAngleLimits().size());

        VoltageAngleLimit val0 = network.getVoltageAngleLimits().get(0);
        assertEquals("VOLTAGE_ANGLE_LIMIT_LINE_S2S3", val0.getName());

        assertEquals("LINE_S2S3", val0.getReferenceTerminal().getConnectable().getId());
        assertTrue(TerminalRef.getConnectableSide(val0.getReferenceTerminal()).isPresent());
        TerminalRef.getConnectableSide(val0.getReferenceTerminal()).ifPresent(side -> assertEquals(Side.ONE, side));
        assertEquals("LINE_S2S3", val0.getOtherTerminal().getConnectable().getId());
        assertTrue(TerminalRef.getConnectableSide(val0.getOtherTerminal()).isPresent());
        TerminalRef.getConnectableSide(val0.getOtherTerminal()).ifPresent(side -> assertEquals(Side.TWO, side));
        assertTrue(val0.getHighLimit().isPresent());
        assertTrue(val0.getLowLimit().isEmpty());
        assertEquals(1.0, val0.getHighLimit().get());

        VoltageAngleLimit val1 = network.getVoltageAngleLimits().get(1);
        assertEquals("LD1", val1.getReferenceTerminal().getConnectable().getId());
        assertTrue(TerminalRef.getConnectableSide(val1.getReferenceTerminal()).isEmpty());
        assertEquals("LD6", val1.getOtherTerminal().getConnectable().getId());
        assertTrue(TerminalRef.getConnectableSide(val1.getOtherTerminal()).isEmpty());
        assertTrue(val1.getLowLimit().isPresent());
        assertTrue(val1.getHighLimit().isEmpty());
        assertEquals(1.0, val1.getLowLimit().get());
    }

    @Test
    void badThreeWindingsTransformerVoltageAngleLimitTest() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
            .setName("VOLTAGE_ANGLE_LIMIT_3WT")
            .setReferenceTerminal(TerminalRef.create("3WT", Side.ONE))
            .setOtherTerminal(TerminalRef.create("3WT", Side.TWO))
            .setHighLimit(1.0);

        IllegalStateException e = assertThrows(IllegalStateException.class, adder::add);
        assertEquals("VoltageAngleLimit can not be defined on threeWindingsTransformers : 3WT", e.getMessage());
    }

    @Test
    void badSwitchVoltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
            .setName("VOLTAGE_ANGLE_LIMIT_S1VL1_LD1_BREAKER")
            .setReferenceTerminal(TerminalRef.create("S1VL1_LD1_BREAKER", Side.ONE))
            .setOtherTerminal(TerminalRef.create("S1VL1_LD1_BREAKER", Side.TWO))
            .setHighLimit(1.0);
        PowsyblException e = assertThrows(PowsyblException.class, adder::add);
        assertEquals("Unexpected terminal reference identifiable instance: class com.powsybl.iidm.network.impl.SwitchImpl", e.getMessage());
    }

    @Test
    void badBranchSideVoltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
            .setName("VOLTAGE_ANGLE_LIMIT_LINE_S2S3")
            .setReferenceTerminal(TerminalRef.create("LINE_S2S3", Side.ONE))
            .setOtherTerminal(TerminalRef.create("LINE_S2S3", Side.THREE))
            .setHighLimit(1.0);
        IllegalStateException e = assertThrows(IllegalStateException.class, adder::add);
        assertEquals("Unexpected Branch side: THREE", e.getMessage());
    }

    @Test
    void badIdentifiableSideVoltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
            .setName("VOLTAGE_ANGLE_LIMIT_LINE_S2S3")
            .setReferenceTerminal(TerminalRef.create("LIN_S2S3", Side.ONE))
            .setOtherTerminal(TerminalRef.create("LINE_S2S3", Side.THREE))
            .setHighLimit(1.0);
        PowsyblException e = assertThrows(PowsyblException.class, adder::add);
        assertEquals("Terminal reference identifiable not found: 'LIN_S2S3'", e.getMessage());
    }

    @Test
    void noNameLimitVoltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
                .setReferenceTerminal(TerminalRef.create("LINE_S2S3", Side.ONE))
                .setOtherTerminal(TerminalRef.create("LINE_S2S3", Side.TWO));
        IllegalStateException e = assertThrows(IllegalStateException.class, adder::add);
        assertEquals("VoltageAngleLimit name is mandatory.", e.getMessage());
    }

    @Test
    void badLimitsVoltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
                .setName("VOLTAGE_ANGLE_LIMIT_LINE_S2S3")
                .setReferenceTerminal(TerminalRef.create("LINE_S2S3", Side.ONE))
                .setOtherTerminal(TerminalRef.create("LINE_S2S3", Side.TWO))
                .setLowLimit(20.0)
                .setHighLimit(-20.0);
        IllegalStateException e = assertThrows(IllegalStateException.class, adder::add);
        assertEquals("VoltageAngleLimit lowLimit must be inferior to highLimit.", e.getMessage());
    }
}
