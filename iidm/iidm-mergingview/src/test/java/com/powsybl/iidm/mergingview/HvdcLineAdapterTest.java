/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HvdcLineAdapterTest {

    private MergingView mergingView;

    private Network networkRef;

    @BeforeEach
    void setUp() {
        mergingView = MergingView.create("HvdcLineAdapterTest", "iidm");
        networkRef = HvdcTestNetwork.createLcc();
        mergingView.merge(networkRef);
    }

    @Test
    void baseTests() {
        final HvdcLine expectedLine = networkRef.getHvdcLine("L");
        final HvdcLine lineAdapted = mergingView.getHvdcLine("L");
        assertTrue(lineAdapted instanceof HvdcLineAdapter);
        assertSame(mergingView, lineAdapted.getNetwork());

        // getters / setters
        HvdcLine.ConvertersMode mode = expectedLine.getConvertersMode();
        assertEquals(mode, lineAdapted.getConvertersMode());
        mode = HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
        lineAdapted.setConvertersMode(mode);
        assertEquals(mode, lineAdapted.getConvertersMode());

        double r = expectedLine.getR();
        assertEquals(r, lineAdapted.getR(), 0.0d);
        assertEquals(lineAdapted, lineAdapted.setR(++r));
        assertEquals(r, lineAdapted.getR(), 0.0d);

        double nominalV = expectedLine.getNominalV();
        assertEquals(nominalV, lineAdapted.getNominalV(), 0.0d);
        assertEquals(lineAdapted, lineAdapted.setNominalV(++nominalV));
        assertEquals(nominalV, lineAdapted.getNominalV(), 0.0d);

        double activePowerSetpoint = expectedLine.getActivePowerSetpoint();
        assertEquals(activePowerSetpoint, lineAdapted.getActivePowerSetpoint(), 0.0d);
        assertEquals(lineAdapted, lineAdapted.setActivePowerSetpoint(++activePowerSetpoint));
        assertEquals(activePowerSetpoint, lineAdapted.getActivePowerSetpoint(), 0.0d);

        double maxP = expectedLine.getMaxP();
        assertEquals(maxP, lineAdapted.getMaxP(), 0.0d);
        assertEquals(lineAdapted, lineAdapted.setMaxP(++maxP));
        assertEquals(maxP, lineAdapted.getMaxP(), 0.0d);

        assertSame(lineAdapted.getConverterStation1().getId(), expectedLine.getConverterStation1().getId());
        assertSame(lineAdapted.getConverterStation1(), lineAdapted.getConverterStation(HvdcLine.Side.ONE));
        assertSame(lineAdapted.getConverterStation2().getId(), expectedLine.getConverterStation2().getId());
        assertSame(lineAdapted.getConverterStation2(), lineAdapted.getConverterStation(HvdcLine.Side.TWO));

        // Test not implemented !
        TestUtil.notImplemented(lineAdapted::remove);

        LccConverterStation cs1 = mergingView.getLccConverterStation("C1");
        LccConverterStation cs2 = mergingView.getLccConverterStation("C2");
        if (cs1.getOtherConverterStation().isPresent()) {
            assertEquals(cs2, cs1.getOtherConverterStation().get());
        }
        if (cs2.getOtherConverterStation().isPresent()) {
            assertEquals(cs1, cs2.getOtherConverterStation().get());
        }
    }

    @Test
    void testAdder() {
        final HvdcLine hvdcLine = mergingView.newHvdcLine()
                                                .setId("hvdc_line")
                                                .setName("hvdcLine")
                                                .setEnsureIdUnicity(true)
                                                .setR(5.0)
                                                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                                                .setNominalV(440.0)
                                                .setMaxP(50.0)
                                                .setActivePowerSetpoint(20.0)
                                                .setConverterStationId1("C1")
                                                .setConverterStationId2("C2")
                                            .add();
        assertTrue(hvdcLine instanceof HvdcLineAdapter);
        assertSame(hvdcLine, mergingView.getHvdcLine("hvdc_line"));
    }

    @Test
    void testAdderWithoutConverterStationId1() {
        // Test not allowed HvdcLine creation
        PowsyblException e = assertThrows(PowsyblException.class, () -> mergingView.newHvdcLine()
                .setId("NotAllowed")
                .setName("NotAllowed")
                .setEnsureIdUnicity(false)
                .setR(5.0)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setNominalV(440.0)
                .setMaxP(-50.0)
                .setActivePowerSetpoint(20.0)
                .add());
        assertTrue(e.getMessage().contains("Side 1 converter station is not set"));
    }

    @Test
    void testAdderWithoutConverterStationId2() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> mergingView.newHvdcLine()
                .setId("NotAllowed")
                .setName("NotAllowed")
                .setEnsureIdUnicity(false)
                .setR(5.0)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setNominalV(440.0)
                .setMaxP(-50.0)
                .setActivePowerSetpoint(20.0)
                .setConverterStationId1("C1")
                .add());
        assertTrue(e.getMessage().contains("Side 2 converter station is not set"));
    }

    @Test
    void testAdderWithUnknownConverterStationId1() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> mergingView.newHvdcLine()
                       .setId("NotAllowed")
                       .setName("NotAllowed")
                       .setEnsureIdUnicity(false)
                       .setR(5.0)
                       .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                       .setNominalV(440.0)
                       .setMaxP(-50.0)
                       .setActivePowerSetpoint(20.0)
                       .setConverterStationId1("C1_")
                   .add());
        assertTrue(e.getMessage().contains("Side 1 converter station 'C1_' not found"));
    }

    @Test
    void testAdderWithUnknownConverterStationId2() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> mergingView.newHvdcLine()
                       .setId("NotAllowed")
                       .setName("NotAllowed")
                       .setEnsureIdUnicity(false)
                       .setFictitious(false)
                       .setR(5.0)
                       .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                       .setNominalV(440.0)
                       .setMaxP(-50.0)
                       .setActivePowerSetpoint(20.0)
                       .setConverterStationId1("C1")
                       .setConverterStationId2("C2_")
                   .add());
        assertTrue(e.getMessage().contains("Side 2 converter station 'C2_' not found"));
    }

    @Test
    void testAdderNotAllowed() {
        mergingView.merge(NoEquipmentNetworkFactory.create());
        mergingView.getVoltageLevel("vl1").newLccConverterStation()
                                                  .setId("C3")
                                                  .setBus("busA")
                                                  .setLossFactor(0.011f)
                                                  .setPowerFactor(0.5f)
                                              .add();
        PowsyblException e = assertThrows(PowsyblException.class, () -> mergingView.newHvdcLine()
                       .setId("NotAllowed")
                       .setName("NotAllowed")
                       .setEnsureIdUnicity(false)
                       .setR(5.0)
                       .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                       .setNominalV(440.0)
                       .setMaxP(-50.0)
                       .setActivePowerSetpoint(20.0)
                       .setConverterStationId1("C1")
                       .setConverterStationId2("C3")
                   .add());
        assertTrue(e.getMessage().contains("HvdcLine creation between two networks is not allowed"));
    }
}
