/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.impl.extensions.ActivePowerControlImpl;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serde.AbstractIidmSerDeTest.getVersionedNetworkPath;
import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
class ActivePowerControlXmlTest extends AbstractSerDeTest {

    @Test
    void test() throws IOException {
        Network network = BatteryNetworkFactory.create();
        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);

        ActivePowerControl<Battery> activePowerControl = new ActivePowerControlImpl<>(bat, true, 4.0, 1.2);
        bat.addExtension(ActivePowerControl.class, activePowerControl);

        Generator generator = network.getGenerator("GEN");
        generator.addExtension(ActivePowerControl.class, new ActivePowerControlImpl<>(generator, false, 3.0, 1.0));

        Network network2 = roundTripXmlTest(network,
                NetworkSerDe::writeAndValidate,
                NetworkSerDe::read,
                getVersionedNetworkPath("/activePowerControlRoundTripRef.xml", CURRENT_IIDM_XML_VERSION));

        Battery bat2 = network2.getBattery("BAT");
        assertNotNull(bat2);
        ActivePowerControl<Battery> activePowerControl2 = bat2.getExtension(ActivePowerControl.class);
        assertNotNull(activePowerControl2);

        assertEquals(activePowerControl.isParticipate(), activePowerControl2.isParticipate());
        assertEquals(activePowerControl.getDroop(), activePowerControl2.getDroop(), 0.0);
        assertEquals(activePowerControl.getParticipationFactor(), activePowerControl2.getParticipationFactor(), 0.0);
        assertEquals(activePowerControl.getName(), activePowerControl2.getName());
    }
}
