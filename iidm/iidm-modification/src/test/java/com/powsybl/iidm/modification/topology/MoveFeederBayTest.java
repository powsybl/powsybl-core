/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.PowsyblCoreTestReportResourceBundle;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
class MoveFeederBayTest {
    private static final String NODE_BREAKER_FILE = "/testNetworkNodeBreaker.xiidm";

    private Network loadNodeBreakerNetwork() {
        return Network.read("testNetworkNodeBreaker.xiidm", getClass().getResourceAsStream(NODE_BREAKER_FILE));
    }

    @Test
    void shouldMoveLoadInNodeBreakerNetwork() {
        // Given
        Network network = loadNodeBreakerNetwork();
        Load load = network.getLoad("load1");
        String initialVoltageLevel = load.getTerminal().getVoltageLevel().getId();
        int initialNode = load.getTerminal().getNodeBreakerView().getNode();

        // When: move from vl1 to vl2/bbs5
        moveFeederBay(load.getId(), "bbs5", "vl2", load.getTerminal(), network);

        // Then
        assertTerminalMoved(load.getTerminal(), "vl2", initialVoltageLevel, load.getTerminal().getBusBreakerView().getBus().getId(), "bbs5", initialNode);
        assertFalse(load.getTerminal().isConnected());

        // Further move to bbs6
        int intermediateNode = load.getTerminal().getNodeBreakerView().getNode();
        moveFeederBay(load.getId(), "bbs6", "vl2", load.getTerminal(), network);

        // Verify second move
        assertEquals("vl2", load.getTerminal().getVoltageLevel().getId());
        assertFalse(load.getTerminal().isConnected());
        assertNotEquals(intermediateNode, load.getTerminal().getNodeBreakerView().getNode());
    }

    @Test
    void shouldMoveLineInNodeBreakerNetwork() {
        // Given
        Network network = loadNodeBreakerNetwork();
        Line line = network.getLine("line1");
        String initialVoltageLevel = line.getTerminal1().getVoltageLevel().getId();
        int initialNode = line.getTerminal1().getNodeBreakerView().getNode();
        String terminal2VoltageLevel = line.getTerminal2().getVoltageLevel().getId();

        // When
        moveFeederBay(line.getId(), "bbs5", "vl2", line.getTerminal1(), network);

        // Then
        assertTerminalMoved(line.getTerminal1(), "vl2", initialVoltageLevel, line.getTerminal1().getBusBreakerView().getBus().getId(), "bbs5", initialNode);
        assertFalse(line.getTerminal1().isConnected());
        assertEquals(terminal2VoltageLevel, line.getTerminal2().getVoltageLevel().getId());
    }

    @Test
    void shouldMoveGeneratorInBusBreakerNetwork() {
        // Given
        Network network = EurostagTutorialExample1Factory.create();
        Generator generator = network.getGenerator("GEN");
        String initialBusId = generator.getTerminal().getBusBreakerView().getBus().getId();
        boolean initialConnected = generator.getTerminal().isConnected();

        // When
        moveFeederBay(generator.getId(), "NHV1", "VLHV1", generator.getTerminal(), network);

        // Then
        assertEquals("VLHV1", generator.getTerminal().getVoltageLevel().getId());
        assertEquals("NHV1", generator.getTerminal().getBusBreakerView().getBus().getId());
        assertNotEquals(initialBusId, generator.getTerminal().getBusBreakerView().getBus().getId());
        assertEquals(initialConnected, generator.getTerminal().isConnected());
    }

    @Test
    void shouldFailWhenMovingBusbarSection() {
        // Given
        Network network = loadNodeBreakerNetwork();
        ReportNode reportNode = createReportNode();
        BusbarSection busbarSection = network.getBusbarSection("bbs6");
        String initialVoltageLevel = busbarSection.getTerminal().getVoltageLevel().getId();

        // When/Then
        MoveFeederBay moveFeederBay = createMoveFeederBay("bbs6", "bbs5", "vl1",
                network.getLine("line1").getTerminal1());

        PowsyblException exception = assertThrows(PowsyblException.class,
                () -> moveFeederBay.apply(network, true, reportNode));
        assertEquals("BusbarSection connectables are not allowed as MoveFeederBay input: bbs6",
                exception.getMessage());
        assertEquals(initialVoltageLevel, busbarSection.getTerminal().getVoltageLevel().getId());
    }

    @Test
    void shouldMoveTwoWindingsTransformerInNodeBreakerNetwork() {
        // Given
        Network network = loadNodeBreakerNetwork();
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer("trf1");
        String initialVoltageLevelT1 = transformer.getTerminal1().getVoltageLevel().getId();
        String voltageLevel2 = transformer.getTerminal2().getVoltageLevel().getId();
        int initialNode = transformer.getTerminal1().getNodeBreakerView().getNode();

        // When
        moveFeederBay(transformer.getId(), "bbs5", "vl2", transformer.getTerminal1(), network);

        // Then
        assertTerminalMoved(transformer.getTerminal1(), "vl2", initialVoltageLevelT1, transformer.getTerminal1().getBusBreakerView().getBus().getId(), "bbs5", initialNode);
        assertFalse(transformer.getTerminal1().isConnected());
        assertEquals(voltageLevel2, transformer.getTerminal2().getVoltageLevel().getId());
    }

