/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Denis Bonnand {@literal <denis.bonnand at supergrid-institute.com>}
 */
class DcLineTrippingTest extends AbstractTrippingTest {

    @Test
    void dcLineTrippingTest() {
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();
        DcLine dcLine = network.getDcLine("dcLinePos");

        assertTrue(dcLine.getDcTerminal1().isConnected());
        assertTrue(dcLine.getDcTerminal2().isConnected());

        DcLineTripping dcLineTripping = new DcLineTripping("dcLinePos", "dcNodeFrPos");
        dcLineTripping.apply(network);

        assertEquals("dcNodeFrPos", dcLineTripping.getDcNodeId());
        assertFalse(dcLine.getDcTerminal1().isConnected());
        assertTrue(dcLine.getDcTerminal2().isConnected());

        new DcLineTripping("dcLinePos").apply(network);

        assertFalse(dcLine.getDcTerminal1().isConnected());
        assertFalse(dcLine.getDcTerminal2().isConnected());

        DcLineTripping unknownDcLineTripping = new DcLineTripping("NOT_EXISTS");
        Exception e1 = assertThrows(PowsyblException.class, () -> unknownDcLineTripping.apply(network, true, ReportNode.NO_OP));
        assertEquals("DcLine 'NOT_EXISTS' not found", e1.getMessage());
        assertDoesNotThrow(() -> unknownDcLineTripping.apply(network));

        DcLineTripping unknownVlTripping = new DcLineTripping("dcLinePos", "NOT_EXISTS_VL");
        Exception e2 = assertThrows(PowsyblException.class, () -> unknownVlTripping.apply(network, true, ReportNode.NO_OP));
        assertEquals("DcNode 'NOT_EXISTS_VL' not connected to DC_LINE 'dcLinePos'", e2.getMessage());
        assertDoesNotThrow(() -> unknownVlTripping.apply(network));
    }

    @Test
    void unknownDcLineTrippingTest() {
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();

        DcLineTripping tripping = new DcLineTripping("transformer");
        assertThrows(PowsyblException.class, () -> tripping.apply(network, true, ReportNode.NO_OP));
        assertDoesNotThrow(() -> tripping.apply(network));
    }

    @Test
    void unknownSubstationTrippingTest() {
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();

        DcLineTripping tripping = new DcLineTripping("dcLinePos", "UNKNOWN");
        assertThrows(PowsyblException.class, () -> tripping.apply(network, true, ReportNode.NO_OP));
        assertDoesNotThrow(() -> tripping.apply(network));
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new DcLineTripping("CJ", "C");
        assertEquals("DcLineTripping", networkModification.getName());
    }
}
