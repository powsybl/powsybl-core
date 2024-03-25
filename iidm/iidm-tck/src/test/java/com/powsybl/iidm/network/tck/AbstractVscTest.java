/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public abstract class AbstractVscTest {

    private Network network;
    private HvdcLine hvdcLine;
    protected VscConverterStation cs1;
    private VscConverterStation cs2;

    @BeforeEach
    public void setUp() {
        network = HvdcTestNetwork.createVsc();
        hvdcLine = network.getHvdcLine("L");
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
        assertEquals(1.1f, cs1.getLossFactor(), 0.0f);
        assertEquals(1.1f, cs2.getLossFactor(), 0.0f);
        cs1.setLossFactor(2.2f);
        assertEquals(2.2f, cs1.getLossFactor(), 0.0f);
        assertTrue(cs1.isVoltageRegulatorOn());
        assertEquals(405.0, cs1.getVoltageSetpoint(), 0.0);
        cs1.setVoltageSetpoint(406.0);
        assertEquals(406.0, cs1.getVoltageSetpoint(), 0.0);
        assertTrue(Double.isNaN(cs1.getReactivePowerSetpoint()));
        assertEquals(1.1f, cs2.getLossFactor(), 0.0f);
        assertFalse(cs2.isVoltageRegulatorOn());
        assertEquals(123.0, cs2.getReactivePowerSetpoint(), 0.0);
        cs2.setReactivePowerSetpoint(124.0);
        assertEquals(124.0, cs2.getReactivePowerSetpoint(), 0.0);
        assertTrue(Double.isNaN(cs2.getVoltageSetpoint()));
        cs2.setVoltageSetpoint(405);
        cs2.setVoltageRegulatorOn(true);
        assertTrue(cs2.isVoltageRegulatorOn());
        assertEquals(1, network.getHvdcLineCount());
        HvdcLine l = network.getHvdcLine("L");
        assertNotNull(l);
        assertEquals(1.0, l.getR(), 0.0);
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, l.getConvertersMode());
        assertEquals(300.0, l.getMaxP(), 0.0);
        assertEquals(cs1, l.getConverterStation1());
        assertEquals(cs2, l.getConverterStation2());
        assertTrue(l.getConverterStation1().getTerminal().getBusView().getBus().isInMainConnectedComponent());
        assertTrue(l.getConverterStation2().getTerminal().getBusView().getBus().isInMainConnectedComponent());
        assertNotEquals(l.getConverterStation1().getTerminal().getBusView().getBus().getSynchronousComponent().getNum(),
                l.getConverterStation2().getTerminal().getBusView().getBus().getSynchronousComponent().getNum());

        assertSame(hvdcLine, cs1.getHvdcLine());
        assertSame(hvdcLine, cs2.getHvdcLine());
        assertSame(cs1, hvdcLine.getConverterStation1());
        assertSame(cs1, hvdcLine.getConverterStation(TwoSides.ONE));
        assertSame(cs2, hvdcLine.getConverterStation2());
        assertSame(cs2, hvdcLine.getConverterStation(TwoSides.TWO));

        assertEquals(2, hvdcLine.getConverterStation1().getTerminal().getBusView().getBus().getConnectedComponent().getBusStream().count());
        assertEquals(2, hvdcLine.getConverterStation1().getTerminal().getBusView().getBus().getConnectedComponent().getSize());
        assertTrue(hvdcLine.getConverterStation1().getTerminal().getBusView().getBus().getConnectedComponent().getBuses().iterator().hasNext());
        assertEquals(1, hvdcLine.getConverterStation1().getTerminal().getBusView().getBus().getSynchronousComponent().getBusStream().count());
        assertEquals(1, hvdcLine.getConverterStation1().getTerminal().getBusView().getBus().getSynchronousComponent().getSize());
        assertTrue(hvdcLine.getConverterStation1().getTerminal().getBusView().getBus().getSynchronousComponent().getBuses().iterator().hasNext());

        if (cs1.getOtherConverterStation().isPresent()) {
            assertEquals(cs2, cs1.getOtherConverterStation().get());
        }
        if (cs2.getOtherConverterStation().isPresent()) {
            assertEquals(cs1, cs2.getOtherConverterStation().get());
        }
    }

    @Test
    public void testRemove() {
        try {
            cs1.remove();
            fail();
        } catch (ValidationException e) {
            // Ignored
        }

        network.getHvdcLine("L").remove();
        assertEquals(0, network.getHvdcLineCount());

        assertNull(cs1.getHvdcLine());
        assertNull(cs2.getHvdcLine());

        // remove
        int count = network.getVscConverterStationCount();
        cs1.remove();
        assertNotNull(cs1);
        assertNull(network.getVscConverterStation("C1"));
        assertEquals(count - 1L, network.getVscConverterStationCount());
    }

    @Test
    public void testSetterGetterInMultiVariants() {
        VariantManager variantManager = network.getVariantManager();
        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check values cloned by extend
        assertTrue(cs1.isVoltageRegulatorOn());
        assertTrue(Double.isNaN(cs1.getReactivePowerSetpoint()));
        assertEquals(405.0, cs1.getVoltageSetpoint(), 0.0);
        // change values in s4
        cs1.setReactivePowerSetpoint(1.0);
        cs1.setVoltageRegulatorOn(false);
        cs1.setVoltageSetpoint(10.0);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertFalse(cs1.isVoltageRegulatorOn());
        assertEquals(1.0, cs1.getReactivePowerSetpoint(), 0.0);
        assertEquals(10.0, cs1.getVoltageSetpoint(), 0.0);

        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertTrue(cs1.isVoltageRegulatorOn());
        assertTrue(Double.isNaN(cs1.getReactivePowerSetpoint()));
        assertEquals(405.0, cs1.getVoltageSetpoint(), 0.0);

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        try {
            cs1.isVoltageRegulatorOn();
            fail();
        } catch (Exception ignored) {
            // ignore
        }
    }

    @Test
    public void testRegulatingTerminal() {
        Terminal cs2Terminal = cs2.getTerminal();
        assertEquals(cs1.getTerminal(), cs1.getRegulatingTerminal());
        cs1.setRegulatingTerminal(cs2Terminal);
        assertEquals(cs2Terminal, cs1.getRegulatingTerminal());
        cs1.setRegulatingTerminal(null);
        assertEquals(cs1.getTerminal(), cs1.getRegulatingTerminal());
    }

    @Test
    public void testVscConverterStationAdder() {
        VscConverterStationAdder adder = network.getVoltageLevel("VL1").newVscConverterStation();
        adder.setId("C3")
                .setReactivePowerSetpoint(123)
                .setVoltageSetpoint(405)
                .setVoltageRegulatorOn(true)
                .setRegulatingTerminal(cs2.getTerminal())
                .setConnectableBus("B1")
                .setLossFactor((float) 1.1)
                .add();

        assertEquals(123, network.getVscConverterStation("C3").getReactivePowerSetpoint(), 0);
        assertEquals(405, network.getVscConverterStation("C3").getVoltageSetpoint(), 0);
        assertTrue(network.getVscConverterStation("C3").isVoltageRegulatorOn());
        assertEquals(1.1, network.getVscConverterStation("C3").getLossFactor(), 0.01);
        assertEquals(cs2.getTerminal(), network.getVscConverterStation("C3").getRegulatingTerminal());

    }
}
