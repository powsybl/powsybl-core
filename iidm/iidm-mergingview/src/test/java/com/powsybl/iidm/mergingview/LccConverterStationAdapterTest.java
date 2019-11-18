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

import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.test.HvdcTestNetwork;

import org.junit.Before;
import org.junit.Test;

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

        // Not implemented yet !
        TestUtil.notImplemented(lcc::getHvdcType);
        TestUtil.notImplemented(lcc::getLossFactor);
        TestUtil.notImplemented(() -> lcc.setLossFactor(0.0f));
        TestUtil.notImplemented(lcc::getTerminal);
        TestUtil.notImplemented(lcc::getType);
        TestUtil.notImplemented(lcc::getTerminals);
        TestUtil.notImplemented(lcc::remove);
        TestUtil.notImplemented(lcc::getPowerFactor);
        TestUtil.notImplemented(() -> lcc.setPowerFactor(0.0f));
    }
}
