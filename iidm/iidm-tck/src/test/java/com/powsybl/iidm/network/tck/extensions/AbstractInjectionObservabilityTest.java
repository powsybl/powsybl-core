/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.InjectionObservability;
import com.powsybl.iidm.network.extensions.InjectionObservabilityAdder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public abstract class AbstractInjectionObservabilityTest {

    @Test
    public void test() {
        Network network = BatteryNetworkFactory.create();
        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);
        bat.newExtension(InjectionObservabilityAdder.class)
                .withObservable(true)
                .withStandardDeviationP(0.02d)
                .withRedundantP(true)
                .withStandardDeviationQ(0.5d)
                .withRedundantQ(true)
                .withStandardDeviationV(0.0d)
                .withRedundantV(true)
                .add();
        InjectionObservability<Battery> injectionObservability = bat.getExtension(InjectionObservability.class);
        assertEquals("injectionObservability", injectionObservability.getName());
        assertEquals("BAT", injectionObservability.getExtendable().getId());

        assertTrue(injectionObservability.isObservable());
        injectionObservability.setObservable(false);
        assertFalse(injectionObservability.isObservable());

        assertEquals(0.02d, injectionObservability.getQualityP().getStandardDeviation(), 0d);
        injectionObservability.getQualityP().setStandardDeviation(0.03d);
        assertEquals(0.03d, injectionObservability.getQualityP().getStandardDeviation(), 0d);

        assertTrue(injectionObservability.getQualityP().isRedundant());
        injectionObservability.getQualityP().setRedundant(false);
        assertFalse(injectionObservability.getQualityP().isRedundant());

        assertEquals(0.5d, injectionObservability.getQualityQ().getStandardDeviation(), 0d);
        injectionObservability.getQualityQ().setStandardDeviation(0.6d);
        assertEquals(0.6d, injectionObservability.getQualityQ().getStandardDeviation(), 0d);

        assertTrue(injectionObservability.getQualityQ().isRedundant());
        injectionObservability.getQualityQ().setRedundant(false);
        assertFalse(injectionObservability.getQualityQ().isRedundant());

        assertEquals(0.0d, injectionObservability.getQualityV().getStandardDeviation(), 0d);
        injectionObservability.getQualityV().setStandardDeviation(0.01d);
        assertEquals(0.01d, injectionObservability.getQualityV().getStandardDeviation(), 0d);

        assertTrue(injectionObservability.getQualityV().isRedundant());
        injectionObservability.getQualityV().setRedundant(false);
        assertFalse(injectionObservability.getQualityV().isRedundant());
    }

    @Test
    public void testMissingQuality() {
        Network network = BatteryNetworkFactory.create();
        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);
        bat.newExtension(InjectionObservabilityAdder.class)
                .add();
        InjectionObservability<Battery> injectionObservability = bat.getExtension(InjectionObservability.class);
        assertEquals("injectionObservability", injectionObservability.getName());
        assertEquals("BAT", injectionObservability.getExtendable().getId());

        assertNull(injectionObservability.getQualityP());
        assertNull(injectionObservability.getQualityQ());
        assertNull(injectionObservability.getQualityV());

        assertSame(injectionObservability, injectionObservability.setQualityP(0.03d));
        assertEquals(0.03d, injectionObservability.getQualityP().getStandardDeviation(), 0d);
        assertSame(injectionObservability, injectionObservability.setQualityP(0.04d));
        assertEquals(0.04d, injectionObservability.getQualityP().getStandardDeviation(), 0d);

        assertFalse(injectionObservability.getQualityP().isRedundant());
        injectionObservability.getQualityP().setRedundant(true);
        assertTrue(injectionObservability.getQualityP().isRedundant());

        assertSame(injectionObservability, injectionObservability.setQualityQ(0.6d));
        assertEquals(0.6d, injectionObservability.getQualityQ().getStandardDeviation(), 0d);
        assertSame(injectionObservability, injectionObservability.setQualityQ(0.61d));
        assertEquals(0.61d, injectionObservability.getQualityQ().getStandardDeviation(), 0d);

        assertFalse(injectionObservability.getQualityQ().isRedundant());
        injectionObservability.getQualityQ().setRedundant(true);
        assertTrue(injectionObservability.getQualityQ().isRedundant());

        assertSame(injectionObservability, injectionObservability.setQualityV(0.01d));
        assertEquals(0.01d, injectionObservability.getQualityV().getStandardDeviation(), 0d);
        assertSame(injectionObservability, injectionObservability.setQualityV(0.02d));
        assertEquals(0.02d, injectionObservability.getQualityV().getStandardDeviation(), 0d);

        assertFalse(injectionObservability.getQualityV().isRedundant());
        injectionObservability.getQualityV().setRedundant(true);
        assertTrue(injectionObservability.getQualityV().isRedundant());
    }
}
