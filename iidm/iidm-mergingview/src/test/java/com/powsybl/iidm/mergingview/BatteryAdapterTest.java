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

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BatteryAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("BatteryAdapterTest", "iidm");
        mergingView.merge(BatteryNetworkFactory.create());
    }

    @Test
    public void testSetterGetter() {
        final Battery battery = mergingView.getBattery("BAT");
        assertNotNull(battery);
        assertTrue(battery instanceof BatteryAdapter);
        assertSame(mergingView, battery.getNetwork());

        // Not implemented yet !
        TestUtil.notImplemented(battery::getTerminal);
        TestUtil.notImplemented(battery::getType);
        TestUtil.notImplemented(battery::getTerminals);
        TestUtil.notImplemented(battery::remove);
        TestUtil.notImplemented(battery::getReactiveLimits);
        TestUtil.notImplemented(() -> battery.getReactiveLimits(null));
        TestUtil.notImplemented(battery::newReactiveCapabilityCurve);
        TestUtil.notImplemented(battery::newMinMaxReactiveLimits);
        TestUtil.notImplemented(battery::getP0);
        TestUtil.notImplemented(() -> battery.setP0(0.0d));
        TestUtil.notImplemented(battery::getQ0);
        TestUtil.notImplemented(() -> battery.setQ0(0.0d));
        TestUtil.notImplemented(battery::getMinP);
        TestUtil.notImplemented(() -> battery.setMinP(0.0d));
        TestUtil.notImplemented(battery::getMaxP);
        TestUtil.notImplemented(() -> battery.setMaxP(0.0d));
    }
}
