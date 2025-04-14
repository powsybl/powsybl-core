/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
class MoveFeederBayTest {
    @Test
    void testMoveInjectionNodeBreaker() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));

        // Verify initial state
        Load load = network.getLoad("load1");
        assertEquals("vl1", load.getTerminal().getVoltageLevel().getId());
        assertTrue(load.getTerminal().isConnected());
        int initialNode = load.getTerminal().getNodeBreakerView().getNode();

        // First move: from vl1 to vl2/bbs5
        new MoveFeederBayBuilder()
                .withConnectableId("load1")
                .withTargetBusOrBusBarSectionId("bbs5")
                .withTargetVoltageLevelId("vl2")
                .withTerminal(load.getTerminal())
                .build()
                .apply(network);

        // Verify first move
        assertEquals("vl2", load.getTerminal().getVoltageLevel().getId());
        assertFalse(load.getTerminal().isConnected());
        assertNotEquals(initialNode, load.getTerminal().getNodeBreakerView().getNode());

        // Get node after first move
        int intermediateNode = load.getTerminal().getNodeBreakerView().getNode();

        // Second move: from vl2/bbs5 to vl2/bbs6
        new MoveFeederBayBuilder()
                .withConnectableId("load1")
                .withTargetBusOrBusBarSectionId("bbs6")
                .withTargetVoltageLevelId("vl2")
                .withTerminal(load.getTerminal())
                .build()
                .apply(network);

        // Verify second move
        assertEquals("vl2", load.getTerminal().getVoltageLevel().getId());
        assertFalse(load.getTerminal().isConnected());
        assertNotEquals(intermediateNode, load.getTerminal().getNodeBreakerView().getNode());
    }

    @Test
    void testMoveBranchNodeBreaker() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));

        // Verify initial state
        Line line = network.getLine("line1");
        assertEquals("vl1", line.getTerminal1().getVoltageLevel().getId());
        assertEquals("vlSubst2", line.getTerminal2().getVoltageLevel().getId());
        int initialNode = line.getTerminal1().getNodeBreakerView().getNode();

        // Move the branch terminal
        new MoveFeederBayBuilder()
                .withConnectableId("line1")
                .withTargetBusOrBusBarSectionId("bbs5")
                .withTargetVoltageLevelId("vl2")
                .withTerminal(line.getTerminal1())
                .build()
                .apply(network);

        // Verify move
        assertEquals("vl2", line.getTerminal1().getVoltageLevel().getId());
        assertFalse(line.getTerminal1().isConnected());
        int movedNode = line.getTerminal1().getNodeBreakerView().getNode();
        assertNotEquals(initialNode, movedNode);

        // Verify terminal2 is unchanged
        assertEquals("vlSubst2", line.getTerminal2().getVoltageLevel().getId());
    }

    @Test
    void testMoveInjectionBusBarBreaker() {
        Network network = EurostagTutorialExample1Factory.create();

        // Verify initial state
        Generator generator = network.getGenerator("GEN");
        assertEquals("VLGEN", generator.getTerminal().getVoltageLevel().getId());
        String initialBusId = generator.getTerminal().getBusBreakerView().getBus().getId();
        boolean initialConnected = generator.getTerminal().isConnected();

        // Move generator
        new MoveFeederBayBuilder()
                .withConnectableId("GEN")
                .withTargetBusOrBusBarSectionId("NHV1")
                .withTargetVoltageLevelId("VLHV1")
                .withTerminal(generator.getTerminal())
                .build()
                .apply(network);

        // Verify move
        assertEquals("VLHV1", generator.getTerminal().getVoltageLevel().getId());
        assertEquals("NHV1", generator.getTerminal().getBusBreakerView().getBus().getId());
        assertNotEquals(initialBusId, generator.getTerminal().getBusBreakerView().getBus().getId());
        assertEquals(initialConnected, generator.getTerminal().isConnected());
    }

    @Test
    void testMoveBbsBreaker() {
        Network network = Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream("/testNetworkNodeBreaker.xiidm"));
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withMessageTemplate("reportTestMoveBbs")
                .build();

        // Verify initial state
        BusbarSection busbarSection = network.getBusbarSection("bbs6");
        assertNotNull(busbarSection);
        assertEquals("vl2", busbarSection.getTerminal().getVoltageLevel().getId());

        // Try to move a busbar section (should throw exception)
        MoveFeederBay moveFeederBay = new MoveFeederBayBuilder()
                .withConnectableId("bbs6")
                .withTargetBusOrBusBarSectionId("bbs5")
                .withTargetVoltageLevelId("vl1")
                .withTerminal(network.getLine("line1").getTerminal1())
                .build();

        // Verify exception is thrown with correct message
        PowsyblException e = assertThrows(PowsyblException.class, () -> moveFeederBay.apply(network, true, reportNode));
        assertEquals("BusbarSection connectables are not allowed as MoveFeederBay input: bbs6", e.getMessage());

        // Verify busbar section remains in original location
        assertEquals("vl2", busbarSection.getTerminal().getVoltageLevel().getId());
    }

    @Test
    void testMoveThreeWindingsTransformer() {
        // Create a network with a three-windings transformer
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        ThreeWindingsTransformer twt = network.getThreeWindingsTransformer("3WT");
        assertNotNull(twt);

        // Initial state verification
        assertEquals("VL_132", twt.getLeg1().getTerminal().getVoltageLevel().getId());
        assertEquals("VL_33", twt.getLeg2().getTerminal().getVoltageLevel().getId());
        assertEquals("VL_11", twt.getLeg3().getTerminal().getVoltageLevel().getId());

        // Save initial terminal connection status
        boolean leg1InitiallyConnected = twt.getLeg1().getTerminal().isConnected();
        String initialBusId = twt.getLeg1().getTerminal().getBusBreakerView().getBus().getId();

        // Move leg 1 (high voltage side)
        new MoveFeederBayBuilder()
                .withConnectableId("3WT")
                .withTargetBusOrBusBarSectionId("BUS_33") // Target BusBarSection in VL_33
                .withTargetVoltageLevelId("VL_33")
                .withTerminal(twt.getLeg1().getTerminal())
                .build()
                .apply(network);

        // Verify leg 1 has been moved
        assertEquals("VL_33", twt.getLeg1().getTerminal().getVoltageLevel().getId());
        assertEquals("BUS_33", twt.getLeg1().getTerminal().getBusBreakerView().getBus().getId());
        assertNotEquals(initialBusId, twt.getLeg1().getTerminal().getBusBreakerView().getBus().getId());
        assertEquals(leg1InitiallyConnected, twt.getLeg1().getTerminal().isConnected());

        // Verify the other legs are unchanged
        assertEquals("VL_33", twt.getLeg2().getTerminal().getVoltageLevel().getId());
        assertEquals("VL_11", twt.getLeg3().getTerminal().getVoltageLevel().getId());

        // Move leg 3 (low voltage side)
        boolean leg3InitiallyConnected = twt.getLeg3().getTerminal().isConnected();
        String leg3InitialBusId = twt.getLeg3().getTerminal().getBusBreakerView().getBus().getId();

        new MoveFeederBayBuilder()
                .withConnectableId("3WT")
                .withTargetBusOrBusBarSectionId("BUS_132") // Target busbar in VL_132
                .withTargetVoltageLevelId("VL_132")
                .withTerminal(twt.getLeg3().getTerminal())
                .build()
                .apply(network);

        // Verify leg 3 has been moved
        assertEquals("VL_132", twt.getLeg3().getTerminal().getVoltageLevel().getId());
        assertEquals("BUS_132", twt.getLeg3().getTerminal().getBusBreakerView().getBus().getId());
        assertNotEquals(leg3InitialBusId, twt.getLeg3().getTerminal().getBusBreakerView().getBus().getId());
        assertEquals(leg3InitiallyConnected, twt.getLeg3().getTerminal().isConnected());
    }
}
