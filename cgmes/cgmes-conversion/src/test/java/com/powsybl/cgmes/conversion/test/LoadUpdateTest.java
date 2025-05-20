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
        assertEquals(4, network.getLoadCount());

        Load loadEnergyConsumer = network.getLoad("EnergyConsumer");
        assertTrue(checkEq(loadEnergyConsumer));

        Load loadEnergySource = network.getLoad("EnergySource");
        assertTrue(checkEq(loadEnergySource));

        Load loadAsynchronousMachine = network.getLoad("AsynchronousMachine");
        assertTrue(checkEq(loadAsynchronousMachine));

        Load conformLoad = network.getLoad("ConformLoad");
        assertTrue(checkEq(conformLoad));
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "load_EQ.xml", "load_SSH.xml");
        assertEquals(4, network.getLoadCount());

        Load loadEnergyConsumer = network.getLoad("EnergyConsumer");
        assertTrue(checkSsh(loadEnergyConsumer, 10.0, 5.0));

        Load loadEnergySource = network.getLoad("EnergySource");
        assertTrue(checkSsh(loadEnergySource, -200.0, -90.0));

        Load loadAsynchronousMachine = network.getLoad("AsynchronousMachine");
        assertTrue(checkSsh(loadAsynchronousMachine, 200.0, 50.0));

        Load conformLoad = network.getLoad("ConformLoad");
        assertTrue(checkSsh(conformLoad, 486.0, 230.0));
    }

    @Test
    void importEqTwoSshsAndSvTest() {
        Network network = readCgmesResources(DIR, "load_EQ.xml");
        assertEquals(4, network.getLoadCount());

        Load loadEnergyConsumer = network.getLoad("EnergyConsumer");
        assertTrue(checkEq(loadEnergyConsumer));
        Load loadEnergySource = network.getLoad("EnergySource");
        assertTrue(checkEq(loadEnergySource));
        Load loadAsynchronousMachine = network.getLoad("AsynchronousMachine");
        assertTrue(checkEq(loadAsynchronousMachine));
        Load conformLoad = network.getLoad("ConformLoad");
        assertTrue(checkEq(conformLoad));

        readCgmesResources(network, DIR, "load_SSH.xml");

        assertTrue(checkSsh(loadEnergyConsumer, 10.0, 5.0));
        assertTrue(checkSsh(loadEnergySource, -200.0, -90.0));
        assertTrue(checkSsh(loadAsynchronousMachine, 200.0, 50.0));
        assertTrue(checkSsh(conformLoad, 486.0, 230.0));

        readCgmesResources(network, DIR, "load_SSH_1.xml");

        assertTrue(checkSsh(loadEnergyConsumer, 10.5, 5.5));
        assertTrue(checkSsh(loadEnergySource, -200.5, -90.5));
        assertTrue(checkSsh(loadAsynchronousMachine, 200.5, 50.5));
        assertTrue(checkSsh(conformLoad, 490.0, 235.0));

        readCgmesResources(network, DIR, "load_SV.xml");
        assertTrue(checkFlows(loadEnergyConsumer.getTerminal(), 100.0, 50.0));
        assertTrue(checkFlows(loadEnergySource.getTerminal(), 20.0, 10.0));
        assertTrue(checkFlows(loadAsynchronousMachine.getTerminal(), 10.0, 5.0));
        assertTrue(checkFlows(conformLoad.getTerminal(), -490.0, -235.0));
    }

    private static boolean checkEq(Load load) {
        assertNotNull(load);
        assertTrue(Double.isNaN(load.getP0()));
        assertTrue(Double.isNaN(load.getQ0()));
        String originalClass = load.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS);
        if (originalClass.equals(CgmesNames.ENERGY_CONSUMER)) {
            assertNotNull(load.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.P_FIXED));
            assertNotNull(load.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.Q_FIXED));
        }
        return true;
    }

    private static boolean checkSsh(Load load, double p0, double q0) {
        assertNotNull(load);
        assertEquals(p0, load.getP0());
        assertEquals(q0, load.getQ0());
        return true;
    }

    private static boolean checkFlows(Terminal terminal, double p, double q) {
        double tol = 0.0000001;
        assertEquals(p, terminal.getP(), tol);
        assertEquals(q, terminal.getQ(), tol);
        return true;
    }
}
