/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class StaticVarCompensatorTest {

    private static final String DIR = "/issues/static-var-compensator/";

    @Test
    void staticVarCompensatorControlTest() {
        Network network = readCgmesResources(DIR, "staticVarCompensator_EQ.xml", "staticVarCompensator_SSH.xml");
        assertEquals(7, network.getStaticVarCompensatorCount());

        StaticVarCompensator svc = network.getStaticVarCompensator("StaticVarCompensator");
        assertExpectedControl(svc, StaticVarCompensator.RegulationMode.VOLTAGE, Double.NaN, Double.NaN, false);

        svc = network.getStaticVarCompensator("StaticVarCompensator-voltageSetPoint");
        assertExpectedControl(svc, StaticVarCompensator.RegulationMode.VOLTAGE, Double.NaN, Double.NaN, false);

        svc = network.getStaticVarCompensator("StaticVarCompensator-voltage-svcControlMode");
        assertExpectedControl(svc, StaticVarCompensator.RegulationMode.VOLTAGE, Double.NaN, Double.NaN, false);

        svc = network.getStaticVarCompensator("StaticVarCompensator-voltageSetPoint-svcControlMode");
        assertExpectedControl(svc, StaticVarCompensator.RegulationMode.VOLTAGE, 405.0, Double.NaN, false);

        svc = network.getStaticVarCompensator("StaticVarCompensator-voltageSetPoint-svcControlMode-on");
        assertExpectedControl(svc, StaticVarCompensator.RegulationMode.VOLTAGE, 405.0, Double.NaN, true);

        svc = network.getStaticVarCompensator("StaticVarCompensator-reactivePower-svcControlMode");
        assertExpectedControl(svc, StaticVarCompensator.RegulationMode.VOLTAGE, Double.NaN, Double.NaN, false);

        svc = network.getStaticVarCompensator("StaticVarCompensator-reactivePower-svcControlMode-on");
        assertExpectedControl(svc, StaticVarCompensator.RegulationMode.REACTIVE_POWER, Double.NaN, 10.0, true);
    }

    private static void assertExpectedControl(StaticVarCompensator staticVarCompensator, StaticVarCompensator.RegulationMode defaultRegulationMode, double defaultTargetV, double defaultTargetQ, boolean regulating) {
        assertNotNull(staticVarCompensator);
        assertNotNull(staticVarCompensator.getRegulatingTerminal());
        double tol = 0.0000001;
        assertEquals(defaultTargetV, staticVarCompensator.getVoltageSetpoint(), tol);
        assertEquals(defaultTargetQ, staticVarCompensator.getReactivePowerSetpoint(), tol);
        assertEquals(defaultRegulationMode, staticVarCompensator.getRegulationMode());
        assertEquals(regulating, staticVarCompensator.isRegulating());
    }
}
