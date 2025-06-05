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
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class ShuntCompensatorUpdateTest {

    private static final String DIR = "/update/shunt-compensator/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "shuntCompensator_EQ.xml");
        assertEquals(3, network.getShuntCompensatorCount());

        assertEq(network);
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "shuntCompensator_EQ.xml", "shuntCompensator_SSH.xml");
        assertEquals(3, network.getShuntCompensatorCount());

        assertFirstSsh(network);
    }

    @Test
    void importEqTwoSshsAndSvTest() {
        Network network = readCgmesResources(DIR, "shuntCompensator_EQ.xml");
        assertEquals(3, network.getShuntCompensatorCount());

        assertEq(network);

        readCgmesResources(network, DIR, "shuntCompensator_SSH.xml");
        assertFirstSsh(network);

        readCgmesResources(network, DIR, "shuntCompensator_SSH_1.xml");
        assertSecondSsh(network);

        assertFlowsBeforeSv(network);
        readCgmesResources(network, DIR, "shuntCompensator_SV.xml");
        assertFlowsAfterSv(network);
    }

    private static void assertEq(Network network) {
        assertEq(network.getShuntCompensator("LinearShuntCompensator"));
        assertEq(network.getShuntCompensator("NonLinearShuntCompensator"));
        assertEq(network.getShuntCompensator("EquivalentShunt"));
    }

    private static void assertFirstSsh(Network network) {
        assertSsh(network.getShuntCompensator("LinearShuntCompensator"), 1, 405.0, 0.0, false);
        assertSsh(network.getShuntCompensator("NonLinearShuntCompensator"), 1, 405.0, 0.0, false);
        assertSsh(network.getShuntCompensator("EquivalentShunt"), 0, Double.NaN, Double.NaN, false);
    }

    private static void assertSecondSsh(Network network) {
        assertSsh(network.getShuntCompensator("LinearShuntCompensator"), 1, 407.0, 0.2, true);
        assertSsh(network.getShuntCompensator("NonLinearShuntCompensator"), 1, 406.0, 0.1, true);
        assertSsh(network.getShuntCompensator("EquivalentShunt"), 0, Double.NaN, Double.NaN, false);
    }

    private static void assertFlowsBeforeSv(Network network) {
        assertFlows(network.getShuntCompensator("LinearShuntCompensator").getTerminal(), Double.NaN, Double.NaN);
        assertFlows(network.getShuntCompensator("NonLinearShuntCompensator").getTerminal(), Double.NaN, Double.NaN);
        assertFlows(network.getShuntCompensator("EquivalentShunt").getTerminal(), Double.NaN, Double.NaN);
    }

    private static void assertFlowsAfterSv(Network network) {
        assertFlows(network.getShuntCompensator("LinearShuntCompensator").getTerminal(), 0.0, 50.0);
        assertFlows(network.getShuntCompensator("NonLinearShuntCompensator").getTerminal(), 1.0, 25.0);
        assertFlows(network.getShuntCompensator("EquivalentShunt").getTerminal(), 2.0, -5.0);
    }

    private static void assertEq(ShuntCompensator shuntCompensator) {
        assertNotNull(shuntCompensator);
        assertTrue(Double.isNaN(shuntCompensator.getTargetV()));
        assertTrue(Double.isNaN(shuntCompensator.getTargetDeadband()));
        assertNotNull(shuntCompensator.getRegulatingTerminal());
        assertFalse(shuntCompensator.isVoltageRegulatorOn());

        if (!shuntCompensator.getPropertyNames().contains(Conversion.PROPERTY_IS_EQUIVALENT_SHUNT)) {
            assertNotNull(shuntCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.NORMAL_SECTIONS));
        }

        assertEquals(0, shuntCompensator.getSectionCount());
    }

    private static void assertSsh(ShuntCompensator shuntCompensator, int sectionsCount, double targetV, double targetDeadband, boolean isRegulatingOn) {
        assertNotNull(shuntCompensator);
        double tol = 0.0000001;
        assertEquals(sectionsCount, shuntCompensator.getSectionCount());
        assertEquals(targetV, shuntCompensator.getTargetV(), tol);
        assertEquals(targetDeadband, shuntCompensator.getTargetDeadband(), tol);
        assertEquals(isRegulatingOn, shuntCompensator.isVoltageRegulatorOn());
    }

    private static void assertFlows(Terminal terminal, double p, double q) {
        double tol = 0.0000001;
        assertEquals(p, terminal.getP(), tol);
        assertEquals(q, terminal.getQ(), tol);
    }
}
