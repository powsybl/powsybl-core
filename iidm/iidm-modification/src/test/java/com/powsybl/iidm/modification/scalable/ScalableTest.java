/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.modification.scalable.Scalable.ScalingConvention;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.powsybl.iidm.modification.scalable.Scalable.ScalingConvention.*;
import static com.powsybl.iidm.modification.scalable.ScalableTestNetwork.createNetwork;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.RESPECT_OF_VOLUME_ASKED;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class ScalableTest {

    private Network network;
    private Scalable g1;
    private Scalable g2;
    private Scalable g3;

    private Scalable l1;
    private Scalable l2;
    private Scalable l3;
    private Scalable s;
    private Scalable unknownGenerator;
    private Scalable unknownLoad;
    private Scalable unknownDanglingLine;
    private Scalable dl1;

    private ScalingConvention convention;

    @BeforeEach
    void setUp() {

        network = createNetwork();
        g1 = Scalable.onGenerator("g1");
        g2 = Scalable.onGenerator("g2");
        g3 = Scalable.onGenerator("g3", -10, 80);
        s = Scalable.onGenerator("s");
        unknownGenerator = Scalable.onGenerator("unknown");

        l1 = Scalable.onLoad("l1");
        l2 = Scalable.onLoad("l1", 20, 80);
        l3 = Scalable.onLoad("l1", -50, 100);
        unknownLoad = Scalable.onLoad("unknown");
        unknownDanglingLine = Scalable.onDanglingLine("unknown");
        dl1 = Scalable.onDanglingLine("dl1", 20, 80);

        reset();
    }

    private void reset() {

        Scalable.stack(g1, g2, g3).reset(network);
        Scalable.stack(l1, l2, s, unknownGenerator, unknownLoad, unknownDanglingLine, dl1).reset(network);
        l3.reset(network);
    }

    @Test
    void testInitialValue() {
        assertEquals(0.0, g1.initialValue(network), 0.0);

        Scalable scalable = Scalable.stack(g1, g2, g3);
        assertEquals(0.0, scalable.initialValue(network), 0.0);

        scalable = Scalable.stack(g1, l1);
        assertEquals(0.0, scalable.initialValue(network), 0.0);

        assertEquals(0.0, s.initialValue(network), 0.0);

        assertEquals(0., Scalable.proportional(Arrays.asList(70.0, 30.0), Arrays.asList(g1, l1)).initialValue(network), 1e-3);

        testInvalidProportionalScalable(Collections.singletonList(100.0), Collections.emptyList());
        testInvalidProportionalScalable(Arrays.asList(70.0, 20.0), Arrays.asList(g1, l1));
    }

    private void testInvalidProportionalScalable(List<Double> percentages, List<Scalable> scalables) {
        try {
            Scalable.proportional(percentages, scalables);
            fail();
        } catch (RuntimeException ignored) {
            // Ignored
        }
    }

    @Test
    void testMaximumValue() {
        //By default, ScalingConvention.GENERATOR
        assertEquals(100.0, g1.maximumValue(network), 0.0);
        assertEquals(80.0, g3.maximumValue(network), 0.0);
        assertEquals(0, l1.maximumValue(network), 0.0);
        assertEquals(-20, l2.maximumValue(network), 0.0);
        assertEquals(50, l3.maximumValue(network), 0.0);
        assertEquals(0.0, s.maximumValue(network), 0.0);
        assertEquals(0.0, unknownGenerator.maximumValue(network), 0.0);

        //test StackScalable
        assertEquals(280.0, Scalable.stack(g1, g2, g3).maximumValue(network), 0.0);
        assertEquals(80, Scalable.stack(g1, l1, l2).maximumValue(network), 0.0);

        //test ProportionalScalable
        assertEquals(280, Scalable.proportional(Arrays.asList(70.0, 20.0, 10.0), Arrays.asList(g1, g2, g3)).maximumValue(network), 1e-3);
        assertEquals(80, Scalable.proportional(Arrays.asList(70.0, 20.0, 10.0), Arrays.asList(g1, l1, l2)).maximumValue(network), 1e-3);
    }

    @Test
    void testMaximumValueLoadConvention() {
        convention = LOAD;
        assertEquals(0, g1.maximumValue(network, convention), 0.0);
        assertEquals(0, g3.maximumValue(network, convention), 0.0);
        assertEquals(Double.MAX_VALUE, l1.maximumValue(network, convention), 0.0);
        assertEquals(80, l2.maximumValue(network, convention), 0.0);
        assertEquals(100, l3.maximumValue(network, convention), 0.0);
        assertEquals(0.0, s.maximumValue(network, convention), 0.0);
        assertEquals(0.0, unknownGenerator.maximumValue(network, convention), 0.0);

        //test StackScalable
        assertEquals(0, Scalable.stack(g1, g2, g3).maximumValue(network, convention), 0.0);
        assertEquals(Double.MAX_VALUE, Scalable.stack(g1, l1, l2).maximumValue(network, convention), 0.0);

        //test ProportionalScalable
        assertEquals(0, Scalable.proportional(Arrays.asList(70.0, 20.0, 10.0), Arrays.asList(g1, g2, g3)).maximumValue(network, convention), 1e-3);
        assertEquals(Double.MAX_VALUE, Scalable.proportional(Arrays.asList(70.0, 20.0, 10.0), Arrays.asList(g1, l1, l2)).maximumValue(network, convention), 1e-3);
    }

    @Test
    void testMinimumValue() {
        //By default, ScalingConvention.GENERATOR
        assertEquals(0., g1.minimumValue(network), 0.0);
        assertEquals(0.0, g3.minimumValue(network), 0.0);
        assertEquals(-Double.MAX_VALUE, l1.minimumValue(network), 0.0);
        assertEquals(-80, l2.minimumValue(network), 0.0);
        assertEquals(-100, l3.minimumValue(network), 0.0);
        assertEquals(0.0, s.minimumValue(network), 0.0);
        assertEquals(0.0, unknownGenerator.minimumValue(network), 0.0);

        //test StackScalable
        assertEquals(0.0, Scalable.stack(g1, g2, g3).minimumValue(network), 0.0);
        assertEquals(-Double.MAX_VALUE, Scalable.stack(g1, l1, l2).minimumValue(network), 0.0);

        //test ProportionalScalable
        assertEquals(0., Scalable.proportional(Arrays.asList(70.0, 20.0, 10.0), Arrays.asList(g1, g2, g3)).minimumValue(network), 1e-3);
        assertEquals(-Double.MAX_VALUE, Scalable.proportional(Arrays.asList(70.0, 20.0, 10.0), Arrays.asList(g1, l1, l2)).minimumValue(network), 1e-3);
    }

    @Test
    void testMinimumValueLoadConvention() {
        convention = LOAD;
        assertEquals(-100., g1.minimumValue(network, convention), 0.0);
        assertEquals(-80.0, g3.minimumValue(network, convention), 0.0);
        assertEquals(0, l1.minimumValue(network, convention), 0.0);
        assertEquals(20, l2.minimumValue(network, convention), 0.0);
        assertEquals(-50, l3.minimumValue(network, convention), 0.0);
        assertEquals(0.0, s.minimumValue(network, convention), 0.0);
        assertEquals(0.0, unknownGenerator.minimumValue(network, convention), 0.0);

        //test StackScalable
        assertEquals(-280, Scalable.stack(g1, g2, g3).minimumValue(network, convention), 0.0);
        assertEquals(-80, Scalable.stack(g1, l1, l2).minimumValue(network, convention), 0.0);

        //test ProportionalScalable
        assertEquals(-280., Scalable.proportional(Arrays.asList(70.0, 20.0, 10.0), Arrays.asList(g1, g2, g3)).minimumValue(network, convention), 1e-3);
        assertEquals(-80, Scalable.proportional(Arrays.asList(70.0, 20.0, 10.0), Arrays.asList(g1, l1, l2)).minimumValue(network, convention), 1e-3);
    }

    @Test
    void testProportionalScalableGenerator() {
        double done = Scalable.proportional(Arrays.asList(70.0, 30.0), Arrays.asList(g1, g2)).scale(network, 100.0);
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

        reset();
        done = Scalable.proportional(Arrays.asList(70.0, 20.0, 10.0), Arrays.asList(g1, s, unknownGenerator)).scale(network, 100.0);
        assertEquals(70.0, done, 0.0);
        assertEquals(70.0, network.getGenerator("g1").getTargetP(), 1e-5);
    }

    @Test
    void testProportionalScale() {
        //By default, ScalingConvention.GENERATOR
        reset();
        double done = Scalable.proportional(Arrays.asList(70.0, 30.0), Arrays.asList(g1, l1)).scale(network, 100.0);
        assertEquals(70.0, done, 0.0);
        assertEquals(70.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(0.0, network.getLoad("l1").getP0(), 1e-5);

        reset();
        done = Scalable.proportional(Arrays.asList(70.0, 30.0), Arrays.asList(g1, l1)).scale(network, -100.0);
        assertEquals(-30.0, done, 0.0);
        assertEquals(0.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(30, network.getLoad("l1").getP0(), 1e-5);

        reset();
        done = Scalable.proportional(Arrays.asList(90.0, 10.0), Arrays.asList(g3, l3)).scale(network, 100.0);
        assertEquals(90.0, done, 0.0);
        assertEquals(80.0, network.getGenerator("g3").getTargetP(), 1e-5);
        assertEquals(-10.0, network.getLoad("l1").getP0(), 1e-5);

        reset();
        done = Scalable.proportional(Arrays.asList(30.0, 70.0), Arrays.asList(l1, l2)).scale(network, -100.0);
        assertEquals(-80.0, done, 0.0);
        assertEquals(80.0, network.getLoad("l1").getP0(), 1e-5);

    }

    @Test
    void testProportionalScaleLoadConvention() {
        convention = LOAD;
        reset();

        ScalingParameters parameters = new ScalingParameters().setScalingConvention(convention);
        double done = Scalable.proportional(Arrays.asList(70.0, 30.0), Arrays.asList(g1, l1)).scale(network, 100.0, parameters);
        assertEquals(30.0, done, 0.0);
        assertEquals(0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(30.0, network.getLoad("l1").getP0(), 1e-5);

        reset();
        done = Scalable.proportional(Arrays.asList(70.0, 30.0), Arrays.asList(g1, l1)).scale(network, -100.0, parameters);
        assertEquals(-70.0, done, 0.0);
        assertEquals(70.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(0.0, network.getLoad("l1").getP0(), 1e-5);

        reset();
        done = Scalable.proportional(Arrays.asList(90.0, 10.0), Arrays.asList(g3, l3)).scale(network, 100.0, parameters);
        assertEquals(10.0, done, 0.0);
        assertEquals(0.0, network.getGenerator("g3").getTargetP(), 1e-5);
        assertEquals(10.0, network.getLoad("l1").getP0(), 1e-5);

        reset();
        done = Scalable.proportional(Arrays.asList(90.0, 10.0), Arrays.asList(l3, g3)).scale(network, -100.0, parameters);
        assertEquals(-60.0, done, 0.0);
        assertEquals(-50.0, network.getLoad("l1").getP0(), 1e-5);
        assertEquals(10.0, network.getGenerator("g3").getTargetP(), 1e-5);

        reset();
        done = Scalable.proportional(Arrays.asList(50.0, 50.0), Arrays.asList(l1, l2)).scale(network, 100.0, parameters);
        assertEquals(80.0, done, 0.0);
        assertEquals(80.0, network.getLoad("l1").getP0(), 1e-5);

    }

    @Test
    void testConstantPowerFactorScaling() {
        reset();
        ScalingParameters parameters = new ScalingParameters().setConstantPowerFactor(true);
        network.getLoad("l1").setQ0(10);
        network.getLoad("l1").setP0(100);
        double done = Scalable.proportional(Arrays.asList(50.0, 50.0), Arrays.asList(g1, l1)).scale(network, 100.0, parameters);
        assertEquals(100.0, done, 1e-5);
        assertEquals(50.0, network.getLoad("l1").getP0(), 1e-5);
        assertEquals(5.0, network.getLoad("l1").getQ0(), 1e-5);
        assertEquals(50.0, network.getGenerator("g1").getTargetP(), 1e-5);
    }

    @Test
    void testConstantPowerFactorScalingWithLoadConvention() {
        reset();
        ScalingParameters parameters = new ScalingParameters().setScalingConvention(LOAD).setConstantPowerFactor(true);
        network.getLoad("l1").setQ0(10);
        network.getLoad("l1").setP0(100);
        network.getGenerator("g1").setTargetP(70);
        double done = Scalable.proportional(Arrays.asList(50.0, 50.0), Arrays.asList(g1, l1)).scale(network, 100.0, parameters);
        assertEquals(100.0, done, 1e-5);
        assertEquals(150.0, network.getLoad("l1").getP0(), 1e-5);
        assertEquals(15.0, network.getLoad("l1").getQ0(), 1e-5);
        assertEquals(20.0, network.getGenerator("g1").getTargetP(), 1e-5);
    }

    @Test
    void testStackScale() {
        // By default, ScalingConvention.GENERATOR
        Scalable scalable = Scalable.stack(g1, g2);

        double done = scalable.scale(network, 150.0);
        assertEquals(150.0, done, 0.0);
        assertEquals(100.0, network.getGenerator("g1").getTargetP(), 0.0);
        assertEquals(50.0, network.getGenerator("g2").getTargetP(), 0.0);

        done = scalable.scale(network, 100.0);
        assertEquals(50.0, done, 0.0);
        assertEquals(100.0, network.getGenerator("g1").getTargetP(), 0.0);
        assertEquals(100.0, network.getGenerator("g2").getTargetP(), 0.0);

        scalable = Scalable.stack(s, unknownGenerator, unknownLoad, unknownDanglingLine);
        done = scalable.scale(network, 150.0);
        assertEquals(0.0, done, 0.0);

        reset();
        done = Scalable.stack(g1, l1).scale(network, 100.0);
        assertEquals(100.0, done, 0.0);
        assertEquals(100.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(0.0, network.getLoad("l1").getP0(), 1e-5);

        reset();
        done = Scalable.stack(g1, l1).scale(network, -100.0);
        assertEquals(-100, done, 0.0);
        assertEquals(0.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(100, network.getLoad("l1").getP0(), 1e-5);

        reset();
        done = Scalable.stack(g3, l3).scale(network, 100.0);
        assertEquals(100.0, done, 0.0);
        assertEquals(80.0, network.getGenerator("g3").getTargetP(), 1e-5);
        assertEquals(-20.0, network.getLoad("l1").getP0(), 1e-5);

        reset();
        done = Scalable.stack(l1, l2).scale(network, -100.0);
        assertEquals(-100.0, done, 0.0);
        assertEquals(100, network.getLoad("l1").getP0(), 1e-5);

    }

    @Test
    void testStackScaleLoadConvention() {
        convention = LOAD;
        Scalable scalable = Scalable.stack(g1, g2);

        ScalingParameters parameters = new ScalingParameters().setScalingConvention(convention);
        double done = scalable.scale(network, -150.0, parameters);
        assertEquals(-150.0, done, 0.0);
        assertEquals(100.0, network.getGenerator("g1").getTargetP(), 0.0);
        assertEquals(50.0, network.getGenerator("g2").getTargetP(), 0.0);

        done = scalable.scale(network, -100.0, parameters);
        assertEquals(-50.0, done, 0.0);
        assertEquals(100.0, network.getGenerator("g1").getTargetP(), 0.0);
        assertEquals(100.0, network.getGenerator("g2").getTargetP(), 0.0);

        scalable = Scalable.stack(s, unknownGenerator, unknownLoad, unknownDanglingLine);
        done = scalable.scale(network, -150.0, parameters);
        assertEquals(0.0, done, 0.0);

        reset();
        done = Scalable.stack(g1, l1).scale(network, -100.0, parameters);
        assertEquals(-100.0, done, 0.0);
        assertEquals(100.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(0.0, network.getLoad("l1").getP0(), 1e-5);

        reset();
        done = Scalable.stack(g1, l1).scale(network, 100.0, parameters);
        assertEquals(100, done, 0.0);
        assertEquals(0.0, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(100, network.getLoad("l1").getP0(), 1e-5);

        reset();
        done = Scalable.stack(g3, l3).scale(network, -100.0, parameters);
        assertEquals(-100.0, done, 0.0);
        assertEquals(80.0, network.getGenerator("g3").getTargetP(), 1e-5);
        assertEquals(-20.0, network.getLoad("l1").getP0(), 1e-5);

        reset();
        done = Scalable.stack(l1, l2).scale(network, 100.0, parameters);
        assertEquals(100.0, done, 0.0);
        assertEquals(100, network.getLoad("l1").getP0(), 1e-5);

    }

    @Test
    void testFilterInjections() {
        Generator generator1 = network.getGenerator("g1");
        Generator generator2 = network.getGenerator("g2");
        Load load1 = network.getLoad("l1");

        List<Injection> generators = g1.filterInjections(network);
        assertEquals(1, generators.size());
        assertSame(generator1, generators.get(0));

        generators = Scalable.stack(g1, g2).filterInjections(network);
        assertEquals(2, generators.size());
        assertSame(generator1, generators.get(0));
        assertSame(generator2, generators.get(1));

        Scalable unknownGenerator = Scalable.onGenerator("unknown");
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

    @Test
    void testProportionalScalableIterativeMode() {
        double done = Scalable.proportional(Arrays.asList(70.0, 20.0, 10.0), Arrays.asList(g1, s, unknownGenerator)).scale(network, 100.0);
        assertEquals(70.0, done, 0.0);
        assertEquals(70.0, network.getGenerator("g1").getTargetP(), 1e-5);

        ScalingParameters parameters = new ScalingParameters().setPriority(RESPECT_OF_VOLUME_ASKED);

        reset();
        done = Scalable.proportional(Arrays.asList(70.0, 20.0, 10.0), Arrays.asList(g1, s, unknownGenerator)).scale(network, 100.0, parameters);
        assertEquals(100.0, done, 0.0);
        assertEquals(100.0, network.getGenerator("g1").getTargetP(), 1e-5);

        reset();
        done = Scalable.proportional(Arrays.asList(2.5, 7.5, 90.0), Arrays.asList(g1, g2, g3)).scale(network, 100.0);
        assertEquals(90.0, done, 0.0);
        assertEquals(2.5, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(7.5, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(80.0, network.getGenerator("g3").getTargetP(), 1e-5);

        reset();
        done = Scalable.proportional(Arrays.asList(2.5, 7.5, 90.0), Arrays.asList(g1, g2, g3)).scale(network, 100.0, parameters);
        assertEquals(100.0, done, 0.0);
        assertEquals(5, network.getGenerator("g1").getTargetP(), 1e-5);
        assertEquals(15.0, network.getGenerator("g2").getTargetP(), 1e-5);
        assertEquals(80.0, network.getGenerator("g3").getTargetP(), 1e-5);

        reset();
        done = Scalable.proportional(Arrays.asList(70.0, 10.0, 20.0), Arrays.asList(g3, s, unknownGenerator)).scale(network, 100.0);
        assertEquals(70.0, done, 0.0);
        assertEquals(70.0, network.getGenerator("g3").getTargetP(), 1e-5);

        reset();
        done = Scalable.proportional(Arrays.asList(70.0, 10.0, 20.0), Arrays.asList(g3, s, unknownGenerator)).scale(network, 100.0, parameters);
        assertEquals(80.0, done, 0.0);
        assertEquals(80.0, network.getGenerator("g3").getTargetP(), 1e-5);
    }

    @Test
    void testExceptionWhenIncorrectArgumentsInProportionalScalableConstructor() {
        var gens = Arrays.asList(g1, g2, g3);
        assertThrows(NullPointerException.class, () -> Scalable.proportional(null, gens));

        var percents = Arrays.asList(50.0, 50.0);
        assertThrows(NullPointerException.class, () -> Scalable.proportional(percents, null));

        assertThrows(IllegalArgumentException.class, () -> Scalable.proportional(percents, gens));
    }

    @Test
    void testProportionalScaleIterativeThreeSteps() {
        double done = Scalable.proportional(Arrays.asList(70.0, 20.0, 10.0), Arrays.asList(g1, g2, g3)).scale(network, 270.0);
        assertEquals(181.0, done, 0.0);
        assertEquals(100.0, network.getGenerator("g1").getTargetP(), 1e-3);
        assertEquals(54, network.getGenerator("g2").getTargetP(), 1e-3);
        assertEquals(27, network.getGenerator("g3").getTargetP(), 1e-3);

        ScalingParameters parameters = new ScalingParameters().setPriority(RESPECT_OF_VOLUME_ASKED);

        reset();
        done = Scalable.proportional(Arrays.asList(70.0, 20.0, 10.0), Arrays.asList(g1, g2, g3)).scale(network, 270.0, parameters);
        assertEquals(270.0, done, 0.0);
        assertEquals(100.0, network.getGenerator("g1").getTargetP(), 1e-3);
        assertEquals(100.0, network.getGenerator("g2").getTargetP(), 1e-3);
        assertEquals(70.0, network.getGenerator("g3").getTargetP(), 1e-3);
    }

    @Test
    void testScalableReuse() {
        Scalable scalable = Scalable.proportional(Arrays.asList(70.0, 20.0, 10.0), Arrays.asList(g1, g2, g3));
        ScalingParameters parameters = new ScalingParameters().setPriority(RESPECT_OF_VOLUME_ASKED);
        double done = scalable.scale(network, 270.0, parameters);
        assertEquals(270.0, done, 0.0);
        assertEquals(100.0, network.getGenerator("g1").getTargetP(), 1e-3);
        assertEquals(100.0, network.getGenerator("g2").getTargetP(), 1e-3);
        assertEquals(70.0, network.getGenerator("g3").getTargetP(), 1e-3);

        reset();
        done = scalable.scale(network, 270.0, parameters);
        assertEquals(270.0, done, 0.0);
        assertEquals(100.0, network.getGenerator("g1").getTargetP(), 1e-3);
        assertEquals(100.0, network.getGenerator("g2").getTargetP(), 1e-3);
        assertEquals(70.0, network.getGenerator("g3").getTargetP(), 1e-3);
    }
}
