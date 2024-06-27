/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
import java.util.OptionalDouble;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
class ActivePowerControlXmlTest extends AbstractIidmSerDeTest {

    private Network network;

    @Override
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
    void testTargetPLimits() throws IOException {
        network.getGenerator("GEN").getExtension(ActivePowerControl.class).setMaxTargetP(800.);
        network.getBattery("BAT").getExtension(ActivePowerControl.class).setMinTargetP(10.);
        Network network2 = allFormatsRoundTripTest(network, "/activePowerControlWithLimitRoundTripRef.xml", CURRENT_IIDM_VERSION);

        Generator gen2 = network2.getGenerator("GEN");
        assertNotNull(gen2);
        ActivePowerControl<Generator> activePowerControl1 = gen2.getExtension(ActivePowerControl.class);
        assertNotNull(activePowerControl1);
        assertEquals(OptionalDouble.of(800), activePowerControl1.getMaxTargetP());
        assertTrue(activePowerControl1.getMinTargetP().isEmpty());

        Battery bat2 = network2.getBattery("BAT");
        assertNotNull(bat2);
        ActivePowerControl<Battery> activePowerControl2 = bat2.getExtension(ActivePowerControl.class);
        assertNotNull(activePowerControl2);
        assertTrue(activePowerControl2.getMaxTargetP().isEmpty());
        assertEquals(OptionalDouble.of(10), activePowerControl2.getMinTargetP());
    }

    @Test
    void testIidmV12() throws IOException {
        Network network2 = allFormatsRoundTripTest(network, "/activePowerControlRoundTripRef.xml", IidmVersion.V_1_12);

        Battery bat2 = network2.getBattery("BAT");
        assertNotNull(bat2);
        ActivePowerControl<Battery> activePowerControl2 = bat2.getExtension(ActivePowerControl.class);
        assertNotNull(activePowerControl2);
        assertTrue(activePowerControl2.getMaxTargetP().isEmpty());
        assertTrue(activePowerControl2.getMinTargetP().isEmpty());
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
