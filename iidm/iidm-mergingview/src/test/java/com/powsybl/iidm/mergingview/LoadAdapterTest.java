/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class LoadAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("LoadAdapterTest", "iidm");
        mergingView.merge(FictitiousSwitchFactory.create());
    }

    @Test
    public void testSetterGetter() {
        final Load load = mergingView.getLoad("CE");
        assertNotNull(load);
        assertTrue(load instanceof LoadAdapter);
        assertSame(mergingView, load.getNetwork());

        assertEquals(-72.18689, load.getP0(), 0.001);
        assertTrue(load.setP0(0.0d) instanceof LoadAdapter);
        assertEquals(0.0d, load.getP0(), 0.001);
        assertEquals(50.168945, load.getQ0(), 0.001);
        assertTrue(load.setQ0(0.0d) instanceof LoadAdapter);
        assertEquals(0.0d, load.getQ0(), 0.001);
        assertSame(LoadType.UNDEFINED, load.getLoadType());
        assertTrue(load.setLoadType(LoadType.AUXILIARY) instanceof LoadAdapter);
        assertSame(LoadType.AUXILIARY, load.getLoadType());
        assertSame(ConnectableType.LOAD, load.getType());

        assertTrue(load.getTerminal() instanceof TerminalAdapter);
        List terminals = load.getTerminals();
        for (Object terminal : terminals) {
            assertTrue(terminal instanceof TerminalAdapter);
        }
        // Not implemented yet !
        TestUtil.notImplemented(load::remove);
    }

    @Test
    public void testLoadAdder() {
        final VoltageLevel vlload = mergingView.getVoltageLevel("N");
        final LoadAdder loadAdder = vlload.newLoad().setId("LOADTEST");
        assertNotNull(loadAdder);
        assertTrue(loadAdder instanceof LoadAdderAdapter);
        loadAdder.setName("load")
            .setNode(15)
            .setP0(600.05)
            .setQ0(300.5)
            .setLoadType(LoadType.FICTITIOUS)
            .setEnsureIdUnicity(true).add();

        boolean found = false;
        for (Load load : vlload.getLoads()) {
            if ("LOADTEST".equals(load.getId())) {
                Terminal term = load.getTerminal();
                assertTrue(term instanceof TerminalAdapter);
                assertEquals(600.05, load.getP0(), 0.001);
                assertEquals(300.5, load.getQ0(), 0.001);
                assertSame(LoadType.FICTITIOUS, load.getLoadType());
                assertEquals("load", load.getName());
                found = true;
            }
        }
        assertTrue(found);

        final LoadAdder loadAdapter2 = vlload.newLoad().setId("LOADTEST2");
        loadAdapter2.setName("load2")
            .setNode(17)
            .setP0(699.05)
            .setQ0(399.5)
            .setLoadType(LoadType.AUXILIARY).add();
        found = false;
        for (Load load : vlload.getLoads()) {
            if ("LOADTEST2".equals(load.getId())) {
                assertEquals("load2", load.getName());
                found = true;
            }
        }
        assertTrue(found);

        final VoltageLevel vlvl = mergingView.getSubstation("A").newVoltageLevel()
            .setId("VLVL")
            .setNominalV(225.0)
            .setLowVoltageLimit(0.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();

        vlvl.getBusBreakerView().newBus()
            .setId("BUS")
            .setName("BUS")
            .add();

        final LoadAdder loadAdder3 = vlvl.newLoad().setId("LOADTEST3");
        loadAdder3.setName("load3")
            .setConnectableBus("BUS")
            .setP0(600.05)
            .setQ0(300.5)
            .setLoadType(LoadType.FICTITIOUS)
            .setEnsureIdUnicity(true).add();

        found = false;
        for (Load load : vlvl.getLoads()) {
            if ("LOADTEST3".equals(load.getId())) {
                assertEquals("load3", load.getName());
                found = true;
            }
        }

        final LoadAdder loadAdder4 = vlvl.newLoad().setId("LOADTEST4");
        loadAdder4.setName("load4")
            .setBus("BUS")
            .setP0(600.05)
            .setQ0(300.5)
            .setLoadType(LoadType.FICTITIOUS)
            .setEnsureIdUnicity(true).add();

        found = false;
        for (Load load : vlvl.getLoads()) {
            if ("LOADTEST4".equals(load.getId())) {
                assertEquals("load4", load.getName());
                found = true;
            }
        }
    }
}
