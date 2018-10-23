/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class BoundaryLineTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;
    private VoltageLevel voltageLevel;

    @Before
    public void initNetwork() {
        network = NetworkFactory.create("test", "test");
        Substation substation = network.newSubstation()
                .setId("sub")
                .setCountry(Country.FR)
                .setTso("RTE")
            .add();
        voltageLevel = substation.newVoltageLevel()
                                    .setId("vl")
                                    .setName("vl")
                                    .setNominalV(440.0)
                                    .setHighVoltageLimit(400.0)
                                    .setLowVoltageLimit(200.0)
                                    .setTopologyKind(TopologyKind.BUS_BREAKER)
                                .add();
        voltageLevel.getBusBreakerView().newBus()
                                            .setId("bus_vl")
                                            .setName("bus_vl")
                                        .add();
    }

    @Test
    public void baseTests() {
        double r = 10.0;
        double x = 20.0;
        double g = 30.0;
        double b = 40.0;
        double p0 = 50.0;
        double q0 = 60.0;
        String id = "danglingId";
        String name = "danlingName";
        String ucteXnodeCode = "code";
        voltageLevel.newBoundaryLine()
                        .setId(id)
                        .setName(name)
                        .setR(r)
                        .setX(x)
                        .setG(g)
                        .setB(b)
                        .setP0(p0)
                        .setQ0(q0)
                        .setUcteXnodeCode(ucteXnodeCode)
                        .setBus("bus_vl")
                        .setConnectableBus("bus_vl")
                    .add();
        BoundaryLine boundaryLine = network.getBoundaryLine(id);
        // adder
        assertEquals(ConnectableType.DANGLING_LINE, boundaryLine.getType());
        assertEquals(r, boundaryLine.getR(), 0.0);
        assertEquals(x, boundaryLine.getX(), 0.0);
        assertEquals(g, boundaryLine.getG(), 0.0);
        assertEquals(b, boundaryLine.getB(), 0.0);
        assertEquals(p0, boundaryLine.getP0(), 0.0);
        assertEquals(q0, boundaryLine.getQ0(), 0.0);
        assertEquals(id, boundaryLine.getId());
        assertEquals(name, boundaryLine.getName());
        assertEquals(ucteXnodeCode, boundaryLine.getUcteXnodeCode());

        // setter getter
        double r2 = 11.0;
        double x2 = 21.0;
        double g2 = 31.0;
        double b2 = 41.0;
        double p02 = 51.0;
        double q02 = 61.0;
        boundaryLine.setR(r2);
        assertEquals(r2, boundaryLine.getR(), 0.0);
        boundaryLine.setX(x2);
        assertEquals(x2, boundaryLine.getX(), 0.0);
        boundaryLine.setG(g2);
        assertEquals(g2, boundaryLine.getG(), 0.0);
        boundaryLine.setB(b2);
        assertEquals(b2, boundaryLine.getB(), 0.0);
        boundaryLine.setP0(p02);
        assertEquals(p02, boundaryLine.getP0(), 0.0);
        boundaryLine.setQ0(q02);
        assertEquals(q02, boundaryLine.getQ0(), 0.0);

        boundaryLine.newCurrentLimits()
                        .setPermanentLimit(100.0)
                    .add();
        assertEquals(100.0, boundaryLine.getCurrentLimits().getPermanentLimit(), 0.0);

        Bus bus = voltageLevel.getBusBreakerView().getBus("bus_vl");
        Bus terminal = boundaryLine.getTerminal().getBusBreakerView().getBus();
        assertSame(bus, terminal);
    }

    @Test
    public void testInvalidR() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("r is invalid");
        createDanglingLine("invalid", "invalid", Double.NaN, 1.0, 1.0, 1.0, 1.0, 1.0, "code");
    }

    @Test
    public void testInvalidX() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("x is invalid");
        createDanglingLine("invalid", "invalid", 1.0, Double.NaN, 1.0, 1.0, 1.0, 1.0, "code");
    }

    @Test
    public void testInvalidG() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("g is invalid");
        createDanglingLine("invalid", "invalid", 1.0, 1.0, Double.NaN, 1.0, 1.0, 1.0, "code");
    }

    @Test
    public void testInvalidB() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("b is invalid");
        createDanglingLine("invalid", "invalid", 1.0, 1.0, 1.0, Double.NaN, 1.0, 1.0, "code");
    }

    @Test
    public void testInvalidP0() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("p0 is invalid");
        createDanglingLine("invalid", "invalid", 1.0, 1.0, 1.0, 1.0, Double.NaN, 1.0, "code");
    }

    @Test
    public void testInvalidQ0() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("q0 is invalid");
        createDanglingLine("invalid", "invalid", 1.0, 1.0, 1.0, 1.0, 1.0, Double.NaN, "code");
    }

    @Test
    public void duplicateDanglingLine() {
        createDanglingLine("duplicate", "duplicate", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code");
        assertNotNull(network.getBoundaryLine("duplicate"));
        thrown.expect(PowsyblException.class);
        createDanglingLine("duplicate", "duplicate", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code");
    }

    @Test
    public void testRemove() {
        createDanglingLine("toRemove", "toRemove", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code");
        BoundaryLine boundaryLine = network.getBoundaryLine("toRemove");
        int count = network.getBoundaryLineCount();
        assertNotNull(boundaryLine);
        boundaryLine.remove();
        assertEquals(count - 1, network.getBoundaryLineCount());
        assertNull(network.getBoundaryLine("toRemove"));
        assertNotNull(boundaryLine);
    }

    @Test
    public void testSetterGetterInMultiStates() {
        StateManager stateManager = network.getStateManager();
        createDanglingLine("testMultiState", "testMultiState", 1.0, 1.1, 2.2, 1.0, 1.0, 1.2, "code");
        BoundaryLine boundaryLine = network.getBoundaryLine("testMultiState");
        List<String> statesToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        stateManager.cloneState(StateManagerConstants.INITIAL_STATE_ID, statesToAdd);

        stateManager.setWorkingState("s4");
        // check values cloned by extend
        assertEquals(1.0, boundaryLine.getP0(), 0.0);
        assertEquals(1.2, boundaryLine.getQ0(), 0.0);
        // change values in s4
        boundaryLine.setP0(3.0);
        boundaryLine.setQ0(2.0);

        // remove s2
        stateManager.removeState("s2");

        stateManager.cloneState("s4", "s2b");
        stateManager.setWorkingState("s2b");
        // check values cloned by allocate
        assertEquals(3.0, boundaryLine.getP0(), 0.0);
        assertEquals(2.0, boundaryLine.getQ0(), 0.0);
        // recheck initial state value
        stateManager.setWorkingState(StateManagerConstants.INITIAL_STATE_ID);
        assertEquals(1.0, boundaryLine.getP0(), 0.0);
        assertEquals(1.2, boundaryLine.getQ0(), 0.0);

        // remove working state s4
        stateManager.setWorkingState("s4");
        stateManager.removeState("s4");
        try {
            boundaryLine.getQ0();
            fail();
        } catch (Exception ignored) {
        }
    }

    private void createDanglingLine(String id, String name, double r, double x, double g, double b,
                                    double p0, double q0, String ucteCode) {
        voltageLevel.newBoundaryLine()
                        .setId(id)
                        .setName(name)
                        .setR(r)
                        .setX(x)
                        .setG(g)
                        .setB(b)
                        .setP0(p0)
                        .setQ0(q0)
                        .setUcteXnodeCode(ucteCode)
                        .setBus("bus_vl")
                        .setConnectableBus("bus_vl")
                    .add();
    }

}
