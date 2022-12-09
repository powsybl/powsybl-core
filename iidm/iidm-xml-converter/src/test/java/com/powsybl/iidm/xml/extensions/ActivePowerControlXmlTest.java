/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.impl.extensions.ActivePowerControlImpl;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.iidm.xml.AbstractXmlConverterTest.getVersionedNetworkPath;
import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
public class ActivePowerControlXmlTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        Network network = BatteryNetworkFactory.create();
        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);

        ActivePowerControl<Battery> activePowerControl = new ActivePowerControlImpl<>(bat, true, 4f, 1.2f, 1.3f, 1.4f, 1);
        bat.addExtension(ActivePowerControl.class, activePowerControl);

        Generator generator = network.getGenerator("GEN");
        generator.addExtension(ActivePowerControl.class, new ActivePowerControlImpl<>(generator, false, 3, 1f, 2f, 3f, 0));

        Network network2 = roundTripXmlTest(network,
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionedNetworkPath("/activePowerControlRoundTripRef.xml", CURRENT_IIDM_XML_VERSION));

        Battery bat2 = network2.getBattery("BAT");
        assertNotNull(bat2);
        ActivePowerControl<Battery> activePowerControl2 = bat2.getExtension(ActivePowerControl.class);
        assertNotNull(activePowerControl2);

        assertEquals(activePowerControl.isParticipate(), activePowerControl2.isParticipate());
        assertEquals(activePowerControl.getDroop(), activePowerControl2.getDroop(), 0f);
        assertEquals(activePowerControl.getShortPF(), activePowerControl2.getShortPF(), 0f);
        assertEquals(activePowerControl.getNormalPF(), activePowerControl2.getNormalPF(), 0f);
        assertEquals(activePowerControl.getLongPF(), activePowerControl2.getLongPF(), 0f);
        assertEquals(activePowerControl.getReferencePriority(), activePowerControl2.getReferencePriority());
        assertEquals(activePowerControl.getName(), activePowerControl2.getName());
    }
}
