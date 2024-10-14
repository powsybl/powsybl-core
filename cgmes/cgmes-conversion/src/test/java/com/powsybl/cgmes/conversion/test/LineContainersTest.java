/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */

class LineContainersTest {

    @Test
    void testLineContainerWithOneACLSnotAssociated() {
        // This unit test reproduces a configuration observed in public data from a European TSO (NG)
        // where three ACLSs lie on a common connectivity node inside a cim:Line container
        // Two of the ACLSs have an association with the cim:Line container
        // The other does not have any equipment container

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

    @Test
    void testLineContainerWithSwitchConnectedToVoltageLevel() {
        // This unit test reproduces a configuration observed in public data from a European TSO (NG)
        // where a disconnector has one terminal inside a cim:Line container and the other inside a cim:VoltageLevel.
        // First a fictitious voltage level is created to hold the connectivity node inside the cim:Line container
        // (an alias is created for this fictitious voltage level).
        // The fictitious voltage level is later merged with the voltage level of the other end of the switch.
        // We test the two alternatives for creating the fictitious voltage levels from cim:Lines.

        ResourceDataSource ds = new ResourceDataSource("line_container_switch",
                new ResourceSet("/issues", "line_container_switch_EQ.xml"));
        Properties importParams = new Properties();
        Network network;
        Switch s;

        importParams.put(CgmesImport.CREATE_FICTITIOUS_VOLTAGE_LEVEL_FOR_EVERY_NODE, "false");
        network = Network.read(ds, importParams);
        assertNotNull(network);
        s = network.getSwitch("SwitchBetweenVoltageLevel1AndLineContainer");
        assertNotNull(s);
        assertEquals("VoltageLevel1", s.getVoltageLevel().getId());
        assertEquals(s.getVoltageLevel(), network.getIdentifiable("LineContainer_VL"));

        importParams.put(CgmesImport.CREATE_FICTITIOUS_VOLTAGE_LEVEL_FOR_EVERY_NODE, "true");
        network = Network.read(ds, importParams);
        assertNotNull(network);
        s = network.getSwitch("SwitchBetweenVoltageLevel1AndLineContainer");
        assertNotNull(s);
        assertEquals("VoltageLevel1", s.getVoltageLevel().getId());
        assertEquals(s.getVoltageLevel(), network.getIdentifiable("NodeInsideLineContainer_VL"));
    }
}
