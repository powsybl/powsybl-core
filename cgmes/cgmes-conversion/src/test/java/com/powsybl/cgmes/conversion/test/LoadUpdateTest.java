/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
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
        assertEquals(3, network.getLoadCount());

        Load loadEnergyConsumer = network.getLoad("EnergyConsumer");
        assertTrue(checkNaNLoad(loadEnergyConsumer));

        Load loadEnergySource = network.getLoad("EnergySource");
        assertTrue(checkNaNLoad(loadEnergySource));

        Load loadAsynchronousMachine = network.getLoad("AsynchronousMachine");
        assertTrue(checkNaNLoad(loadAsynchronousMachine));
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "load_EQ.xml", "load_SSH.xml");
        assertEquals(3, network.getLoadCount());

        Load loadEnergyConsumer = network.getLoad("EnergyConsumer");
        assertTrue(checkLoad(loadEnergyConsumer, 10.0, 5.0));

        Load loadEnergySource = network.getLoad("EnergySource");
        assertTrue(checkLoad(loadEnergySource, -200.0, -90.0));

        Load loadAsynchronousMachine = network.getLoad("AsynchronousMachine");
        assertTrue(checkLoad(loadAsynchronousMachine, 200.0, 50.0));
    }

    @Test
    void importEAndSshSeparatelyTest() {
        Network network = readCgmesResources(DIR, "load_EQ.xml");
        assertEquals(3, network.getLoadCount());

        Load loadEnergyConsumer = network.getLoad("EnergyConsumer");
        assertTrue(checkNaNLoad(loadEnergyConsumer));
        Load loadEnergySource = network.getLoad("EnergySource");
        assertTrue(checkNaNLoad(loadEnergySource));
        Load loadAsynchronousMachine = network.getLoad("AsynchronousMachine");
        assertTrue(checkNaNLoad(loadAsynchronousMachine));

        readCgmesResources(network, DIR, "load_SSH.xml");

        assertTrue(checkLoad(loadEnergyConsumer, 10.0, 5.0));
        assertTrue(checkLoad(loadEnergySource, -200.0, -90.0));
        assertTrue(checkLoad(loadAsynchronousMachine, 200.0, 50.0));
    }

    @Test
    void importEAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "load_EQ.xml");
        assertEquals(3, network.getLoadCount());

        Load loadEnergyConsumer = network.getLoad("EnergyConsumer");
        assertTrue(checkNaNLoad(loadEnergyConsumer));
        Load loadEnergySource = network.getLoad("EnergySource");
        assertTrue(checkNaNLoad(loadEnergySource));
        Load loadAsynchronousMachine = network.getLoad("AsynchronousMachine");
        assertTrue(checkNaNLoad(loadAsynchronousMachine));

        readCgmesResources(network, DIR, "load_SSH.xml");

        assertTrue(checkLoad(loadEnergyConsumer, 10.0, 5.0));
        assertTrue(checkLoad(loadEnergySource, -200.0, -90.0));
        assertTrue(checkLoad(loadAsynchronousMachine, 200.0, 50.0));

        readCgmesResources(network, DIR, "load_SSH_1.xml");

        assertTrue(checkLoad(loadEnergyConsumer, 10.5, 5.5));
        assertTrue(checkLoad(loadEnergySource, -200.5, -90.5));
        assertTrue(checkLoad(loadAsynchronousMachine, 200.5, 50.5));
    }

    private static boolean checkNaNLoad(Load load) {
        assertNotNull(load);
        assertTrue(Double.isNaN(load.getP0()));
        assertTrue(Double.isNaN(load.getQ0()));
        return true;
    }

    private static boolean checkLoad(Load load, double p0, double q0) {
        assertNotNull(load);
        assertEquals(p0, load.getP0());
        assertEquals(q0, load.getQ0());
        return true;
    }
}
