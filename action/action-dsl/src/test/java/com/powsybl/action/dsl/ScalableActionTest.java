/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import groovy.lang.GroovyCodeSource;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ScalableActionTest {

    private Network network;
    private Generator g1;
    private Generator g2;
    private Generator g3;


    @Before
    public void setUp() {
        network = EurostagTutorialExample1Factory.create();
        addTwoMoreGensInNetwork();
        g1 = network.getGenerator("GEN");
        g2 = network.getGenerator("GEN2");
        g3 = network.getGenerator("GEN3");
    }

    @Test
    public void testGeneratorScalableStack() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/scalable.groovy"))).load(network);
        Action action = actionDb.getAction("actionScale"); // scale to 15000
        assertEquals(607.0, g1.getTargetP(), 0.0);
        assertEquals(9999.99, g1.getMaxP(), 0.0);
        action.run(network, null);
        assertEquals(9999.99, g1.getTargetP(), 0.0);
    }

    @Test
    public void testCompatible() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/scalable.groovy"))).load(network);
        Action action = actionDb.getAction("testCompatible"); // scale to 15000
        assertEquals(607.0, g1.getTargetP(), 0.0);
        assertEquals(9999.99, g1.getMaxP(), 0.0);
        action.run(network, null);
        assertEquals(9999.99, g1.getTargetP(), 0.0);
    }

    @Test
    public void testGeneratorScalableProportional() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/scalable.groovy"))).load(network);
        Action action = actionDb.getAction("testProportional"); // scale to 15000
        assertEquals(607.0, g1.getTargetP(), 0.0);
        assertEquals(9999.99, g1.getMaxP(), 0.0);
        action.run(network, null);
        assertEquals(7500.0, g1.getTargetP(), 0.0);
        assertEquals(3000.0, g2.getTargetP(), 0.0);
        assertEquals(4500.0, g3.getTargetP(), 0.0);
    }

    private void addTwoMoreGensInNetwork() {
        VoltageLevel vlgen = network.getVoltageLevel("VLGEN");
        Generator generator2 = vlgen.newGenerator()
                .setId("GEN2")
                .setBus("NGEN")
                .setConnectableBus("NGEN")
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setVoltageRegulatorOn(true)
                .setTargetV(24.5)
                .setTargetP(607.0)
                .setTargetQ(301.0)
            .add();
        Generator generator3 = vlgen.newGenerator()
                .setId("GEN3")
                .setBus("NGEN")
                .setConnectableBus("NGEN")
                .setMinP(-9999.99)
                .setMaxP(9999.99)
                .setVoltageRegulatorOn(true)
                .setTargetV(24.5)
                .setTargetP(607.0)
                .setTargetQ(301.0)
            .add();
    }
}
