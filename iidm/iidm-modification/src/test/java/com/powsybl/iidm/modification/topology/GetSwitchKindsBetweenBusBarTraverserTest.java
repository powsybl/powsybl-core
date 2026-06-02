/**
 * Copyright (c) 2026, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
class GetSwitchKindsBetweenBusBarTraverserTest {

    /*
    vl1 :
      busbar section 1 - busbar section 2 - busbar section 3
         |                  |
      coupler             coupler
         |                  |
      busbar section 4 - busbar section 5 - busbar section 6
     */
    Network createNetwork() {
        NetworkFactory networkFactory = NetworkFactory.findDefault();
        Network network = networkFactory.createNetwork("fourSubstations", "test");
        network.setCaseDate(ZonedDateTime.parse("2017-06-25T17:43:00.000+01:00"));
        network.setForecastDistance(0);

        Substation s1 = network.newSubstation()
                .setId("S1")
                .add();

        VoltageLevel s1vl1 = s1.newVoltageLevel()
                .setId("S1VL1")
                .setNominalV(225.0)
                .setLowVoltageLimit(220.0)
                .setHighVoltageLimit(240.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        // BBS1
        BusbarSection bbs1 = s1vl1.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setName("BBS1")
                .setNode(0)
                .add();
        bbs1.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(1).withSectionIndex(1).add();
        // BBS2
        BusbarSection bbs2 = s1vl1.getNodeBreakerView().newBusbarSection()
                .setId("BBS2")
                .setName("BBS2")
                .setNode(3)
                .add();
        bbs2.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(1).withSectionIndex(2).add();
        // BBS3
        BusbarSection bbs3 = s1vl1.getNodeBreakerView().newBusbarSection()
                .setId("BBS3")
                .setName("BBS3")
                .setNode(6)
                .add();
        bbs3.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(1).withSectionIndex(3).add();
        // BBS4
        BusbarSection bbs4 = s1vl1.getNodeBreakerView().newBusbarSection()
                .setId("BBS4")
                .setName("BBS4")
                .setNode(9)
                .add();
        bbs4.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(2).withSectionIndex(1).add();
        // BBS5
        BusbarSection bbs5 = s1vl1.getNodeBreakerView().newBusbarSection()
                .setId("BBS5")
                .setName("BBS5")
                .setNode(12)
                .add();
        bbs5.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(2).withSectionIndex(2).add();
        // BBS6
        BusbarSection bbs6 = s1vl1.getNodeBreakerView().newBusbarSection()
                .setId("BBS6")
                .setName("BBS6")
                .setNode(15)
                .add();
        bbs6.newExtension(BusbarSectionPositionAdder.class).withBusbarIndex(2).withSectionIndex(3).add();

        // Add a coupler between the busbar 1 and 4 sections
        createSwitch(s1vl1, "BBS1_COUPLER_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 16);
        createSwitch(s1vl1, "BBS3_COUPLER_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 9, 17);
        createSwitch(s1vl1, "BBS1_BBS3_COUPLER", SwitchKind.BREAKER, false, 16, 17);

        // Add a coupler between the busbar 2 and 5 sections
        createSwitch(s1vl1, "BBS2_COUPLER_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 3, 18);
        createSwitch(s1vl1, "BBS4_COUPLER_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 12, 19);
        createSwitch(s1vl1, "BBS2_BBS4_COUPLER", SwitchKind.BREAKER, false, 18, 19);

        // create switchs between bar 1 and 2
        createSwitch(s1vl1, "BBS1_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(s1vl1, "BBS21_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 3, 2);
        createSwitch(s1vl1, "BBS1_BBS2_BREAKER", SwitchKind.BREAKER, false, 1, 2);

        // create switchs between bar 2 and 3
        createSwitch(s1vl1, "BBS23_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 3, 4);
        createSwitch(s1vl1, "BBS3_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 6, 5);
        createSwitch(s1vl1, "BBS2_BBS3_BREAKER", SwitchKind.BREAKER, false, 4, 5);

        // create switchs between bar 4 and 5
        createSwitch(s1vl1, "BBS4_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 9, 10);
        createSwitch(s1vl1, "BBS54_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 12, 11);
        createSwitch(s1vl1, "BBS4_BBS5_BREAKER", SwitchKind.BREAKER, false, 10, 11);

        // create switchs between bar 5 and 6
        createSwitch(s1vl1, "BBS56_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 12, 13);
        createSwitch(s1vl1, "BBS6_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 15, 14);
        createSwitch(s1vl1, "BBS5_BBS6_BREAKER", SwitchKind.BREAKER, false, 13, 14);

        // Connect a load on BBS1
        createSwitch(s1vl1, "BBS1_LD1_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 20);
        createSwitch(s1vl1, "BBS1_LD1_BREAKER", SwitchKind.BREAKER, false, 20, 21);
        Load load6 = s1vl1.newLoad()
                .setId("LD1")
                .setLoadType(LoadType.UNDEFINED)
                .setP0(240)
                .setQ0(10)
                .setNode(21)
                .add();
        load6.getTerminal().setP(240.0).setQ(10.0);
        return network;
    }

    private static void createSwitch(VoltageLevel vl, String id, SwitchKind kind, boolean open, int node1, int node2) {
        vl.getNodeBreakerView().newSwitch()
                .setId(id)
                .setName(id)
                .setKind(kind)
                .setRetained(kind.equals(SwitchKind.BREAKER))
                .setOpen(open)
                .setFictitious(false)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }

    @Test
    void test() {
        Network network = createNetwork();
        BusbarSection busbarSection = network.getBusbarSection("BBS1");
        GetSwitchKindsBetweenBusBarTraverser traverser = new GetSwitchKindsBetweenBusBarTraverser(busbarSection);
        busbarSection.getTerminal().traverse(traverser);
        List<List<String>> switchIdsBbs1 = traverser.getSwitchesBetweenBusBarSections().stream().map(switchList -> switchList.stream().map(Switch::getId).collect(Collectors.toList())).toList();
        assertEquals(2, switchIdsBbs1.size());
        assertEquals(3, switchIdsBbs1.get(0).size());
        assertEquals(3, switchIdsBbs1.get(1).size());
        assertTrue(switchIdsBbs1.containsAll(List.of(List.of("BBS1_DISCONNECTOR", "BBS1_BBS2_BREAKER", "BBS21_DISCONNECTOR"),
                List.of("BBS23_DISCONNECTOR", "BBS2_BBS3_BREAKER", "BBS3_DISCONNECTOR"))));

        BusbarSection busbarSection2 = network.getBusbarSection("BBS2");
        GetSwitchKindsBetweenBusBarTraverser traverser2 = new GetSwitchKindsBetweenBusBarTraverser(busbarSection2);
        busbarSection2.getTerminal().traverse(traverser2);
        List<List<String>> switchIdsBbs2 = traverser2.getSwitchesBetweenBusBarSections().stream().map(switchList -> switchList.stream().map(Switch::getId).collect(Collectors.toList())).toList();
        assertEquals(2, switchIdsBbs2.size());
        assertEquals(3, switchIdsBbs2.get(0).size());
        assertEquals(3, switchIdsBbs2.get(1).size());
        assertTrue(switchIdsBbs1.getFirst().containsAll(switchIdsBbs2.getFirst()));
        assertTrue(switchIdsBbs1.get(1).containsAll(switchIdsBbs2.get(1)));

        BusbarSection busbarSection3 = network.getBusbarSection("BBS4");
        GetSwitchKindsBetweenBusBarTraverser traverser3 = new GetSwitchKindsBetweenBusBarTraverser(busbarSection3);
        busbarSection3.getTerminal().traverse(traverser3);
        List<List<String>> switchIdsBbs3 = traverser3.getSwitchesBetweenBusBarSections().stream().map(switchList -> switchList.stream().map(Switch::getId).collect(Collectors.toList())).toList();
        assertEquals(2, switchIdsBbs3.size());
        assertEquals(3, switchIdsBbs3.get(0).size());
        assertEquals(3, switchIdsBbs3.get(1).size());
        assertTrue(switchIdsBbs3.containsAll(List.of(List.of("BBS4_DISCONNECTOR", "BBS4_BBS5_BREAKER", "BBS54_DISCONNECTOR"),
                List.of("BBS56_DISCONNECTOR", "BBS5_BBS6_BREAKER", "BBS6_DISCONNECTOR"))));
    }
}
