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

        Generator synchronousMachine = network.getGenerator("SynchronousMachine");
        assertTrue(checkEq(synchronousMachine));

        Generator externalNetworkInjection = network.getGenerator("ExternalNetworkInjection");
        assertTrue(checkEq(externalNetworkInjection));

        Generator equivalentInjection = network.getGenerator("EquivalentInjection");
        assertTrue(checkEq(equivalentInjection));
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "generator_EQ.xml", "generator_SSH.xml");
        assertEquals(3, network.getGeneratorCount());

        Generator synchronousMachine = network.getGenerator("SynchronousMachine");
        assertTrue(checkSsh(synchronousMachine, 160.0, 0.0, 131.34, true, 0.0, 0));

        Generator externalNetworkInjection = network.getGenerator("ExternalNetworkInjection");
        assertTrue(checkSsh(externalNetworkInjection, -0.0, -0.0, Double.NaN, false, 0.0, 0));

        Generator equivalentInjection = network.getGenerator("EquivalentInjection");
        assertTrue(checkSsh(equivalentInjection, -184.0, 0.0, Double.NaN, false, 0.0, 0));
    }

    @Test
    void importEqAndSshSeparatelyTest() {
        Network network = readCgmesResources(DIR, "generator_EQ.xml");
        assertEquals(3, network.getGeneratorCount());

        Generator synchronousMachine = network.getGenerator("SynchronousMachine");
        assertTrue(checkEq(synchronousMachine));
        Generator externalNetworkInjection = network.getGenerator("ExternalNetworkInjection");
        assertTrue(checkEq(externalNetworkInjection));
        Generator equivalentInjection = network.getGenerator("EquivalentInjection");
        assertTrue(checkEq(equivalentInjection));

        readCgmesResources(network, DIR, "generator_SSH.xml");

        assertTrue(checkSsh(synchronousMachine, 160.0, 0.0, 131.34, true, 0.0, 0));
        assertTrue(checkSsh(externalNetworkInjection, -0.0, -0.0, Double.NaN, false, 0.0, 0));
        assertTrue(checkSsh(equivalentInjection, -184.0, 0.0, Double.NaN, false, 0.0, 0));
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "generator_EQ.xml");
        assertEquals(3, network.getGeneratorCount());

        Generator synchronousMachine = network.getGenerator("SynchronousMachine");
        assertTrue(checkEq(synchronousMachine));
        Generator externalNetworkInjection = network.getGenerator("ExternalNetworkInjection");
        assertTrue(checkEq(externalNetworkInjection));
        Generator equivalentInjection = network.getGenerator("EquivalentInjection");
        assertTrue(checkEq(equivalentInjection));

        readCgmesResources(network, DIR, "generator_SSH.xml");

        assertTrue(checkSsh(synchronousMachine, 160.0, 0.0, 131.34, true, 0.0, 0));
        assertTrue(checkSsh(externalNetworkInjection, -0.0, -0.0, Double.NaN, false, 0.0, 0));
        assertTrue(checkSsh(equivalentInjection, -184.0, 0.0, Double.NaN, false, 0.0, 0));

        readCgmesResources(network, DIR, "generator_SSH_1.xml");

        assertTrue(checkSsh(synchronousMachine, 165.0, -5.0, 130.34, true, 0.9, 1));
        assertTrue(checkSsh(externalNetworkInjection, -10.0, -5.0, Double.NaN, false, 0.0, 0));
        assertTrue(checkSsh(equivalentInjection, -174.0, -5.0, Double.NaN, false, 0.0, 0));
    }

    private static boolean checkEq(Generator generator) {
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
        return true;
    }

    private static boolean checkSsh(Generator generator, double targetP, double targetQ, double targetV, boolean isRegulatingOn, double normalPF, int referencePriority) {
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
        return true;
    }
}
