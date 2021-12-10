/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import com.powsybl.action.util.Scalable.ScalingConvention;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Network;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.powsybl.action.util.Scalable.ScalingConvention.GENERATOR;
import static com.powsybl.action.util.Scalable.ScalingConvention.LOAD;
import static com.powsybl.action.util.ScalableTestNetwork.createNetwork;
import static org.junit.Assert.*;

/**
 * @author Ameni Walha <ameni.walha at rte-france.com>
 */
public class GeneratorScalableTest {

    private Network network;
    private Scalable g1;
    private Scalable g2;
    private Scalable g3;
    private Scalable g4;
    private Scalable g5;
    private Scalable unknownGeneratorScalable;

    private ScalingConvention convention;

    @Before
    public void setUp() {

        network = createNetwork();
        g1 = Scalable.onGenerator("g1");
        unknownGeneratorScalable = Scalable.onGenerator("unknown");
        g2 = Scalable.onGenerator("g2", -10., 120);
        g3 = Scalable.onGenerator("g3");

        g4 = Scalable.onGenerator("g2", 0., 80);
        g5 = Scalable.onGenerator("g2", 20., 100);

    }

    @Test(expected = PowsyblException.class)
    public void testConstructorInvalidP() {
        new GeneratorScalable("g1", 20, 10);
    }

    @Test
    public void testInitialValue() {
        //In this network case, initialValue is always giving 0 because of getTerminal()
        assertEquals(0.0, g1.initialValue(network), 0.0);
        assertEquals(0.0, g2.initialValue(network), 0.0);
        assertEquals(0.0, g3.initialValue(network), 0.0);
    }

    @Test
    public void testMaximumValue() {

        assertEquals(100.0, g1.maximumValue(network), 0.0);

        assertEquals(100.0, g1.maximumValue(network, GENERATOR), 0.0);
        assertEquals(0, g1.maximumValue(network, LOAD), 0.0);

        assertEquals(100.0, g2.maximumValue(network, GENERATOR), 0.0);
        assertEquals(0, g2.maximumValue(network, LOAD), 0.0);

        assertEquals(80, g4.maximumValue(network, GENERATOR), 0);
        assertEquals(0, g4.maximumValue(network, LOAD), 0);

        assertEquals(100, g5.maximumValue(network, GENERATOR), 0);
        assertEquals(-20, g5.maximumValue(network, LOAD), 0);
    }

    @Test
    public void testMinimumValue() {

        assertEquals(0.0, g1.minimumValue(network), 0.0);

        assertEquals(0, g2.minimumValue(network, GENERATOR), 0.0);
        assertEquals(-100, g2.minimumValue(network, LOAD), 0.0);

        assertEquals(0, g4.minimumValue(network, GENERATOR), 0);
        assertEquals(-80, g4.minimumValue(network, LOAD), 0);

        assertEquals(20, g5.minimumValue(network, GENERATOR), 0);
        assertEquals(-100, g5.minimumValue(network, LOAD), 0);
    }

    @Test
    public void testListGenerators() {
        Generator generator1 = network.getGenerator("g1");

        List<Generator> generators = g1.listGenerators(network);
        assertEquals(1, generators.size());
        assertSame(generator1, generators.get(0));
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
        List<Injection> generators = g1.filterInjections(network);
        assertEquals(1, generators.size());
        assertSame(generator1, generators.get(0));
    }

