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
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
*
* @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
* @author José Antonio Marqués {@literal <marquesja at aia.es>}
*/
class VoltageAngleLimitTest {

    @Test
    void voltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        network.newVoltageAngleLimit().setId("VOLTAGE_ANGLE_LIMIT_LINE_S2S3")
            .from(network.getLine("LINE_S2S3").getTerminal1())
            .to(network.getLine("LINE_S2S3").getTerminal2())
            .setHighLimit(1.0)
            .add();

        network.newVoltageAngleLimit().setId("VOLTAGE_ANGLE_LIMIT_LD1_LD6")
            .from(network.getLoad("LD1").getTerminal())
            .to(network.getLoad("LD6").getTerminal())
            .setLowLimit(1.0)
            .add();

        assertEquals(2, network.getVoltageAngleLimitsStream().count());

        VoltageAngleLimit val0 = network.getVoltageAngleLimitsStream().toList().get(1);
        assertEquals("LD1", val0.getTerminalFrom().getConnectable().getId());
        assertTrue(Terminal.getConnectableSide(val0.getTerminalFrom()).isEmpty());
        assertEquals("LD6", val0.getTerminalTo().getConnectable().getId());
        assertTrue(Terminal.getConnectableSide(val0.getTerminalTo()).isEmpty());
        assertTrue(val0.getLowLimit().isPresent());
        assertTrue(val0.getHighLimit().isEmpty());
        assertEquals(1.0, val0.getLowLimit().getAsDouble());

        VoltageAngleLimit val1 = network.getVoltageAngleLimitsStream().toList().get(0);
        assertEquals("VOLTAGE_ANGLE_LIMIT_LINE_S2S3", val1.getId());

        assertEquals("LINE_S2S3", val1.getTerminalFrom().getConnectable().getId());
        assertTrue(Terminal.getConnectableSide(val1.getTerminalFrom()).isPresent());
        Terminal.getConnectableSide(val1.getTerminalFrom()).ifPresent(side -> assertEquals(ThreeSides.ONE, side));
        assertEquals("LINE_S2S3", val1.getTerminalTo().getConnectable().getId());
        assertTrue(Terminal.getConnectableSide(val1.getTerminalTo()).isPresent());
        Terminal.getConnectableSide(val1.getTerminalTo()).ifPresent(side -> assertEquals(ThreeSides.TWO, side));
        assertTrue(val1.getHighLimit().isPresent());
        assertTrue(val1.getLowLimit().isEmpty());
        assertEquals(1.0, val1.getHighLimit().getAsDouble());
    }

    @Test
    void noNameLimitVoltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
                .from(network.getLine("LINE_S2S3").getTerminal1())
                .to(network.getLine("LINE_S2S3").getTerminal2());
        IllegalStateException e = assertThrows(IllegalStateException.class, adder::add);
        assertEquals("Voltage angle limit id is mandatory.", e.getMessage());
    }

    @Test
    void badLimitsVoltageAngleLimitTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();

        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
                .setId("VOLTAGE_ANGLE_LIMIT_LINE_S2S3")
                .from(network.getLine("LINE_S2S3").getTerminal1())
                .to(network.getLine("LINE_S2S3").getTerminal2())
                .setLowLimit(20.0)
                .setHighLimit(-20.0);
        IllegalStateException e = assertThrows(IllegalStateException.class, adder::add);
        assertEquals("Voltage angle low limit must be lower than the high limit.", e.getMessage());
    }

    @Test
    void uniqueIdTest() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        network.newVoltageAngleLimit()
                .setId("Limit")
                .from(network.getLine("LINE_S2S3").getTerminal1())
                .to(network.getLine("LINE_S2S3").getTerminal2())
                .setLowLimit(-20.0)
                .setHighLimit(20.0)
                .add();
        VoltageAngleLimitAdder adder = network.newVoltageAngleLimit()
                .setId("Limit")
                .from(network.getLine("LINE_S2S3").getTerminal1())
                .to(network.getLine("LINE_S2S3").getTerminal2())
                .setLowLimit(-20.0)
                .setHighLimit(20.0);
        PowsyblException e = assertThrows(PowsyblException.class, adder::add);
        assertEquals("The network " + network.getId()
                + " already contains a voltage angle limit with the id 'Limit'", e.getMessage());
    }

    @Test
    void removeTest() {
        String id = "VOLTAGE_ANGLE_LIMIT_LINE_S2S3";

        Network network = FourSubstationsNodeBreakerFactory.create();
        Line lineS2S3 = network.getLine("LINE_S2S3");
        network.newVoltageAngleLimit().setId(id)
                .from(lineS2S3.getTerminal1())
                .to(lineS2S3.getTerminal2())
                .setHighLimit(10.0)
                .add();

        assertNotNull(network.getVoltageAngleLimit(id));
        network.getVoltageAngleLimit(id).remove();
        assertNull(network.getVoltageAngleLimit(id));
    }

}
