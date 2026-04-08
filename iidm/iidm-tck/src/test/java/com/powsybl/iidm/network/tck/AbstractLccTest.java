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

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public abstract class AbstractLccTest {

    private static final String C1_FILTER2 = "C1_Filter2";

    private Network network;
    private HvdcLine hvdcLine;
    private LccConverterStation cs1;
    private LccConverterStation cs2;

    @BeforeEach
    public void setUp() {
        network = HvdcTestNetwork.createLcc();
        hvdcLine = network.getHvdcLine("L");
        cs1 = network.getLccConverterStation("C1");
        cs2 = network.getLccConverterStation("C2");
    }

    @Test
    public void testBase() {
        assertNotNull(cs1);
        assertNotNull(cs2);
        assertEquals(1, network.getVoltageLevel("VL1").getLccConverterStationCount());
        assertEquals(1, network.getVoltageLevel("VL2").getLccConverterStationCount());
        assertEquals(1.1f, cs1.getLossFactor(), 0.0f);
        assertEquals(1.1f, cs2.getLossFactor(), 0.0f);
        cs1.setLossFactor(2.2f);
        assertEquals(2.2f, cs1.getLossFactor(), 0.0f);
        assertEquals(0.5f, cs1.getPowerFactor(), 0.0f);
        assertEquals(0.6f, cs2.getPowerFactor(), 0.0f);
        cs1.setPowerFactor(0.6f);
        assertEquals(0.6f, cs1.getPowerFactor(), 0.0f);
        assertEquals(2, network.getVoltageLevel("VL1").getShuntCompensatorCount());
        assertEquals(2, network.getVoltageLevel("VL2").getShuntCompensatorCount());
        assertEquals(1e-5, network.getShuntCompensator("C1_Filter1").getB(), 0.0);
        assertTrue(network.getShuntCompensator("C1_Filter1").getTerminal().isConnected());
        assertEquals(0.0, network.getShuntCompensator(C1_FILTER2).getB(), 0.0);

        assertFalse(network.getShuntCompensator(C1_FILTER2).getTerminal().isConnected());
        assertEquals(1, network.getHvdcLineCount());
        HvdcLine l = network.getHvdcLine("L");
        assertNotNull(l);
        assertEquals(1.0, l.getR(), 0.0);
        assertEquals(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, l.getConvertersMode());
        assertEquals(300.0, l.getMaxP(), 0.0);
        assertEquals(cs1, l.getConverterStation1());
        assertEquals(cs2, l.getConverterStation2());

        assertSame(hvdcLine, cs1.getHvdcLine());
        assertSame(hvdcLine, cs2.getHvdcLine());
        assertSame(cs1, hvdcLine.getConverterStation1());
        assertSame(cs1, hvdcLine.getConverterStation(TwoSides.ONE));
        assertSame(cs2, hvdcLine.getConverterStation2());
        assertSame(cs2, hvdcLine.getConverterStation(TwoSides.TWO));

        if (cs1.getOtherConverterStation().isPresent()) {
            assertEquals(cs2, cs1.getOtherConverterStation().get());
        }
        if (cs2.getOtherConverterStation().isPresent()) {
            assertEquals(cs1, cs2.getOtherConverterStation().get());
        }
    }

    @Test
    public void testHvdcLineRemove() {
        try {
            cs1.remove();
            fail();
        } catch (ValidationException e) {
            // Ignored
        }

        network.getHvdcLine("L").remove();
        assertEquals(0, network.getHvdcLineCount());

        assertNull(cs1.getHvdcLine());
        assertNull(hvdcLine.getConverterStation1());
        assertNull(cs2.getHvdcLine());
        assertNull(hvdcLine.getConverterStation2());

        // remove
        int count = network.getLccConverterStationCount();
        cs1.remove();
        assertNotNull(cs1);
        assertNull(network.getLccConverterStation("C1"));
        assertEquals(count - 1L, network.getLccConverterStationCount());
    }

    @Test
    public void invalidLossFactor() {
        ValidationException e = assertThrows(ValidationException.class, () -> cs1.setLossFactor(Float.NaN));
        assertTrue(e.getMessage().contains("loss factor is invalid"));
    }

    @Test
    public void invalidNegativeLossFactor() {
        ValidationException e = assertThrows(ValidationException.class, () -> cs1.setLossFactor(-1.0f));
        assertTrue(e.getMessage().contains("loss factor must be >= 0"));
    }

    @Test
    public void invalidPowerFactor() {
        ValidationException e = assertThrows(ValidationException.class, () -> cs1.setPowerFactor(Float.NaN));
        assertTrue(e.getMessage().contains("power factor is invalid"));
    }
}
