/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
class VoltageRegulationTest extends AbstractSerDeTest {

    private static final String DIR = "/issues/voltageRegulation/";

    @Test
    void generatorVoltageRegulationEq() {
        // EQ only import: regulation is created without targets and not enabled
        Network network = readCgmesResources(DIR, "generator_EQ.xml");

        // G0: no regulation (not CGMES compliant)
        Generator g0 = network.getGenerator("SM_0");
        assertLocalTargets(g0, Double.NaN, Double.NaN);
        VoltageRegulation reg0 = g0.getVoltageRegulation();
        assertNull(reg0);

        // G1: local voltage regulation
        Generator g1 = network.getGenerator("SM_1");
        assertLocalTargets(g1, Double.NaN, Double.NaN);
        VoltageRegulation reg1 = g1.getVoltageRegulation();
        assertVoltageRegulation(reg1, RegulationMode.VOLTAGE, null, Double.NaN, false);

        // G2: remote voltage regulation
        Generator g2 = network.getGenerator("SM_2");
        assertLocalTargets(g2, Double.NaN, Double.NaN);
        VoltageRegulation reg2 = g2.getVoltageRegulation();
        assertVoltageRegulation(reg2, RegulationMode.VOLTAGE, "BBS", Double.NaN, false);

        // G3: local reactive power regulation
        Generator g3 = network.getGenerator("SM_3");
        assertLocalTargets(g3, Double.NaN, Double.NaN);
        VoltageRegulation reg3 = g3.getVoltageRegulation();
        assertVoltageRegulation(reg3, RegulationMode.REACTIVE_POWER, "SM_3", Double.NaN, false);

        // G4: remote reactive power regulation
        Generator g4 = network.getGenerator("SM_4");
        assertLocalTargets(g4, Double.NaN, Double.NaN);
        VoltageRegulation reg4 = g4.getVoltageRegulation();
        assertVoltageRegulation(reg4, RegulationMode.REACTIVE_POWER, "PT", Double.NaN, false);
    }

    @Test
    void generatorVoltageRegulationEqAndSsh() {
        // Full import: regulation is created with correct targets and enabled.
        Network network = readCgmesResources(DIR, "generator_EQ.xml", "generator_SSH.xml");

        // G0: no regulation (not CGMES compliant)
        Generator g0 = network.getGenerator("SM_0");
        assertLocalTargets(g0, 10, Double.NaN);
        VoltageRegulation reg0 = g0.getVoltageRegulation();
        assertNull(reg0);

        // G1: local voltage regulation
        Generator g1 = network.getGenerator("SM_1");
        assertLocalTargets(g1, 10, 400);
        VoltageRegulation reg1 = g1.getVoltageRegulation();
        assertVoltageRegulation(reg1, RegulationMode.VOLTAGE, null, Double.NaN, true);

        // G2: remote voltage regulation
        Generator g2 = network.getGenerator("SM_2");
        assertLocalTargets(g2, 10, Double.NaN);
        VoltageRegulation reg2 = g2.getVoltageRegulation();
        assertVoltageRegulation(reg2, RegulationMode.VOLTAGE, "BBS", 400, true);

        // G3: local reactive power regulation
        Generator g3 = network.getGenerator("SM_3");
        assertLocalTargets(g3, 10, Double.NaN);
        VoltageRegulation reg3 = g3.getVoltageRegulation();
        assertVoltageRegulation(reg3, RegulationMode.REACTIVE_POWER, "SM_3", 10, true);

        // G4: remote reactive power regulation
        Generator g4 = network.getGenerator("SM_4");
        assertLocalTargets(g4, 10, Double.NaN);
        VoltageRegulation reg4 = g4.getVoltageRegulation();
        assertVoltageRegulation(reg4, RegulationMode.REACTIVE_POWER, "PT", 20, true);
    }

    private void assertLocalTargets(Generator gen, double localTargetQ, double localTargetV) {
        assertNotNull(gen);
        assertEquals(localTargetQ, gen.getLocalTargetQ());
        assertEquals(localTargetV, gen.getLocalTargetV());
    }

    private void assertVoltageRegulation(VoltageRegulation reg, RegulationMode mode, String terminalId, double targetValue, boolean isRegulating) {
        assertNotNull(reg);
        assertEquals(mode, reg.getMode());
        if (reg.getTerminal() == null) {
            assertNull(terminalId);
        } else {
            assertEquals(terminalId, reg.getTerminal().getConnectable().getId());
        }
        assertEquals(targetValue, reg.getTargetValue());
        assertEquals(isRegulating, reg.isRegulating());
    }

}
