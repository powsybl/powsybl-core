/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.topology.DefaultNamingStrategy;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
class StaticVarCompensatorTrippingTest extends AbstractTrippingTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = SvcTestCaseFactory.create();
    }

    @Test
    void generatorTrippingTest() {
        assertTrue(network.getStaticVarCompensator("SVC2").getTerminal().isConnected());

        StaticVarCompensatorTripping tripping = new StaticVarCompensatorTripping("SVC2");
        tripping.apply(network);

        assertFalse(network.getStaticVarCompensator("SVC2").getTerminal().isConnected());
    }

    @Test
    void unknownShuntCompensatorTest() {
        StaticVarCompensatorTripping tripping = new StaticVarCompensatorTripping("SVC");
        assertThrows(PowsyblException.class, () -> tripping.apply(network, new DefaultNamingStrategy(), true, ReportNode.NO_OP));
        assertDoesNotThrow(() -> tripping.apply(network));
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new StaticVarCompensatorTripping("ID");
        assertEquals("StaticVarCompensatorTripping", networkModification.getName());
    }

}
