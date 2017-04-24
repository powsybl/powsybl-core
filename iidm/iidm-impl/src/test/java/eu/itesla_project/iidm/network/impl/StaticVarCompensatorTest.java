/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.StaticVarCompensator;
import eu.itesla_project.iidm.network.VoltageLevel;
import eu.itesla_project.iidm.network.test.SvcTestCaseFactory;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

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
}
