/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class HvdcLineAdapterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MergingView mergingView;

    private Network networkRef;

    @Before
    public void setUp() {
        mergingView = MergingView.create("HvdcLineAdapterTest", "iidm");
        networkRef = HvdcTestNetwork.createLcc();
        mergingView.merge(networkRef);
    }

    @Test
    public void baseTests() {
        final HvdcLine expectedLine = networkRef.getHvdcLine("L");
        final HvdcLine lineAdapted = mergingView.getHvdcLine("L");
        assertTrue(lineAdapted instanceof HvdcLineAdapter);
        assertSame(mergingView, lineAdapted.getNetwork());

        // getters / setters
        HvdcLine.ConvertersMode mode = expectedLine.getConvertersMode();
        assertEquals(mode,  lineAdapted.getConvertersMode());
        mode = HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER;
        lineAdapted.setConvertersMode(mode);
        assertEquals(mode,  lineAdapted.getConvertersMode());

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
    }

    @Test
    public void testAdder() {
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
    public void testAdderWithoutConverterStationId1() {
        // Test not allowed HvdcLine creation
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Side 1 converter station is not set");
        mergingView.newHvdcLine()
                .setId("NotAllowed")
                .setName("NotAllowed")
                .setEnsureIdUnicity(false)
                .setR(5.0)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setNominalV(440.0)
                .setMaxP(-50.0)
                .setActivePowerSetpoint(20.0)
                .add();
    }

    @Test
    public void testAdderWithoutConverterStationId2() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Side 2 converter station is not set");
        mergingView.newHvdcLine()
                .setId("NotAllowed")
                .setName("NotAllowed")
                .setEnsureIdUnicity(false)
                .setR(5.0)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setNominalV(440.0)
                .setMaxP(-50.0)
                .setActivePowerSetpoint(20.0)
                .setConverterStationId1("C1")
                .add();
    }

    @Test
    public void testAdderWithUnknownConverterStationId1() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Side 1 converter station 'C1_' not found");
        mergingView.newHvdcLine()
                       .setId("NotAllowed")
                       .setName("NotAllowed")
                       .setEnsureIdUnicity(false)
                       .setR(5.0)
                       .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                       .setNominalV(440.0)
                       .setMaxP(-50.0)
                       .setActivePowerSetpoint(20.0)
                       .setConverterStationId1("C1_")
                   .add();
    }

    @Test
    public void testAdderWithUnknownConverterStationId2() {
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Side 2 converter station 'C2_' not found");
        mergingView.newHvdcLine()
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
                   .add();
    }

    @Test
    public void testAdderNotAllowed() {
        mergingView.merge(NoEquipmentNetworkFactory.create());
        mergingView.getVoltageLevel("vl1").newLccConverterStation()
                                                  .setId("C3")
                                                  .setBus("busA")
                                                  .setLossFactor(0.011f)
                                                  .setPowerFactor(0.5f)
                                              .add();
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("HvdcLine creation between two networks is not allowed");
        mergingView.newHvdcLine()
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
                   .add();
    }
}
