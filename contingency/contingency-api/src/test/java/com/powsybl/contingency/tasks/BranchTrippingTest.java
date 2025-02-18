/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.tasks;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.modification.topology.DefaultNamingStrategy;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.modification.NetworkModification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class BranchTrippingTest {

    @Test
    void lineTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();
        Line line = network.getLine("NHV1_NHV2_1");

        assertTrue(line.getTerminal1().isConnected());
        assertTrue(line.getTerminal2().isConnected());

        Contingency contingency = Contingency.line("NHV1_NHV2_1", "VLHV2");
        contingency.toModification().apply(network);

        assertTrue(line.getTerminal1().isConnected());
        assertFalse(line.getTerminal2().isConnected());

        contingency = Contingency.line("NHV1_NHV2_1");
        contingency.toModification().apply(network);

        assertFalse(line.getTerminal1().isConnected());
        assertFalse(line.getTerminal2().isConnected());

        NamingStrategy namingStrategy = new DefaultNamingStrategy();
        ComputationManager computationManager = LocalComputationManager.getDefault();

        NetworkModification unknownLineModif = Contingency.line("NOT_EXISTS").toModification();
        Exception e1 = assertThrows(PowsyblException.class, () -> unknownLineModif.apply(network, namingStrategy, true, computationManager, ReportNode.NO_OP));
        assertEquals("Line 'NOT_EXISTS' not found", e1.getMessage());
        assertDoesNotThrow(() -> unknownLineModif.apply(network));

        NetworkModification unknownVlModif = Contingency.line("NHV1_NHV2_1", "NOT_EXISTS_VL").toModification();
        Exception e2 = assertThrows(PowsyblException.class, () -> unknownVlModif.apply(network, namingStrategy, true, computationManager, ReportNode.NO_OP));
        assertEquals("VoltageLevel 'NOT_EXISTS_VL' not connected to LINE 'NHV1_NHV2_1'", e2.getMessage());
        assertDoesNotThrow(() -> unknownVlModif.apply(network));
    }

    @Test
    void transformerTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer("NHV2_NLOAD");

        assertTrue(transformer.getTerminal1().isConnected());
        assertTrue(transformer.getTerminal2().isConnected());

        Contingency contingency = Contingency.twoWindingsTransformer("NHV2_NLOAD", "VLHV2");
        contingency.toModification().apply(network);

        assertFalse(transformer.getTerminal1().isConnected());
        assertTrue(transformer.getTerminal2().isConnected());

        contingency = Contingency.twoWindingsTransformer("NHV2_NLOAD");
        contingency.toModification().apply(network);

        assertFalse(transformer.getTerminal1().isConnected());
        assertFalse(transformer.getTerminal2().isConnected());

        NamingStrategy namingStrategy = new DefaultNamingStrategy();
        ComputationManager computationManager = LocalComputationManager.getDefault();

        NetworkModification modifUnknown2wt = Contingency.twoWindingsTransformer("NOT_EXISTS").toModification();
        Exception e1 = assertThrows(PowsyblException.class, () -> modifUnknown2wt.apply(network, namingStrategy, true, computationManager, ReportNode.NO_OP));
        assertEquals("Two windings transformer 'NOT_EXISTS' not found", e1.getMessage());
        assertDoesNotThrow(() -> modifUnknown2wt.apply(network));

        NetworkModification modifUnknownVl = Contingency.twoWindingsTransformer("NHV2_NLOAD", "NOT_EXISTS_VL").toModification();
        Exception e2 = assertThrows(PowsyblException.class, () -> modifUnknownVl.apply(network, namingStrategy, true, computationManager, ReportNode.NO_OP));
        assertEquals("VoltageLevel 'NOT_EXISTS_VL' not connected to TWO_WINDINGS_TRANSFORMER 'NHV2_NLOAD'", e2.getMessage());
        assertDoesNotThrow(() -> modifUnknownVl.apply(network));
    }

    @Test
    void legacyTest() {
        Network network = EurostagTutorialExample1Factory.create();
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer("NHV2_NLOAD");

        assertTrue(transformer.getTerminal1().isConnected());
        assertTrue(transformer.getTerminal2().isConnected());

        Contingency contingency = Contingency.branch("NHV2_NLOAD", "VLHV2");
        contingency.toModification().apply(network);

        assertFalse(transformer.getTerminal1().isConnected());
        assertTrue(transformer.getTerminal2().isConnected());

        contingency = Contingency.branch("NHV2_NLOAD");
        contingency.toModification().apply(network);

        assertFalse(transformer.getTerminal1().isConnected());
        assertFalse(transformer.getTerminal2().isConnected());
    }
}
