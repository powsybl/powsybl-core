/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class LoadTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    Network network;
    VoltageLevel voltageLevel;

    @Before
    public void initNetwork() {
        network = FictitiousSwitchFactory.create();
        voltageLevel = network.getVoltageLevel("C");
    }

    @Test
    public void testSetterGetter() {
        Load load = network.getLoad("CE");
        load.setP0(-1.0);
        assertEquals(-1.0, load.getP0(), 0.0);
        load.setQ0(-2.0);
        assertEquals(-2.0, load.getQ0(), 0.0);
        load.setP0(1.0);
        assertEquals(1.0, load.getP0(), 0.0);
        load.setQ0(0.0);
        assertEquals(0.0, load.getQ0(), 0.0);
        load.setLoadType(LoadType.AUXILIARY);
        assertEquals(LoadType.AUXILIARY, load.getLoadType());
    }

    @Test
    public void invalidP0() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("p0 is invalid");
        createLoad("invalid", Double.NaN, 1.0);
    }

    @Test
    public void invalidQ0() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("q0 is invalid");
        createLoad("invalid", 20.0, Double.NaN);
    }

    @Test
    public void duplicateEquipment() {
        voltageLevel.newLoad()
                        .setId("duplicate")
                        .setP0(2.0)
                        .setQ0(1.0)
                        .setNode(1)
                    .add();
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("with the id 'duplicate'");
        createLoad("duplicate", 2.0, 1.0);
    }

    @Test
    public void duplicateId() {
        // "C" id of voltageLevel
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("with the id 'C'");
        createLoad("C", 2.0, 1.0);
    }

    @Test
    public void testAdder() {
        Load load = voltageLevel.newLoad()
                        .setId("testAdder")
                        .setP0(2.0)
                        .setQ0(1.0)
                        .setLoadType(LoadType.AUXILIARY)
                        .setNode(1)
                    .add();
        assertEquals(2.0, load.getP0(), 0.0);
        assertEquals(1.0, load.getQ0(), 0.0);
        assertEquals("testAdder", load.getId());
        assertEquals(LoadType.AUXILIARY, load.getLoadType());
    }

    @Test
    public void testRemove() {
        createLoad("toRemove", 2.0, 1.0);
        Load load = network.getLoad("toRemove");
        int loadCount = network.getLoadCount();
        assertNotNull(load);
        load.remove();
        assertNotNull(load);
        assertNull(network.getLoad("toRemove"));
        assertEquals(loadCount - 1, network.getLoadCount());
    }

    @Test
    public void testSetterGetterInMultiStates() {
        StateManager stateManager = network.getStateManager();
        createLoad("testMultiState", 0.6d, 0.7d);
        Load load = network.getLoad("testMultiState");
        List<String> statesToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        stateManager.cloneState(StateManagerConstants.INITIAL_STATE_ID, statesToAdd);

        stateManager.setWorkingState("s4");
        // check values cloned by extend
        assertEquals(0.6d, load.getP0(), 0.0);
        assertEquals(0.7d, load.getQ0(), 0.0);
        // change values in s4
        load.setP0(3.0);
        load.setQ0(2.0);

        // remove s2
        stateManager.removeState("s2");

        stateManager.cloneState("s4", "s2b");
        stateManager.setWorkingState("s2b");
        // check values cloned by allocate
        assertEquals(3.0, load.getP0(), 0.0);
        assertEquals(2.0, load.getQ0(), 0.0);
        // recheck initial state value
        stateManager.setWorkingState(StateManagerConstants.INITIAL_STATE_ID);
        assertEquals(0.6, load.getP0(), 0.0);
        assertEquals(0.7, load.getQ0(), 0.0);

        // remove working state s4
        stateManager.setWorkingState("s4");
        stateManager.removeState("s4");
        try {
            load.getQ0();
            fail();
        } catch (Exception ignored) {
        }
    }

    private void createLoad(String id, double p0, double q0) {
        voltageLevel.newLoad()
                        .setId(id)
                        .setP0(p0)
                        .setQ0(q0)
                        .setNode(1)
                    .add();
    }

}
