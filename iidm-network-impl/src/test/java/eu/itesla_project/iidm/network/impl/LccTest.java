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
        assertTrue(network.getVoltageLevel("VL1").getLccConverterStationCount() == 1);
        assertTrue(network.getVoltageLevel("VL2").getLccConverterStationCount() == 1);
        assertTrue(cs1.getPowerFactor() == 0.5f);
        assertTrue(cs2.getPowerFactor() == 0.6f);
        assertTrue(cs1.getFilterCount() == 2);
        assertTrue(cs1.getFilterAt(0).getB() == 0.00001f);
        assertTrue(cs1.getFilterAt(0).isConnected());
        assertTrue(cs1.getFilterAt(1).getB() == 0.00002f);
        assertFalse(cs1.getFilterAt(1).isConnected());
        assertTrue(cs2.getFilterCount() == 2);
        assertTrue(cs2.getFilterAt(0).getB() == 0.00003f);
        assertTrue(cs2.getFilterAt(0).isConnected());
        assertTrue(cs2.getFilterAt(1).getB() == 0.00004f);
        assertTrue(cs2.getFilterAt(1).isConnected());
        assertTrue(network.getHvdcLineCount() == 1);
        HvdcLine l = network.getHvdcLine("L");
        assertNotNull(l);
        assertTrue(l.getR() == 1);
        assertTrue(l.getConvertersMode() == HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER);
        assertTrue(l.getMaxP() == 300);
        assertTrue(l. getConverterStation1() == cs1);
        assertTrue(l. getConverterStation2() == cs2);
    }

    @Test
    public void testHvdcLineRemove() {
        Network network = HvdcTestNetwork.createLcc();
        network.getHvdcLine("L").remove();
        assertTrue(network.getHvdcLineCount() == 0);
    }

    @Test
    public void testFilterRemove() {
        Network network = HvdcTestNetwork.createLcc();
        LccConverterStation cs1 = (LccConverterStation) network.getHvdcConverterStation("C1");
        cs1.removeFilterAt(0);
        assertTrue(cs1.getFilterCount() == 1);
        assertTrue(cs1.getFilterAt(0).getB() == 0.00002f);
        assertFalse(cs1.getFilterAt(0).isConnected());
    }

    @Test
    public void testAddInvalidFilter() {
        try {
            Network network = HvdcTestNetwork.createLcc();
            LccConverterStation cs1 = (LccConverterStation) network.getHvdcConverterStation("C1");
            cs1.newFilter().setB(1.0f).add();
            fail();
        } catch (ValidationException exc) {
        }

        try {
            Network network = HvdcTestNetwork.createLcc();
            LccConverterStation cs1 = (LccConverterStation) network.getHvdcConverterStation("C1");
            cs1.newFilter().setConnected(true).add();
            fail();
        } catch (ValidationException exc) {
        }
    }
}
