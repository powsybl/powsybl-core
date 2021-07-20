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

        assertEquals(0.02d, injectionObservability.getStandardDeviationP(), 0d);
        injectionObservability.setStandardDeviationP(0.03d);
        assertEquals(0.03d, injectionObservability.getStandardDeviationP(), 0d);

        assertTrue(injectionObservability.isRedundantP());
        injectionObservability.setRedundantP(false);
        assertFalse(injectionObservability.isRedundantP());

        assertEquals(0.5d, injectionObservability.getStandardDeviationQ(), 0d);
        injectionObservability.setStandardDeviationQ(0.6d);
        assertEquals(0.6d, injectionObservability.getStandardDeviationQ(), 0d);

        assertTrue(injectionObservability.isRedundantQ());
        injectionObservability.setRedundantQ(false);
        assertFalse(injectionObservability.isRedundantQ());

        assertEquals(0.0d, injectionObservability.getStandardDeviationV(), 0d);
        injectionObservability.setStandardDeviationV(0.01d);
        assertEquals(0.01d, injectionObservability.getStandardDeviationV(), 0d);

        assertTrue(injectionObservability.isRedundantV());
        injectionObservability.setRedundantV(false);
        assertFalse(injectionObservability.isRedundantV());
    }
}
