/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.tasks;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import com.powsybl.iidm.modification.NetworkModification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
class StaticVarCompensatorTrippingTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = SvcTestCaseFactory.create();
    }

    @Test
    void generatorTrippingTest() {
        assertTrue(network.getStaticVarCompensator("SVC2").getTerminal().isConnected());

        Contingency contingency = Contingency.staticVarCompensator("SVC2");

        NetworkModification modification = contingency.toModification();
        modification.apply(network);

        assertFalse(network.getStaticVarCompensator("SVC2").getTerminal().isConnected());
    }
}
