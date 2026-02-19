/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
class TieLineTrippingTest extends AbstractTrippingTest {

    @Test
    void tieLineTrippingTest() {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        TieLine line = network.getTieLine("NHV1_NHV2_1");

        assertTrue(line.getDanglingLine1().getTerminal().isConnected());
        assertTrue(line.getDanglingLine2().getTerminal().isConnected());

        new TieLineTripping("NHV1_NHV2_1", "VLHV2").apply(network);

        assertTrue(line.getDanglingLine1().getTerminal().isConnected());
        assertFalse(line.getDanglingLine2().getTerminal().isConnected());

        new TieLineTripping("NHV1_NHV2_1").apply(network);

        assertFalse(line.getDanglingLine1().getTerminal().isConnected());
        assertFalse(line.getDanglingLine2().getTerminal().isConnected());

        TieLineTripping unknownLineTripping = new TieLineTripping("NOT_EXISTS");
        Exception e1 = assertThrows(PowsyblException.class, () -> unknownLineTripping.apply(network, true, ReportNode.NO_OP));
        assertEquals("Tie line 'NOT_EXISTS' not found", e1.getMessage());
        assertDoesNotThrow(() -> unknownLineTripping.apply(network));

        TieLineTripping unknownVlTripping = new TieLineTripping("NHV1_NHV2_1", "NOT_EXISTS_VL");
        Exception e2 = assertThrows(PowsyblException.class, () -> unknownVlTripping.apply(network, true, ReportNode.NO_OP));
        assertEquals("VoltageLevel 'NOT_EXISTS_VL' not connected to TIE_LINE 'NHV1_NHV2_1'", e2.getMessage());
        assertDoesNotThrow(() -> unknownVlTripping.apply(network));
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new TieLineTripping("ID");
        assertEquals("TieLineTripping", networkModification.getName());
    }
}
