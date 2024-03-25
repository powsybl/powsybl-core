/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.tasks;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import com.powsybl.iidm.modification.NetworkModification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class DanglingLineTrippingTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = DanglingLineNetworkFactory.create();
    }

    @Test
    void dlTrippingTest() {
        assertTrue(network.getDanglingLine("DL").getTerminal().isConnected());

        Contingency contingency = Contingency.danglingLine("DL");

        NetworkModification task = contingency.toModification();
        task.apply(network);

        assertFalse(network.getDanglingLine("DL").getTerminal().isConnected());
    }
}
