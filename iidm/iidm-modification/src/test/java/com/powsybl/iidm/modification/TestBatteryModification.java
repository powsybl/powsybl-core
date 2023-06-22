/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Nicolas PIERRE <nicolas.pierre at artelys.com>
 */
class TestBatteryModification {

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
    public void testBatteryModification() {
        BatteryModification batteryModification = new BatteryModification(battery.getId(), 2.);
        assertEquals(0, battery.getTargetQ());
        batteryModification.apply(network);
        assertEquals(2., battery.getTargetQ());
    }
}
