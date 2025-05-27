/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class LoadUpdateTest {

    private static final String DIR = "/update/load/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "load_EQ.xml");
        assertEquals(7, network.getLoadCount());

        Load loadEnergyConsumer = network.getLoad("EnergyConsumer");
        assertEq(loadEnergyConsumer);

        Load loadEnergySource = network.getLoad("EnergySource");
        assertEq(loadEnergySource);

        Load loadAsynchronousMachine = network.getLoad("AsynchronousMachine");
        assertEq(loadAsynchronousMachine);

        Load conformLoad = network.getLoad("ConformLoad");
        assertEq(conformLoad);

        Load loadEnergyConsumerOnlyEQ = network.getLoad("EnergyConsumerOnlyEQ");
        assertEq(loadEnergyConsumerOnlyEQ);

        Load loadEnergySourceOnlyEQ = network.getLoad("EnergySourceOnlyEQ");
        assertEq(loadEnergySourceOnlyEQ);

        Load loadAsynchronousMachineOnlyEQ = network.getLoad("AsynchronousMachineOnlyEQ");
        assertEq(loadAsynchronousMachineOnlyEQ);
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "load_EQ.xml", "load_SSH.xml");
        assertEquals(7, network.getLoadCount());

        Load loadEnergyConsumer = network.getLoad("EnergyConsumer");
        assertSsh(loadEnergyConsumer, 10.0, 5.0);

        Load loadEnergySource = network.getLoad("EnergySource");
        assertSsh(loadEnergySource, -200.0, -90.0);

        Load loadAsynchronousMachine = network.getLoad("AsynchronousMachine");
        assertSsh(loadAsynchronousMachine, 200.0, 50.0);

        Load conformLoad = network.getLoad("ConformLoad");
        assertSsh(conformLoad, 486.0, 230.0);

        Load loadEnergyConsumerOnlyEQ = network.getLoad("EnergyConsumerOnlyEQ");
        assertSsh(loadEnergyConsumerOnlyEQ, 0.0, 0.0);

        Load loadEnergySourceOnlyEQ = network.getLoad("EnergySourceOnlyEQ");
        assertSsh(loadEnergySourceOnlyEQ, 0.0, 0.0);

        Load loadAsynchronousMachineOnlyEQ = network.getLoad("AsynchronousMachineOnlyEQ");
        assertSsh(loadAsynchronousMachineOnlyEQ, 0.0, 0.0);
    }

    @Test
    void importEqTwoSshsAndSvTest() {
        Network network = readCgmesResources(DIR, "load_EQ.xml");
        assertEquals(7, network.getLoadCount());

        Load loadEnergyConsumer = network.getLoad("EnergyConsumer");
        assertEq(loadEnergyConsumer);
        Load loadEnergySource = network.getLoad("EnergySource");
        assertEq(loadEnergySource);
        Load loadAsynchronousMachine = network.getLoad("AsynchronousMachine");
        assertEq(loadAsynchronousMachine);
        Load conformLoad = network.getLoad("ConformLoad");
        assertEq(conformLoad);
        Load loadEnergyConsumerOnlyEQ = network.getLoad("EnergyConsumerOnlyEQ");
        assertEq(loadEnergyConsumerOnlyEQ);
        Load loadEnergySourceOnlyEQ = network.getLoad("EnergySourceOnlyEQ");
        assertEq(loadEnergySourceOnlyEQ);
        Load loadAsynchronousMachineOnlyEQ = network.getLoad("AsynchronousMachineOnlyEQ");
        assertEq(loadAsynchronousMachineOnlyEQ);

        readCgmesResources(network, DIR, "load_SSH.xml");

        assertSsh(loadEnergyConsumer, 10.0, 5.0);
        assertSsh(loadEnergySource, -200.0, -90.0);
        assertSsh(loadAsynchronousMachine, 200.0, 50.0);
        assertSsh(conformLoad, 486.0, 230.0);
        assertSsh(loadEnergyConsumerOnlyEQ, 0.0, 0.0);
        assertSsh(loadEnergySourceOnlyEQ, 0.0, 0.0);
        assertSsh(loadAsynchronousMachineOnlyEQ, 0.0, 0.0);

        readCgmesResources(network, DIR, "load_SSH_1.xml");

        assertSsh(loadEnergyConsumer, 10.5, 5.5);
        assertSsh(loadEnergySource, -200.5, -90.5);
        assertSsh(loadAsynchronousMachine, 200.5, 50.5);
        assertSsh(conformLoad, 490.0, 235.0);
        assertSsh(loadEnergyConsumerOnlyEQ, 0.0, 0.0);
        assertSsh(loadEnergySourceOnlyEQ, 0.0, 0.0);
        assertSsh(loadAsynchronousMachineOnlyEQ, 0.0, 0.0);

        assertFlows(loadEnergyConsumer.getTerminal(), Double.NaN, Double.NaN);
        assertFlows(loadEnergySource.getTerminal(), Double.NaN, Double.NaN);
        assertFlows(loadAsynchronousMachine.getTerminal(), Double.NaN, Double.NaN);
        assertFlows(conformLoad.getTerminal(), Double.NaN, Double.NaN);
        assertSsh(loadEnergyConsumerOnlyEQ, 0.0, 0.0);
        assertSsh(loadEnergySourceOnlyEQ, 0.0, 0.0);
        assertSsh(loadAsynchronousMachineOnlyEQ, 0.0, 0.0);

        readCgmesResources(network, DIR, "load_SV.xml");

        assertFlows(loadEnergyConsumer.getTerminal(), 100.0, 50.0);
        assertFlows(loadEnergySource.getTerminal(), 20.0, 10.0);
        assertFlows(loadAsynchronousMachine.getTerminal(), 10.0, 5.0);
        assertFlows(conformLoad.getTerminal(), -490.0, -235.0);
        assertSsh(loadEnergyConsumerOnlyEQ, 0.0, 0.0);
        assertSsh(loadEnergySourceOnlyEQ, 0.0, 0.0);
        assertSsh(loadAsynchronousMachineOnlyEQ, 0.0, 0.0);
    }

    private static void assertEq(Load load) {
        assertNotNull(load);
        assertTrue(Double.isNaN(load.getP0()));
        assertTrue(Double.isNaN(load.getQ0()));
        String originalClass = load.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS);
        if (originalClass.equals(CgmesNames.ENERGY_CONSUMER)) {
            assertNotNull(load.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.P_FIXED));
            assertNotNull(load.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.Q_FIXED));
        }
    }

    private static void assertSsh(Load load, double p0, double q0) {
        assertNotNull(load);
        assertEquals(p0, load.getP0());
        assertEquals(q0, load.getQ0());
    }

    private static void assertFlows(Terminal terminal, double p, double q) {
        double tol = 0.0000001;
        assertEquals(p, terminal.getP(), tol);
        assertEquals(q, terminal.getQ(), tol);
    }
}
