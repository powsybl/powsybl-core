/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Nicolas Pierre <nicolas.pierre at artelys.com>
 */
class ConnectionShuntModificationTest {
    private String shuntId;
    private Network network;

    @BeforeEach
    void setup() {
        network = FourSubstationsNodeBreakerFactory.create();
        shuntId = network.getShuntCompensatorStream().findFirst().get().getId();
    }

    @Test
    void testReconnection() {
        network.getShuntCompensator(shuntId).getTerminal().disconnect();
        new ConnectionShuntModification(shuntId, true).apply(network);
        Assertions.assertTrue(network.getShuntCompensator(shuntId).getTerminal().isConnected());
    }

    @Test
    void testDisconnection() {
        new ConnectionShuntModification(shuntId, false).apply(network);
        Assertions.assertFalse(network.getShuntCompensator(shuntId).getTerminal().isConnected());

    }
}