    @Test
    public void testGeneratorScaleDefault() {
        assertEquals(0, unknownGeneratorScalable.scale(network, 30), 1e-3);

        Generator generator1 = network.getGenerator("g1");

        //test GeneratorScalable without min and max values defined
        g1.reset(network);
        assertEquals(0, generator1.getTargetP(), 1e-3);
        assertEquals(50, g1.scale(network, 50), 1e-3);
        assertEquals(50, generator1.getTargetP(), 1e-3);
        assertEquals(100, g1.maximumValue(network), 1e-3);
        assertEquals(0, g1.minimumValue(network), 1e-3);
        assertEquals(50, g1.scale(network, 60), 1e-3);
        assertEquals(100, generator1.getTargetP(), 1e-3);
        assertEquals(-100, g1.scale(network, -120), 1e-3);
        assertEquals(0, generator1.getTargetP(), 1e-3);

        //test GeneratorScalable with min and max values defined
        //Case 1 : GeneratorScalable.minValue < generator.getMinP() && GeneratorScalable.maxValue > generator.getMaxP()
        Generator generator2 = network.getGenerator("g2");
        g2.reset(network);
        assertEquals(0, generator2.getTargetP(), 1e-3);
        assertEquals(50, g2.scale(network, 50), 1e-3);
        assertEquals(50, generator2.getTargetP(), 1e-3);
        assertEquals(100, g2.maximumValue(network), 1e-3);
        assertEquals(0, g2.minimumValue(network), 1e-3);
        assertEquals(50, g2.scale(network, 60), 1e-3);
        assertEquals(100, generator2.getTargetP(), 1e-3);
        assertEquals(-100, g2.scale(network, -120), 1e-3);
        assertEquals(0, generator2.getTargetP(), 1e-3);

        //Case 2 : GeneratorScalable.maxValue < generator.getMaxP()
        g4.reset(network);
        assertEquals(0, generator2.getTargetP(), 1e-3);
        assertEquals(80, g4.maximumValue(network), 1e-3);
        assertEquals(80, g4.scale(network, 100), 1e-3);
        assertEquals(80, generator2.getTargetP(), 1e-3);

        //Case 3 : GeneratorScalable.minValue > generator.getMinP()
        assertEquals(80, generator2.getTargetP(), 1e-3);
        assertEquals(20, g5.minimumValue(network), 1e-3);
        assertEquals(-60, g5.scale(network, -80), 1e-3);
        assertEquals(20, generator2.getTargetP(), 1e-3);

        g5.reset(network);
        //Case 4 : generator.getTargetP() not in interval, skipped
        assertEquals(0, generator2.getTargetP(), 1e-3);
        assertEquals(0, g5.scale(network, 50), 1e-3);
    }

    @Test
    public void testGeneratorScaleGeneratorConvention() {
        //test with ScalingConvention.GENERATOR (by default)
        convention = GENERATOR;

        assertEquals(0, unknownGeneratorScalable.scale(network, 30, convention), 1e-3);

        Generator generator1 = network.getGenerator("g1");

        //test GeneratorScalable without min and max values defined
        g1.reset(network);
        assertEquals(0, generator1.getTargetP(), 1e-3);
        assertEquals(50, g1.scale(network, 50, convention), 1e-3);
        assertEquals(50, generator1.getTargetP(), 1e-3);
        assertEquals(100, g1.maximumValue(network), 1e-3);
        assertEquals(0, g1.minimumValue(network), 1e-3);
        assertEquals(50, g1.scale(network, 60, convention), 1e-3);
        assertEquals(100, generator1.getTargetP(), 1e-3);
        assertEquals(-100, g1.scale(network, -120, convention), 1e-3);
        assertEquals(0, generator1.getTargetP(), 1e-3);

        //test GeneratorScalable with min and max values defined
        //Case 1 : GeneratorScalable.minValue < generator.getMinP() && GeneratorScalable.maxValue > generator.getMaxP()
        Generator generator2 = network.getGenerator("g2");
        g2.reset(network);
        assertEquals(0, generator2.getTargetP(), 1e-3);
        assertEquals(50, g2.scale(network, 50, convention), 1e-3);
        assertEquals(50, generator2.getTargetP(), 1e-3);
        assertEquals(100, g2.maximumValue(network), 1e-3);
        assertEquals(0, g2.minimumValue(network), 1e-3);
        assertEquals(50, g2.scale(network, 60, convention), 1e-3);
        assertEquals(100, generator2.getTargetP(), 1e-3);
        assertEquals(-100, g2.scale(network, -120, convention), 1e-3);
        assertEquals(0, generator2.getTargetP(), 1e-3);

        //Case 2 : GeneratorScalable.maxValue < generator.getMaxP()
        g4.reset(network);
        assertEquals(0, generator2.getTargetP(), 1e-3);
        assertEquals(80, g4.maximumValue(network), 1e-3);
        assertEquals(80, g4.scale(network, 100, convention), 1e-3);
        assertEquals(80, generator2.getTargetP(), 1e-3);

        //Case 3 : GeneratorScalable.minValue > generator.getMinP()
        assertEquals(80, generator2.getTargetP(), 1e-3);
        assertEquals(20, g5.minimumValue(network), 1e-3);
        assertEquals(-60, g5.scale(network, -80, convention), 1e-3);
        assertEquals(20, generator2.getTargetP(), 1e-3);

        g5.reset(network);
        //Case 4 : generator.getTargetP() not in interval, skipped
        assertEquals(0, generator2.getTargetP(), 1e-3);
        assertEquals(0, g5.scale(network, 50, convention), 1e-3);
    }

