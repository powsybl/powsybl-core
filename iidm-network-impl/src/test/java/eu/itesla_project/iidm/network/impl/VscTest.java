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
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class VscTest {

    @Test
    public void testBase() {
        Network network = HvdcTestNetwork.createVsc();
        VscConverterStation cs1 = (VscConverterStation) network.getHvdcConverterStation("C1");
        assertNotNull(cs1);
        VscConverterStation cs2 = (VscConverterStation) network.getHvdcConverterStation("C2");
        assertNotNull(cs2);
        assertTrue(network.getVoltageLevel("VL1").getVscConverterStationCount() == 1);
        assertTrue(network.getVoltageLevel("VL2").getVscConverterStationCount() == 1);
        assertTrue(cs1.isVoltageRegulatorOn());
        assertTrue(cs1.getVoltageSetPoint() == 405f);
        assertTrue(Float.isNaN(cs1.getReactivePowerSetPoint()));
        assertFalse(cs2.isVoltageRegulatorOn());
        assertTrue(cs2.getReactivePowerSetPoint() == 123f);
        assertTrue(Float.isNaN(cs2.getVoltageSetPoint()));
        assertTrue(network.getHvdcLineCount() == 1);
        HvdcLine l = network.getHvdcLine("L");
        assertNotNull(l);
        assertTrue(l.getR() == 1f);
        assertTrue(l.getConvertersMode() == HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER);
        assertTrue(l.getMaxP() == 300f);
        assertTrue(l. getConverterStation1() == cs1);
        assertTrue(l. getConverterStation2() == cs2);
    }

    @Test
    public void testRemove() {
        Network network = HvdcTestNetwork.createVsc();
        network.getHvdcLine("L").remove();
        assertTrue(network.getHvdcLineCount() == 0);
    }
}
