/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.iidm.modification.tripping.BatteryTripping;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
class BatteryContingencyTest {
    @Test
    void test() {
        var batContingency = new BatteryContingency("bat");
        assertEquals("bat", batContingency.getId());
        assertEquals(ContingencyElementType.BATTERY, batContingency.getType());

        assertNotNull(batContingency.toModification());
        assertTrue(batContingency.toModification() instanceof BatteryTripping);

        new EqualsTester()
                .addEqualityGroup(new BatteryContingency("foo"), new BatteryContingency("foo"))
                .addEqualityGroup(new BatteryContingency("bar"), new BatteryContingency("bar"))
                .testEquals();
    }

    @Test
    void testContingencyElement() {
        Network network = BatteryNetworkFactory.create();
        Battery battery = network.getBattery("BAT");
        assertNotNull(battery);
        ContingencyElement element = ContingencyElement.of(battery);
        assertNotNull(element);
        assertTrue(element instanceof BatteryContingency);
        assertEquals("BAT", element.getId());
        assertEquals(ContingencyElementType.BATTERY, element.getType());
    }
}
