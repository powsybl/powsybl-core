/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */

class LineContainersTest {

    @Test
    void testLineContainerWithOneACLSNotAssociated() {
        // This unit test reproduces a configuration observed in public data from a European TSO (NG)
        // where three ACLSs lie on a common connectivity node inside a cim:Line container
        // Two of the ACLSs have an association with the cim:Line container
        // The other does not any equipment container

        // A fictitious voltage level is created to hold the connectivity node inside the cim:Line container,
        // All three ACLSs must be imported
        // And they have to share a bus

        Network network = Network.read("line_container_3acls_EQ.xml", getClass().getResourceAsStream("/issues/line_container_3acls_EQ.xml"));
        assertNotNull(network);

        // Check all three ACLSs have been imported
        Line l1 = network.getLine("ACLS1-without-Line-Container");
        Line l2 = network.getLine("ACLS2");
        Line l3 = network.getLine("ACLS3");
        assertNotNull(l1);
        assertNotNull(l2);
        assertNotNull(l3);

        VoltageLevel vlNodeInsideLineContainer = l1.getTerminal2().getVoltageLevel();
        // Check that the fictitious voltage level created has a reference to the original Line container
        assertEquals("LineContainer", vlNodeInsideLineContainer.getProperty("CGMES.LineContainerId"));
        assertEquals("Line Container", vlNodeInsideLineContainer.getNameOrId());
        // Check that the only bus in the fictitious voltage level has a terminal for each line
        assertEquals(1, vlNodeInsideLineContainer.getBusBreakerView().getBusCount());
        Bus tbus = vlNodeInsideLineContainer.getBusBreakerView().getBuses().iterator().next();
        assertEquals(3, tbus.getConnectedTerminalCount());
        assertEquals(List.of("ACLS1-without-Line-Container", "ACLS2", "ACLS3"),
                tbus.getConnectedTerminalStream().map(Terminal::getConnectable).map(Connectable::getId).sorted().toList());
    }
}
