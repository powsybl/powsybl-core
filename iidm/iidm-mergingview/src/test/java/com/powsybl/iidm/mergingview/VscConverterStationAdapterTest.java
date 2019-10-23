/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.test.HvdcTestNetwork;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class VscConverterStationAdapterTest {

    private MergingView mergingView;
    private VscConverterStation cs1;

    @Before
    public void setUp() {
        mergingView = MergingView.create("VscConverterStationAdapterTest", "iidm");
        mergingView.merge(HvdcTestNetwork.createVsc());
        cs1 = mergingView.getVscConverterStation("C1");
    }

    @Test
    public void testSetterGetter() {
        final VscConverterStation vscConverterStation = mergingView.getVscConverterStation("C1");
        assertNotNull(vscConverterStation);
        assertSame(vscConverterStation, mergingView.getHvdcConverterStation("C1"));
        assertTrue(vscConverterStation instanceof VscConverterStationAdapter);
        assertSame(mergingView, vscConverterStation.getNetwork());

        assertNotNull(cs1);
        assertEquals(HvdcConverterStation.HvdcType.VSC, cs1.getHvdcType());
        cs1.setLossFactor(0.022f);
        assertEquals(0.022f, cs1.getLossFactor(), 0.0f);
        assertNotNull(cs1.getTerminal());
        assertEquals(ConnectableType.HVDC_CONVERTER_STATION, cs1.getType());
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
        TestUtil.notImplemented(vscConverterStation::remove);
    }
}
