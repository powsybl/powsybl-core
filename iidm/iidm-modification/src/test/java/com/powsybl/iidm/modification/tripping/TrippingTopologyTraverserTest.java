/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TrippingTopologyTraverserTest {

    @Test
    void test() {
        Network network = Network.create("test", "");
        Substation s1 = network.newSubstation()
                .setId("s1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setNominalV(400)
                .add();
        VoltageLevel vl2 = s1.newVoltageLevel()
                .setId("vl2")
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setNominalV(400)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        Line l1 = network.newLine()
                .setId("l12")
                .setVoltageLevel1("vl1")
                .setConnectableBus1("b1")
                .setBus1("b1")
                .setVoltageLevel2("vl2")
                .setConnectableBus2("b2")
                .setBus2("b2")
                .setR(1)
                .setX(1)
                .setG1(0)
                .setG2(0)
                .setB1(0)
                .setB2(0)
                .add();

        Set<Switch> switchesToOpen = new HashSet<>();
        Set<Terminal> terminalsToDisconnect = new HashSet<>();
        TrippingTopologyTraverser.traverse(l1.getTerminal1(), switchesToOpen, terminalsToDisconnect, null);
        assertTrue(switchesToOpen.isEmpty());
        assertEquals(1, terminalsToDisconnect.size());
        assertEquals("BusTerminal[b1]", terminalsToDisconnect.iterator().next().toString());

        l1.getTerminal1().disconnect();
        assertNull(l1.getTerminal1().getBusBreakerView().getBus());

        switchesToOpen.clear();
        terminalsToDisconnect.clear();
        TrippingTopologyTraverser.traverse(l1.getTerminal1(), switchesToOpen, terminalsToDisconnect, null);
        assertTrue(switchesToOpen.isEmpty());
        assertTrue(terminalsToDisconnect.isEmpty());

        switchesToOpen.clear();
        terminalsToDisconnect.clear();
        TrippingTopologyTraverser.traverse(l1.getTerminal2(), switchesToOpen, terminalsToDisconnect, null);
        assertTrue(switchesToOpen.isEmpty());
        assertEquals(1, terminalsToDisconnect.size());
        assertEquals("BusTerminal[b2]", terminalsToDisconnect.iterator().next().toString());
    }
}
