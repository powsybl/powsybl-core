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

import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.test.HvdcTestNetwork;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ShuntCompensatorAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("ShuntCompensatorAdapterTest", "iidm");
        mergingView.merge(HvdcTestNetwork.createLcc());
    }

    @Test
    public void testSetterGetter() {
        final ShuntCompensator shuntCompensator = mergingView.getShuntCompensator("C1_Filter1");
        assertNotNull(shuntCompensator);
        assertTrue(shuntCompensator instanceof ShuntCompensatorAdapter);
        assertSame(mergingView, shuntCompensator.getNetwork());

        // Not implemented yet !
        TestUtil.notImplemented(shuntCompensator::getTerminal);
        TestUtil.notImplemented(shuntCompensator::getType);
        TestUtil.notImplemented(shuntCompensator::getTerminals);
        TestUtil.notImplemented(shuntCompensator::remove);
        TestUtil.notImplemented(shuntCompensator::getMaximumSectionCount);
        TestUtil.notImplemented(() -> shuntCompensator.setMaximumSectionCount(0));
        TestUtil.notImplemented(shuntCompensator::getCurrentSectionCount);
        TestUtil.notImplemented(() -> shuntCompensator.setCurrentSectionCount(0));
        TestUtil.notImplemented(shuntCompensator::getbPerSection);
        TestUtil.notImplemented(() -> shuntCompensator.setbPerSection(0.0d));
        TestUtil.notImplemented(shuntCompensator::getMaximumB);
        TestUtil.notImplemented(shuntCompensator::getCurrentB);
    }
}
