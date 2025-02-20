/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class DanglingLineTrippingTest extends AbstractTrippingTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = DanglingLineNetworkFactory.create();
    }

    @Test
    void dlTrippingTest() {
        assertTrue(network.getDanglingLine("DL").getTerminal().isConnected());

        DanglingLineTripping tripping = new DanglingLineTripping("DL");
        tripping.apply(network);

        assertFalse(network.getDanglingLine("DL").getTerminal().isConnected());
    }

    @Test
    void unknownDlTest() {
        DanglingLineTripping tripping = new DanglingLineTripping("DL_THAT_DO_NOT_EXIST");
        NamingStrategy namingStrategy = new DefaultNamingStrategy();
        assertThrows(PowsyblException.class, () -> tripping.apply(network, namingStrategy, true, ReportNode.NO_OP));
        assertDoesNotThrow(() -> tripping.apply(network));
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new DanglingLineTripping("ID");
        assertEquals("DanglingLineTripping", networkModification.getName());
    }
}
