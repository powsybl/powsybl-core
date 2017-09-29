/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class VscTest {

    private Network network;
    private VscConverterStation cs1;
    private VscConverterStation cs2;

    @Before
    public void setUp() {
        network = HvdcTestNetwork.createVsc();
        cs1 = network.getVscConverterStation("C1");
        cs2 = network.getVscConverterStation("C2");
    }

    @Test
    public void testBase() {
        assertNotNull(cs1);
        assertNotNull(cs2);
        assertEquals(HvdcConverterStation.HvdcType.VSC, cs1.getHvdcType());
        assertEquals(1, network.getVoltageLevel("VL1").getVscConverterStationCount());
        assertEquals(1, network.getVoltageLevel("VL2").getVscConverterStationCount());
        assertEquals(0.011f, cs1.getLossFactor(), 0.0f);
        assertEquals(0.011f, cs2.getLossFactor(), 0.0f);
        cs1.setLossFactor(0.022f);
        assertEquals(0.022f, cs1.getLossFactor(), 0.0f);
        assertTrue(cs1.isVoltageRegulatorOn());
        assertEquals(405f, cs1.getVoltageSetpoint(), 0.0f);
        cs1.setVoltageSetpoint(406f);
        assertEquals(406f, cs1.getVoltageSetpoint(), 0.0f);
        assertTrue(Float.isNaN(cs1.getReactivePowerSetpoint()));
        assertEquals(0.011f, cs2.getLossFactor(), 0.0f);
        assertFalse(cs2.isVoltageRegulatorOn());
        assertEquals(123f, cs2.getReactivePowerSetpoint(), 0.0f);
        cs2.setReactivePowerSetpoint(124f);
        assertEquals(124f, cs2.getReactivePowerSetpoint(), 0.0f);
        assertTrue(Float.isNaN(cs2.getVoltageSetpoint()));
        cs2.setVoltageSetpoint(405f);
        cs2.setVoltageRegulatorOn(true);
        assertTrue(cs2.isVoltageRegulatorOn());
        assertEquals(1, network.getHvdcLineCount());
        HvdcLine l = network.getHvdcLine("L");
        assertNotNull(l);
        assertEquals(1f, l.getR(), 0.0f);
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, l.getConvertersMode());
        assertEquals(300f, l.getMaxP(), 0.0f);
        assertEquals(cs1, l.getConverterStation1());
        assertEquals(cs2, l.getConverterStation2());
        assertTrue(l.getConverterStation1().getTerminal().getBusView().getBus().isInMainConnectedComponent());
        assertTrue(l.getConverterStation2().getTerminal().getBusView().getBus().isInMainConnectedComponent());
        assertNotEquals(l.getConverterStation1().getTerminal().getBusView().getBus().getSynchronousComponent().getNum(),
                        l.getConverterStation2().getTerminal().getBusView().getBus().getSynchronousComponent().getNum());

        // remove
        int count = network.getVscConverterStationCount();
        cs1.remove();
        assertNull(network.getVscConverterStation("C1"));
        assertNotNull(cs1);
        assertEquals(count - 1, network.getVscConverterStationCount());
    }

    @Test
    public void testRemove() {
        network.getHvdcLine("L").remove();
        assertEquals(0, network.getHvdcLineCount());
    }

    @Test
    public void testReactiveLimits() {
        cs1.newMinMaxReactiveLimits()
                .setMinQ(10.0f)
                .setMaxQ(100.0f)
            .add();
        assertEquals(100.0f, cs1.getReactiveLimits().getMaxQ(2.0f), 0.0f);
        try {
            cs1.getReactiveLimits(ReactiveCapabilityCurveImpl.class);
            fail();
        } catch (Exception ignored) {
        }
        cs1.getReactiveLimits(MinMaxReactiveLimitsImpl.class);
    }

    @Test
    public void testSetterGetterInMultiStates() {
        StateManager stateManager = network.getStateManager();
        List<String> statesToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        stateManager.cloneState(StateManager.INITIAL_STATE_ID, statesToAdd);

        stateManager.setWorkingState("s4");
        // check values cloned by extend
        assertTrue(cs1.isVoltageRegulatorOn());
        assertTrue(Float.isNaN(cs1.getReactivePowerSetpoint()));
        assertEquals(405f, cs1.getVoltageSetpoint(), 0.0f);
        // change values in s4
        cs1.setReactivePowerSetpoint(1.0f);
        cs1.setVoltageRegulatorOn(false);
        cs1.setVoltageSetpoint(10.0f);

        // remove s2
        stateManager.removeState("s2");

        stateManager.cloneState("s4", "s2b");
        stateManager.setWorkingState("s2b");
        // check values cloned by allocate
        assertFalse(cs1.isVoltageRegulatorOn());
        assertEquals(1.0f, cs1.getReactivePowerSetpoint(), 0.0f);
        assertEquals(10.0f, cs1.getVoltageSetpoint(), 0.0f);

        // recheck initial state value
        stateManager.setWorkingState(StateManager.INITIAL_STATE_ID);
        assertTrue(cs1.isVoltageRegulatorOn());
        assertTrue(Float.isNaN(cs1.getReactivePowerSetpoint()));
        assertEquals(405f, cs1.getVoltageSetpoint(), 0.0f);

        // remove working state s4
        stateManager.setWorkingState("s4");
        stateManager.removeState("s4");
        try {
            cs1.isVoltageRegulatorOn();
            fail();
        } catch (Exception ignored) {
        }
    }
}
