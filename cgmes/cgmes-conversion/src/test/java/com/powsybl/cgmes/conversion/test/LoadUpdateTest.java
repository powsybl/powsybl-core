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

        assertEq(network);
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "load_EQ.xml", "load_SSH.xml");
        assertEquals(7, network.getLoadCount());

        assertFirstSsh(network);
    }

    @Test
    void importEqTwoSshsAndSvTest() {
        Network network = readCgmesResources(DIR, "load_EQ.xml");
        assertEquals(7, network.getLoadCount());
        assertEq(network);

        readCgmesResources(network, DIR, "load_SSH.xml");
        assertFirstSsh(network);

        readCgmesResources(network, DIR, "load_SSH_1.xml");
        assertSecondSsh(network);
        assertFlowsBeforeSv(network);

        readCgmesResources(network, DIR, "load_SV.xml");
        assertFlowsAfterSv(network);
    }

    private static void assertEq(Network network) {
        assertEq(network.getLoad("EnergyConsumer"));
        assertEq(network.getLoad("EnergySource"));
        assertEq(network.getLoad("AsynchronousMachine"));
        assertEq(network.getLoad("ConformLoad"));
        assertEq(network.getLoad("EnergyConsumerOnlyEQ"));
        assertEq(network.getLoad("EnergySourceOnlyEQ"));
        assertEq(network.getLoad("AsynchronousMachineOnlyEQ"));
    }

    void assertFirstSsh(Network network) {
        assertSsh(network.getLoad("EnergyConsumer"), 10.0, 5.0);
        assertSsh(network.getLoad("EnergySource"), -200.0, -90.0);
        assertSsh(network.getLoad("AsynchronousMachine"), 200.0, 50.0);
        assertSsh(network.getLoad("ConformLoad"), 486.0, 230.0);
        assertSsh(network.getLoad("EnergyConsumerOnlyEQ"), 0.0, 0.0);
        assertSsh(network.getLoad("EnergySourceOnlyEQ"), 0.0, 0.0);
        assertSsh(network.getLoad("AsynchronousMachineOnlyEQ"), 0.0, 0.0);
    }

    void assertSecondSsh(Network network) {
        assertSsh(network.getLoad("EnergyConsumer"), 10.5, 5.5);
        assertSsh(network.getLoad("EnergySource"), -200.5, -90.5);
        assertSsh(network.getLoad("AsynchronousMachine"), 200.5, 50.5);
        assertSsh(network.getLoad("ConformLoad"), 490.0, 235.0);
        assertSsh(network.getLoad("EnergyConsumerOnlyEQ"), 0.0, 0.0);
        assertSsh(network.getLoad("EnergySourceOnlyEQ"), 0.0, 0.0);
        assertSsh(network.getLoad("AsynchronousMachineOnlyEQ"), 0.0, 0.0);
    }

    void assertFlowsBeforeSv(Network network) {
        assertFlows(network.getLoad("EnergyConsumer").getTerminal(), Double.NaN, Double.NaN);
        assertFlows(network.getLoad("EnergySource").getTerminal(), Double.NaN, Double.NaN);
        assertFlows(network.getLoad("AsynchronousMachine").getTerminal(), Double.NaN, Double.NaN);
        assertFlows(network.getLoad("ConformLoad").getTerminal(), Double.NaN, Double.NaN);
        assertFlows(network.getLoad("EnergyConsumerOnlyEQ").getTerminal(), Double.NaN, Double.NaN);
        assertFlows(network.getLoad("EnergySourceOnlyEQ").getTerminal(), Double.NaN, Double.NaN);
        assertFlows(network.getLoad("AsynchronousMachineOnlyEQ").getTerminal(), Double.NaN, Double.NaN);
    }

    void assertFlowsAfterSv(Network network) {
        assertFlows(network.getLoad("EnergyConsumer").getTerminal(), 100.0, 50.0);
        assertFlows(network.getLoad("EnergySource").getTerminal(), 20.0, 10.0);
        assertFlows(network.getLoad("AsynchronousMachine").getTerminal(), 10.0, 5.0);
        assertFlows(network.getLoad("ConformLoad").getTerminal(), -490.0, -235.0);
        assertFlows(network.getLoad("EnergyConsumerOnlyEQ").getTerminal(), Double.NaN, Double.NaN);
        assertFlows(network.getLoad("EnergySourceOnlyEQ").getTerminal(), Double.NaN, Double.NaN);
        assertFlows(network.getLoad("AsynchronousMachineOnlyEQ").getTerminal(), Double.NaN, Double.NaN);
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
