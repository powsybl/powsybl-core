/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class GeneratorAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("GeneratorAdapterTest", "iidm");
        mergingView.merge(EurostagTutorialExample1Factory.create());
    }

    @Test
    public void testSetterGetter() {
        final Generator generator = mergingView.getGenerator("GEN").setVoltageRegulatorOn(true);
        assertNotNull(generator);
        assertTrue(generator instanceof GeneratorAdapter);
        assertSame(mergingView, generator.getNetwork());

        assertEquals("GEN", generator.getId());
        assertTrue(generator.isVoltageRegulatorOn());
        assertEquals(24.5, generator.getTargetV(), 0.001);
        assertTrue(generator.setTargetV(25.2) instanceof GeneratorAdapter);
        assertEquals(25.2, generator.getTargetV(), 0.001);
        assertEquals(607.0, generator.getTargetP(), 0.001);
        assertTrue(generator.setTargetP(609.5) instanceof GeneratorAdapter);
        assertEquals(609.5, generator.getTargetP(), 0.001);
        assertEquals(301.0, generator.getTargetQ(), 0.001);
        assertTrue(generator.setTargetQ(301.5) instanceof GeneratorAdapter);
        assertEquals(301.5, generator.getTargetQ(), 0.001);
        assertEquals(-9999.99, generator.getMinP(), 0.001);
        assertTrue(generator.setMinP(-9999.95) instanceof GeneratorAdapter);
        assertEquals(-9999.95, generator.getMinP(), 0.001);
        assertEquals(9999.99, generator.getMaxP(), 0.001);
        assertTrue(generator.setMaxP(9999.95) instanceof GeneratorAdapter);
        assertEquals(9999.95, generator.getMaxP(), 0.001);
        assertEquals(9999.99, generator.getReactiveLimits().getMaxQ(1), 0.001);
        assertEquals(-9999.99, generator.getReactiveLimits().getMinQ(1), 0.001);
        generator.newMinMaxReactiveLimits().setMaxQ(9999.95).setMinQ(-9999.95).add();
        assertEquals(9999.95, generator.getReactiveLimits().getMaxQ(1), 0.001);
        assertEquals(-9999.95, generator.getReactiveLimits().getMinQ(1), 0.001);
        assertTrue(generator.setEnergySource(EnergySource.NUCLEAR) instanceof GeneratorAdapter);
        assertSame(EnergySource.NUCLEAR, generator.getEnergySource());
        assertTrue(generator.setRatedS(15) instanceof GeneratorAdapter);
        assertEquals(15, generator.getRatedS(), 0.001);
        assertTrue(generator.getRegulatingTerminal() instanceof TerminalAdapter);
        TerminalAdapter term;

        assertEquals(generator.getType(), ConnectableType.GENERATOR);

        assertNotNull(generator.newReactiveCapabilityCurve().beginPoint()
            .setMaxQ(99999.99)
            .setMinQ(-99999.99)
            .setP(0.0d)
            .endPoint()
            .beginPoint()
            .setMaxQ(99999.95)
            .setMinQ(-99999.95)
            .setP(0.1d)
            .endPoint()
            .add());

        generator.getReactiveLimits().getMinQ(0.0d);
        assertEquals(99999.99, generator.getReactiveLimits().getMaxQ(0.0d), 0.001);
        assertEquals(-99999.99, generator.getReactiveLimits().getMinQ(0.0d), 0.001);
        assertEquals(99999.95, generator.getReactiveLimits().getMaxQ(0.1d), 0.001);
        assertEquals(-99999.95, generator.getReactiveLimits().getMinQ(0.1d), 0.001);

        assertTrue(generator.getTerminal() instanceof TerminalAdapter);

        /* Not implemented (Terminal adapter)
        assertEquals(-605, generator.getTerminal().getP(), 0.001);
        assertEquals(-225, generator.getTerminal().getQ(), 0.001); */

        List terminals = generator.getTerminals();
        for (Object terminal : terminals) {
            assertTrue(terminal instanceof TerminalAdapter);
        }

        Iterable<Generator> generators = mergingView.getGenerators();
        int size = 0;
        for (Generator gen : generators) {
            assertTrue(gen instanceof GeneratorAdapter);
            size++;
        }
        assertEquals(size, 1);

        // Not implemented yet !
        TestUtil.notImplemented(generator::remove);
    }

    @Test
    public void testGeneratorAdder() {
        final VoltageLevel vlgen = mergingView.getVoltageLevel("VLGEN");
        final GeneratorAdder generatorAdder = vlgen.newGenerator().setId("GENTEST").setVoltageRegulatorOn(true);
        assertNotNull(generatorAdder);
        assertTrue(generatorAdder instanceof GeneratorAdderAdapter);
        generatorAdder.setName("generator")
            .setBus("NGEN")
            .setMaxP(9999.99)
            .setMinP(-9999.99)
            .setTargetV(25.5)
            .setTargetP(600.05)
            .setTargetQ(300.5)
            .setRatedS(10.5)
            .setEnergySource(EnergySource.NUCLEAR)
            .setEnsureIdUnicity(true).add();
        boolean found = false;
        for (Generator generator : vlgen.getGenerators()) {
            if ("GENTEST".equals(generator.getId())) {
                Terminal term = generator.getRegulatingTerminal();
                assertTrue(term instanceof TerminalAdapter);
                assertEquals(25.5, generator.getTargetV(), 0.001);
                assertEquals(600.05, generator.getTargetP(), 0.001);
                assertEquals(300.5, generator.getTargetQ(), 0.001);
                assertEquals(9999.99, generator.getMaxP(), 0.001);
                assertEquals(-9999.99, generator.getMinP(), 0.001);
                assertEquals(10.5, generator.getRatedS(), 0.001);
                assertSame(EnergySource.NUCLEAR, generator.getEnergySource());
                assertEquals("generator", generator.getName());
                found = true;
            }
        }
        assertTrue(found);

        final GeneratorAdder generatorAdapter2 = vlgen.newGenerator().setId("GENTEST2").setVoltageRegulatorOn(true);
        generatorAdapter2.setName("generator2").setConnectableBus("NGEN")
            .setMaxP(9999.99)
            .setMinP(-9999.99)
            .setTargetV(25.5)
            .setTargetP(600.05)
            .setTargetQ(300.5).add();
        found = false;
        for (Generator generator : vlgen.getGenerators()) {
            if ("GENTEST2".equals(generator.getId())) {
                assertEquals("generator2", generator.getName());
                found = true;
            }
        }
        assertTrue(found);
    }
}
