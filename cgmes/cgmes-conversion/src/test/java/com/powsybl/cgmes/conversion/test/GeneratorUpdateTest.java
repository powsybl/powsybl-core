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
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class GeneratorUpdateTest {

    private static final String DIR = "/update/generator/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "generator_EQ.xml");
        assertEquals(3, network.getGeneratorCount());

        assertEq(network);
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "generator_EQ.xml", "generator_SSH.xml");
        assertEquals(3, network.getGeneratorCount());

        assertFirstSsh(network);
    }

    @Test
    void importEqTwoSshsAndSvTest() {
        Network network = readCgmesResources(DIR, "generator_EQ.xml");
        assertEquals(3, network.getGeneratorCount());

        assertEq(network);

        readCgmesResources(network, DIR, "generator_SSH.xml");
        assertFirstSsh(network);

        readCgmesResources(network, DIR, "generator_SSH_1.xml");
        assertSecondSsh(network);

        assertFlowsBeforeSv(network);
        readCgmesResources(network, DIR, "generator_SV.xml");
        assertFlowsAfterSv(network);
    }

    private static void assertEq(Network network) {
        assertEq(network.getGenerator("SynchronousMachine"));
        assertEq(network.getGenerator("ExternalNetworkInjection"));
        assertEq(network.getGenerator("EquivalentInjection"));
    }

    private static void assertFirstSsh(Network network) {
        assertSsh(network.getGenerator("SynchronousMachine"), 160.0, 0.0, 405.0, true, 0.0, 0);
        assertSsh(network.getGenerator("ExternalNetworkInjection"), -0.0, -0.0, Double.NaN, false, 0.0, 0);
        assertSsh(network.getGenerator("EquivalentInjection"), -184.0, 0.0, Double.NaN, false, 0.0, 0);
    }

    private static void assertSecondSsh(Network network) {
        assertSsh(network.getGenerator("SynchronousMachine"), 165.0, -5.0, 410.0, true, 0.9, 1);
        assertSsh(network.getGenerator("ExternalNetworkInjection"), -10.0, -5.0, Double.NaN, false, 0.0, 0);
        assertSsh(network.getGenerator("EquivalentInjection"), -174.0, -5.0, Double.NaN, false, 0.0, 0);
    }

    private static void assertFlowsBeforeSv(Network network) {
        assertFlows(network.getGenerator("SynchronousMachine").getTerminal(), Double.NaN, Double.NaN);
        assertFlows(network.getGenerator("ExternalNetworkInjection").getTerminal(), Double.NaN, Double.NaN);
        assertFlows(network.getGenerator("EquivalentInjection").getTerminal(), Double.NaN, Double.NaN);
    }

    private static void assertFlowsAfterSv(Network network) {
        assertFlows(network.getGenerator("SynchronousMachine").getTerminal(), 100.0, -50.0);
        assertFlows(network.getGenerator("ExternalNetworkInjection").getTerminal(), 250.0, -30.0);
        assertFlows(network.getGenerator("EquivalentInjection").getTerminal(), 150.0, 50.0);
    }

    private static void assertEq(Generator generator) {
        assertNotNull(generator);
        assertTrue(Double.isNaN(generator.getTargetP()));
        assertTrue(Double.isNaN(generator.getTargetQ()));
        assertTrue(Double.isNaN(generator.getTargetV()));
        assertNotNull(generator.getRegulatingTerminal());
        assertFalse(generator.isVoltageRegulatorOn());

        String originalClass = generator.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS);
        if (originalClass.equals(CgmesNames.SYNCHRONOUS_MACHINE)) {
            assertNotNull(generator.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.MODE));
        }
    }

    private static void assertSsh(Generator generator, double targetP, double targetQ, double targetV, boolean isRegulatingOn, double normalPF, int referencePriority) {
        assertNotNull(generator);
        double tol = 0.0000001;
        assertEquals(targetP, generator.getTargetP(), tol);
        assertEquals(targetQ, generator.getTargetQ(), tol);
        assertEquals(targetV, generator.getTargetV(), tol);
        assertEquals(isRegulatingOn, generator.isVoltageRegulatorOn());

        ActivePowerControl<Generator> activePowerControl = generator.getExtension(ActivePowerControl.class);
        if (activePowerControl != null) {
            assertEquals(normalPF, activePowerControl.getParticipationFactor());
        }
        assertEquals(referencePriority, ReferencePriority.get(generator));
    }

    private static void assertFlows(Terminal terminal, double p, double q) {
        double tol = 0.0000001;
        assertEquals(p, terminal.getP(), tol);
        assertEquals(q, terminal.getQ(), tol);
    }
}
