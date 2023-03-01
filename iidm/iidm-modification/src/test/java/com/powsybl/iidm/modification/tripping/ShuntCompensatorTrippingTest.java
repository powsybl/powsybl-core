/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
class ShuntCompensatorTrippingTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = HvdcTestNetwork.createLcc();
    }

    @Test
    void generatorTrippingTest() {
        assertTrue(network.getShuntCompensator("C1_Filter1").getTerminal().isConnected());

        new ShuntCompensatorTripping("C1_Filter1").apply(network);

        assertFalse(network.getShuntCompensator("C1_Filter1").getTerminal().isConnected());
    }

    @Test
    void unknownShuntCompensatorTest() {
        ShuntCompensatorTripping tripping = new ShuntCompensatorTripping("C_Filter");
        assertThrows(PowsyblException.class, () -> tripping.apply(network));
    }
}
