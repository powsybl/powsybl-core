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
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class ShuntCompensatorTrippingTest {

    private Network network;

    @Before
    public void setUp() {
        network = HvdcTestNetwork.createLcc();
    }

    @Test
    public void generatorTrippingTest() {
        assertTrue(network.getShuntCompensator("C1_Filter1").getTerminal().isConnected());

        new ShuntCompensatorTripping("C1_Filter1").apply(network);

        assertFalse(network.getShuntCompensator("C1_Filter1").getTerminal().isConnected());
    }

    @Test(expected = PowsyblException.class)
    public void unknownShuntCompensatorTest() {
        ShuntCompensatorTripping tripping = new ShuntCompensatorTripping("C_Filter");
        tripping.apply(network);
    }
}