    @Test
    public void testGeneratorScaleLoadConvention() {
        //test with ScalingConvention.LOAD
        convention = LOAD;

        assertEquals(0, unknownGeneratorScalable.scale(network, 30, convention), 1e-3);

        Generator generator1 = network.getGenerator("g1");

        //test GeneratorScalable without min and max values defined
        g1.reset(network);
        assertEquals(0, generator1.getTargetP(), 1e-3);
        assertEquals(-50, g1.scale(network, -50, convention), 1e-3);
        assertEquals(50, generator1.getTargetP(), 1e-3);
        assertEquals(100, g1.maximumValue(network), 1e-3);
        assertEquals(0, g1.minimumValue(network), 1e-3);
        assertEquals(-50, g1.scale(network, -60, convention), 1e-3);
        assertEquals(100, generator1.getTargetP(), 1e-3);
        assertEquals(100, g1.scale(network, 120, convention), 1e-3);
        assertEquals(0, generator1.getTargetP(), 1e-3);

        //test GeneratorScalable with min and max values defined
        //Case 1 : GeneratorScalable.minValue < generator.getMinP() && GeneratorScalable.maxValue > generator.getMaxP()
        Generator generator2 = network.getGenerator("g2");
        g2.reset(network);
        assertEquals(0, generator2.getTargetP(), 1e-3);
        assertEquals(-50, g2.scale(network, -50, convention), 1e-3);
        assertEquals(50, generator2.getTargetP(), 1e-3);
        assertEquals(100, g2.maximumValue(network), 1e-3);
        assertEquals(0, g2.minimumValue(network), 1e-3);
        assertEquals(-50, g2.scale(network, -60, convention), 1e-3);
        assertEquals(100, generator2.getTargetP(), 1e-3);
        assertEquals(100, g2.scale(network, 120, convention), 1e-3);
        assertEquals(0, generator2.getTargetP(), 1e-3);

        //Case 2 : GeneratorScalable.maxValue < generator.getMaxP()
        g4.reset(network);
        assertEquals(0, generator2.getTargetP(), 1e-3);
        assertEquals(80, g4.maximumValue(network), 1e-3);
        assertEquals(-80, g4.scale(network, -100, convention), 1e-3);
        assertEquals(80, generator2.getTargetP(), 1e-3);

        //Case 3 : GeneratorScalable.minValue > generator.getMinP()
        assertEquals(80, generator2.getTargetP(), 1e-3);
        assertEquals(20, g5.minimumValue(network), 1e-3);
        assertEquals(60, g5.scale(network, 80, convention), 1e-3);
        assertEquals(20, generator2.getTargetP(), 1e-3);

        g5.reset(network);
        //Case 4 : generator.getTargetP() not in interval, skipped
        assertEquals(0, generator2.getTargetP(), 1e-3);
        assertEquals(0, g5.scale(network, 50, convention), 1e-3);
    }

}
