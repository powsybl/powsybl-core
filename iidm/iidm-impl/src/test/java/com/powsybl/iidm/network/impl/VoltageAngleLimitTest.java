/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.TerminalRef.Side;
import com.powsybl.iidm.network.VoltageAngleLimit.FlowDirection;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            .from(TerminalRef.create("LINE_S2S3", Side.ONE))
            .to(TerminalRef.create("LINE_S2S3", Side.TWO))
            .withLimit(1.0)
            .withFlowDirection(FlowDirection.FROM_TO)
            .add();

        network.newVoltageAngleLimit()
            .from(TerminalRef.create("LD1"))
            .to(TerminalRef.create("LD6"))
            .withLimit(1.0)
            .withFlowDirection(FlowDirection.TO_FROM)
            .add();

        assertEquals(2, network.getVoltageAngleLimits().size());

        VoltageAngleLimit val0 = network.getVoltageAngleLimits().get(0);
        assertEquals("LINE_S2S3", val0.getTerminalFrom().getConnectable().getId());
        assertEquals(Side.ONE, TerminalRef.getConnectableSide(val0.getTerminalFrom()));
        assertEquals("LINE_S2S3", val0.getTerminalTo().getConnectable().getId());
        assertEquals(Side.TWO, TerminalRef.getConnectableSide(val0.getTerminalTo()));
        assertEquals(1.0, val0.getLimit());
        assertEquals(FlowDirection.FROM_TO, val0.getFlowDirection());

        VoltageAngleLimit val1 = network.getVoltageAngleLimits().get(1);
        assertEquals("LD1", val1.getTerminalFrom().getConnectable().getId());
        assertEquals(Side.ONE, TerminalRef.getConnectableSide(val1.getTerminalFrom()));
        assertEquals("LD6", val1.getTerminalTo().getConnectable().getId());
        assertEquals(Side.ONE, TerminalRef.getConnectableSide(val1.getTerminalTo()));
        assertEquals(1.0, val1.getLimit());
        assertEquals(FlowDirection.TO_FROM, val1.getFlowDirection());
    }

    @Test
    void badThreeWindingsTransformerVoltageAngleLimitTest() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
            .from(TerminalRef.create("3WT", Side.ONE))
            .to(TerminalRef.create("3WT", Side.TWO))
            .withLimit(1.0)
            .withFlowDirection(FlowDirection.FROM_TO);

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> adder.add());
        assertEquals("VoltageAngleLimit can not be defined on threeWindingsTransformers : 3WT", e.getMessage());
    }

    @Test
    void badSwitchVoltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
            .from(TerminalRef.create("S1VL1_LD1_BREAKER", Side.ONE))
            .to(TerminalRef.create("S1VL1_LD1_BREAKER", Side.TWO))
            .withLimit(1.0)
            .withFlowDirection(FlowDirection.BOTH_DIRECTIONS);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> adder.add());
        assertEquals("VoltageAngleLimit can not be defined on switches : S1VL1_LD1_BREAKER", e.getMessage());
    }

    @Test
    void badLimitVoltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
            .from(TerminalRef.create("LINE_S2S3", Side.ONE))
            .to(TerminalRef.create("LINE_S2S3", Side.TWO))
            .withLimit(-1.0)
            .withFlowDirection(FlowDirection.FROM_TO);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> adder.add());
        assertEquals("Limit <= 0: -1.0", e.getMessage());
    }

    @Test
    void badBranchSideVoltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
            .from(TerminalRef.create("LINE_S2S3", Side.ONE))
            .to(TerminalRef.create("LINE_S2S3", Side.THREE))
            .withLimit(1.0)
            .withFlowDirection(FlowDirection.FROM_TO);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> adder.add());
        assertEquals("Unexpected Branch side: THREE", e.getMessage());
    }

    @Test
    void badIdentifiableSideVoltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
            .from(TerminalRef.create("LIN_S2S3", Side.ONE))
            .to(TerminalRef.create("LINE_S2S3", Side.THREE))
            .withLimit(1.0)
            .withFlowDirection(FlowDirection.FROM_TO);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> adder.add());
        assertEquals("Identifiable from not found: LIN_S2S3", e.getMessage());
    }
}
