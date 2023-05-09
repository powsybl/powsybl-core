/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Anne Tilloy <anne.tilloy at rte-france.com>
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
        Exception e1 = assertThrows(PowsyblException.class, () -> unknownLineTripping.apply(network));
        assertEquals("Tie line 'NOT_EXISTS' not found", e1.getMessage());

        TieLineTripping unknownVlTripping = new TieLineTripping("NHV1_NHV2_1", "NOT_EXISTS_VL");
        Exception e2 = assertThrows(PowsyblException.class, () -> unknownVlTripping.apply(network));
        assertEquals("VoltageLevel 'NOT_EXISTS_VL' not connected to TIE_LINE 'NHV1_NHV2_1'", e2.getMessage());
    }
}
