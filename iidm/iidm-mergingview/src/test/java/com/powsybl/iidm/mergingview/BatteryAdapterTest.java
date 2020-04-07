/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BatteryAdapterTest {
    private MergingView mergingView;

    @Before
    public void setup() {
        mergingView = MergingView.create("BatteryAdapterTest", "iidm");
        mergingView.merge(NoEquipmentNetworkFactory.create());
    }

    @Test
    public void testSetterGetter() {
        double delta = 0.0;
        final VoltageLevel vlbat = mergingView.getVoltageLevel("vl1");
        final Battery battery = vlbat.newBattery()
                                         .setId("BATEST")
                                         .setName("BATEST")
                                         .setFictitious(true)
                                         .setBus("busA")
                                         .setMaxP(9999.99d)
                                         .setMinP(-9999.99d)
                                         .setP0(15.0d)
                                         .setQ0(-15.0d)
                                         .setEnsureIdUnicity(true)
                                     .add();

        assertSame(battery, mergingView.getBattery("BATEST"));
        assertEquals(ConnectableType.BATTERY, battery.getType());
        assertTrue(battery instanceof BatteryAdapter);
        assertSame(mergingView, battery.getNetwork());
        assertEquals("BATEST", battery.getId());
        assertTrue(battery.isFictitious());
        assertEquals(15.0d, battery.getP0(), delta);
        assertNotNull(battery.setP0(0.0d));
        assertEquals(0.0d, battery.getP0(), delta);
        assertEquals(-15.0d, battery.getQ0(), delta);
        assertNotNull(battery.setQ0(0.0d));
        assertEquals(0.0d, battery.getQ0(), delta);
        assertEquals(-9999.99d, battery.getMinP(), delta);
        assertNotNull(battery.setMinP(-9999.95d));
        assertEquals(-9999.95d, battery.getMinP(), delta);
        assertEquals(9999.99d, battery.getMaxP(), delta);
        assertNotNull(battery.setMaxP(9999.95d));
        assertEquals(9999.95d, battery.getMaxP(), delta);
        MinMaxReactiveLimits mmrl = battery.newMinMaxReactiveLimits()
                                               .setMaxQ(9999.95d)
                                               .setMinQ(-9999.95d)
                                           .add();
        assertSame(mmrl, battery.getReactiveLimits(MinMaxReactiveLimits.class));
        assertEquals(mmrl.getMaxQ(), battery.getReactiveLimits().getMaxQ(1), delta);
        assertEquals(mmrl.getMinQ(), battery.getReactiveLimits().getMinQ(1), delta);

        ReactiveCapabilityCurve rcc = battery.newReactiveCapabilityCurve()
                                                 .beginPoint()
                                                     .setMaxQ(99999.99)
                                                     .setMinQ(-99999.99)
                                                     .setP(0.0d)
                                                 .endPoint()
                                                 .beginPoint()
                                                     .setMaxQ(99999.95)
                                                     .setMinQ(-99999.95)
                                                     .setP(0.1d)
                                                 .endPoint()
                                             .add();
        assertNotNull(rcc);

        assertTrue(battery.getTerminal() instanceof TerminalAdapter);
        battery.getTerminals().forEach(t -> {
            assertTrue(t instanceof TerminalAdapter);
            assertNotNull(t);
        });

        mergingView.getBatteries().forEach(b -> {
            assertTrue(b instanceof BatteryAdapter);
            assertNotNull(b);
        });

        // Not implemented yet !
        TestUtil.notImplemented(battery::remove);
    }
}
