/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.tasks;

import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class BusbarSectionTrippingTest extends AbstractTrippingTest {

    @Test
    public void busbarSectionTrippingTest() throws IOException {
        busbarSectionTrippingTest("D", Sets.newHashSet("BD", "BL"));
        busbarSectionTrippingTest("O", Sets.newHashSet("BJ", "BT"));
        busbarSectionTrippingTest("P", Sets.newHashSet("BJ", "BL", "BV", "BX", "BZ"));
    }

    public void busbarSectionTrippingTest(String bbsId, Set<String> switchIds) {
        Network network = FictitiousSwitchFactory.create();
        List<Boolean> expectedSwitchStates = getSwitchStates(network, switchIds);

        BusbarSectionTripping tripping = new BusbarSectionTripping(bbsId);

        Set<Switch> switchesToOpen = new HashSet<>();
        Set<Terminal> terminalsToDisconnect = new HashSet<>();
        tripping.traverse(network, null, switchesToOpen, terminalsToDisconnect);
        assertEquals(switchIds, switchesToOpen.stream().map(Switch::getId).collect(Collectors.toSet()));
        assertEquals(Collections.emptySet(), terminalsToDisconnect);

        tripping.modify(network, null);
        for (String id : switchIds) {
            assertTrue(network.getSwitch(id).isOpen());
        }

        List<Boolean> switchStates = getSwitchStates(network, switchIds);
        assertEquals(expectedSwitchStates, switchStates);
    }

    @Test(expected = PowsyblException.class)
    public void unknownBusbarSectionTrippingTest() {
        Network network = FictitiousSwitchFactory.create();

        BusbarSectionTripping tripping = new BusbarSectionTripping("bbs");
        tripping.modify(network, null);
    }
}
