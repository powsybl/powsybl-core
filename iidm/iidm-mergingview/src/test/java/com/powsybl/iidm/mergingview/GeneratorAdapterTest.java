/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class GeneratorAdapterTest {
    private MergingView mergingView;

    private double delta;
    private String id;
    private String name;
    private double maxP;
    private double minP;
    private double targetV;
    private double targetP;
    private double targetQ;
    private double ratedS;
    private EnergySource energySource = EnergySource.NUCLEAR;

    @Before
    public void setup() {
        mergingView = MergingView.create("GeneratorAdapterTest", "iidm");
        mergingView.merge(FictitiousSwitchFactory.create());
        delta = 0.0;
        id = "GENTEST";
        name = "generator";
        maxP = 9999.99;
        minP = -9999.99;
        targetV = 25.5;
        targetP = 600.05;
        targetQ = 300.5;
        ratedS = 10.5;
        energySource = EnergySource.NUCLEAR;
    }

    @Test
    public void testSetterGetter() {
        final VoltageLevel vlNode = mergingView.getVoltageLevel("C");
        final GeneratorAdder generatorAdder = vlNode.newGenerator();
        assertNotNull(generatorAdder);
        assertTrue(generatorAdder instanceof GeneratorAdderAdapter);
        final Generator generator = createGenerator(generatorAdder);
        assertNotNull(generator);
        assertTrue(generator instanceof GeneratorAdapter);
        assertSame(mergingView, generator.getNetwork());

        assertTrue(generator.getTerminal() instanceof TerminalAdapter);
        generator.getTerminals().forEach(t -> {
            assertTrue(t instanceof TerminalAdapter);
            assertNotNull(t);
        });

        assertEquals(id, generator.getId());
        assertTrue(generator.isVoltageRegulatorOn());
        assertTrue(generator.getRegulatingTerminal() instanceof TerminalAdapter);
        assertEquals(ConnectableType.GENERATOR, generator.getType());

        assertEquals(targetV, generator.getTargetV(), delta);
        assertTrue(generator.setTargetV(++targetV) instanceof GeneratorAdapter);
        assertEquals(targetV, generator.getTargetV(), delta);

        assertEquals(targetP, generator.getTargetP(), delta);
        assertTrue(generator.setTargetP(++targetP) instanceof GeneratorAdapter);
        assertEquals(targetP, generator.getTargetP(), delta);

        assertEquals(targetQ, generator.getTargetQ(), delta);
        assertTrue(generator.setTargetQ(++targetQ) instanceof GeneratorAdapter);
        assertEquals(targetQ, generator.getTargetQ(), delta);

        assertEquals(minP, generator.getMinP(), delta);
        assertTrue(generator.setMinP(--minP) instanceof GeneratorAdapter);
        assertEquals(minP, generator.getMinP(), delta);

        assertEquals(maxP, generator.getMaxP(), delta);
        assertTrue(generator.setMaxP(--maxP) instanceof GeneratorAdapter);
        assertEquals(maxP, generator.getMaxP(), delta);

        double maxQ = 9999.95;
        double minQ = -9999.95;
        generator.newMinMaxReactiveLimits()
                 .setMaxQ(maxQ)
                 .setMinQ(minQ)
                 .add();
        assertEquals(maxQ, generator.getReactiveLimits().getMaxQ(1), delta);
        assertEquals(minQ, generator.getReactiveLimits().getMinQ(1), delta);
        assertNotNull(generator.getReactiveLimits(MinMaxReactiveLimits.class));

        assertEquals(energySource, generator.getEnergySource());
        energySource = EnergySource.HYDRO;
        assertTrue(generator.setEnergySource(energySource) instanceof GeneratorAdapter);
        assertSame(energySource, generator.getEnergySource());

        assertEquals(ratedS, generator.getRatedS(), delta);
        assertTrue(generator.setRatedS(++ratedS) instanceof GeneratorAdapter);
        assertEquals(ratedS, generator.getRatedS(), delta);

        assertTrue(generator.getAliases().isEmpty());
        generator.addAlias("alias");
        generator.addAlias("typedAlias", "type");
        assertEquals(2, generator.getAliases().size());
        assertTrue(generator.getAliases().contains("alias"));
        assertTrue(generator.getAliases().contains("typedAlias"));
        assertFalse(generator.getAliasType("alias").isPresent());
        assertEquals("type", generator.getAliasType("typedAlias").orElse(null));
        assertEquals("typedAlias", generator.getAliasFromType("type").orElse(null));
        generator.addAlias("alias", true);
        assertEquals(3, generator.getAliases().size());
        assertTrue(generator.getAliases().contains("alias#0"));
        generator.addAlias("alias", "type2", true);
        assertEquals(4, generator.getAliases().size());
        assertTrue(generator.getAliases().contains("alias#1"));
        generator.removeAlias("alias");
        assertEquals(3, generator.getAliases().size());
        assertFalse(generator.getAliases().contains("alias"));

        // Not implemented yet !
        TestUtil.notImplemented(generator::remove);
    }

    @Test
    public void testAdderFromExisting() {
        createGenerator(mergingView.getVoltageLevel("C").newGenerator());
        mergingView.getVoltageLevel("C").newGenerator(mergingView.getGenerator(id))
                .setId("duplicate")
                .setNode(3)
                .add();
        Generator generator = mergingView.getGenerator("duplicate");
        assertNotNull(generator);
        assertTrue(generator.isVoltageRegulatorOn());
        assertEquals(targetV, generator.getTargetV(), delta);
        assertEquals(targetP, generator.getTargetP(), delta);
        assertEquals(targetQ, generator.getTargetQ(), delta);
        assertEquals(minP, generator.getMinP(), delta);
        assertEquals(maxP, generator.getMaxP(), delta);
        assertEquals(energySource, generator.getEnergySource());
        assertEquals(ratedS, generator.getRatedS(), delta);
    }

    private Generator createGenerator(GeneratorAdder generatorAdder) {
        return generatorAdder.setId(id)
                .setVoltageRegulatorOn(true)
                .setName(name)
                .setNode(1)
                .setMaxP(maxP)
                .setMinP(minP)
                .setTargetV(targetV)
                .setTargetP(targetP)
                .setTargetQ(targetQ)
                .setRatedS(ratedS)
                .setEnergySource(energySource)
                .setEnsureIdUnicity(true)
                .add();
    }
}
