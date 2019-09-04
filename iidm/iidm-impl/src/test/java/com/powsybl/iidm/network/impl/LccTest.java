/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class LccTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;
    private LccConverterStation cs1;
    private LccConverterStation cs2;

    @Before
    public void setUp() {
        network = HvdcTestNetwork.createLcc();
        cs1 = network.getLccConverterStation("C1");
        cs2 = network.getLccConverterStation("C2");
    }

    @Test
    public void testBase() {
        assertNotNull(cs1);
        assertNotNull(cs2);
        assertEquals(1, network.getVoltageLevel("VL1").getLccConverterStationCount());
        assertEquals(1, network.getVoltageLevel("VL2").getLccConverterStationCount());
        assertEquals(0.011f, cs1.getLossFactor(), 0.0f);
        assertEquals(0.011f, cs2.getLossFactor(), 0.0f);
        cs1.setLossFactor(0.022f);
        assertEquals(0.022f, cs1.getLossFactor(), 0.0f);
        assertEquals(0.5f, cs1.getPowerFactor(), 0.0f);
        assertEquals(0.6f, cs2.getPowerFactor(), 0.0f);
        cs1.setPowerFactor(0.6f);
        assertEquals(0.6f, cs1.getPowerFactor(), 0.0f);
        assertEquals(2, network.getVoltageLevel("VL1").getShuntCompensatorCount());
        assertEquals(2, network.getVoltageLevel("VL2").getShuntCompensatorCount());
        assertEquals(1e-5, network.getShuntCompensator("C1_Filter1").getCurrentB(), 0.0);
        assertTrue(network.getShuntCompensator("C1_Filter1").getTerminal().isConnected());
        assertEquals(0.0, network.getShuntCompensator("C1_Filter2").getCurrentB(), 0.0);
        assertEquals(2e-5, network.getShuntCompensator("C1_Filter2").getMaximumB(), 0.0);
        assertFalse(network.getShuntCompensator("C1_Filter2").getTerminal().isConnected());
        assertEquals(1, network.getHvdcLineCount());
        HvdcLine l = network.getHvdcLine("L");
        assertNotNull(l);
        assertEquals(1.0, l.getR(), 0.0);
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, l.getConvertersMode());
        assertEquals(300.0, l.getMaxP(), 0.0);
        assertEquals(cs1, l.getConverterStation1());
        assertEquals(cs2, l.getConverterStation2());

        // remove
        int count = network.getLccConverterStationCount();
        cs1.remove();
        assertNotNull(cs1);
        assertNull(network.getLccConverterStation("C1"));
        assertEquals(count - 1, network.getLccConverterStationCount());
    }

    @Test
    public void testHvdcLineRemove() {
        network.getHvdcLine("L").remove();
        assertEquals(0, network.getHvdcLineCount());
    }

    @Test
    public void invalidLossFactor() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("loss factor is invalid");
        cs1.setLossFactor(Float.NaN);
    }

    @Test
    public void invalidNegativeLossFactor() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("loss factor must be >= 0");
        cs1.setLossFactor(-1.0f);
    }

    @Test
    public void invalidPowerFactor() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("power factor is invalid");
        cs1.setPowerFactor(Float.NaN);
    }
}
