/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.tasks;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.modification.NetworkModification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Bertrand Rix <bertrand.rix at artelys.com>
 */
class BusTrippingTest {

    @Test
    void busTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();
        for (Terminal t : network.getBusBreakerView().getBus("NGEN").getConnectedTerminals()) {
            assertTrue(t.isConnected());
        }

        Contingency contingency = Contingency.bus("NGEN");

        NetworkModification task = contingency.toModification();
        task.apply(network);

        for (Terminal t : network.getBusBreakerView().getBus("NGEN").getConnectedTerminals()) {
            assertFalse(t.isConnected());
        }
    }
}
