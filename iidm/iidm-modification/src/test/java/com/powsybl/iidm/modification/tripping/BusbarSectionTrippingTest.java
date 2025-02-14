/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.topology.DefaultNamingStrategy;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class BusbarSectionTrippingTest extends AbstractTrippingTest {

    @Test
    void busbarSectionTrippingTest() throws IOException {
        busbarSectionTrippingTest("D", Sets.newHashSet("BD", "BL"));
        busbarSectionTrippingTest("O", Sets.newHashSet("BJ", "BT"));
        busbarSectionTrippingTest("P", Sets.newHashSet("BJ", "BL", "BV", "BX", "BZ"));
    }

    void busbarSectionTrippingTest(String bbsId, Set<String> switchIds) {
        Network network = FictitiousSwitchFactory.create();
        List<Boolean> expectedSwitchStates = getSwitchStates(network, switchIds);

        BusbarSectionTripping tripping = new BusbarSectionTripping(bbsId);

        Set<Switch> switchesToOpen = new HashSet<>();
        Set<Terminal> terminalsToDisconnect = new HashSet<>();
        tripping.traverse(network, switchesToOpen, terminalsToDisconnect);
        assertEquals(switchIds, switchesToOpen.stream().map(Switch::getId).collect(Collectors.toSet()));
        assertEquals(Collections.emptySet(), terminalsToDisconnect);

        tripping.apply(network);
        for (String id : switchIds) {
            assertTrue(network.getSwitch(id).isOpen());
        }

        List<Boolean> switchStates = getSwitchStates(network, switchIds);
        assertEquals(expectedSwitchStates, switchStates);
    }

    @Test
    void unknownBusbarSectionTrippingTest() {
        Network network = FictitiousSwitchFactory.create();

        BusbarSectionTripping tripping = new BusbarSectionTripping("bbs");
        assertThrows(PowsyblException.class, () -> tripping.apply(network, new DefaultNamingStrategy(), true, ReportNode.NO_OP));
        assertDoesNotThrow(() -> tripping.apply(network));
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new BusbarSectionTripping("ID");
        assertEquals("BusbarSectionTripping", networkModification.getName());
    }
}
