/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
public abstract class AbstractActivePowerControlTest {

    @Test
    public void test() {
        Network network = BatteryNetworkFactory.create();
        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);
        bat.newExtension(ActivePowerControlAdder.class)
                .withDroop(4f)
                .withParticipate(true)
                .add();
        ActivePowerControl<Battery> activePowerControl = bat.getExtension(ActivePowerControl.class);
        assertEquals("activePowerControl", activePowerControl.getName());
        assertEquals("BAT", activePowerControl.getExtendable().getId());

        assertTrue(activePowerControl.isParticipate());
        assertEquals(4f, activePowerControl.getDroop(), 0f);
        activePowerControl.setParticipate(false);
        assertFalse(activePowerControl.isParticipate());
        activePowerControl.setDroop(6f);
        assertEquals(6f, activePowerControl.getDroop(), 0f);
    }
}
