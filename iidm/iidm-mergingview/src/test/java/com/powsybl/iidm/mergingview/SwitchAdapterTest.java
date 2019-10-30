/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.test.NetworkTest1Factory;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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

        assertFalse(sw.isOpen());
        sw.setOpen(true);
        assertTrue(sw.isOpen());

        sw.setRetained(true);
        assertTrue(sw.isRetained());
        sw.setFictitious(true);
        assertTrue(sw.isFictitious());

        assertTrue(sw.getKind() instanceof SwitchKind);
        assertSame(SwitchKind.DISCONNECTOR, sw.getKind());

        assertTrue(sw.getVoltageLevel() instanceof VoltageLevelAdapter);
        assertEquals("voltageLevel1", sw.getVoltageLevel().getId());

    }
}
