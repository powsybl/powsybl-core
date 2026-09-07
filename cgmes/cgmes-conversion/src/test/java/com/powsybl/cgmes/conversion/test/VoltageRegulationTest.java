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

import java.io.IOException;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
class VoltageRegulationTest extends AbstractSerDeTest {

    private static final String DIR = "/issues/voltageRegulation/";
    private static final String MODE_KIND_VOLTAGE = "http://iec.ch/TC57/CIM100#RegulatingControlModeKind.voltage";
    private static final String MODE_KIND_REACTIVE_POWER = "http://iec.ch/TC57/CIM100#RegulatingControlModeKind.reactivePower";

    @Test
    void generatorVoltageRegulationEqTest() {
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
        assertVoltageRegulation(reg2, RegulationMode.VOLTAGE, "BBS_1", Double.NaN, false);

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
    void generatorVoltageRegulationEqAndSshTest() {
        // Full import: regulation is created with correct targets and enabled.
        Network network = readCgmesResources(DIR, "generator_EQ.xml", "generator_SSH.xml");

        // G0: no regulation (not CGMES compliant)
        Generator g0 = network.getGenerator("SM_0");
        assertLocalTargets(g0, 10, Double.NaN);
        VoltageRegulation reg0 = g0.getVoltageRegulation();
        assertNull(reg0);

        // G1: local voltage regulation
        Generator g1 = network.getGenerator("SM_1");
        assertLocalTargets(g1, -10, 400);
        VoltageRegulation reg1 = g1.getVoltageRegulation();
        assertVoltageRegulation(reg1, RegulationMode.VOLTAGE, null, Double.NaN, true);

        // G2: remote voltage regulation
        Generator g2 = network.getGenerator("SM_2");
        assertLocalTargets(g2, -10, Double.NaN);
        VoltageRegulation reg2 = g2.getVoltageRegulation();
        assertVoltageRegulation(reg2, RegulationMode.VOLTAGE, "BBS_1", 400, true);

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

    @Test
    void generatorVoltageRegulationExportTest() throws IOException {
        Network network = readCgmesResources(DIR, "generator_EQ.xml", "generator_SSH.xml");

        String eqFile = writeCgmesProfile(network, "EQ", tmpDir);
        String sshFile = writeCgmesProfile(network, "SSH", tmpDir);

        assertSynchronousMachine(eqFile, sshFile, "SM_0", null, "-10", "false");

        assertSynchronousMachine(eqFile, sshFile, "SM_1", "RC_1", "10", "true");
        assertRegulatingControl(eqFile, sshFile, "RC_1", "T_SM_1", MODE_KIND_VOLTAGE,
            "false", "true", "400", "0");

        assertSynchronousMachine(eqFile, sshFile, "SM_2", "RC_2", "10", "true");
        assertRegulatingControl(eqFile, sshFile, "RC_2", "T_BBS_1", MODE_KIND_VOLTAGE,
            "false", "true", "400", "0");

        assertSynchronousMachine(eqFile, sshFile, "SM_3", "RC_3", "-10", "true");
        assertRegulatingControl(eqFile, sshFile, "RC_3", "T_SM_3", MODE_KIND_REACTIVE_POWER,
            "false", "true", "-10", "0");

        assertSynchronousMachine(eqFile, sshFile, "SM_4", "RC_4", "-10", "true");
        assertRegulatingControl(eqFile, sshFile, "RC_4", "T_PTE_1", MODE_KIND_REACTIVE_POWER,
            "false", "true", "-20", "0");
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

    private void assertSynchronousMachine(String eqFile, String sshFile, String connectableId, String regulatingControlId, String localTargetQ, String controlEnabled) {
        assertConnectable(eqFile, sshFile, "SynchronousMachine", connectableId,
            "RegulatingCondEq.RegulatingControl", regulatingControlId,
            "RotatingMachine.q", localTargetQ,
            "RegulatingCondEq.controlEnabled", controlEnabled);
    }

    private void assertConnectable(String eqFile, String sshFile, String connectableClass, String connectableId,
                                   String regulatingControlAttribute, String regulatingControlId,
                                   String initialStateValueAttribute, String initialStateValue,
                                   String controlEnabledAttribute, String controlEnabled) {
        // Check connectable EQ
        String connectableEq = getElement(eqFile, connectableClass, connectableId);
        assertNotNull(connectableEq);
        assertEquals(regulatingControlId, getResource(connectableEq, regulatingControlAttribute));

        // Check connectable SSH
        String connectableSsh = getElement(sshFile, connectableClass, connectableId);
        assertEquals(initialStateValue, getAttribute(connectableSsh, initialStateValueAttribute));
        assertEquals(controlEnabled, getAttribute(connectableSsh, controlEnabledAttribute));
    }

    private void assertRegulatingControl(String eqFile, String sshFile, String regulatingControlId,
                                         String terminal, String mode,
                                         String discrete, String enabled, String targetValue, String targetDeadband) {
        // Check EQ RegulatingControl
        String regulatingControlEq = getElement(eqFile, "RegulatingControl", regulatingControlId);
        assertEquals(terminal, getResource(regulatingControlEq, "RegulatingControl.Terminal"));
        assertEquals(mode, getResource(regulatingControlEq, "RegulatingControl.mode"));

        // Check SSH RegulatingControl
        String regulatingControlSsh = getElement(sshFile, "RegulatingControl", regulatingControlId);
        assertEquals(discrete, getAttribute(regulatingControlSsh, "RegulatingControl.discrete"));
        assertEquals(enabled, getAttribute(regulatingControlSsh, "RegulatingControl.enabled"));
        assertEquals(targetValue, getAttribute(regulatingControlSsh, "RegulatingControl.targetValue"));
        assertEquals(targetDeadband, getAttribute(regulatingControlSsh, "RegulatingControl.targetDeadband"));
    }

}
