/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class VscConverterStationAdapterTest {

    private MergingView mergingView;

    @Before
    public void setUp() {
        mergingView = MergingView.create("VscConverterStationAdapterTest", "iidm");
        mergingView.merge(HvdcTestNetwork.createVsc());
    }

    @Test
    public void testSetterGetter() {
        final VscConverterStation cs1 = mergingView.getVscConverterStation("C1");
        assertNotNull(cs1);
        assertSame(cs1, mergingView.getHvdcConverterStation("C1"));
        assertTrue(cs1 instanceof VscConverterStationAdapter);
        assertSame(mergingView, cs1.getNetwork());

        assertEquals(HvdcConverterStation.HvdcType.VSC, cs1.getHvdcType());
        assertSame(mergingView.getHvdcLine("L"), cs1.getHvdcLine());
        cs1.setLossFactor(0.022f);
        assertEquals(0.022f, cs1.getLossFactor(), 0.0f);
        assertNotNull(cs1.getTerminal());
        assertEquals(IdentifiableType.HVDC_CONVERTER_STATION, cs1.getType());
        assertEquals(1, cs1.getTerminals().size());

        assertNotNull(cs1.getReactiveLimits());
        assertNotNull(cs1.getReactiveLimits(ReactiveCapabilityCurve.class));

        cs1.newReactiveCapabilityCurve()
                .beginPoint()
                .setP(0)
                .setMinQ(-59.3)
                .setMaxQ(60.0)
                .endPoint()
                .beginPoint()
                .setP(70.0)
                .setMinQ(-54.55)
                .setMaxQ(46.25)
                .endPoint()
                .add();

        cs1.newMinMaxReactiveLimits()
                .setMinQ(-9999.99)
                .setMaxQ(9999.99)
                .add();

        cs1.setVoltageSetpoint(406.0);
        assertEquals(406.0, cs1.getVoltageSetpoint(), 0.0);
        assertTrue(Double.isNaN(cs1.getReactivePowerSetpoint()));
        cs1.setReactivePowerSetpoint(124.0);
        assertEquals(124.0, cs1.getReactivePowerSetpoint(), 0.0);
        cs1.setVoltageRegulatorOn(false);
        assertFalse(cs1.isVoltageRegulatorOn());

        // Not implemented yet !
        TestUtil.notImplemented(cs1::remove);
    }

    @Test
    public void testAdderFromExisting() {
        mergingView.getVoltageLevel("VL1").newVscConverterStation(mergingView.getVscConverterStation("C1"))
                .setId("DUPLICATE")
                .setBus("B1")
                .add();

        VscConverterStation converterStation = mergingView.getVscConverterStation("DUPLICATE");
        assertNotNull(converterStation);
        assertEquals(1.1f, converterStation.getLossFactor(), 0.0);
        assertEquals(405.0, converterStation.getVoltageSetpoint(), 0.0);
        assertTrue(converterStation.isVoltageRegulatorOn());
        assertTrue(Double.isNaN(converterStation.getReactivePowerSetpoint()));
    }

    @Test
    public void testRegulatingTerminal() {
        final VscConverterStation cs1 = mergingView.getVscConverterStation("C1");
        final VscConverterStation cs2 = mergingView.getVscConverterStation("C2");
        assertEquals(cs1.getTerminal(), cs2.getRegulatingTerminal());
        cs1.setRegulatingTerminal(cs2.getTerminal());
        assertEquals(cs2.getTerminal(), cs1.getRegulatingTerminal());
        cs2.setRegulatingTerminal(null);
        assertEquals(cs2.getTerminal(), cs2.getRegulatingTerminal());
    }
}
