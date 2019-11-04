/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.ShuntCompensatorAdder;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.HvdcTestNetwork;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

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
        assertEquals("C1_Filter1", shuntCompensator.getId());
        assertSame(ConnectableType.SHUNT_COMPENSATOR, shuntCompensator.getType());
        assertEquals(1, shuntCompensator.getMaximumSectionCount());
        assertTrue(shuntCompensator.setMaximumSectionCount(2) instanceof ShuntCompensatorAdapter);
        assertEquals(2, shuntCompensator.getMaximumSectionCount());
        assertEquals(1, shuntCompensator.getCurrentSectionCount());
        assertTrue(shuntCompensator.setCurrentSectionCount(2) instanceof ShuntCompensatorAdapter);
        assertEquals(2, shuntCompensator.getCurrentSectionCount());
        assertEquals(1e-5, shuntCompensator.getbPerSection(), 0.001);
        assertTrue(shuntCompensator.setbPerSection(1e-6) instanceof ShuntCompensatorAdapter);
        assertEquals(1e-6, shuntCompensator.getbPerSection(), 0.001);
        assertEquals(0, shuntCompensator.getCurrentB(), 0.001);
        assertEquals(0, shuntCompensator.getMaximumB(), 0.001);

        assertTrue(shuntCompensator.getTerminal() instanceof TerminalAdapter);
        List terminals = shuntCompensator.getTerminals();
        for (Object terminal : terminals) {
            assertTrue(terminal instanceof TerminalAdapter);
        }

        // Not implemented yet !
        TestUtil.notImplemented(shuntCompensator::remove);
    }

    @Test
    public void testShuntCompensatorAdder() {
        final VoltageLevel vlshunt = mergingView.getVoltageLevel("VL1");
        final ShuntCompensatorAdder shuntAdder = vlshunt.newShuntCompensator().setId("SHUNTEST");
        assertNotNull(shuntAdder);
        assertTrue(shuntAdder instanceof ShuntCompensatorAdderAdapter);

        shuntAdder.setName("SHUNT")
            .setConnectableBus("B1")
            .setBus("B1")
            .setCurrentSectionCount(1)
            .setMaximumSectionCount(1)
            .setbPerSection(2e-4)
            .setEnsureIdUnicity(true).add();

        boolean found = false;
        for (ShuntCompensator shunt : vlshunt.getShuntCompensators()) {
            if ("SHUNTEST".equals(shunt.getId())) {
                found = true;
                assertEquals(1, shunt.getCurrentSectionCount());
                assertEquals(1, shunt.getMaximumSectionCount());
                assertEquals(2e-4, shunt.getbPerSection(), 0.001);
            }
        }
        assertTrue(found);

        final VoltageLevel vlshunt2 = mergingView.getVoltageLevel("VL2");
        final ShuntCompensatorAdder shuntAdder2 = vlshunt2.newShuntCompensator().setId("SHUNTEST2");
        assertNotNull(shuntAdder2);
        assertTrue(shuntAdder2 instanceof ShuntCompensatorAdderAdapter);

        shuntAdder2.setName("SHUNT2")
            .setNode(1)
            .setCurrentSectionCount(2)
            .setMaximumSectionCount(2)
            .setbPerSection(2e-5)
            .setEnsureIdUnicity(true).add();

        found = false;
        for (ShuntCompensator shunt : vlshunt2.getShuntCompensators()) {
            if ("SHUNTEST2".equals(shunt.getId())) {
                found = true;
                assertEquals(2, shunt.getCurrentSectionCount());
                assertEquals(2, shunt.getMaximumSectionCount());
                assertEquals(2e-5, shunt.getbPerSection(), 0.001);
            }
        }
        assertTrue(found);
    }
}
