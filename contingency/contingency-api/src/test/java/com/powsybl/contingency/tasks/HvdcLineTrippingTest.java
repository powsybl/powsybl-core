/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.tasks;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.modification.NetworkModification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class HvdcLineTrippingTest {

    @Test
    void lineTrippingTest() {
        Network network = HvdcTestNetwork.createLcc();
        HvdcLine hvdcLine = network.getHvdcLine("L");
        Terminal terminal1 = hvdcLine.getConverterStation1().getTerminal();
        Terminal terminal2 = hvdcLine.getConverterStation2().getTerminal();

        assertTrue(terminal1.isConnected());
        assertTrue(terminal2.isConnected());

        Contingency contingency = Contingency.hvdcLine("L");

        NetworkModification modification = contingency.toModification();
        modification.apply(network);

        assertFalse(terminal1.isConnected());
        assertFalse(terminal2.isConnected());

        terminal1.connect();
        terminal2.connect();
        assertTrue(terminal1.isConnected());
        assertTrue(terminal2.isConnected());

        contingency = Contingency.hvdcLine("L", "VL1");
        contingency.toModification().apply(network);

        assertFalse(terminal1.isConnected());
        assertTrue(terminal2.isConnected());

        terminal1.connect();
        terminal2.connect();
        assertTrue(terminal1.isConnected());
        assertTrue(terminal2.isConnected());

        contingency = Contingency.hvdcLine("L", "VL2");
        contingency.toModification().apply(network);

        assertTrue(terminal1.isConnected());
        assertFalse(terminal2.isConnected());
    }
}
