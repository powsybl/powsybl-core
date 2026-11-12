/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.InjectionObservability;
import com.powsybl.iidm.network.extensions.InjectionObservabilityAdder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
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

        assertTrue(injectionObservability.getQualityP().isPresent());
        assertTrue(injectionObservability.getQualityQ().isPresent());
        assertTrue(injectionObservability.getQualityV().isPresent());

        assertEquals(injectionObservability.getNullableQualityP(), injectionObservability.getQualityP().orElse(null));
        assertEquals(injectionObservability.getNullableQualityQ(), injectionObservability.getQualityQ().orElse(null));
        assertEquals(injectionObservability.getNullableQualityV(), injectionObservability.getQualityV().orElse(null));

        assertEquals(0.02d, injectionObservability.getNullableQualityP().getStandardDeviation(), 0d);
        injectionObservability.getNullableQualityP().setStandardDeviation(0.03d);
        assertEquals(0.03d, injectionObservability.getNullableQualityP().getStandardDeviation(), 0d);

        assertTrue(injectionObservability.getNullableQualityP().isRedundant().isPresent());
        assertTrue(injectionObservability.getNullableQualityP().isRedundant().get());
        injectionObservability.getNullableQualityP().setRedundant(false);
        assertTrue(injectionObservability.getNullableQualityP().isRedundant().isPresent());
        assertFalse(injectionObservability.getNullableQualityP().isRedundant().get());

        assertEquals(0.5d, injectionObservability.getNullableQualityQ().getStandardDeviation(), 0d);
        injectionObservability.getNullableQualityQ().setStandardDeviation(0.6d);
        assertEquals(0.6d, injectionObservability.getNullableQualityQ().getStandardDeviation(), 0d);

        assertTrue(injectionObservability.getNullableQualityQ().isRedundant().isPresent());
        assertTrue(injectionObservability.getNullableQualityQ().isRedundant().get());
        injectionObservability.getNullableQualityQ().setRedundant(false);
        assertTrue(injectionObservability.getNullableQualityQ().isRedundant().isPresent());
        assertFalse(injectionObservability.getNullableQualityQ().isRedundant().get());

        assertEquals(0.0d, injectionObservability.getNullableQualityV().getStandardDeviation(), 0d);
        injectionObservability.getNullableQualityV().setStandardDeviation(0.01d);
        assertEquals(0.01d, injectionObservability.getNullableQualityV().getStandardDeviation(), 0d);

        assertTrue(injectionObservability.getNullableQualityV().isRedundant().isPresent());
        assertTrue(injectionObservability.getNullableQualityV().isRedundant().get());
        injectionObservability.getNullableQualityV().setRedundant(false);
        assertTrue(injectionObservability.getNullableQualityV().isRedundant().isPresent());
        assertFalse(injectionObservability.getNullableQualityV().isRedundant().get());
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

        assertFalse(injectionObservability.getQualityP().isPresent());
        assertFalse(injectionObservability.getQualityQ().isPresent());
        assertFalse(injectionObservability.getQualityV().isPresent());

        assertNull(injectionObservability.getNullableQualityP());
        assertNull(injectionObservability.getNullableQualityQ());
        assertNull(injectionObservability.getNullableQualityV());

        assertSame(injectionObservability, injectionObservability.setQualityP(0.03d));
        assertEquals(0.03d, injectionObservability.getNullableQualityP().getStandardDeviation(), 0d);
        assertSame(injectionObservability, injectionObservability.setQualityP(0.04d));
        assertEquals(0.04d, injectionObservability.getNullableQualityP().getStandardDeviation(), 0d);

        assertFalse(injectionObservability.getNullableQualityP().isRedundant().isPresent());
        injectionObservability.getNullableQualityP().setRedundant(true);
        assertTrue(injectionObservability.getNullableQualityP().isRedundant().isPresent());
        assertTrue(injectionObservability.getNullableQualityP().isRedundant().get());

        assertSame(injectionObservability, injectionObservability.setQualityQ(0.6d));
        assertEquals(0.6d, injectionObservability.getNullableQualityQ().getStandardDeviation(), 0d);
        assertSame(injectionObservability, injectionObservability.setQualityQ(0.61d));
        assertEquals(0.61d, injectionObservability.getNullableQualityQ().getStandardDeviation(), 0d);

        assertFalse(injectionObservability.getNullableQualityQ().isRedundant().isPresent());
        injectionObservability.getNullableQualityQ().setRedundant(true);
        assertTrue(injectionObservability.getNullableQualityQ().isRedundant().isPresent());
        assertTrue(injectionObservability.getNullableQualityQ().isRedundant().get());

        assertSame(injectionObservability, injectionObservability.setQualityV(0.01d));
        assertEquals(0.01d, injectionObservability.getNullableQualityV().getStandardDeviation(), 0d);
        assertSame(injectionObservability, injectionObservability.setQualityV(0.02d));
        assertEquals(0.02d, injectionObservability.getNullableQualityV().getStandardDeviation(), 0d);

        assertFalse(injectionObservability.getNullableQualityV().isRedundant().isPresent());
        injectionObservability.getNullableQualityV().setRedundant(true);
        assertTrue(injectionObservability.getNullableQualityV().isRedundant().isPresent());
        assertTrue(injectionObservability.getNullableQualityV().isRedundant().get());
    }

    @Test
    public void testRedundancy() {
        Network network = EurostagTutorialExample1Factory.create();
        InjectionObservabilityAdder adder = network.getLoad("LOAD").newExtension(InjectionObservabilityAdder.class);
        adder.withStandardDeviationV(0.5)
                .withRedundantV(false)
                .withRedundantP(true)
                .withStandardDeviationQ(0.2)
                .add();
        InjectionObservability injectionObservability = network.getLoad("LOAD").getExtension(InjectionObservability.class);
        assertNull(injectionObservability.getNullableQualityP());
        assertFalse((Boolean) injectionObservability.getNullableQualityV().isRedundant().get());
        assertEquals(0.5, injectionObservability.getNullableQualityV().getStandardDeviation(), 0.01);
        assertFalse(injectionObservability.getNullableQualityQ().isRedundant().isPresent());
        assertEquals(0.2, injectionObservability.getNullableQualityQ().getStandardDeviation(), 0.01);
    }
}
