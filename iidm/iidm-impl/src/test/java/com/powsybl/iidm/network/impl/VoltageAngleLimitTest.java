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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
*
* @author Luma Zamarreño <zamarrenolm at aia.es>
* @author José Antonio Marqués <marquesja at aia.es>
*/
class VoltageAngleLimitTest {

    @Test
    void limitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        network.newVoltageAngleLimit()
            .from(TerminalRef.create("LINE_S2S3", Side.ONE))
            .to(TerminalRef.create("LINE_S2S3", Side.TWO))
            .withLimit(1.0)
            .withFlowDirection(FlowDirection.FROM_TO)
            .add();

        network.newVoltageAngleLimit()
            .from(TerminalRef.create("S1VL1_LD1_BREAKER", Side.ONE))
            .to(TerminalRef.create("S1VL1_LD1_BREAKER", Side.TWO))
            .withLimit(1.0)
            .withFlowDirection(FlowDirection.BOTH_DIRECTIONS)
            .add();

        network.newVoltageAngleLimit()
            .from(TerminalRef.create("LD1"))
            .to(TerminalRef.create("LD6"))
            .withLimit(1.0)
            .withFlowDirection(FlowDirection.TO_FROM)
            .add();

        assertEquals(3, network.getVoltageAngleLimits().size());

        assertEquals("LINE_S2S3", network.getVoltageAngleLimits().get(0).getFrom().getId());
        assertEquals(Side.ONE, network.getVoltageAngleLimits().get(0).getFrom().getSide());
        assertEquals("LINE_S2S3", network.getVoltageAngleLimits().get(0).getTo().getId());
        assertEquals(Side.TWO, network.getVoltageAngleLimits().get(0).getTo().getSide());
        assertEquals(1.0, network.getVoltageAngleLimits().get(0).getLimit());
        assertEquals(FlowDirection.FROM_TO, network.getVoltageAngleLimits().get(0).getFlowDirection());

        assertTrue(network.getVoltageAngleLimits().get(0).getTerminalFrom().map(t -> compareTerminal(t, "LINE_S2S3")).orElse(false));
        assertTrue(network.getVoltageAngleLimits().get(0).getTerminalTo().map(t -> compareTerminal(t, "LINE_S2S3")).orElse(false));

        assertEquals("S1VL1_LD1_BREAKER", network.getVoltageAngleLimits().get(1).getFrom().getId());
        assertEquals(Side.ONE, network.getVoltageAngleLimits().get(1).getFrom().getSide());
        assertEquals("S1VL1_LD1_BREAKER", network.getVoltageAngleLimits().get(1).getTo().getId());
        assertEquals(Side.TWO, network.getVoltageAngleLimits().get(1).getTo().getSide());
        assertEquals(1.0, network.getVoltageAngleLimits().get(1).getLimit());
        assertEquals(FlowDirection.BOTH_DIRECTIONS, network.getVoltageAngleLimits().get(1).getFlowDirection());

        assertTrue(network.getVoltageAngleLimits().get(1).getTerminalFrom().map(t -> compareTerminal(t, "S1VL1_BBS")).orElse(false));
        assertTrue(network.getVoltageAngleLimits().get(1).getTerminalTo().map(t -> compareTerminal(t, "S1VL1_BBS")).orElse(false));

        assertEquals("LD1", network.getVoltageAngleLimits().get(2).getFrom().getId());
        assertEquals(Side.ONE, network.getVoltageAngleLimits().get(2).getFrom().getSide());
        assertEquals("LD6", network.getVoltageAngleLimits().get(2).getTo().getId());
        assertEquals(Side.ONE, network.getVoltageAngleLimits().get(2).getTo().getSide());
        assertEquals(1.0, network.getVoltageAngleLimits().get(2).getLimit());
        assertEquals(FlowDirection.TO_FROM, network.getVoltageAngleLimits().get(2).getFlowDirection());

        assertTrue(network.getVoltageAngleLimits().get(2).getTerminalFrom().map(t -> compareTerminal(t, "LD1")).orElse(false));
        assertTrue(network.getVoltageAngleLimits().get(2).getTerminalTo().map(t -> compareTerminal(t, "LD6")).orElse(false));
    }

    @Test
    void threeWindingsTransformerLimitTest() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();

        network.newVoltageAngleLimit()
            .from(TerminalRef.create("3WT", Side.ONE))
            .to(TerminalRef.create("3WT", Side.TWO))
            .withLimit(1.0)
            .withFlowDirection(FlowDirection.FROM_TO)
            .add();

        network.newVoltageAngleLimit()
            .from(TerminalRef.create("3WT", Side.ONE))
            .to(TerminalRef.create("3WT", Side.THREE))
            .withLimit(1.1)
            .withFlowDirection(FlowDirection.FROM_TO)
            .add();

        assertEquals(2, network.getVoltageAngleLimits().size());

        assertEquals("3WT", network.getVoltageAngleLimits().get(0).getFrom().getId());
        assertEquals(Side.ONE, network.getVoltageAngleLimits().get(0).getFrom().getSide());
        assertEquals("3WT", network.getVoltageAngleLimits().get(0).getTo().getId());
        assertEquals(Side.TWO, network.getVoltageAngleLimits().get(0).getTo().getSide());
        assertEquals(1.0, network.getVoltageAngleLimits().get(0).getLimit());
        assertEquals(FlowDirection.FROM_TO, network.getVoltageAngleLimits().get(0).getFlowDirection());

        assertTrue(network.getVoltageAngleLimits().get(0).getTerminalFrom().map(t -> compareTerminal(t, "3WT")).orElse(false));
        assertTrue(network.getVoltageAngleLimits().get(0).getTerminalTo().map(t -> compareTerminal(t, "3WT")).orElse(false));

        assertEquals("3WT", network.getVoltageAngleLimits().get(1).getFrom().getId());
        assertEquals(Side.ONE, network.getVoltageAngleLimits().get(1).getFrom().getSide());
        assertEquals("3WT", network.getVoltageAngleLimits().get(1).getTo().getId());
        assertEquals(Side.THREE, network.getVoltageAngleLimits().get(1).getTo().getSide());
        assertEquals(1.1, network.getVoltageAngleLimits().get(1).getLimit());
        assertEquals(FlowDirection.FROM_TO, network.getVoltageAngleLimits().get(01).getFlowDirection());

        assertTrue(network.getVoltageAngleLimits().get(1).getTerminalFrom().map(t -> compareTerminal(t, "3WT")).orElse(false));
        assertTrue(network.getVoltageAngleLimits().get(1).getTerminalTo().map(t -> compareTerminal(t, "3WT")).orElse(false));
    }

    private static boolean compareTerminal(Terminal terminal, String eqId) {
        return eqId.equals(terminal.getConnectable().getId());
    }
}
