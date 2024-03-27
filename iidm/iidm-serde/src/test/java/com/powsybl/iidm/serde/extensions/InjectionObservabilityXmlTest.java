/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.InjectionObservability;
import com.powsybl.iidm.network.impl.extensions.InjectionObservabilityImpl;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class InjectionObservabilityXmlTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = BatteryNetworkFactory.create();
        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);

        InjectionObservability<Battery> injectionObservability = new InjectionObservabilityImpl<>(bat, true);
        injectionObservability.setQualityP(0.03d, false);
        injectionObservability.setQualityQ(0.6d, false);

        bat.addExtension(InjectionObservability.class, injectionObservability);

        Generator generator = network.getGenerator("GEN");
        generator.addExtension(InjectionObservability.class, new InjectionObservabilityImpl<>(generator, false, 0.02d, true, 0.5d, true, 0.0d, true));
        Network network2 = allFormatsRoundTripTest(network, "/injectionObservabilityRoundTripRef.xml", CURRENT_IIDM_VERSION);

        Battery bat2 = network2.getBattery("BAT");
        assertNotNull(bat2);
        InjectionObservability <Battery> injectionObservability2 = bat2.getExtension(InjectionObservability.class);
        assertNotNull(injectionObservability2);

        assertEquals(injectionObservability.isObservable(), injectionObservability2.isObservable());
        assertEquals(injectionObservability.getQualityP().getStandardDeviation(), injectionObservability2.getQualityP().getStandardDeviation(), 0.0d);
        assertEquals(injectionObservability.getQualityQ().getStandardDeviation(), injectionObservability2.getQualityQ().getStandardDeviation(), 0.0d);
        assertEquals(injectionObservability.getQualityP().isRedundant(), injectionObservability2.getQualityP().isRedundant());
        assertEquals(injectionObservability.getQualityQ().isRedundant(), injectionObservability2.getQualityQ().isRedundant());
        assertEquals(injectionObservability.getQualityV(), injectionObservability2.getQualityV());

        assertEquals(injectionObservability.getName(), injectionObservability2.getName());
    }

    @Test
    void invalidTest() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> NetworkSerDe.read(getClass().getResourceAsStream(getVersionedNetworkPath("/injectionObservabilityRoundTripRefInvalid.xml", CURRENT_IIDM_VERSION))));
        assertEquals("Unknown element name 'qualityZ' in 'injectionObservability'", e.getMessage());
    }
}
