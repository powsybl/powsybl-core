/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import org.junit.Before;
import org.junit.Test;

import static com.powsybl.action.util.ScalableTestNetwork.createNetwork;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public class GeneratorUtilTest {
    private Network network;
    private Bus bus1;
    private Generator g1;
    private Generator g2;
    private Generator g3;

    @Before
    public void setUp() {
        network = createNetwork();
        g1 = network.getGenerator("g1");
        g2 = network.getGenerator("g2");
        g3 = network.getGenerator("g3");
        bus1 = network.getVoltageLevel("vl1").getBusBreakerView().getBus("bus1");
        g1.setTargetV(11.);
        g2.setTargetV(22.);
        g3.setTargetV(33.);
        g1.getTerminal().disconnect();
        g2.getTerminal().disconnect();
        bus1.setV(99.);
    }

    @Test
    public void testConnectVoltageRegulatorOff() {
        g2.setVoltageRegulatorOn(false);
        GeneratorUtil.connectGenerator(g2);
        assertTrue(g2.getTerminal().isConnected());
        assertEquals(22., g2.getTargetV(), 0.01);
    }

    @Test
    public void testConnectVoltageRegulatorOnWithAlreadyConnectedGenerators() {
        g2.setRegulatingTerminal(g2.getTerminal());
        g2.setVoltageRegulatorOn(true);
        GeneratorUtil.connectGenerator(g2);
        assertTrue(g2.getTerminal().isConnected());
        assertEquals(33., g2.getTargetV(), 0.01);
    }

    @Test
    public void testConnectVoltageRegulatorOnWithoutAlreadyConnectedGenerators() {
        g3.getTerminal().disconnect();
        g2.setRegulatingTerminal(g2.getTerminal());
        g2.setVoltageRegulatorOn(true);
        GeneratorUtil.connectGenerator(g2);
        assertTrue(g2.getTerminal().isConnected());
        assertEquals(99., g2.getTargetV(), 0.01);
    }
}
