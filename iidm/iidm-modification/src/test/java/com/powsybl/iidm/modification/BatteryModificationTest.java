/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
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

        // With no exception thrown
        ReportNode reportNode = ReportNode.newRootReportNode()
            .withMessageTemplate("test", "test")
            .build();
        batteryModification3.apply(network, false, reportNode);
        assertEquals("Battery 'BAT_NOT_EXISTING' not found",
            reportNode.getChildren().get(0).getMessage());
    }

    @Test
    void testBatteryModificationGetters() {
        BatteryModification batteryModification = new BatteryModification(battery.getId(), 1.0, null);
        assertEquals(battery.getId(), batteryModification.getBatteryId());
        assertEquals(1.0, batteryModification.getTargetP());
        assertNull(batteryModification.getTargetQ());
    }

    @Test
    void testDryRun() {
        // Passing dryRun
        BatteryModification batteryModificationPassing = new BatteryModification(battery.getId(), 1.0, null);
        assertTrue(batteryModificationPassing.apply(network, true));

        // Useful methods for dry run
        assertFalse(batteryModificationPassing.hasImpactOnNetwork());
        assertTrue(batteryModificationPassing.isLocalDryRunPossible());

        // Failing dryRun
        ReportNode reportNode = ReportNode.newRootReportNode()
            .withMessageTemplate("", "")
            .build();
        BatteryModification batteryModificationFailing = new BatteryModification("BAT_NOT_EXISTING", 1.0, null);
        assertFalse(batteryModificationFailing.apply(network, reportNode, true));
        assertEquals("Dry-run failed for BatteryModification. The issue is: Battery 'BAT_NOT_EXISTING' not found",
            reportNode.getChildren().get(0).getChildren().get(0).getMessage());
    }
}
