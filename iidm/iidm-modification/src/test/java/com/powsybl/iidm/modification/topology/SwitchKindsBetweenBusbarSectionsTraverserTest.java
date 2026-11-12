/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
class SwitchKindsBetweenBusbarSectionsTraverserTest {

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

        // create switches between bar 1 and 2
        createSwitch(s1vl1, "BBS1_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 0, 1);
        createSwitch(s1vl1, "BBS21_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 3, 2);
        createSwitch(s1vl1, "BBS1_BBS2_BREAKER", SwitchKind.BREAKER, false, 1, 2);

        // create switches between bar 2 and 3
        createSwitch(s1vl1, "BBS23_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 3, 6);

        // create switches between bar 4 and 5
        createSwitch(s1vl1, "BBS4_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 9, 10);
        createSwitch(s1vl1, "BBS54_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 12, 11);
        createSwitch(s1vl1, "BBS4_BBS5_BREAKER", SwitchKind.BREAKER, false, 10, 11);

        // create switches between bar 5 and 6
        createSwitch(s1vl1, "BBS56_DISCONNECTOR", SwitchKind.DISCONNECTOR, false, 12, 15);

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
        SwitchKindsBetweenBusbarSectionsTraverser traverser = new SwitchKindsBetweenBusbarSectionsTraverser(busbarSection);
        busbarSection.getTerminal().traverse(traverser);
        List<SwitchKind> leftSwitchKindBbs1 = traverser.getLeftSwitchesBetweenBusbar();
        List<SwitchKind> rightSwitchKindsBbs1 = traverser.getRightSwitchesBetweenBusbar();
        assertTrue(leftSwitchKindBbs1.isEmpty());
        assertEquals(2, rightSwitchKindsBbs1.size());
        assertEquals(List.of(SwitchKind.BREAKER, SwitchKind.DISCONNECTOR), rightSwitchKindsBbs1);

        BusbarSection busbarSection2 = network.getBusbarSection("BBS2");
        SwitchKindsBetweenBusbarSectionsTraverser traverser2 = new SwitchKindsBetweenBusbarSectionsTraverser(busbarSection2);
        busbarSection2.getTerminal().traverse(traverser2);
        List<SwitchKind> leftSwitchKindBbs2 = traverser2.getLeftSwitchesBetweenBusbar();
        List<SwitchKind> rightSwitchKindsBbs2 = traverser2.getRightSwitchesBetweenBusbar();
        assertEquals(1, leftSwitchKindBbs2.size());
        assertEquals(1, rightSwitchKindsBbs2.size());
        assertEquals(List.of(SwitchKind.BREAKER), leftSwitchKindBbs2);
        assertEquals(List.of(SwitchKind.DISCONNECTOR), rightSwitchKindsBbs2);

        BusbarSection busbarSection3 = network.getBusbarSection("BBS6");
        SwitchKindsBetweenBusbarSectionsTraverser traverser3 = new SwitchKindsBetweenBusbarSectionsTraverser(busbarSection3);
        busbarSection3.getTerminal().traverse(traverser3);
        List<SwitchKind> leftSwitchKindBbs3 = traverser3.getLeftSwitchesBetweenBusbar();
        List<SwitchKind> rightSwitchKindsBbs3 = traverser3.getRightSwitchesBetweenBusbar();
        assertTrue(rightSwitchKindsBbs3.isEmpty());
        assertEquals(2, leftSwitchKindBbs3.size());
        assertEquals(List.of(SwitchKind.DISCONNECTOR, SwitchKind.BREAKER), leftSwitchKindBbs3);

    }
}
