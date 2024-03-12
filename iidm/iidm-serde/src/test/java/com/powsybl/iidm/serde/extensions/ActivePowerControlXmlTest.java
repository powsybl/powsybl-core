/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.impl.extensions.ActivePowerControlImpl;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.IidmVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
class ActivePowerControlXmlTest extends AbstractIidmSerDeTest {

    private Network network;

    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        network = BatteryNetworkFactory.create();

        Battery bat = network.getBattery("BAT");
        bat.addExtension(ActivePowerControl.class, new ActivePowerControlImpl<>(bat, true, 4.0, 1.2));

        Generator generator = network.getGenerator("GEN");
        generator.addExtension(ActivePowerControl.class, new ActivePowerControlImpl<>(generator, false, 3.0, 1.0));

    }

    @Test
    void test() throws IOException {
        Network network2 = allFormatsRoundTripTest(network, "/activePowerControlRoundTripRef.xml", CURRENT_IIDM_VERSION);

        Battery bat2 = network2.getBattery("BAT");
        assertNotNull(bat2);
        ActivePowerControl<Battery> activePowerControl2 = bat2.getExtension(ActivePowerControl.class);
        assertNotNull(activePowerControl2);
    }

    @Test
    void testIidmV10() throws IOException {
        Network network2 = allFormatsRoundTripTest(network, "/batteryNetworkWithActivePowerControlRoundTripRef.xml", IidmVersion.V_1_0);

        Battery bat2 = network2.getBattery("BAT");
        assertNotNull(bat2);
        ActivePowerControl<Battery> activePowerControl2 = bat2.getExtension(ActivePowerControl.class);
        assertNotNull(activePowerControl2);
    }

}
