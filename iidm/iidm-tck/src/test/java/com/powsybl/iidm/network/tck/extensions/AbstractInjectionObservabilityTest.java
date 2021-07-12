/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
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
                .withStandardDeviationP(0.02f)
                .withRedundantP(true)
                .withStandardDeviationQ(0.5f)
                .withRedundantQ(true)
                .withStandardDeviationV(0.0f)
                .withRedundantV(true)
                .add();
        InjectionObservability<Battery> injectionObservability = bat.getExtension(InjectionObservability.class);
        assertEquals("injectionObservability", injectionObservability.getName());
        assertEquals("BAT", injectionObservability.getExtendable().getId());

        assertTrue(injectionObservability.isObservable());
        injectionObservability.setObservable(false);
        assertFalse(injectionObservability.isObservable());

        assertEquals(0.02f, injectionObservability.getStandardDeviationP(), 0f);
        injectionObservability.setStandardDeviationP(0.03f);
        assertEquals(0.03f, injectionObservability.getStandardDeviationP(), 0f);

        assertTrue(injectionObservability.isRedundantP());
        injectionObservability.setRedundantP(false);
        assertFalse(injectionObservability.isRedundantP());

        assertEquals(0.5f, injectionObservability.getStandardDeviationQ(), 0f);
        injectionObservability.setStandardDeviationQ(0.6f);
        assertEquals(0.6f, injectionObservability.getStandardDeviationQ(), 0f);

        assertTrue(injectionObservability.isRedundantQ());
        injectionObservability.setRedundantQ(false);
        assertFalse(injectionObservability.isRedundantQ());

        assertEquals(0.0f, injectionObservability.getStandardDeviationV(), 0f);
        injectionObservability.setStandardDeviationV(0.01f);
        assertEquals(0.01f, injectionObservability.getStandardDeviationV(), 0f);

        assertTrue(injectionObservability.isRedundantV());
        injectionObservability.setRedundantV(false);
        assertFalse(injectionObservability.isRedundantV());
    }
}
