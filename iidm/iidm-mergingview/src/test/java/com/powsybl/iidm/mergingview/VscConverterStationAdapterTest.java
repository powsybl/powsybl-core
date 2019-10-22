/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.test.HvdcTestNetwork;

import org.junit.Before;
import org.junit.Test;

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
        final VscConverterStation vscConverterStation = mergingView.getVscConverterStation("C1");
        assertNotNull(vscConverterStation);
        assertSame(vscConverterStation, mergingView.getHvdcConverterStation("C1"));
        assertTrue(vscConverterStation instanceof VscConverterStationAdapter);
        assertSame(mergingView, vscConverterStation.getNetwork());

        // Not implemented yet !
        TestUtil.notImplemented(vscConverterStation::getHvdcType);
        TestUtil.notImplemented(vscConverterStation::getLossFactor);
        TestUtil.notImplemented(() -> vscConverterStation.setLossFactor(0.0f));
        TestUtil.notImplemented(vscConverterStation::getTerminal);
        TestUtil.notImplemented(vscConverterStation::getType);
        TestUtil.notImplemented(vscConverterStation::getTerminals);
        TestUtil.notImplemented(vscConverterStation::remove);
        TestUtil.notImplemented(vscConverterStation::getReactiveLimits);
        TestUtil.notImplemented(() -> vscConverterStation.getReactiveLimits(null));
        TestUtil.notImplemented(vscConverterStation::newReactiveCapabilityCurve);
        TestUtil.notImplemented(vscConverterStation::newMinMaxReactiveLimits);
        TestUtil.notImplemented(vscConverterStation::isVoltageRegulatorOn);
        TestUtil.notImplemented(() -> vscConverterStation.setVoltageRegulatorOn(false));
        TestUtil.notImplemented(vscConverterStation::getVoltageSetpoint);
        TestUtil.notImplemented(() -> vscConverterStation.setVoltageSetpoint(0.0d));
        TestUtil.notImplemented(vscConverterStation::getReactivePowerSetpoint);
        TestUtil.notImplemented(() -> vscConverterStation.setReactivePowerSetpoint(0.0d));
    }
}
