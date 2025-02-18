/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class LoadTrippingTest extends AbstractTrippingTest {

    @Test
    void loadTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();
        assertTrue(network.getLoad("LOAD").getTerminal().isConnected());

        new LoadTripping("LOAD").apply(network);

        assertFalse(network.getLoad("LOAD").getTerminal().isConnected());
    }

    @Test
    void unknownLoadTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();
        NamingStrategy namingStrategy = new DefaultNamingStrategy();

        LoadTripping tripping = new LoadTripping("generator");
        assertThrows(PowsyblException.class, () -> tripping.apply(network, namingStrategy, true, ReportNode.NO_OP));
        assertDoesNotThrow(() -> tripping.apply(network));
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new LoadTripping("ID");
        assertEquals("LoadTripping", networkModification.getName());
    }
}
