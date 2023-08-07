/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
class ConnectGeneratorTest {
    private Network network;
    private Generator g2;
    private Generator g3;

    @BeforeEach
    void setUp() {
        network = FourSubstationsNodeBreakerFactory.create();
        Generator g1 = network.getGenerator("GH1");
        g2 = network.getGenerator("GH2");
        g3 = network.getGenerator("GH3");
        g1.setTargetV(11.);
        g2.setTargetV(22.);
        g3.setTargetV(33.);
        g1.getTerminal().disconnect();
        g2.getTerminal().disconnect();
        network.getVoltageLevel("S1VL2").getBusView().getBus("S1VL2_0").setV(99.);
    }

    @Test
    void testConnectVoltageRegulatorOff() {
        g2.setVoltageRegulatorOn(false);
        new ConnectGenerator(g2.getId()).apply(network);
        assertTrue(g2.getTerminal().isConnected());
        assertEquals(22., g2.getTargetV(), 0.01);
    }

    @Test
    void testConnectVoltageRegulatorOnWithAlreadyConnectedGenerators() {
        g2.setVoltageRegulatorOn(true);
        new ConnectGenerator(g2.getId()).apply(network);
        assertTrue(g2.getTerminal().isConnected());
        assertEquals(33., g2.getTargetV(), 0.01);
    }

    @Test
    void testConnectVoltageRegulatorOnWithoutAlreadyConnectedGenerators() {
        g3.getTerminal().disconnect();
        g2.setVoltageRegulatorOn(true);
        new ConnectGenerator(g2.getId()).apply(network);
        assertTrue(g2.getTerminal().isConnected());
        assertEquals(99., g2.getTargetV(), 0.01);
    }

    @Test
    void testConnectGeneratorCorrectSetPoint() {
        g2.setVoltageRegulatorOn(true);
        g2.setRegulatingTerminal(g3.getTerminal());
        new ConnectGenerator(g2.getId()).apply(network);
        assertTrue(g2.getTerminal().isConnected());
        assertEquals(33, g2.getTargetV(), 0.01);
    }

    @Test
    void testConnectGeneratorCorrectSetPointWithShunt() {
        double shuntTargetV = 123;
        network.getShuntCompensatorStream().forEach(sc -> {
            sc.setTargetV(shuntTargetV);
            sc.setTargetDeadband(1);
            sc.setVoltageRegulatorOn(true);
        });
        g2.setVoltageRegulatorOn(true);
        g2.setRegulatingTerminal(g3.getTerminal());
        new ConnectGenerator(g2.getId()).apply(network);
        assertTrue(g2.getTerminal().isConnected());
        assertEquals(shuntTargetV, g2.getTargetV(), 0.01);
    }

    @Test
    void testConnectGeneratorCorrectSetPointWithNoRegulatingElmt() {
        double busV = 123;
        g3.getTerminal().getBusView().getBus().setV(busV);
        g3.setVoltageRegulatorOn(false);
        g2.setVoltageRegulatorOn(true);
        g2.setRegulatingTerminal(g3.getTerminal());
        new ConnectGenerator(g2.getId()).apply(network);
        assertTrue(g2.getTerminal().isConnected());
        assertEquals(busV, g2.getTargetV(), 0.01);
    }
}
