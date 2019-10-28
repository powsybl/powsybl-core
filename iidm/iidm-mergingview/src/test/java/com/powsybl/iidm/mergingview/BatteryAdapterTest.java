/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

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
        assertEquals("BAT", battery.getId());
        assertEquals(9999.99, battery.getP0(), 0.001);
        assertTrue(battery.setP0(0.0d) instanceof BatteryAdapter);
        assertEquals(0.0d, battery.getP0(), 0.001);
        assertEquals(9999.99, battery.getQ0(), 0.001);
        assertTrue(battery.setQ0(0.0d) instanceof BatteryAdapter);
        assertEquals(0.0d, battery.getQ0(), 0.001);
        assertEquals(-9999.99, battery.getMinP(), 0.001);
        assertTrue(battery.setMinP(-9999.95) instanceof BatteryAdapter);
        assertEquals(-9999.95, battery.getMinP(), 0.001);
        assertEquals(9999.99, battery.getMaxP(), 0.001);
        assertTrue(battery.setMaxP(9999.95) instanceof BatteryAdapter);
        assertEquals(9999.95, battery.getMaxP(), 0.001);
        assertEquals(9999.99, battery.getReactiveLimits().getMaxQ(1), 0.001);
        assertEquals(-9999.99, battery.getReactiveLimits().getMinQ(1), 0.001);
        battery.newMinMaxReactiveLimits().setMaxQ(9999.95).setMinQ(-9999.95).add();
        assertEquals(9999.95, battery.getReactiveLimits().getMaxQ(1), 0.001);
        assertEquals(-9999.95, battery.getReactiveLimits().getMinQ(1), 0.001);
        assertEquals(battery.getType(), ConnectableType.BATTERY);

        assertNotNull(battery.newReactiveCapabilityCurve().beginPoint()
            .setMaxQ(99999.99)
            .setMinQ(-99999.99)
            .setP(0.0d)
            .endPoint()
            .beginPoint()
            .setMaxQ(99999.95)
            .setMinQ(-99999.95)
            .setP(0.1d)
            .endPoint()
            .add());

        battery.getReactiveLimits().getMinQ(0.0d);
        assertEquals(99999.99, battery.getReactiveLimits().getMaxQ(0.0d), 0.001);
        assertEquals(-99999.99, battery.getReactiveLimits().getMinQ(0.0d), 0.001);
        assertEquals(99999.95, battery.getReactiveLimits().getMaxQ(0.1d), 0.001);
        assertEquals(-99999.95, battery.getReactiveLimits().getMinQ(0.1d), 0.001);

        assertTrue(battery.getTerminal() instanceof TerminalAdapter);

        /* Not implemented (Terminal adapter)
        assertEquals(-605, battery.getTerminal().getP(), 0.001);
        assertEquals(-225, battery.getTerminal().getQ(), 0.001); */

        List terminals = battery.getTerminals();
        for (Object terminal : terminals) {
            assertTrue(terminal instanceof TerminalAdapter);
        }

        Iterable<Battery> batteries = mergingView.getBatteries();
        int size = 0;
        for (Battery bat : batteries) {
            assertTrue(bat instanceof BatteryAdapter);
            size++;
        }
        assertEquals(size, 2);

        // Not implemented yet !
        TestUtil.notImplemented(battery::remove);
    }

    @Test
    public void testBatteryAdder() {
        final VoltageLevel vlbat = mergingView.getVoltageLevel("VLBAT");
        final BatteryAdder batteryAdder = vlbat.newBattery().setId("BATEST");
        assertNotNull(batteryAdder);
        assertTrue(batteryAdder instanceof BatteryAdderAdapter);
        batteryAdder.setName("battery")
            .setBus("NBAT")
            .setMaxP(9999.99)
            .setMinP(-9999.99)
            .setP0(15)
            .setQ0(-15)
            .setEnsureIdUnicity(true).add();
        boolean found = false;
        for (Battery battery : vlbat.getBatteries()) {
            if ("BATEST".equals(battery.getId())) {
                assertEquals(15, battery.getP0(), 0.001);
                assertEquals(-15, battery.getQ0(), 0.001);
                assertEquals(9999.99, battery.getMaxP(), 0.001);
                assertEquals(-9999.99, battery.getMinP(), 0.001);
                assertEquals("battery", battery.getName());
                found = true;
            }
        }
        assertTrue(found);

        final BatteryAdder batteryAdder2 = vlbat.newBattery().setId("BATEST2");
        batteryAdder2.setName("battery2").setConnectableBus("NBAT")
            .setMaxP(9999.99)
            .setMinP(-9999.99)
            .setP0(15)
            .setQ0(-15).add();
        found = false;
        for (Battery battery : vlbat.getBatteries()) {
            if ("BATEST2".equals(battery.getId())) {
                assertEquals("battery2", battery.getName());
                found = true;
            }
        }
        assertTrue(found);
    }
}
