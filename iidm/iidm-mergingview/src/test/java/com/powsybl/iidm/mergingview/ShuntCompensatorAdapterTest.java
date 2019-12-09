/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ShuntCompensatorAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("ShuntCompensatorAdapterTest", "iidm");
    }

    @Test
    public void testSetterGetter() {
        Network networkRef = HvdcTestNetwork.createLcc();
        mergingView.merge(networkRef);

        final ShuntCompensator shuntCExpected = networkRef.getShuntCompensator("C1_Filter1");
        final ShuntCompensator shuntCActual   = mergingView.getShuntCompensator("C1_Filter1");
        assertNotNull(shuntCActual);
        assertTrue(shuntCActual instanceof ShuntCompensatorAdapter);
        assertSame(mergingView, shuntCActual.getNetwork());

        assertEquals(shuntCExpected.getType(), shuntCActual.getType());
        assertTrue(shuntCActual.getTerminal() instanceof TerminalAdapter);
        shuntCActual.getTerminals().forEach(t -> {
            assertTrue(t instanceof TerminalAdapter);
            assertNotNull(t);
        });

        int maxCount = shuntCExpected.getMaximumSectionCount();
        assertEquals(maxCount, shuntCActual.getMaximumSectionCount());
        assertTrue(shuntCActual.setMaximumSectionCount(++maxCount) instanceof ShuntCompensatorAdapter);
        assertEquals(maxCount, shuntCActual.getMaximumSectionCount());

        int currentCount = shuntCExpected.getCurrentSectionCount();
        assertEquals(currentCount, shuntCActual.getCurrentSectionCount());
        assertTrue(shuntCActual.setCurrentSectionCount(++currentCount) instanceof ShuntCompensatorAdapter);
        assertEquals(currentCount, shuntCActual.getCurrentSectionCount());

        double b = shuntCExpected.getbPerSection();
        assertEquals(b, shuntCActual.getbPerSection(), 0.0d);
        assertTrue(shuntCActual.setbPerSection(++b) instanceof ShuntCompensatorAdapter);
        assertEquals(shuntCExpected.getCurrentB(), shuntCActual.getCurrentB(), 0.0d);

        // Not implemented yet !
        TestUtil.notImplemented(shuntCActual::remove);
    }
}
