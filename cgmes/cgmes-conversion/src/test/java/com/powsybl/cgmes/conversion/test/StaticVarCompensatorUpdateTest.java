/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class StaticVarCompensatorUpdateTest {

    private static final String DIR = "/update/static-var-compensator/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "staticVarCompensator_EQ.xml");
        assertEquals(1, network.getStaticVarCompensatorCount());

        StaticVarCompensator staticVarCompensator = network.getStaticVarCompensator("StaticVarCompensator");
        assertTrue(checkEq(staticVarCompensator));
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "staticVarCompensator_EQ.xml", "staticVarCompensator_SSH.xml");
        assertEquals(1, network.getStaticVarCompensatorCount());

        StaticVarCompensator staticVarCompensator = network.getStaticVarCompensator("StaticVarCompensator");
        assertTrue(checkSsh(staticVarCompensator, Double.NaN, 405.0, StaticVarCompensator.RegulationMode.VOLTAGE));
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "staticVarCompensator_EQ.xml");
        assertEquals(1, network.getStaticVarCompensatorCount());

        StaticVarCompensator staticVarCompensator = network.getStaticVarCompensator("StaticVarCompensator");
        assertTrue(checkEq(staticVarCompensator));

        readCgmesResources(network, DIR, "staticVarCompensator_SSH.xml");
        assertTrue(checkSsh(staticVarCompensator, Double.NaN, 405.0, StaticVarCompensator.RegulationMode.VOLTAGE));

        readCgmesResources(network, DIR, "staticVarCompensator_SSH_1.xml");
        assertTrue(checkSsh(staticVarCompensator, Double.NaN, 400.0, StaticVarCompensator.RegulationMode.OFF));
    }

    private static boolean checkEq(StaticVarCompensator staticVarCompensator) {
        assertNotNull(staticVarCompensator);
        assertTrue(Double.isNaN(staticVarCompensator.getReactivePowerSetpoint()));
        assertTrue(Double.isNaN(staticVarCompensator.getVoltageSetpoint()));
        assertNotNull(staticVarCompensator.getRegulatingTerminal());
        assertEquals(StaticVarCompensator.RegulationMode.OFF, staticVarCompensator.getRegulationMode());

        assertNotNull(staticVarCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.MODE));
        assertNotNull(staticVarCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.REGULATING_CONTROL));
        return true;
    }

    private static boolean checkSsh(StaticVarCompensator staticVarCompensator, double targetQ, double targetV, StaticVarCompensator.RegulationMode regulationMode) {
        assertNotNull(staticVarCompensator);
        double tol = 0.0000001;
        assertEquals(targetQ, staticVarCompensator.getReactivePowerSetpoint(), tol);
        assertEquals(targetV, staticVarCompensator.getVoltageSetpoint(), tol);
        assertEquals(regulationMode, staticVarCompensator.getRegulationMode());
        return true;
    }
}
