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

        ShuntCompensator linearShuntCompensator = network.getShuntCompensator("LinearShuntCompensator");
        assertTrue(checkEq(linearShuntCompensator));

        ShuntCompensator nonLinearShuntCompensator = network.getShuntCompensator("NonLinearShuntCompensator");
        assertTrue(checkEq(nonLinearShuntCompensator));

        ShuntCompensator equivalentShunt = network.getShuntCompensator("EquivalentShunt");
        assertTrue(checkEq(equivalentShunt));
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "shuntCompensator_EQ.xml", "shuntCompensator_SSH.xml");
        assertEquals(3, network.getShuntCompensatorCount());

        ShuntCompensator linearShuntCompensator = network.getShuntCompensator("LinearShuntCompensator");
        assertTrue(checkSsh(linearShuntCompensator, 1, 405.0, 0.0, false));

        ShuntCompensator nonLinearShuntCompensator = network.getShuntCompensator("NonLinearShuntCompensator");
        assertTrue(checkSsh(nonLinearShuntCompensator, 1, 405.0, 0.0, false));

        ShuntCompensator equivalentShunt = network.getShuntCompensator("EquivalentShunt");
        assertTrue(checkSsh(equivalentShunt, 0, Double.NaN, Double.NaN, false));
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "shuntCompensator_EQ.xml");
        assertEquals(3, network.getShuntCompensatorCount());

        ShuntCompensator linearShuntCompensator = network.getShuntCompensator("LinearShuntCompensator");
        assertTrue(checkEq(linearShuntCompensator));
        ShuntCompensator nonLinearShuntCompensator = network.getShuntCompensator("NonLinearShuntCompensator");
        assertTrue(checkEq(nonLinearShuntCompensator));
        ShuntCompensator equivalentShunt = network.getShuntCompensator("EquivalentShunt");
        assertTrue(checkEq(equivalentShunt));

        readCgmesResources(network, DIR, "shuntCompensator_SSH.xml");
        assertTrue(checkSsh(linearShuntCompensator, 1, 405.0, 0.0, false));
        assertTrue(checkSsh(nonLinearShuntCompensator, 1, 405.0, 0.0, false));
        assertTrue(checkSsh(equivalentShunt, 0, Double.NaN, Double.NaN, false));

        readCgmesResources(network, DIR, "shuntCompensator_SSH_1.xml");
        assertTrue(checkSsh(linearShuntCompensator, 1, 407.0, 0.2, true));
        assertTrue(checkSsh(nonLinearShuntCompensator, 1, 406.0, 0.1, true));
        assertTrue(checkSsh(equivalentShunt, 0, Double.NaN, Double.NaN, false));
    }

    private static boolean checkEq(ShuntCompensator shuntCompensator) {
        assertNotNull(shuntCompensator);
        assertTrue(Double.isNaN(shuntCompensator.getTargetV()));
        assertTrue(Double.isNaN(shuntCompensator.getTargetDeadband()));
        assertNotNull(shuntCompensator.getRegulatingTerminal());
        assertFalse(shuntCompensator.isVoltageRegulatorOn());

        if (!shuntCompensator.getPropertyNames().contains(Conversion.PROPERTY_IS_EQUIVALENT_SHUNT)) {
            assertNotNull(shuntCompensator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.NORMAL_SECTIONS));
        }

        assertEquals(0, shuntCompensator.getSectionCount());
        return true;
    }

    private static boolean checkSsh(ShuntCompensator shuntCompensator, int sectionsCount, double targetV, double targetDeadband, boolean isRegulatingOn) {
        assertNotNull(shuntCompensator);
        double tol = 0.0000001;
        assertEquals(sectionsCount, shuntCompensator.getSectionCount());
        assertEquals(targetV, shuntCompensator.getTargetV(), tol);
        assertEquals(targetDeadband, shuntCompensator.getTargetDeadband(), tol);
        assertEquals(isRegulatingOn, shuntCompensator.isVoltageRegulatorOn());
        return true;
    }
}
