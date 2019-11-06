/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NetworkTest1Factory;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

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
        final VoltageLevel vl = mergingView.getVoltageLevel("voltageLevel1");

        assertNotNull(busbarSection);
        assertTrue(busbarSection instanceof BusbarSectionAdapter);
        assertSame(mergingView, busbarSection.getNetwork());
        assertTrue(busbarSection.getTerminal() instanceof TerminalAdapter);
        assertEquals("NaN", Double.toString(busbarSection.getV()));
        assertEquals("NaN", Double.toString(busbarSection.getAngle()));
        assertSame(ConnectableType.BUSBAR_SECTION, busbarSection.getType());

        List terminals = busbarSection.getTerminals();
        for (Object term : terminals) {
            assertTrue(term instanceof TerminalAdapter);
        }

        final BusbarSectionAdder busbarAdder = vl.getNodeBreakerView().newBusbarSection();
        assertTrue(busbarAdder instanceof BusbarSectionAdderAdapter);
        busbarAdder.setId("BUSSECTION")
            .setName("bussection_name")
            .setNode(6)
            .setEnsureIdUnicity(true).add();

        final BusbarSection busbarSection2 = mergingView.getBusbarSection("BUSSECTION");
        assertNotNull(busbarSection2);
        assertTrue(busbarSection2 instanceof BusbarSectionAdapter);
        assertTrue(busbarSection2.getTerminal() instanceof TerminalAdapter);
        assertEquals("NaN", Double.toString(busbarSection2.getV()));
        assertEquals("NaN", Double.toString(busbarSection2.getAngle()));
        assertSame(ConnectableType.BUSBAR_SECTION, busbarSection2.getType());

        // Not implemented yet !
        TestUtil.notImplemented(busbarSection::remove);
    }
}
