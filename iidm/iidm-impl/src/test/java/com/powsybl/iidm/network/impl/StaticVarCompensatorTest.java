/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StateManager;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StaticVarCompensatorTest {

    private Network network;

    @Before
    public void setUp() throws Exception {
        network = SvcTestCaseFactory.create();
    }

    @After
    public void tearDown() throws Exception {
        network = null;
    }

    @Test
    public void initialStateTest() {
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        assertNotNull(svc);
        assertTrue(network.getStaticVarCompensatorCount() == 1);
        assertTrue(network.getStaticVarCompensators().iterator().next() == svc);
        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        assertTrue(vl2.getStaticVarCompensatorCount() == 1);
        assertTrue(vl2.getStaticVarCompensators().iterator().next() == svc);
        assertTrue(svc.getBmin() == 0.0002f);
        assertTrue(svc.getBmax() == 0.0008f);
        assertTrue(svc.getRegulationMode() == StaticVarCompensator.RegulationMode.VOLTAGE);
        assertTrue(svc.getVoltageSetPoint() == 390f);
    }

    @Test
    public void removeTest() {
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        svc.remove();
        svc = network.getStaticVarCompensator("SVC2");
        assertNull(svc);
        assertTrue(network.getStaticVarCompensatorCount() == 0);
        assertFalse(network.getStaticVarCompensators().iterator().hasNext());
        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        assertTrue(vl2.getStaticVarCompensatorCount() == 0);
        assertFalse(vl2.getStaticVarCompensators().iterator().hasNext());
    }

    @Test
    public void changeBminTest() {
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        svc.setBmin(0.0003f);
        assertTrue(svc.getBmin() == 0.0003f);
    }

    @Test
    public void changeBmaxTest() {
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        svc.setBmax(0.0007f);
        assertTrue(svc.getBmax() == 0.0007f);
    }

    @Test
    public void changeRegulationModeErrorTest() {
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        try {
            svc.setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER);
            fail();
        } catch (Exception ignored) {
        }
    }

    @Test
    public void changeRegulationModeSuccessTest() {
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        svc.setReactivePowerSetPoint(200f);
        svc.setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER);
        assertTrue(svc.getReactivePowerSetPoint() == 200f);
        assertTrue(svc.getRegulationMode() == StaticVarCompensator.RegulationMode.REACTIVE_POWER);
    }

    @Test
    public void changeVoltageSetPointTest() {
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        svc.setVoltageSetPoint(391f);
        assertTrue(svc.getVoltageSetPoint() == 391f);
    }

    @Test
    public void testSetterGetterInMultiStates() {
        StateManager stateManager = network.getStateManager();
        createSvc("testMultiState");
        StaticVarCompensator svc = network.getStaticVarCompensator("testMultiState");
        List<String> statesToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        stateManager.cloneState(StateManager.INITIAL_STATE_ID, statesToAdd);

        stateManager.setWorkingState("s4");
        // check values cloned by extend
        assertEquals(1.0f, svc.getReactivePowerSetPoint(), 0.0f);
        assertEquals(StaticVarCompensator.RegulationMode.VOLTAGE, svc.getRegulationMode());
        assertEquals(390.0f, svc.getVoltageSetPoint(), 0.0f);
        // change values in s4
        svc.setReactivePowerSetPoint(3.0f);
        svc.setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER);
        svc.setVoltageSetPoint(440.0f);

        // remove s2
        stateManager.removeState("s2");

        stateManager.cloneState("s4", "s2b");
        stateManager.setWorkingState("s2b");
        // check values cloned by allocate
        assertEquals(3.0f, svc.getReactivePowerSetPoint(), 0.0f);
        assertEquals(StaticVarCompensator.RegulationMode.REACTIVE_POWER, svc.getRegulationMode());
        assertEquals(440.0f, svc.getVoltageSetPoint(), 0.0f);

        // recheck initial state value
        stateManager.setWorkingState(StateManager.INITIAL_STATE_ID);
        assertEquals(1.0f, svc.getReactivePowerSetPoint(), 0.0f);
        assertEquals(StaticVarCompensator.RegulationMode.VOLTAGE, svc.getRegulationMode());
        assertEquals(390.0f, svc.getVoltageSetPoint(), 0.0f);

        // remove working state s4
        stateManager.setWorkingState("s4");
        stateManager.removeState("s4");
        try {
            svc.getReactivePowerSetPoint();
            fail();
        } catch (Exception ignored) {
        }
    }

    private void createSvc(String id) {
        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        vl2.newStaticVarCompensator()
                .setId(id)
                .setConnectableBus("B2")
                .setBus("B2")
                .setBmin(0.0002f)
                .setBmax(0.0008f)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetPoint(390f)
                .setReactivePowerSetPoint(1.0f)
            .add();
    }
}
