/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.RegulationMode;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import groovy.lang.GroovyCodeSource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public class GeneratorModificationActionTest {

    private Network network;
    private Generator g;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        network = EurostagTutorialExample1Factory.create();
        g = network.getGenerator("GEN");
    }

    @Test
    public void testExceptionOnWrongGeneratorId() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("unknown generator");
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Generator 'UNKNOWN' not found");
        action.run(network, null);
    }

    @Test
    public void testTargetVAndQWithVoltageRegulatorOff() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("targetV and targetQ with voltageRegulator OFF");
        action.run(network, null);
        assertEquals(20., g.getMinP(), 0.1);
        assertEquals(60., g.getMaxP(), 0.1);
        assertEquals(50., g.getTargetP(), 0.1);
        assertEquals(10, g.getTargetV(), 0.1);
        assertEquals(25., g.getTargetQ(), 0.1);
        assertSame(RegulationMode.OFF, g.getRegulationMode());
    }

    @Test
    public void testTargetVAndQWithVoltageRegulatorOn() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("targetV and targetQ with voltageRegulator ON");
        action.run(network, null);
        assertEquals(10, g.getTargetV(), 0.1);
        assertEquals(25., g.getTargetQ(), 0.1);
        assertSame(RegulationMode.VOLTAGE, g.getRegulationMode());
    }

    @Test
    public void testDeltaTargetPInBoundaries() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("deltaTargetP within boundaries");
        action.run(network, null);
        assertEquals(606., g.getTargetP(), 0.1);
    }

    @Test
    public void testDeltaTargetPLowerBoundaryOverflow() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("deltaTargetP lower boundary overflow");
        action.run(network, null);
        assertEquals(-9999.99, g.getTargetP(), 0.1);
    }

    @Test
    public void testDeltaTargetPUpperBoundaryOverflow() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("deltaTargetP upper boundary overflow");
        action.run(network, null);
        assertEquals(9999.99, g.getTargetP(), 0.1);
    }

    @Test
    public void testTargetPLowerBoundaryOverflow() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("targetP lower boundary overflow");
        action.run(network, null);
        assertEquals(-9999.99, g.getTargetP(), 0.1);
    }

    @Test
    public void testTargetPUpperBoundaryOverflow() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("targetP upper boundary overflow");
        action.run(network, null);
        assertEquals(9999.99, g.getTargetP(), 0.1);
    }

    @Test
    public void testBothTargetpAndDeltaTargetP() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("targetP/deltaTargetP actions are both found in generatorModification on 'GEN'");
        new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/exception-generator-modification-action.groovy"))).load(network);
    }

    @Test
    public void testConnectionOnOff() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("disconnect");
        action.run(network, null);
        assertFalse(g.getTerminal().isConnected());
        action = actionDb.getAction("connect");
        action.run(network, null);
        assertTrue(g.getTerminal().isConnected());
    }

    @Test
    public void testConnectionOnOffWithTargetPChange() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("disconnect with targetP change");
        action.run(network, null);
        assertFalse(g.getTerminal().isConnected());
        assertEquals(50.0, g.getTargetP(), 0.5);
        action = actionDb.getAction("connect with targetP change");
        action.run(network, null);
        assertTrue(g.getTerminal().isConnected());
        assertEquals(100.0, g.getTargetP(), 0.5);
    }

    @Test
    public void testAlreadyAtTheConnectionStateAsked() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("connect");
        action.run(network, null);
        assertTrue(g.getTerminal().isConnected());
        action = actionDb.getAction("disconnect");
        action.run(network, null);
        assertFalse(g.getTerminal().isConnected());
        action.run(network, null);
        assertFalse(g.getTerminal().isConnected());
    }

    @Test
    public void testConnectionOnOffWithTargetVChange() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("disconnect");
        action.run(network, null);
        assertFalse(g.getTerminal().isConnected());
        assertEquals(24.5, g.getTargetV(), 0.01);
        action = actionDb.getAction("connect with targetV change");
        action.run(network, null);
        assertTrue(g.getTerminal().isConnected());
        assertEquals(1234.56, g.getTargetV(), 0.01);
    }
}
