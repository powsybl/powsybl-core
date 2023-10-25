/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.math.graph.TraversalType;
import com.powsybl.math.graph.TraverseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Small network to test topology traversal by depth first or breadth first
 * Below is a diagram of the network:
 * <div>
 *    <object data="doc-files/traversalByDepthOrBreadthNetwork.svg" type="image/svg+xml"></object>
 * </div>
 *
 * @author Franck Lecuyer {@literal <franck.lecuyer at rte-france.com>}
 */
class TopologyTraverseDepthAndBreadthTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = Network.create("test", "test");
        Substation s1 = network.newSubstation().setId("S1").setCountry(Country.FR).add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL1").setNominalV(400f).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl1.getNodeBreakerView().newBusbarSection().setId("VL1_B1").setNode(0).add();
        vl1.getNodeBreakerView().newBusbarSection().setId("VL1_B2").setNode(1).add();
        vl1.newGenerator().setId("G1").setNode(2).setMinP(0).setMaxP(1).setTargetP(1).setTargetQ(0).setVoltageRegulatorOn(false).add();
        vl1.newGenerator().setId("G2").setNode(5).setMinP(0).setMaxP(1).setTargetP(1).setTargetQ(0).setVoltageRegulatorOn(false).add();
        vl1.getNodeBreakerView().newBreaker().setId("VL1_BREAKER3").setOpen(false).setNode1(3).setNode2(9).add();
        vl1.getNodeBreakerView().newBreaker().setId("VL1_BREAKER1").setOpen(false).setNode1(3).setNode2(4).add();
        vl1.getNodeBreakerView().newBreaker().setId("VL1_BREAKER2").setOpen(false).setNode1(9).setNode2(10).add();
        vl1.getNodeBreakerView().newBreaker().setId("VL1_BREAKER7").setOpen(false).setNode1(6).setNode2(12).add();
        vl1.getNodeBreakerView().newBreaker().setId("VL1_BREAKER5").setOpen(false).setNode1(6).setNode2(7).add();
        vl1.getNodeBreakerView().newBreaker().setId("VL1_BREAKER6").setOpen(false).setNode1(12).setNode2(13).add();
        vl1.getNodeBreakerView().newDisconnector().setId("VL1_DISCONNECTOR1").setOpen(false).setNode1(2).setNode2(3).add();
        vl1.getNodeBreakerView().newDisconnector().setId("VL1_DISCONNECTOR2").setOpen(false).setNode1(8).setNode2(9).add();
        vl1.getNodeBreakerView().newDisconnector().setId("VL1_DISCONNECTOR3").setOpen(false).setNode1(1).setNode2(4).add();
        vl1.getNodeBreakerView().newDisconnector().setId("VL1_DISCONNECTOR4").setOpen(false).setNode1(0).setNode2(10).add();
        vl1.getNodeBreakerView().newDisconnector().setId("VL1_DISCONNECTOR5").setOpen(false).setNode1(5).setNode2(6).add();
        vl1.getNodeBreakerView().newDisconnector().setId("VL1_DISCONNECTOR6").setOpen(false).setNode1(1).setNode2(7).add();
        vl1.getNodeBreakerView().newDisconnector().setId("VL1_DISCONNECTOR7").setOpen(false).setNode1(11).setNode2(12).add();
        vl1.getNodeBreakerView().newDisconnector().setId("VL1_DISCONNECTOR8").setOpen(false).setNode1(0).setNode2(13).add();

        Substation s2 = network.newSubstation().setId("S2").setCountry(Country.FR).add();
        VoltageLevel vl2 = s2.newVoltageLevel().setId("VL2").setNominalV(400f).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl2.getNodeBreakerView().newBusbarSection().setId("VL2_B1").setNode(0).add();
        vl2.getNodeBreakerView().newBreaker().setId("VL2_BREAKER1").setOpen(false).setNode1(1).setNode2(2).add();
        vl2.getNodeBreakerView().newDisconnector().setId("VL2_DISCONNECTOR1").setOpen(false).setNode1(0).setNode2(2).add();

        Substation s3 = network.newSubstation().setId("S3").setCountry(Country.FR).add();
        VoltageLevel vl3 = s3.newVoltageLevel().setId("VL3").setNominalV(400f).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl3.getNodeBreakerView().newBusbarSection().setId("VL3_B1").setNode(0).add();
        vl3.getNodeBreakerView().newBreaker().setId("VL3_BREAKER1").setOpen(false).setNode1(1).setNode2(2).add();
        vl3.getNodeBreakerView().newDisconnector().setId("VL3_DISCONNECTOR1").setOpen(false).setNode1(0).setNode2(2).add();

        network.newLine().setId("L1").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(8).setNode2(1).setR(1).setX(1).setG1(0).setB1(0).setG2(0).setB2(0).add();
        network.newLine().setId("L2").setVoltageLevel1("VL1").setVoltageLevel2("VL3").setNode1(11).setNode2(1).setR(1).setX(1).setG1(0).setB1(0).setG2(0).setB2(0).add();
    }

    private static class BusbarSectionFinderTraverser implements Terminal.TopologyTraverser {
        private final boolean onlyConnectedBbs;
        private String firstTraversedBbsId;

        public BusbarSectionFinderTraverser(boolean onlyConnectedBbs) {
            this.onlyConnectedBbs = onlyConnectedBbs;
        }

        @Override
        public TraverseResult traverse(Terminal terminal, boolean connected) {
            if (terminal.getConnectable().getType() == IdentifiableType.BUSBAR_SECTION) {
                firstTraversedBbsId = terminal.getConnectable().getId();
                return TraverseResult.TERMINATE_TRAVERSER;
            }
            return TraverseResult.CONTINUE;
        }

        @Override
        public TraverseResult traverse(Switch aSwitch) {
            if (onlyConnectedBbs && aSwitch.isOpen()) {
                return TraverseResult.TERMINATE_PATH;
            }
            return TraverseResult.CONTINUE;
        }

        public String getFirstTraversedBbsId() {
            return firstTraversedBbsId;
        }
    }

    private static String getBusbarSectionId(Terminal terminal, TraversalType traversalType) {
        BusbarSectionFinderTraverser connectedBusbarSectionFinder = new BusbarSectionFinderTraverser(terminal.isConnected());
        terminal.traverse(connectedBusbarSectionFinder, traversalType);
        return connectedBusbarSectionFinder.getFirstTraversedBbsId();
    }

    @Test
    void testTraverseByDepthFirst() {
        assertEquals("VL1_B1", getBusbarSectionId(network.getGenerator("G1").getTerminal(), TraversalType.DEPTH_FIRST));
        assertEquals("VL1_B1", getBusbarSectionId(network.getGenerator("G2").getTerminal(), TraversalType.DEPTH_FIRST));
        assertEquals("VL1_B2", getBusbarSectionId(network.getLine("L1").getTerminal("VL1"), TraversalType.DEPTH_FIRST));
        assertEquals("VL2_B1", getBusbarSectionId(network.getLine("L1").getTerminal("VL2"), TraversalType.DEPTH_FIRST));
        assertEquals("VL1_B2", getBusbarSectionId(network.getLine("L2").getTerminal("VL1"), TraversalType.DEPTH_FIRST));
        assertEquals("VL3_B1", getBusbarSectionId(network.getLine("L2").getTerminal("VL3"), TraversalType.DEPTH_FIRST));
    }

    @Test
    void testTraverseByBreadthFirst() {
        assertEquals("VL1_B2", getBusbarSectionId(network.getGenerator("G1").getTerminal(), TraversalType.BREADTH_FIRST));
        assertEquals("VL1_B2", getBusbarSectionId(network.getGenerator("G2").getTerminal(), TraversalType.BREADTH_FIRST));
        assertEquals("VL1_B1", getBusbarSectionId(network.getLine("L1").getTerminal("VL1"), TraversalType.BREADTH_FIRST));
        assertEquals("VL2_B1", getBusbarSectionId(network.getLine("L1").getTerminal("VL2"), TraversalType.BREADTH_FIRST));
        assertEquals("VL1_B1", getBusbarSectionId(network.getLine("L2").getTerminal("VL1"), TraversalType.BREADTH_FIRST));
        assertEquals("VL3_B1", getBusbarSectionId(network.getLine("L2").getTerminal("VL3"), TraversalType.BREADTH_FIRST));
    }
}
