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
import com.powsybl.iidm.network.Terminal;
import org.junit.jupiter.api.Test;

import java.util.Properties;

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
        assertEquals(2, network.getStaticVarCompensatorCount());

        assertEq(network);
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "staticVarCompensator_EQ.xml", "staticVarCompensator_SSH.xml");
        assertEquals(2, network.getStaticVarCompensatorCount());

        assertFirstSsh(network);
    }

    @Test
    void importEqTwoSshsAndSvTest() {
        Network network = readCgmesResources(DIR, "staticVarCompensator_EQ.xml");
        assertEquals(2, network.getStaticVarCompensatorCount());

        assertEq(network);

        readCgmesResources(network, DIR, "staticVarCompensator_SSH.xml");
        assertFirstSsh(network);

        readCgmesResources(network, DIR, "staticVarCompensator_SSH_1.xml");
        assertSecondSsh(network);

        assertFlowsBeforeSv(network);
        readCgmesResources(network, DIR, "staticVarCompensator_SV.xml");
        assertFlowsAfterSv(network);
    }

    @Test
    void usePreviousValuesTest() {
        Network network = readCgmesResources(DIR, "staticVarCompensator_EQ.xml", "staticVarCompensator_SSH.xml", "staticVarCompensator_SV.xml");
        assertEquals(2, network.getStaticVarCompensatorCount());
        assertFirstSsh(network);
        assertFlowsAfterSv(network);

        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.use-previous-values-during-update", "true");
        readCgmesResources(network, properties, DIR, "../empty_SSH.xml", "../empty_SV.xml");
        assertFirstSsh(network);
        assertFlowsAfterEmptySv(network);
    }

    private static void assertEq(Network network) {
        assertEq(network.getStaticVarCompensator("StaticVarCompensator-V"), StaticVarCompensator.RegulationMode.VOLTAGE);
        assertEq(network.getStaticVarCompensator("StaticVarCompensator-Q"), StaticVarCompensator.RegulationMode.REACTIVE_POWER);
    }

    private static void assertFirstSsh(Network network) {
        assertSsh(network.getStaticVarCompensator("StaticVarCompensator-V"), Double.NaN, 405.0, true);
        assertSsh(network.getStaticVarCompensator("StaticVarCompensator-Q"), 210.0, Double.NaN, true);
    }

    private static void assertSecondSsh(Network network) {
        assertSsh(network.getStaticVarCompensator("StaticVarCompensator-V"), Double.NaN, 400.0, false);
        assertSsh(network.getStaticVarCompensator("StaticVarCompensator-Q"), 215.0, Double.NaN, false);
    }

    private static void assertFlowsBeforeSv(Network network) {
        assertFlows(network.getStaticVarCompensator("StaticVarCompensator-V").getTerminal(), Double.NaN, Double.NaN);
        assertFlows(network.getStaticVarCompensator("StaticVarCompensator-Q").getTerminal(), Double.NaN, Double.NaN);
    }

    private static void assertFlowsAfterSv(Network network) {
        assertFlows(network.getStaticVarCompensator("StaticVarCompensator-V").getTerminal(), 0.0, -50.0);
        assertFlows(network.getStaticVarCompensator("StaticVarCompensator-Q").getTerminal(), 0.0, -200.0);
    }

    private static void assertFlowsAfterEmptySv(Network network) {
        assertFlows(network.getStaticVarCompensator("StaticVarCompensator-V").getTerminal(), Double.NaN, Double.NaN);
        assertFlows(network.getStaticVarCompensator("StaticVarCompensator-Q").getTerminal(), Double.NaN, Double.NaN);
    }

    private static void assertEq(StaticVarCompensator staticVarCompensator, StaticVarCompensator.RegulationMode regulationMode) {
        assertNotNull(staticVarCompensator);
        assertTrue(Double.isNaN(staticVarCompensator.getReactivePowerSetpoint()));
        assertTrue(Double.isNaN(staticVarCompensator.getVoltageSetpoint()));
        assertNotNull(staticVarCompensator.getRegulatingTerminal());
        assertEquals(regulationMode, staticVarCompensator.getRegulationMode());
        assertFalse(staticVarCompensator.isRegulating());

        if (regulationMode == StaticVarCompensator.RegulationMode.REACTIVE_POWER) {
            assertNotNull(staticVarCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL_SIGN));
        }
        assertNotNull(staticVarCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.REGULATING_CONTROL));
    }

    private static void assertSsh(StaticVarCompensator staticVarCompensator, double targetQ, double targetV, boolean regulating) {
        assertNotNull(staticVarCompensator);
        double tol = 0.0000001;
        assertEquals(targetQ, staticVarCompensator.getReactivePowerSetpoint(), tol);
        assertEquals(targetV, staticVarCompensator.getVoltageSetpoint(), tol);
        assertEquals(regulating, staticVarCompensator.isRegulating());
    }

    private static void assertFlows(Terminal terminal, double p, double q) {
        double tol = 0.0000001;
        assertEquals(p, terminal.getP(), tol);
        assertEquals(q, terminal.getQ(), tol);
    }
}
