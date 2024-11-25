/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class BatteryModificationTest {

    private Network network;
    private Battery battery;

    @BeforeEach
    public void setUp() {
        network = BatteryNetworkFactory.create();
        assertTrue(network.getBatteryCount() > 0);
        battery = network.getBatteries().iterator().next();
        battery.setTargetQ(0);
    }

    @Test
    void testBatteryModification() {
        BatteryModification batteryModification = new BatteryModification(battery.getId(), 1.0, null);
        assertEquals(9999.99, battery.getTargetP());
        batteryModification.apply(network);
        assertEquals(1., battery.getTargetP());

        BatteryModification batteryModification2 = new BatteryModification(battery.getId(), null, 2.0);
        assertEquals(0, battery.getTargetQ());
        batteryModification2.apply(network);
        assertEquals(2., battery.getTargetQ());

        BatteryModification batteryModification3 = new BatteryModification("BAT_NOT_EXISTING", null, 2.0);
        PowsyblException exception = assertThrows(PowsyblException.class, () -> batteryModification3.apply(network, true, ReportNode.NO_OP));
        assertEquals("Battery 'BAT_NOT_EXISTING' not found", exception.getMessage());
    }

    @Test
    void testBatteryModificationGetters() {
        BatteryModification batteryModification = new BatteryModification(battery.getId(), 1.0, null);
        assertEquals(battery.getId(), batteryModification.getBatteryId());
        assertEquals(1.0, batteryModification.getTargetP());
        assertNull(batteryModification.getTargetQ());
    }

    @Test
    void testHasImpact() {
        BatteryModification batteryModification1 = new BatteryModification("BAT_NOT_EXISTING", null, 2.0);
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, batteryModification1.hasImpactOnNetwork(network));

        BatteryModification batteryModification2 = new BatteryModification(battery.getId(), 1.0, null);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, batteryModification2.hasImpactOnNetwork(network));

        BatteryModification batteryModification3 = new BatteryModification(battery.getId(), null, 2.0);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, batteryModification3.hasImpactOnNetwork(network));

        BatteryModification batteryModification4 = new BatteryModification(battery.getId(), null, null);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, batteryModification4.hasImpactOnNetwork(network));

        BatteryModification batteryModification5 = new BatteryModification(battery.getId(), 9999.99, 0.0);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, batteryModification5.hasImpactOnNetwork(network));
    }
}
