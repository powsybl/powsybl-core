/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.HvdcLine;
import eu.itesla_project.iidm.network.LccConverterStation;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.test.HvdcTestNetwork;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class LccTest {

    @Test
    public void testBase() {
        Network network = HvdcTestNetwork.createLcc();
        LccConverterStation cs1 = (LccConverterStation) network.getHvdcConverterStation("C1");
        assertNotNull(cs1);
        LccConverterStation cs2 = (LccConverterStation) network.getHvdcConverterStation("C2");
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
        assertEquals(2, network.getVoltageLevel("VL1").getShuntCount());
        assertEquals(2, network.getVoltageLevel("VL2").getShuntCount());
        assertEquals(1e-5f, network.getShunt("C1_Filter1").getCurrentB(), 0.0f);
        assertTrue(network.getShunt("C1_Filter1").getTerminal().isConnected());
        assertEquals(0.0f, network.getShunt("C1_Filter2").getCurrentB(), 0.0f);
        assertEquals(2e-5f, network.getShunt("C1_Filter2").getMaximumB(), 0.0f);
        assertFalse(network.getShunt("C1_Filter2").getTerminal().isConnected());
        assertEquals(1, network.getHvdcLineCount());
        HvdcLine l = network.getHvdcLine("L");
        assertNotNull(l);
        assertEquals(1.0f, l.getR(), 0.0f);
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, l.getConvertersMode());
        assertEquals(300f, l.getMaxP(), 0.0f);
        assertEquals(cs1, l.getConverterStation1());
        assertEquals(cs2, l.getConverterStation2());
    }

    @Test
    public void testHvdcLineRemove() {
        Network network = HvdcTestNetwork.createLcc();
        network.getHvdcLine("L").remove();
        assertEquals(0, network.getHvdcLineCount());
    }
}
