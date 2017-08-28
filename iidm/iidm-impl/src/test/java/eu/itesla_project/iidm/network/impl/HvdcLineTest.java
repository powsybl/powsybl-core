/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.iidm.network.HvdcConverterStation;
import eu.itesla_project.iidm.network.HvdcLine;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.test.HvdcTestNetwork;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class HvdcLineTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    Network network;

    @Before
    public void initNetwork() {
        network = HvdcTestNetwork.createLcc();
    }

    @Test
    public void testHvdcLineOneStateAttributes() {
        Network network = HvdcTestNetwork.createLcc();
        HvdcLine l = network.getHvdcLine("L");
        assertNotNull(l);
        assertEquals(1f, l.getR(), 0.0f);
        assertEquals(400.0f, l.getNominalV(), 0.0f);
        assertEquals(300f, l.getMaxP(), 0.0f);
        l.setR(2f);
        assertEquals(2f, l.getR(), 0.0f);
        l.setNominalV(220.0f);
        assertEquals(220.0f, l.getNominalV(), 0.0f);
        l.setMaxP(1.11f);
        assertEquals(1.11f, l.getMaxP(), 0.0f);
        l.setActivePowerSetpoint(421f);
        assertEquals(421f, l.getActivePowerSetpoint(), 0.0f);
        l.setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER);
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, l.getConvertersMode());
    }

    @Test
    public void testAdder() {
        HvdcLine hvdcLine = network.newHvdcLine()
                                        .setId("hvdc_line")
                                        .setName("hvdcLine")
                                        .setR(5.0f)
                                        .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                                        .setNominalV(440.0f)
                                        .setMaxP(-50.0f)
                                        .setActivePowerSetpoint(20.0f)
                                        .setConverterStationId1("C1")
                                        .setConverterStationId2("C2")
                                    .add();
        assertNotNull(hvdcLine);
        assertEquals("hvdc_line", hvdcLine.getId());
        assertEquals("hvdcLine", hvdcLine.getName());
        assertEquals(5.0f, hvdcLine.getR(), 0.0f);
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, hvdcLine.getConvertersMode());
        assertEquals(440.0f, hvdcLine.getNominalV(), 0.0f);
        assertEquals(-50.0f, hvdcLine.getMaxP(), 0.0f);
        assertEquals(20.0f, hvdcLine.getActivePowerSetpoint(), 0.0f);
        assertSame(network.getHvdcConverterStation("C1"), hvdcLine.getConverterStation1());
        assertSame(network.getHvdcConverterStation("C2"), hvdcLine.getConverterStation2());
    }

    @Test
    public void invalidR() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("r is invalid");
        createHvdcLine("invlid", "invalid", Float.NaN, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                440.0f, 10.0f, 20.0f, "C1", "C2");
    }

    @Test
    public void invalidConvertersMode() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("converter mode is invalid");
        createHvdcLine("invlid", "invalid", 10.0f, null,
                440.0f, 10.0f, 20.0f, "C1", "C2");
    }

    @Test
    public void invalidNominalV() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("nominal voltage is invalid");
        createHvdcLine("invlid", "invalid", 10.0f, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                0.0f, 10.0f, 20.0f, "C1", "C2");
    }

    @Test
    public void invalidActivePowerSetpoint() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for active power setpoint");
        createHvdcLine("invlid", "invalid", 10.0f, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                510.0f, Float.NaN, 20.0f, "C1", "C2");
    }

    @Test
    public void invalidMaxP() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("for maximum P");
        createHvdcLine("invlid", "invalid", 10.0f, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                510.0f, 77.0f, Float.NaN, "C1", "C2");
    }

    @Test
    public void nonExistingConverterStationSide1() {
        HvdcConverterStation<?> nonExistingConverterStation = network.getHvdcConverterStation("non_existing");
        assertNull(nonExistingConverterStation);
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Side 1 converter station");
        createHvdcLine("invlid", "invalid", 10.0f, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                510.0f, 77.0f, 22.0f, "non_existing", "C2");
    }

    @Test
    public void nonExistingConverterStationSide2() {
        HvdcConverterStation<?> nonExistingConverterStation = network.getHvdcConverterStation("non_existing");
        assertNull(nonExistingConverterStation);
        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Side 2 converter station");
        createHvdcLine("invlid", "invalid", 10.0f, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                510.0f, 77.0f, 22.0f, "C1", "non_existing");
    }

    @Test
    public void duplicateHvdcLine() {
        createHvdcLine("duplicate", "duplicate", 10.0f, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                10.0f, 10.0f, 20.0f, "C1", "C2");
        HvdcLine line = network.getHvdcLine("duplicate");
        assertNotNull(line);
        thrown.expect(ITeslaException.class);
        createHvdcLine("duplicate", "duplicate", 10.0f, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                10.0f, 10.0f, 20.0f, "C1", "C2");
    }

    @Test
    public void removeHvdcLine() {
        createHvdcLine("toRemove", "toRemove", 10.0f, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER,
                10.0f, 10.0f, 20.0f, "C1", "C2");
        HvdcLine line = network.getHvdcLine("toRemove");
        assertNotNull(line);
        int hvdcLineCount = network.getHvdcLineCount();
        line.remove();
        assertNotNull(line);
        assertNull(network.getHvdcLine("toRemove"));
        assertEquals(hvdcLineCount - 1, network.getHvdcLineCount());
    }

    private void createHvdcLine(String id, String name, float r, HvdcLine.ConvertersMode mode, float v,
                                float activePowerSetpoint, float p, String converterStationId1, String converterStationId2) {
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
