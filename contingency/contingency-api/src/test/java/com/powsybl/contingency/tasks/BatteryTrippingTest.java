/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.tasks;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class BatteryTrippingTest {

    @Test
    public void test() {
        Network network = BatteryNetworkFactory.create();
        assertTrue(network.getBattery("BAT").getTerminal().isConnected());

        Contingency contingency = Contingency.battery("BAT");

        NetworkModification task = contingency.toModification();
        task.apply(network);

        assertFalse(network.getBattery("BAT").getTerminal().isConnected());
    }
}
