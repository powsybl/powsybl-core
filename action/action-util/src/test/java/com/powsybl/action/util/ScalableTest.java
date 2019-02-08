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

import static com.powsybl.action.util.ScalableTestNetwork.createNetwork;
import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ScalableTest {

    private Network network;
    private Scalable g1;
    private Scalable g2;
    private Scalable g3;

    private Scalable l1;
    private Scalable s;
    private Scalable unknownGenerator;
    private Scalable unknownLoad;

    @Before
    public void setUp() {

        network = createNetwork();
        g1 = Scalable.gen("g1");
        g2 = Scalable.gen("g2");
        g3 = Scalable.gen("g3");
        l1 = Scalable.load("l1");
        s = Scalable.gen("s");
        unknownGenerator = Scalable.gen("unknown");
        unknownLoad = Scalable.load("unknown");

        reset();
    }

    private void reset() {
        Scalable.stack(g1, g2).reset(network);

        Scalable.stack(l1, s, unknownGenerator).reset(network);
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

        reset();
        done = Scalable.proportional(Arrays.asList(70.f, 30.f), Arrays.asList(g1, l1)).scale(network, 100.0);
        assertEquals(100.0, done, 0.0);
        assertEquals(70.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(-30.0, network.getLoad("l1").getP0(), 1e-5);

        reset();
        done = s.scale(network, 10);
        assertEquals(0, done, 0.0);
        done = Scalable.proportional(Arrays.asList(70.f, 30.f), Arrays.asList(g1, s)).scale(network, 100.0);
        assertEquals(70.0, done, 0.0);
        assertEquals(70.0, network.getGenerator("g1").getTargetP(), 1e-5);

        reset();
        done = unknownGenerator.scale(network, 10);
        assertEquals(0, done, 0.0);
        done = Scalable.proportional(Arrays.asList(70.f, 30.f), Arrays.asList(g1, s)).scale(network, 100.0);
        assertEquals(70.0, done, 0.0);
        assertEquals(70.0, network.getGenerator("g1").getTargetP(), 1e-5);


    }

    private void testInvalidProportionalScalable(List<Float> percentages, List<Scalable> scalables) {
        try {
            Scalable.proportional(percentages, scalables);
            fail();
        } catch (RuntimeException ignored) {
            // Ignored
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

        reset();
        scalable = Scalable.stack(g1, l1);
        done = scalable.scale(network, 150.0);
        assertEquals(150.0, done, 0.0);
        assertEquals(100.0, network.getGenerator("g1").getTargetP(), 0.0);
        assertEquals(-50.0, network.getLoad("l1").getP0(), 0.0);

        scalable = Scalable.stack(s, unknownGenerator, unknownLoad);
        done = scalable.scale(network, 150.0);
        assertEquals(0.0, done, 0.0);
    }

    @Test
    public void testInitialValue() {
        assertEquals(0.0, g1.initialValue(network), 0.0);

        Scalable scalable = Scalable.stack(g1, g2);
        assertEquals(0.0, scalable.initialValue(network), 0.0);

        scalable = Scalable.stack(g1, l1);
        assertEquals(0.0, scalable.initialValue(network), 0.0);

        assertEquals(0.0, s.initialValue(network), 0.0);
    }

    @Test
    public void testMaximumValue() {
        assertEquals(100.0, g1.maximumValue(network), 0.0);

        Scalable scalable = Scalable.stack(g1, g2);
        assertEquals(200.0, scalable.maximumValue(network), 0.0);

        assertEquals(Double.POSITIVE_INFINITY, l1.maximumValue(network), 0.0);

        scalable = Scalable.stack(g1, l1);
        assertEquals(Double.POSITIVE_INFINITY, scalable.maximumValue(network), 0.0);

        assertEquals(0.0, s.maximumValue(network), 0.0);
        assertEquals(0.0, unknownGenerator.maximumValue(network), 0.0);
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

        generators = new ArrayList<>();
        notFoundGenerators.clear();
        Scalable.stack(g1, g2, l1, s).listGenerators(network, generators, notFoundGenerators);
        Identifiable identifiabletest = network.getIdentifiable("s");
        assertEquals(2, generators.size());
        assertSame(generator1, generators.get(0));
        assertSame(generator2, generators.get(1));
        assertEquals(2, notFoundGenerators.size());

    }

    @Test
    public void testDisconnectedGenerator() {
        g3.scale(network, 100.0);

        assertTrue(network.getGenerator("g3").getTerminal().isConnected());
        assertEquals(100.0, network.getGenerator("g3").getTargetP(), 0.0);
        assertEquals(1.0, network.getGenerator("g3").getTargetV(), 0.0);
    }


    @Test
    public void testFilterInjections() {
        Generator generator1 = network.getGenerator("g1");
        Generator generator2 = network.getGenerator("g2");
        Load load1 = network.getLoad("l1");


        List<Injection> generatorList = g1.filterInjections(network);
        List<Injection> generators = g1.filterInjections(network);
        assertEquals(1, generators.size());
        assertSame(generator1, generators.get(0));

        generators = Scalable.stack(g1, g2).filterInjections(network);
        assertEquals(2, generators.size());
        assertSame(generator1, generators.get(0));
        assertSame(generator2, generators.get(1));

        Scalable unknownGenerator = Scalable.gen("unknown");
        List<String> notFoundGenerators = new ArrayList<>();
        generators = Scalable.stack(g1, g2, unknownGenerator).filterInjections(network, notFoundGenerators);
        assertEquals(2, generators.size());
        assertSame(generator1, generators.get(0));
        assertSame(generator2, generators.get(1));
        assertEquals(1, notFoundGenerators.size());
        assertEquals("unknown", notFoundGenerators.get(0));


        generators = new ArrayList<>();
        notFoundGenerators.clear();
        Scalable.stack(g1, g2, unknownGenerator).filterInjections(network, generators, notFoundGenerators);
        assertEquals(2, generators.size());

        assertSame(generator1, generators.get(0));
        assertSame(generator2, generators.get(1));
        assertEquals(1, notFoundGenerators.size());
        assertEquals("unknown", notFoundGenerators.get(0));


        List<Injection> injections = new ArrayList<>();
        List<String> notFoundInjections = new ArrayList<>();
        Scalable.stack(g1, g2, l1).filterInjections(network, injections, notFoundInjections);
        assertEquals(3, injections.size());

        assertSame(generator1, injections.get(0));
        assertSame(generator2, injections.get(1));
        assertEquals(0, notFoundInjections.size());

        injections = new ArrayList<>();
        notFoundInjections.clear();
        Scalable.stack(g1, g2, l1, s).filterInjections(network, injections, notFoundInjections);
        assertEquals(3, injections.size());

        assertSame(generator1, injections.get(0));
        assertSame(generator2, injections.get(1));
        assertSame(load1, injections.get(2));

        assertEquals(1, notFoundInjections.size());
        assertEquals("s", notFoundInjections.get(0));

        injections = l1.filterInjections(network);
        assertEquals(1, injections.size());
        assertSame(load1, injections.get(0));

        injections = Scalable.stack(g1, l1).filterInjections(network);
        assertEquals(2, injections.size());
        assertSame(generator1, injections.get(0));
        assertSame(load1, injections.get(1));

        notFoundInjections.clear();
        injections = Scalable.stack(g1, l1, unknownGenerator).filterInjections(network, notFoundInjections);
        assertEquals(2, injections.size());
        assertSame(generator1, injections.get(0));
        assertSame(load1, injections.get(1));
        assertEquals(1, notFoundInjections.size());
        assertEquals("unknown", notFoundInjections.get(0));

        notFoundInjections.clear();
        injections = Scalable.stack(g1, l1, unknownGenerator, s).filterInjections(network, notFoundInjections);
        assertEquals(2, injections.size());
        assertSame(generator1, injections.get(0));
        assertSame(load1, injections.get(1));
        assertEquals(2, notFoundInjections.size());
        assertEquals("unknown", notFoundInjections.get(0));
        assertSame("s", notFoundInjections.get(1));
    }

}