    @Test
    void shouldMoveThreeWindingsTransformerLegInNodeBreakerNetwork() {
        // Given
        Network network = loadNodeBreakerNetwork();
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer("trf6");
        String initialVoltageLevel = transformer.getLeg1().getTerminal().getVoltageLevel().getId();
        int initialNode = transformer.getLeg1().getTerminal().getNodeBreakerView().getNode();

        // When
        moveFeederBay(transformer.getId(), "bbs5", "vl2", transformer.getLeg1().getTerminal(), network);

        // Then
        assertTerminalMoved(transformer.getLeg1().getTerminal(), "vl2", initialVoltageLevel, transformer.getLeg1().getTerminal().getBusBreakerView().getBus().getId(), "bbs5", initialNode);
        assertFalse(transformer.getLeg1().getTerminal().isConnected());
    }

    @Test
    void shouldMoveThreeWindingsTransformerInBusBreakerNetwork() {
        // Given
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        ThreeWindingsTransformer transformer = network.getThreeWindingsTransformer("3WT");

        // Initial state verification for leg1
        String leg1InitialBusId = transformer.getLeg1().getTerminal().getBusBreakerView().getBus().getId();
        boolean leg1InitiallyConnected = transformer.getLeg1().getTerminal().isConnected();

        // When moving leg1
        moveFeederBay(transformer.getId(), "BUS_33", "VL_33", transformer.getLeg1().getTerminal(), network);

        // Then
        assertEquals("VL_33", transformer.getLeg1().getTerminal().getVoltageLevel().getId());
        assertEquals("BUS_33", transformer.getLeg1().getTerminal().getBusBreakerView().getBus().getId());
        assertNotEquals(leg1InitialBusId, transformer.getLeg1().getTerminal().getBusBreakerView().getBus().getId());
        assertEquals(leg1InitiallyConnected, transformer.getLeg1().getTerminal().isConnected());

        // When moving leg3
        String leg3InitialBusId = transformer.getLeg3().getTerminal().getBusBreakerView().getBus().getId();
        boolean leg3InitiallyConnected = transformer.getLeg3().getTerminal().isConnected();

        moveFeederBay(transformer.getId(), "BUS_132", "VL_132", transformer.getLeg3().getTerminal(), network);

        // Then
        assertEquals("VL_132", transformer.getLeg3().getTerminal().getVoltageLevel().getId());
        assertEquals("BUS_132", transformer.getLeg3().getTerminal().getBusBreakerView().getBus().getId());
        assertNotEquals(leg3InitialBusId, transformer.getLeg3().getTerminal().getBusBreakerView().getBus().getId());
        assertEquals(leg3InitiallyConnected, transformer.getLeg3().getTerminal().isConnected());
    }

    @Test
    void shouldReportCorrectImpact() {
        // Given
        Network network = FourSubstationsNodeBreakerFactory.create();
        Terminal terminal = network.getLoad("LD1").getTerminal();

        // When/Then - Wrong ID
        NetworkModification modificationWrongId = new MoveFeederBay(
                "WRONG_ID", "S4VL1_BBS", "S1VL2", terminal);
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED,
                modificationWrongId.hasImpactOnNetwork(network));

        // When/Then - Same connectable/busbar
        NetworkModification modificationSameBusbar = new MoveFeederBay(
                "S4VL1_BBS", "S4VL1_BBS", "S1VL2", terminal);
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED,
                modificationSameBusbar.hasImpactOnNetwork(network));

        // When/Then - Non-existent target busbar
        NetworkModification modificationNonExistentBusbar = new MoveFeederBay(
                "LD1", "bbs5", "S1VL2", terminal);
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED,
                modificationNonExistentBusbar.hasImpactOnNetwork(network));

        // When/Then - Valid parameters
        NetworkModification modificationValid = new MoveFeederBay(
                "LD1", "S1VL1_BBS", "S1VL1", terminal);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK,
                modificationValid.hasImpactOnNetwork(network));
    }

    private void moveFeederBay(String connectableId, String targetBusId, String targetVlId, Terminal terminal, Network network) {
        MoveFeederBay moveFeederBay = createMoveFeederBay(connectableId, targetBusId, targetVlId, terminal);
        moveFeederBay.apply(network);
    }

    private MoveFeederBay createMoveFeederBay(String connectableId, String targetBusId, String targetVlId, Terminal terminal) {
        return new MoveFeederBayBuilder()
                .withConnectableId(connectableId)
                .withTargetBusOrBusBarSectionId(targetBusId)
                .withTargetVoltageLevelId(targetVlId)
                .withTerminal(terminal)
                .build();
    }

    private void assertTerminalMoved(Terminal terminal, String expectedVoltageLevel,
                                     String originalVoltageLevel, String originalBusBarSection,
                                     String targetBusBarSection, int originalNode) {
        assertEquals(expectedVoltageLevel, terminal.getVoltageLevel().getId());
        assertNotEquals(originalBusBarSection, targetBusBarSection);
        assertNotEquals(originalVoltageLevel, terminal.getVoltageLevel().getId());
        assertNotEquals(originalBusBarSection, terminal.getVoltageLevel().getId());

        if (terminal.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            assertNotEquals(originalNode, terminal.getNodeBreakerView().getNode());
        }
    }

    private ReportNode createReportNode() {
        return ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME,
                        PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("reportTestMoveBbs")
                .build();
    }
}
