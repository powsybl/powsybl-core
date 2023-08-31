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
            .from(TerminalRef.create("LINE_S2S3", Side.ONE))
            .to(TerminalRef.create("LINE_S2S3", Side.TWO))
            .setHighLimit(1.0)
            .add();

        network.newVoltageAngleLimit().setName("VOLTAGE_ANGLE_LIMIT_LD1_LD6")
            .from(TerminalRef.create("LD1"))
            .to(TerminalRef.create("LD6"))
            .setLowLimit(1.0)
            .add();

        assertEquals(2, network.getVoltageAngleLimits().size());

        VoltageAngleLimit val0 = network.getVoltageAngleLimits().get(0);
        assertEquals("VOLTAGE_ANGLE_LIMIT_LINE_S2S3", val0.getName());

        assertEquals("LINE_S2S3", val0.getTerminalFrom().getConnectable().getId());
        assertTrue(TerminalRef.getConnectableSide(val0.getTerminalFrom()).isPresent());
        TerminalRef.getConnectableSide(val0.getTerminalFrom()).ifPresent(side -> assertEquals(Side.ONE, side));
        assertEquals("LINE_S2S3", val0.getTerminalTo().getConnectable().getId());
        assertTrue(TerminalRef.getConnectableSide(val0.getTerminalTo()).isPresent());
        TerminalRef.getConnectableSide(val0.getTerminalTo()).ifPresent(side -> assertEquals(Side.TWO, side));
        assertTrue(val0.getHighLimit().isPresent());
        assertTrue(val0.getLowLimit().isEmpty());
        assertEquals(1.0, val0.getHighLimit().get());

        VoltageAngleLimit val1 = network.getVoltageAngleLimits().get(1);
        assertEquals("LD1", val1.getTerminalFrom().getConnectable().getId());
        assertTrue(TerminalRef.getConnectableSide(val1.getTerminalFrom()).isEmpty());
        assertEquals("LD6", val1.getTerminalTo().getConnectable().getId());
        assertTrue(TerminalRef.getConnectableSide(val1.getTerminalTo()).isEmpty());
        assertTrue(val1.getLowLimit().isPresent());
        assertTrue(val1.getHighLimit().isEmpty());
        assertEquals(1.0, val1.getLowLimit().get());
    }

    @Test
    void badSwitchVoltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
            .setName("VOLTAGE_ANGLE_LIMIT_S1VL1_LD1_BREAKER")
            .from(TerminalRef.create("S1VL1_LD1_BREAKER", Side.ONE))
            .to(TerminalRef.create("S1VL1_LD1_BREAKER", Side.TWO))
            .setHighLimit(1.0);
        PowsyblException e = assertThrows(PowsyblException.class, adder::add);
        assertEquals("Unexpected terminal reference identifiable instance: class com.powsybl.iidm.network.impl.SwitchImpl", e.getMessage());
    }

    @Test
    void badBranchSideVoltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
            .setName("VOLTAGE_ANGLE_LIMIT_LINE_S2S3")
            .from(TerminalRef.create("LINE_S2S3", Side.ONE))
            .to(TerminalRef.create("LINE_S2S3", Side.THREE))
            .setHighLimit(1.0);
        IllegalStateException e = assertThrows(IllegalStateException.class, adder::add);
        assertEquals("Unexpected Branch side: THREE", e.getMessage());
    }

    @Test
    void badIdentifiableSideVoltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
            .setName("VOLTAGE_ANGLE_LIMIT_LINE_S2S3")
            .from(TerminalRef.create("LIN_S2S3", Side.ONE))
            .to(TerminalRef.create("LINE_S2S3", Side.THREE))
            .setHighLimit(1.0);
        PowsyblException e = assertThrows(PowsyblException.class, adder::add);
        assertEquals("Terminal reference identifiable not found: 'LIN_S2S3'", e.getMessage());
    }

    @Test
    void noNameLimitVoltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
                .from(TerminalRef.create("LINE_S2S3", Side.ONE))
                .to(TerminalRef.create("LINE_S2S3", Side.TWO));
        IllegalStateException e = assertThrows(IllegalStateException.class, adder::add);
        assertEquals("Voltage angle limit name is mandatory.", e.getMessage());
    }

    @Test
    void badLimitsVoltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
                .setName("VOLTAGE_ANGLE_LIMIT_LINE_S2S3")
                .from(TerminalRef.create("LINE_S2S3", Side.ONE))
                .to(TerminalRef.create("LINE_S2S3", Side.TWO))
                .setLowLimit(20.0)
                .setHighLimit(-20.0);
        IllegalStateException e = assertThrows(IllegalStateException.class, adder::add);
        assertEquals("Voltage angle low limit must be lower than the high limit.", e.getMessage());
    }
}
