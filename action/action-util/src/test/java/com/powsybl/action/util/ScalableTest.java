/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.iidm.network.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ScalableTest {

    private Network network;
    private Scalable g1;
    private Scalable g2;
    private Scalable g3;

    private static Network createNetwork() {
        Network network = NetworkFactory.create("network", "test");
        Substation s = network.newSubstation()
                .setId("s")
                .setCountry(Country.US)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("vl1")
                .setNominalV(380.0)
                .setLowVoltageLimit(0.8 * 380.0)
                .setHighVoltageLimit(1.2 * 380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl.getBusBreakerView().newBus()
                .setId("bus1")
                .add();
        vl.newGenerator()
                .setId("g1")
                .setBus("bus1")
                .setConnectableBus("bus1")
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(0.0)
                .setVoltageRegulatorOn(false)
                .setTargetQ(0.0)
                .add();
        vl.newGenerator()
                .setId("g2")
                .setBus("bus1")
                .setConnectableBus("bus1")
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(0.0)
                .setVoltageRegulatorOn(false)
                .setTargetQ(0.0)
                .add();
        vl.newGenerator()
                .setId("g3")
                .setConnectableBus("bus1")
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(0.0)
                .setVoltageRegulatorOn(true)
                .setTargetV(1.0)
                .add();

        return network;
    }

    @Before
    public void setUp() {
        network = createNetwork();
        g1 = Scalable.gen("g1");
        g2 = Scalable.gen("g2");
        g3 = Scalable.gen("g3");
        reset();
    }

    private void reset() {
        Scalable.stack(g1, g2).reset(network);
    }

    @Test
    public void testProportionalScalable() {
        double done = Scalable.proportional(Arrays.asList(70.f, 30.f), Arrays.asList(g1, g2)).scale(network, 100.0);
        assertEquals(100.0, done, 0.0);
        assertEquals(70.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(30.0, network.getGenerator("g2").getTargetP(), 1e-5);

        reset();

        done = Scalable.proportional(100.0f, g1).scale(network, 100.0);
        assertEquals(100.0, done, 0.0);
        assertEquals(100.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(0.0, network.getGenerator("g2").getTargetP(), 1e-5);

        reset();

        done = Scalable.proportional(75.0f, g1, 25.0f, g2).scale(network, 100.0);
        assertEquals(100.0, done, 0.0);
        assertEquals(75.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(25.0, network.getGenerator("g2").getTargetP(), 1e-5);

        reset();

        done = Scalable.proportional(50.0f, g1, 25.0f, g1, 25.0f, g2).scale(network, 100.0);
        assertEquals(100.0, done, 0.0);
        assertEquals(75.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(25.0, network.getGenerator("g2").getTargetP(), 1e-5);

        reset();

        done = Scalable.proportional(25.0f, g1, 25.0f, g1, 25.0f, g1, 25.0f, g1).scale(network, 100.0);
        assertEquals(100.0, done, 0.0);
        assertEquals(100.0, network.getGenerator("g1").getTargetP(), 0.0);

        reset();

        done = Scalable.proportional(20.0f, g1, 20.0f, g1, 20.0f, g1, 20.0f, g1, 20.0f, g1).scale(network, 100.0);
        assertEquals(100.0, done, 0.0);
        assertEquals(100.0, network.getGenerator("g1").getTargetP(), 0.0);

        testInvalidProportionalScalable(Collections.singletonList(100.0f), Collections.emptyList());
        testInvalidProportionalScalable(Collections.emptyList(), Collections.emptyList());
    }

    private void testInvalidProportionalScalable(List<Float> percentages, List<Scalable> scalables) {
        try {
            Scalable.proportional(percentages, scalables);
            fail();
        } catch (RuntimeException ignored) {
        }
    }

    @Test
    public void testStackScalable() {
        Scalable scalable = Scalable.stack(g1, g2);

        double done = scalable.scale(network, 150.0);
        assertEquals(150.0, done, 0.0);
        assertEquals(100.0, network.getGenerator("g1").getTargetP(), 0.0);
        assertEquals(50.0, network.getGenerator("g2").getTargetP(), 0.0);

        done = scalable.scale(network, 100.0);
        assertEquals(50.0, done, 0.0);
        assertEquals(100.0, network.getGenerator("g1").getTargetP(), 0.0);
        assertEquals(100.0, network.getGenerator("g2").getTargetP(), 0.0);
    }

    @Test
    public void testInitialValue() {
        assertEquals(0.0, g1.initialValue(network), 0.0);

        Scalable scalable = Scalable.stack(g1, g2);
        assertEquals(0.0, scalable.initialValue(network), 0.0);
    }

    @Test
    public void testMaximumValue() {
        assertEquals(100.0, g1.maximumValue(network), 0.0);

        Scalable scalable = Scalable.stack(g1, g2);
        assertEquals(200.0, scalable.maximumValue(network), 0.0);
    }

    @Test
    public void testListGenerators() {
        Generator generator1 = network.getGenerator("g1");
        Generator generator2 = network.getGenerator("g2");

        List<Generator> generators = g1.listGenerators(network);
        assertEquals(1, generators.size());
        assertSame(generator1, generators.get(0));

        generators = Scalable.stack(g1, g2).listGenerators(network);
        assertEquals(2, generators.size());
        assertSame(generator1, generators.get(0));
        assertSame(generator2, generators.get(1));

        Scalable unknownGenerator = Scalable.gen("unknown");
        List<String> notFoundGenerators = new ArrayList<>();
        generators = Scalable.stack(g1, g2, unknownGenerator).listGenerators(network, notFoundGenerators);
        assertEquals(2, generators.size());
        assertSame(generator1, generators.get(0));
        assertSame(generator2, generators.get(1));
        assertEquals(1, notFoundGenerators.size());
        assertEquals("unknown", notFoundGenerators.get(0));

        generators = new ArrayList<>();
        notFoundGenerators.clear();
        Scalable.stack(g1, g2, unknownGenerator).listGenerators(network, generators, notFoundGenerators);
        assertEquals(2, generators.size());
        assertSame(generator1, generators.get(0));
        assertSame(generator2, generators.get(1));
        assertEquals(1, notFoundGenerators.size());
        assertEquals("unknown", notFoundGenerators.get(0));
    }

    @Test
    public void testDisconnectedGenerator() {
        g3.scale(network, 100.0);

        assertTrue(network.getGenerator("g3").getTerminal().isConnected());
        assertEquals(100.0, network.getGenerator("g3").getTargetP(), 0.0);
        assertEquals(1.0, network.getGenerator("g3").getTargetV(), 0.0);
    }
}
