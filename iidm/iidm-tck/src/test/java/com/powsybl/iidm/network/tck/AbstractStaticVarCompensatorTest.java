/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractStaticVarCompensatorTest {

    private Network network;

    @BeforeEach
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
        assertSame(RegulationMode.VOLTAGE, svc.getVoltageRegulation().getMode());
        assertEquals(390.0, svc.getVoltageRegulation().getTargetValue(), 0.0);
        assertEquals(Double.NaN, svc.getTargetQ());
        assertEquals(Double.NaN, svc.getTargetV());
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
            svc.getVoltageRegulation().setRegulating(false);
            svc.getVoltageRegulation().setTargetValue(Double.NaN);
            svc.getVoltageRegulation().setMode(RegulationMode.REACTIVE_POWER);
            svc.getVoltageRegulation().setRegulating(true);
            fail();
        } catch (Exception ignored) {
            // ignore
        }
    }

    @Test
    public void changeRegulationModeSuccessTest() {
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        svc.getVoltageRegulation().setMode(RegulationMode.REACTIVE_POWER);
        svc.getVoltageRegulation().setTargetValue(200.0);
        assertEquals(200.0, svc.getVoltageRegulation().getTargetValue(), 0.0);
        assertEquals(200.0, svc.getRegulatingTargetQ(), 0.0);
        assertSame(RegulationMode.REACTIVE_POWER, svc.getVoltageRegulation().getMode());
    }

    @Test
    public void changeVoltageSetpointTest() {
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        svc.getVoltageRegulation().setTargetValue(391.0);
        assertEquals(391.0, svc.getRegulatingTargetV(), 0.0);
        assertEquals(391.0, svc.getVoltageRegulation().getTargetValue(), 0.0);
    }

    @Test
    public void changeSlopeTest() {
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        VoltageRegulation voltageRegulation = svc.getVoltageRegulation();

        voltageRegulation.setSlope(0.3);
        assertEquals(0.3, voltageRegulation.getSlope(), 0.0);

        var previousMode = voltageRegulation.getMode();
        voltageRegulation.setMode(RegulationMode.VOLTAGE_PER_REACTIVE_POWER);

        assertThrows(ValidationException.class, () -> voltageRegulation.setSlope(Double.NaN));
        assertFalse(Double.isNaN(voltageRegulation.getSlope()));

        voltageRegulation.setMode(previousMode);
        voltageRegulation.setSlope(Double.NaN);
        assertTrue(Double.isNaN(voltageRegulation.getSlope()));
    }

    @Test
    public void regulatingTerminalTest() {
        StaticVarCompensator svc = network.getStaticVarCompensator("SVC2");
        assertEquals(svc.getTerminal(), svc.getRegulatingTerminal());

        Terminal loadTerminal = network.getLoad("L2").getTerminal();
        svc.getVoltageRegulation().setTerminal(loadTerminal);
        assertEquals(loadTerminal, svc.getRegulatingTerminal());

        svc.getVoltageRegulation().removeTerminal();
        assertEquals(svc.getTerminal(), svc.getRegulatingTerminal());

        StaticVarCompensator svc3 = createSvc("SVC3", null, RegulationMode.VOLTAGE);
        assertEquals(svc3.getTerminal(), svc3.getRegulatingTerminal());

        svc3.remove();
        StaticVarCompensator svc4 = createSvc("SVC4", loadTerminal, RegulationMode.VOLTAGE);
        assertEquals(loadTerminal, svc4.getRegulatingTerminal());

        network.getLoad("L2").remove();
        assertEquals(svc4.getTerminal(), svc4.getRegulatingTerminal());
        assertEquals(RegulationMode.VOLTAGE, svc4.getVoltageRegulation().getMode());

        Terminal lineTerminal = network.getLine("L1").getTerminal2();
        StaticVarCompensator svc5 = createSvc("SVC5", lineTerminal, RegulationMode.REACTIVE_POWER);
        assertEquals(lineTerminal, svc5.getRegulatingTerminal());
        assertEquals(RegulationMode.REACTIVE_POWER, svc5.getVoltageRegulation().getMode());
        lineTerminal.getConnectable().remove();
        assertEquals(svc5.getTerminal(), svc5.getRegulatingTerminal());
        assertEquals(RegulationMode.REACTIVE_POWER, svc5.getVoltageRegulation().getMode()); // TODO MSA check that why not keeping the old mode ??
    }

    @Test
    public void testSetterGetterInMultiVariants() {
        VariantManager variantManager = network.getVariantManager();
        createSvc("testMultiVariant", null, RegulationMode.VOLTAGE);
        StaticVarCompensator svc = network.getStaticVarCompensator("testMultiVariant");
        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check values cloned by extend
        assertEquals(1.0, svc.getTargetQ(), 0.0);
        assertEquals(RegulationMode.VOLTAGE, svc.getVoltageRegulation().getMode());
        assertEquals(123, svc.getRegulatingTargetV(), 0.0);
        assertEquals(390.0, svc.getTargetV(), 0.0);
        // change values in s4
        svc.setTargetQ(3.0);
        svc.setTargetV(44.0);
        svc.getVoltageRegulation().setMode(RegulationMode.REACTIVE_POWER);
        svc.getVoltageRegulation().setTargetValue(440.0);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertEquals(3.0, svc.getTargetQ(), 0.0);
        assertEquals(RegulationMode.REACTIVE_POWER, svc.getVoltageRegulation().getMode());
        assertEquals(440.0, svc.getVoltageRegulation().getTargetValue(), 0.0);
        assertEquals(440.0, svc.getRegulatingTargetQ(), 0.0);
        assertEquals(44.0, svc.getTargetV(), 0.0);

        // recheck initial variant value
        assertEquals(RegulationMode.REACTIVE_POWER, svc.getVoltageRegulation().getMode());
        svc.getVoltageRegulation().setMode(RegulationMode.VOLTAGE);
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(1.0, svc.getTargetQ(), 0.0);
        assertEquals(RegulationMode.VOLTAGE, svc.getVoltageRegulation().getMode());
        assertEquals(123, svc.getVoltageRegulation().getTargetValue(), 0.0);
        assertEquals(123, svc.getRegulatingTargetV(), 0.0);
        assertEquals(390.0, svc.getTargetV(), 0.0);

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        PowsyblException exception = assertThrows(PowsyblException.class, svc::getTargetQ);
        assertEquals("Variant index not set", exception.getMessage());
    }

    private StaticVarCompensator createSvc(String id, Terminal regulatingTerminal, RegulationMode regulationMode) {
        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        return vl2.newStaticVarCompensator()
                .setId(id)
                .setConnectableBus("B2")
                .setBus("B2")
                .setBmin(0.0002)
                .setBmax(0.0008)
                .newVoltageRegulation()
                    .withMode(regulationMode)
                    .withTargetValue(123)
                    .withTerminal(regulatingTerminal)
                    .add()
                .setTargetV(390.0)
                .setTargetQ(1.0)
                .add();
    }
}
