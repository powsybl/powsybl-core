/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.HvdcLine;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.VscConverterStation;
import eu.itesla_project.iidm.network.test.HvdcTestNetwork;
import org.junit.Before;
import org.junit.Test;

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
}
