/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.dsl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import groovy.lang.GroovyCodeSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class GeneratorModificationActionTest {

    private Network network;
    private Generator g;

    @BeforeEach
    void setUp() {
        network = EurostagTutorialExample1Factory.create();
        g = network.getGenerator("GEN");
    }

    @Test
    void testExceptionOnWrongGeneratorId() {
        ActionDb actionDb = new ActionDslLoader(
                new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("unknown generator");
        NetworkModification genModif = action.getModifications().get(0);
        // should throw with ThrowException = true
        PowsyblException e = assertThrows(PowsyblException.class, () -> genModif.apply(network, true, ReportNode.NO_OP));
        assertTrue(e.getMessage().contains("Generator 'UNKNOWN' not found"));
        // should not throw with ThrowException = false (default)
        assertDoesNotThrow(() -> genModif.apply(network));
    }

    @Test
    void testTargetVAndQWithVoltageRegulatorOff() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("targetV and targetQ with voltageRegulator OFF");
        action.run(network);
        assertEquals(20., g.getMinP(), 0.1);
        assertEquals(60., g.getMaxP(), 0.1);
        assertEquals(50., g.getTargetP(), 0.1);
        assertEquals(10, g.getTargetV(), 0.1);
        assertEquals(25., g.getTargetQ(), 0.1);
        assertFalse(g.isVoltageRegulatorOn());
    }

    @Test
    void testTargetVAndQWithVoltageRegulatorOn() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("targetV and targetQ with voltageRegulator ON");
        action.run(network);
        assertEquals(10, g.getTargetV(), 0.1);
        assertEquals(25., g.getTargetQ(), 0.1);
        assertTrue(g.isVoltageRegulatorOn());
    }

    @Test
    void testDeltaTargetPInBoundaries() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("deltaTargetP within boundaries");
        action.run(network);
        assertEquals(606., g.getTargetP(), 0.1);
    }

    @Test
    void testDeltaTargetPLowerBoundaryOverflow() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("deltaTargetP lower boundary overflow");
        action.run(network);
        assertEquals(-9999.99, g.getTargetP(), 0.1);
    }

    @Test
    void testDeltaTargetPUpperBoundaryOverflow() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("deltaTargetP upper boundary overflow");
        action.run(network);
        assertEquals(9999.99, g.getTargetP(), 0.1);
    }

    @Test
    void testTargetPLowerBoundaryOverflow() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("targetP lower boundary overflow");
        action.run(network);
        assertEquals(-9999.99, g.getTargetP(), 0.1);
    }

    @Test
    void testTargetPUpperBoundaryOverflow() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("targetP upper boundary overflow");
        action.run(network);
        assertEquals(9999.99, g.getTargetP(), 0.1);
    }

    @Test
    void testBothTargetpAndDeltaTargetP() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/exception-generator-modification-action.groovy"))).load(network));
        assertTrue(e.getMessage().contains("targetP/deltaTargetP actions are both found in generatorModification on 'GEN'"));
    }

    @Test
    void testConnectionOnOff() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("disconnect");
        action.run(network);
        assertFalse(g.getTerminal().isConnected());
        action = actionDb.getAction("connect");
        action.run(network);
        assertTrue(g.getTerminal().isConnected());
    }

    @Test
    void testConnectionOnOffWithTargetPChange() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("disconnect with targetP change");
        action.run(network);
        assertFalse(g.getTerminal().isConnected());
        assertEquals(50.0, g.getTargetP(), 0.5);
        action = actionDb.getAction("connect with targetP change");
        action.run(network);
        assertTrue(g.getTerminal().isConnected());
        assertEquals(100.0, g.getTargetP(), 0.5);
    }

    @Test
    void testAlreadyAtTheConnectionStateAsked() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("connect");
        action.run(network);
        assertTrue(g.getTerminal().isConnected());
        action = actionDb.getAction("disconnect");
        action.run(network);
        assertFalse(g.getTerminal().isConnected());
        action.run(network);
        assertFalse(g.getTerminal().isConnected());
    }

    @Test
    void testConnectionOnOffWithTargetVChange() {
        ActionDb actionDb = new ActionDslLoader(new GroovyCodeSource(getClass().getResource("/generator-modification-action.groovy"))).load(network);
        Action action = actionDb.getAction("disconnect");
        action.run(network);
        assertFalse(g.getTerminal().isConnected());
        assertEquals(24.5, g.getTargetV(), 0.01);
        action = actionDb.getAction("connect with targetV change");
        action.run(network);
        assertTrue(g.getTerminal().isConnected());
        assertEquals(1234.56, g.getTargetV(), 0.01);
    }
}
