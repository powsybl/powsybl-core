/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Clement Philipot {@literal <clement.philipot at rte-france.com>}
 */
class BoundaryLineConversionTest {

    private static final String RESOURCE_DIR = "/update/boundary-line/";
    private static Network network;

    @BeforeAll
    static void setUp() {
        network = readCgmesResources(RESOURCE_DIR, "boundaryLine_EQ.xml", "boundaryLine_EQ_BD.xml", "boundaryLine_SSH.xml");
    }

    @Test
    void boundaryLinesAreCreatedFromDifferentEquipmentTypes() {
        assertEquals(4, network.getBoundaryLineCount());

        BoundaryLine switchBoundary = network.getBoundaryLine("Breaker");
        assertNotNull(switchBoundary);
        assertEquals(0.0, switchBoundary.getR(), 0.0);
        assertEquals(0.0, switchBoundary.getX(), 0.0);
        assertEquals(0.0, switchBoundary.getG(), 0.0);
        assertEquals(0.0, switchBoundary.getB(), 0.0);

        BoundaryLine acLine = network.getBoundaryLine("ACLineSegment");
        assertNotNull(acLine);
        assertEquals(1.5, acLine.getR(), 0.0);
        assertEquals(18.35, acLine.getX(), 0.0);

        BoundaryLine equivalentBranch = network.getBoundaryLine("EquivalentBranch");
        assertNotNull(equivalentBranch);
        assertEquals(13.085, equivalentBranch.getR(), 0.0);
        assertEquals(31.151, equivalentBranch.getX(), 0.0);

        BoundaryLine transformerBoundary = network.getBoundaryLine("PowerTransformer");
        assertNotNull(transformerBoundary);
        assertEquals(0.537890625, transformerBoundary.getR(), 0.0);
        assertEquals(18.193359375, transformerBoundary.getX(), 0.0);
    }

    @Test
    void equivalentInjectionValuesAndRegulationAreMappedToBoundaryLines() {
        BoundaryLine regulatingBoundary = network.getBoundaryLine("EquivalentBranch");
        assertEquals(0.0, regulatingBoundary.getP0(), 0.0);
        assertEquals(0.0, regulatingBoundary.getQ0(), 0.0);
        assertNotNull(regulatingBoundary.getGeneration());
        assertEquals(-275.0, regulatingBoundary.getGeneration().getTargetP(), 0.0);
        assertEquals(-50.0, regulatingBoundary.getGeneration().getTargetQ(), 0.0);
        assertEquals(405.0, regulatingBoundary.getGeneration().getTargetV(), 0.0);
        assertTrue(regulatingBoundary.getGeneration().isVoltageRegulationOn());

        BoundaryLine nonRegulatingBoundary = network.getBoundaryLine("ACLineSegment");
        assertEquals(284.5, nonRegulatingBoundary.getP0(), 0.0);
        assertEquals(70.5, nonRegulatingBoundary.getQ0(), 0.0);
        assertNull(nonRegulatingBoundary.getGeneration());
    }

    @Test
    void multipleLinesAtAnUnmergedBoundaryPointAreImportedSeparately() {
        Network networkWithUnmergedLines = readCgmesResources(RESOURCE_DIR,
                "boundaryLine_EQ.xml",
                "boundaryLine_EQ_BD.xml",
                "boundaryLineUnmerged_EQ.xml",
                "boundaryLine_TP.xml",
                "boundaryLineUnmerged_TP.xml");

        assertEquals(5, networkWithUnmergedLines.getBoundaryLineCount());
        assertNotNull(networkWithUnmergedLines.getBoundaryLine("ACLineSegment"));
        assertNotNull(networkWithUnmergedLines.getBoundaryLine("UnmergedBoundaryLine"));
    }

    @Test
    void multipleConnectedLinesShareBoundaryInjectionButDisconnectedLineDoesNot() {
        Network networkWithSharedBoundary = readCgmesResources(RESOURCE_DIR,
                "boundaryLineMultiple_EQ.xml",
                "boundaryLineMultiple_EQ_BD.xml",
                "boundaryLineMultiple_SSH.xml");

        BoundaryLine line1 = networkWithSharedBoundary.getBoundaryLine("ConnectedBoundaryLine1");
        BoundaryLine line2 = networkWithSharedBoundary.getBoundaryLine("ConnectedBoundaryLine2");
        BoundaryLine disconnectedLine = networkWithSharedBoundary.getBoundaryLine("DisconnectedBoundaryLine");
        assertNotNull(line1);
        assertNotNull(line2);
        assertNotNull(disconnectedLine);
        assertTrue(line1.getTerminal().isConnected());
        assertTrue(line2.getTerminal().isConnected());
        assertFalse(disconnectedLine.getTerminal().isConnected());
        assertEquals(Conversion.getBoundaryLineBoundaryNode(line1), Conversion.getBoundaryLineBoundaryNode(line2));
        assertEquals(Conversion.getBoundaryLineBoundaryNode(line1), Conversion.getBoundaryLineBoundaryNode(disconnectedLine));
        assertEquals(-13.5143, line1.getP0(), 1e-4);
        assertEquals(60.3944, line1.getQ0(), 1e-4);
        assertEquals(-13.5143, line2.getP0(), 1e-4);
        assertEquals(60.3944, line2.getQ0(), 1e-4);
        assertEquals(-27.0286, disconnectedLine.getP0(), 1e-4);
        assertEquals(120.7887, disconnectedLine.getQ0(), 1e-4);
    }
}
