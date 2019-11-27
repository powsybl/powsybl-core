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

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.test.NetworkTest1Factory;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class SwitchAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("SwitchAdapterTest", "iidm");
        mergingView.merge(NetworkTest1Factory.create());
    }

    @Test
    public void testSetterGetter() {
        final Switch sw = mergingView.getSwitch("load1Disconnector1");
        assertNotNull(sw);
        assertTrue(sw instanceof SwitchAdapter);
        assertSame(mergingView, sw.getNetwork());

        // Not implemented yet !
        TestUtil.notImplemented(sw::getVoltageLevel);
        TestUtil.notImplemented(sw::getKind);
        TestUtil.notImplemented(sw::isOpen);
        TestUtil.notImplemented(() -> sw.setOpen(false));
        TestUtil.notImplemented(sw::isRetained);
        TestUtil.notImplemented(() -> sw.setRetained(false));
        TestUtil.notImplemented(sw::isFictitious);
        TestUtil.notImplemented(() -> sw.setFictitious(false));
    }
}
