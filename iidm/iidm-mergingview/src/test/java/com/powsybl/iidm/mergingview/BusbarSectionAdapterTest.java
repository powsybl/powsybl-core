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

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.test.NetworkTest1Factory;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BusbarSectionAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("BusbarSectionAdapterTest", "iidm");
        mergingView.merge(NetworkTest1Factory.create());
    }

    @Test
    public void testSetterGetter() {
        final BusbarSection busbarSection = mergingView.getBusbarSection("voltageLevel1BusbarSection1");
        assertNotNull(busbarSection);
        assertTrue(busbarSection instanceof BusbarSectionAdapter);
        assertSame(mergingView, busbarSection.getNetwork());

        // Not implemented yet !
        TestUtil.notImplemented(busbarSection::getTerminal);
        TestUtil.notImplemented(busbarSection::getType);
        TestUtil.notImplemented(busbarSection::getTerminals);
        TestUtil.notImplemented(busbarSection::remove);
        TestUtil.notImplemented(busbarSection::getV);
        TestUtil.notImplemented(busbarSection::getAngle);
    }
}
