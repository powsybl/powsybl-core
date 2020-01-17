/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StaticVarCompensatorTest {

    private Network network;

    @Before
    public void setUp() {
        network = SvcTestCaseFactory.create();
    }

    @Test
    public void initialVariantTest() {
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        assertNotNull(svc);
        assertEquals(1, network.getStaticVarCompensatorCount());
        assertSame(svc, network.getStaticVarCompensators().iterator().next());
        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        assertEquals(1, vl2.getStaticVarCompensatorCount());
        assertSame(svc, vl2.getStaticVarCompensators().iterator().next());
        assertEquals(0.0002, svc.getBmin(), 0.0);
        assertEquals(0.0008, svc.getBmax(), 0.0);
        assertSame(StaticVarCompensator.RegulationMode.VOLTAGE, svc.getRegulationMode());
        assertEquals(390.0, svc.getVoltageSetPoint(), 0.0);
    }

    @Test
    public void removeTest() {
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        svc.remove();
        svc = network.getStaticVarCompensator("SVC2");
        assertNull(svc);
        assertEquals(0, network.getStaticVarCompensatorCount());
        assertFalse(network.getStaticVarCompensators().iterator().hasNext());
        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        assertEquals(0, vl2.getStaticVarCompensatorCount());
        assertFalse(vl2.getStaticVarCompensators().iterator().hasNext());
    }

    @Test
    public void changeBminTest() {
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        svc.setBmin(0.0003);
        assertEquals(0.0003, svc.getBmin(), 0.0);
    }

    @Test
    public void changeBmaxTest() {
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        svc.setBmax(0.0007);
        assertEquals(0.0007, svc.getBmax(), 0.0);
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
        svc.setReactivePowerSetPoint(200.0);
        svc.setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER);
        assertEquals(200.0, svc.getReactivePowerSetPoint(), 0.0);
        assertSame(StaticVarCompensator.RegulationMode.REACTIVE_POWER, svc.getRegulationMode());
    }

    @Test
    public void changeVoltageSetPointTest() {
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        svc.setVoltageSetPoint(391.0);
        assertEquals(391.0, svc.getVoltageSetPoint(), 0.0);
    }

    @Test
    public void regulatingTerminalTest() {
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        assertEquals(svc.getTerminal(), svc.getRegulatingTerminal());

        Terminal loadTerminal = network.getLoad("L2").getTerminal();
        svc.setRegulatingTerminal(loadTerminal);
        assertEquals(loadTerminal, svc.getRegulatingTerminal());

        StaticVarCompensator svc3 = createSvc("SVC3", null);
        assertEquals(svc3.getTerminal(), svc3.getRegulatingTerminal());

        svc3.remove();
        StaticVarCompensator svc4 = createSvc("SVC4", loadTerminal);
        assertEquals(loadTerminal, svc4.getRegulatingTerminal());
    }

    @Test
    public void testSetterGetterInMultiVariants() {
        VariantManager variantManager = network.getVariantManager();
        createSvc("testMultiVariant", null);
        StaticVarCompensator svc = network.getStaticVarCompensator("testMultiVariant");
        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check values cloned by extend
        assertEquals(1.0, svc.getReactivePowerSetPoint(), 0.0);
        assertEquals(StaticVarCompensator.RegulationMode.VOLTAGE, svc.getRegulationMode());
        assertEquals(390.0, svc.getVoltageSetPoint(), 0.0);
        // change values in s4
        svc.setReactivePowerSetPoint(3.0);
        svc.setRegulationMode(StaticVarCompensator.RegulationMode.REACTIVE_POWER);
        svc.setVoltageSetPoint(440.0);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertEquals(3.0, svc.getReactivePowerSetPoint(), 0.0);
        assertEquals(StaticVarCompensator.RegulationMode.REACTIVE_POWER, svc.getRegulationMode());
        assertEquals(440.0, svc.getVoltageSetPoint(), 0.0);

        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(1.0, svc.getReactivePowerSetPoint(), 0.0);
        assertEquals(StaticVarCompensator.RegulationMode.VOLTAGE, svc.getRegulationMode());
        assertEquals(390.0, svc.getVoltageSetPoint(), 0.0);

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        try {
            svc.getReactivePowerSetPoint();
            fail();
        } catch (Exception ignored) {
        }
    }

    private StaticVarCompensator createSvc(String id, Terminal regulatingTerminal) {
        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        return vl2.newStaticVarCompensator()
                .setId(id)
                .setConnectableBus("B2")
                .setBus("B2")
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetPoint(390.0)
                .setReactivePowerSetPoint(1.0)
                .setRegulatingTerminal(regulatingTerminal)
                .add();
    }
}
