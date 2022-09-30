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
public class LccConverterStationAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("LccConverterStationAdapterTest", "iidm");
        mergingView.merge(HvdcTestNetwork.createLcc());
    }

    @Test
    public void testSetterGetter() {
        final LccConverterStation lcc = mergingView.getLccConverterStation("C1");
        assertNotNull(lcc);
        assertSame(lcc, mergingView.getHvdcConverterStation("C1"));
        assertTrue(lcc instanceof LccConverterStationAdapter);
        assertSame(mergingView, lcc.getNetwork());

        assertEquals(HvdcConverterStation.HvdcType.LCC, lcc.getHvdcType());
        assertSame(mergingView.getHvdcLine("L"), lcc.getHvdcLine());
        lcc.setLossFactor(0.022f);
        assertEquals(0.022f, lcc.getLossFactor(), 0.0f);
        assertNotNull(lcc.getTerminal());
        assertEquals(IdentifiableType.HVDC_CONVERTER_STATION, lcc.getType());
        assertEquals(1, lcc.getTerminals().size());

        lcc.setPowerFactor(1.0f);
        assertEquals(1.0f, lcc.getPowerFactor(), 0.0f);

        // Not implemented yet !
        TestUtil.notImplemented(lcc::remove);
    }

    @Test
    public void testAdderFromExisting() {
        mergingView.getVoltageLevel("VL1").newLccConverterStation(mergingView.getLccConverterStation("C1")).setId("C3")
                .setBus("B1")
                .add();
        LccConverterStation converterStation = mergingView.getLccConverterStation("C3");
        assertNotNull(converterStation);
        assertEquals(1.1f, converterStation.getLossFactor(), 0.0);
        assertEquals(0.5f, converterStation.getPowerFactor(), 0.0);
    }
}
