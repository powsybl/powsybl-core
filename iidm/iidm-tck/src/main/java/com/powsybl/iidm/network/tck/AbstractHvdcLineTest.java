/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public abstract class AbstractHvdcLineTest {

    private static final String INVALID = "invalid";

    private static final String DUPLICATE = "duplicate";

    private static final String INVLID = "invlid";

    private static final String NON_EXISTING = "non_existing";

    private static final String TEST_MULTI_VARIANT = "testMultiVariant";

    private static final String TO_REMOVE = "toRemove";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    Network network;

    @Before
    public void initNetwork() {
        network = HvdcTestNetwork.createLcc();
    }

    @Test
    public void testHvdcLineOneVariantAttributes() {
        HvdcLine l = network.getHvdcLine("L");
        assertNotNull(l);
        assertEquals(1, l.getR(), 0.0);
        assertEquals(400.0, l.getNominalV(), 0.0);
        assertEquals(300, l.getMaxP(), 0.0);
        l.setR(2);
        assertEquals(2, l.getR(), 0.0);
        l.setNominalV(220.0);
        assertEquals(220.0, l.getNominalV(), 0.0);
        l.setMaxP(1.11);
        assertEquals(1.11, l.getMaxP(), 0.0);
        l.setActivePowerSetpoint(421.0);
        assertEquals(421.0, l.getActivePowerSetpoint(), 0.0);
        l.setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER);
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, l.getConvertersMode());
    }

    @Test
    public void testAdder() {
        HvdcLine hvdcLine = network.newHvdcLine()
                                        .setId("hvdc_line")
                                        .setName("hvdcLine")
                                        .setR(5.0)
                                        .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                                        .setNominalV(440.0)
                                        .setMaxP(-50.0)
                                        .setActivePowerSetpoint(20.0)
                                        .setConverterStationId1("C1")
                                        .setConverterStationId2("C2")
                                    .add();
        assertNotNull(hvdcLine);
        assertEquals("hvdc_line", hvdcLine.getId());
        assertEquals("hvdcLine", hvdcLine.getName());
        assertEquals(5.0, hvdcLine.getR(), 0.0);
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, hvdcLine.getConvertersMode());
        assertEquals(440.0, hvdcLine.getNominalV(), 0.0);
        assertEquals(-50.0, hvdcLine.getMaxP(), 0.0);
        assertEquals(20.0, hvdcLine.getActivePowerSetpoint(), 0.0);
        assertSame(network.getHvdcConverterStation("C1"), hvdcLine.getConverterStation1());
        assertSame(network.getHvdcConverterStation("C2"), hvdcLine.getConverterStation2());
    }

    @Test
    public void invalidR() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("r is invalid");
        createHvdcLine(INVLID, INVALID, Double.NaN, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                440.0, 10.0, 20.0, "C1", "C2");
    }

    @Test
    public void invalidConvertersMode() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("converter mode is invalid");
        createHvdcLine(INVLID, INVALID, 10.0, null,
                440.0, 10.0, 20.0, "C1", "C2");
    }

    @Test
    public void invalidNominalV() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("nominal voltage is invalid");
        createHvdcLine(INVLID, INVALID, 10.0, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                0.0, 10.0, 20.0, "C1", "C2");
    }

    @Test
    public void invalidActivePowerSetpoint() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for active power setpoint");
        createHvdcLine(INVLID, INVALID, 10.0, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                510.0, Double.NaN, 20.0, "C1", "C2");
    }

    @Test
    public void invalidMaxP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for maximum P");
        createHvdcLine(INVLID, INVALID, 10.0, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                510.0, 77.0, Double.NaN, "C1", "C2");
    }

    @Test
    public void nonExistingConverterStationSide1() {
        HvdcConverterStation<?> nonExistingConverterStation = network.getHvdcConverterStation(NON_EXISTING);
        assertNull(nonExistingConverterStation);
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Side 1 converter station");
        createHvdcLine(INVLID, INVALID, 10.0, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                510.0, 77.0, 22.0, NON_EXISTING, "C2");
    }

    @Test
    public void nonExistingConverterStationSide2() {
        HvdcConverterStation<?> nonExistingConverterStation = network.getHvdcConverterStation(NON_EXISTING);
        assertNull(nonExistingConverterStation);
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Side 2 converter station");
        createHvdcLine(INVLID, INVALID, 10.0, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                510.0, 77.0, 22.0, "C1", NON_EXISTING);
    }

    @Test
    public void duplicateHvdcLine() {
        createHvdcLine(DUPLICATE, DUPLICATE, 10.0, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                10.0, 10.0, 20.0, "C1", "C2");
        HvdcLine line = network.getHvdcLine(DUPLICATE);
        assertNotNull(line);
        thrown.expect(PowsyblException.class);
        createHvdcLine(DUPLICATE, DUPLICATE, 10.0, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                10.0, 10.0, 20.0, "C1", "C2");
    }

    @Test
    public void removeHvdcLine() {
        createHvdcLine(TO_REMOVE, TO_REMOVE, 10.0, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                10.0, 10.0, 20.0, "C1", "C2");
        HvdcLine line = network.getHvdcLine(TO_REMOVE);
        assertNotNull(line);
        int hvdcLineCount = network.getHvdcLineCount();
        line.remove();
        assertNotNull(line);
        assertNull(network.getHvdcLine(TO_REMOVE));
        assertEquals(hvdcLineCount - 1L, network.getHvdcLineCount());
    }

    @Test
    public void testSetterGetterInMultiVariants() {
        VariantManager variantManager = network.getVariantManager();
        createHvdcLine(TEST_MULTI_VARIANT, TEST_MULTI_VARIANT, 10.0, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                11.0, 12.0, 22.0, "C1", "C2");
        HvdcLine hvdcLine = network.getHvdcLine(TEST_MULTI_VARIANT);
        List<String> variantsToAdd = Arrays.asList("s1", "s2", "s3", "s4");
        variantManager.cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, variantsToAdd);

        variantManager.setWorkingVariant("s4");
        // check values cloned by extend
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, hvdcLine.getConvertersMode());
        assertEquals(12.0, hvdcLine.getActivePowerSetpoint(), 0.0);
        // change values in s4
        hvdcLine.setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER);
        hvdcLine.setActivePowerSetpoint(22.0);

        // remove s2
        variantManager.removeVariant("s2");

        variantManager.cloneVariant("s4", "s2b");
        variantManager.setWorkingVariant("s2b");
        // check values cloned by allocate
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, hvdcLine.getConvertersMode());
        assertEquals(22.0, hvdcLine.getActivePowerSetpoint(), 0.0);

        // recheck initial variant value
        variantManager.setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, hvdcLine.getConvertersMode());
        assertEquals(12.0, hvdcLine.getActivePowerSetpoint(), 0.0);

        // remove working variant s4
        variantManager.setWorkingVariant("s4");
        variantManager.removeVariant("s4");
        try {
            hvdcLine.getConvertersMode();
            fail();
        } catch (Exception ignored) {
            // ignore
        }
    }

    private void createHvdcLine(String id, String name, double r, HvdcLine.ConvertersMode mode, double v,
                                double activePowerSetpoint, double p, String converterStationId1, String converterStationId2) {
        network.newHvdcLine()
                    .setId(id)
                    .setName(name)
                    .setR(r)
                    .setConvertersMode(mode)
                    .setNominalV(v)
                    .setActivePowerSetpoint(activePowerSetpoint)
                    .setMaxP(p)
                    .setConverterStationId1(converterStationId1)
                    .setConverterStationId2(converterStationId2)
                .add();
    }
}
