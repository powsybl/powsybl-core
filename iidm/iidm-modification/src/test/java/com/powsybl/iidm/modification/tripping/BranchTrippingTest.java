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
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.topology.DefaultNamingStrategy;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class BranchTrippingTest extends AbstractTrippingTest {

    @Test
    void lineTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();
        Line line = network.getLine("NHV1_NHV2_1");

        assertTrue(line.getTerminal1().isConnected());
        assertTrue(line.getTerminal2().isConnected());

        new LineTripping("NHV1_NHV2_1", "VLHV2").apply(network);

        assertTrue(line.getTerminal1().isConnected());
        assertFalse(line.getTerminal2().isConnected());

        new LineTripping("NHV1_NHV2_1").apply(network);

        assertFalse(line.getTerminal1().isConnected());
        assertFalse(line.getTerminal2().isConnected());

        LineTripping unknownLineTripping = new LineTripping("NOT_EXISTS");
        Exception e1 = assertThrows(PowsyblException.class, () -> unknownLineTripping.apply(network, new DefaultNamingStrategy(), true, LocalComputationManager.getDefault(), ReportNode.NO_OP));
        assertEquals("Line 'NOT_EXISTS' not found", e1.getMessage());
        assertDoesNotThrow(() -> unknownLineTripping.apply(network));

        LineTripping unknownVlTripping = new LineTripping("NHV1_NHV2_1", "NOT_EXISTS_VL");
        Exception e2 = assertThrows(PowsyblException.class, () -> unknownVlTripping.apply(network, new DefaultNamingStrategy(), true, LocalComputationManager.getDefault(), ReportNode.NO_OP));
        assertEquals("VoltageLevel 'NOT_EXISTS_VL' not connected to LINE 'NHV1_NHV2_1'", e2.getMessage());
        assertDoesNotThrow(() -> unknownVlTripping.apply(network));
    }

    @Test
    void transformerTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer("NHV2_NLOAD");

        assertTrue(transformer.getTerminal1().isConnected());
        assertTrue(transformer.getTerminal2().isConnected());

        new TwoWindingsTransformerTripping("NHV2_NLOAD", "VLHV2").apply(network);

        assertFalse(transformer.getTerminal1().isConnected());
        assertTrue(transformer.getTerminal2().isConnected());

        new TwoWindingsTransformerTripping("NHV2_NLOAD").apply(network);

        assertFalse(transformer.getTerminal1().isConnected());
        assertFalse(transformer.getTerminal2().isConnected());

        TwoWindingsTransformerTripping unknown2wtTripping = new TwoWindingsTransformerTripping("NOT_EXISTS");
        Exception e1 = assertThrows(PowsyblException.class, () -> unknown2wtTripping.apply(network, new DefaultNamingStrategy(), true, LocalComputationManager.getDefault(), ReportNode.NO_OP));
        assertEquals("Two windings transformer 'NOT_EXISTS' not found", e1.getMessage());
        assertDoesNotThrow(() -> unknown2wtTripping.apply(network));

        TwoWindingsTransformerTripping unknownVlTripping = new TwoWindingsTransformerTripping("NHV2_NLOAD", "NOT_EXISTS_VL");
        Exception e2 = assertThrows(PowsyblException.class, () -> unknownVlTripping.apply(network, new DefaultNamingStrategy(), true, LocalComputationManager.getDefault(), ReportNode.NO_OP));
        assertEquals("VoltageLevel 'NOT_EXISTS_VL' not connected to TWO_WINDINGS_TRANSFORMER 'NHV2_NLOAD'", e2.getMessage());
        assertDoesNotThrow(() -> unknownVlTripping.apply(network));
    }

    @Test
    void legacyTest() {
        Network network = EurostagTutorialExample1Factory.create();
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer("NHV2_NLOAD");

        assertTrue(transformer.getTerminal1().isConnected());
        assertTrue(transformer.getTerminal2().isConnected());

        new BranchTripping("NHV2_NLOAD", "VLHV2").apply(network);

        assertFalse(transformer.getTerminal1().isConnected());
        assertTrue(transformer.getTerminal2().isConnected());

        new BranchTripping("NHV2_NLOAD").apply(network);

        assertFalse(transformer.getTerminal1().isConnected());
        assertFalse(transformer.getTerminal2().isConnected());
    }

    @Test
    void unknownBranchTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();

        BranchTripping tripping = new BranchTripping("transformer");
        assertThrows(PowsyblException.class, () -> tripping.apply(network, new DefaultNamingStrategy(), true, LocalComputationManager.getDefault(), ReportNode.NO_OP));
        assertDoesNotThrow(() -> tripping.apply(network));
    }

    @Test
    void unknownSubstationTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();

        BranchTripping tripping = new BranchTripping("NHV2_NLOAD", "UNKNOWN");
        assertThrows(PowsyblException.class, () -> tripping.apply(network, new DefaultNamingStrategy(), true, LocalComputationManager.getDefault(), ReportNode.NO_OP));
        assertDoesNotThrow(() -> tripping.apply(network));
    }

    @Test
    void fictitiousSwitchTest() {
        Set<String> switchIds = Sets.newHashSet("BD", "BL");

        Network network = FictitiousSwitchFactory.create();
        List<Boolean> expectedSwitchStates = getSwitchStates(network, switchIds);

        BranchTripping tripping = new BranchTripping("CJ", "C");

        Set<Switch> switchesToOpen = new HashSet<>();
        Set<Terminal> terminalsToDisconnect = new HashSet<>();
        Set<Terminal> affectedTerminals = new HashSet<>();
        tripping.traverse(network, switchesToOpen, terminalsToDisconnect, affectedTerminals);
        assertEquals(switchIds, switchesToOpen.stream().map(Switch::getId).collect(Collectors.toSet()));
        assertEquals(affectedTerminals.stream().map(Terminal::getConnectable).map(Connectable::getId).collect(Collectors.toSet()),
            Set.of("CI", "D", "CJ"));
        assertEquals(Collections.emptySet(), terminalsToDisconnect);

        tripping.apply(network);
        assertTrue(network.getSwitch("BD").isOpen());
        assertTrue(network.getSwitch("BL").isOpen());

        List<Boolean> switchStates = getSwitchStates(network, switchIds);
        assertEquals(expectedSwitchStates, switchStates);
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new BranchTripping("CJ", "C");
        assertEquals("BranchTripping", networkModification.getName());

        networkModification = new TwoWindingsTransformerTripping("NHV2_NLOAD", "VLHV2");
        assertEquals("TwoWindingsTransformerTripping", networkModification.getName());

        networkModification = new LineTripping("NHV1_NHV2_1", "VLHV2");
        assertEquals("LineTripping", networkModification.getName());
    }
}
