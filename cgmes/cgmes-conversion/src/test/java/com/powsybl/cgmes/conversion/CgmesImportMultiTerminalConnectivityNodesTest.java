/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.cgmes.conversion.test.ConversionUtil;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class CgmesImportMultiTerminalConnectivityNodesTest {

    @Test
    void testNever() {
        Properties cgmesImportParameters = new Properties();
        cgmesImportParameters.put(CgmesImport.CREATE_FICTITIOUS_SWITCHES_FOR_DISCONNECTED_TERMINALS_MODE, "NEVER");
        Network network = ConversionUtil.readCgmesResources(cgmesImportParameters, "/multi-terminal-connectivity-nodes", "multiTerminalConnectivityNodes_EQ.xml", "multiTerminalConnectivityNodes_SSH.xml");

        // BBS1, Load1 and Disconnector connected to same ConnectivityNode: in IIDM that leads to BBS1 and Load1
        // connected with a single InternalConnection. The corresponding connectivity node is mirrored by the BBS:
        // the Switch connected to that connectivity node is therefore directly connected to the BBS.
        assertEquals(getLoadOrBbsNodes(network, "BBS1"), getNodesConnectedWithInternalConnectionsToLoadOrBbs(network, "Load1"));
        int nodeBbs1 = network.getBusbarSection("BBS1").getTerminal().getNodeBreakerView().getNode();
        assertEquals(List.of(network.getSwitch("Disconnector")), network.getVoltageLevel("VoltageLevel").getNodeBreakerView().getSwitches(nodeBbs1));

        // Load2 and Disconnector connected to same ConnectivityNode: no InternalConnection needed
        assertTrue(getNodesConnectedWithInternalConnectionsToLoadOrBbs(network, "Load2").isEmpty());

        // Load3 and BBS6 connected to same ConnectivityNode: in IIDM they're connected with a single InternalConnection
        assertEquals(getLoadOrBbsNodes(network, "BBS6"), getNodesConnectedWithInternalConnectionsToLoadOrBbs(network, "Load3"));

        // Load4 and Load5 connected to same ConnectivityNode: in IIDM they're connected with a single InternalConnection
        assertEquals(getLoadOrBbsNodes(network, "Load5"), getNodesConnectedWithInternalConnectionsToLoadOrBbs(network, "Load4"));

        // Load6, Load7 and Load8 connected to same ConnectivityNode: in IIDM they're connected each one to the same
        // IIDM node with an InternalConnection
        assertLoadsOrBbsStarShapedConnectedWithInternalConnections(network, "Load6", "Load7", "Load8");

        // BBS4, BBS5 and Load9 connected to same ConnectivityNode: in IIDM BBS4 is mirroring that ConnectivityNode,
        // and BBS5 and Load9 are connected each one to BBS4 with an InternalConnection
        int nodeBbs4 = network.getBusbarSection("BBS4").getTerminal().getNodeBreakerView().getNode();
        assertEquals(List.of(nodeBbs4), getNodesConnectedWithInternalConnectionsToLoadOrBbs(network, "BBS5"));
        assertEquals(List.of(nodeBbs4), getNodesConnectedWithInternalConnectionsToLoadOrBbs(network, "Load9"));

        // Load10, BBS7 and Load11 connected to same ConnectivityNode: in IIDM BBS7 is mirroring that ConnectivityNode,
        // and Load10 and Load11 are connected each one to BBS7 with an InternalConnection
        int nodeBbs7 = network.getBusbarSection("BBS7").getTerminal().getNodeBreakerView().getNode();
        assertEquals(List.of(nodeBbs7), getNodesConnectedWithInternalConnectionsToLoadOrBbs(network, "Load10"));
        assertEquals(List.of(nodeBbs7), getNodesConnectedWithInternalConnectionsToLoadOrBbs(network, "Load11"));
    }

    @Test
    void testAlways() {
        Properties cgmesImportParameters = new Properties();
        cgmesImportParameters.put(CgmesImport.CREATE_FICTITIOUS_SWITCHES_FOR_DISCONNECTED_TERMINALS_MODE, "ALWAYS");
        Network network = ConversionUtil.readCgmesResources(cgmesImportParameters, "/multi-terminal-connectivity-nodes", "multiTerminalConnectivityNodes_EQ.xml", "multiTerminalConnectivityNodes_SSH.xml");

        // BBS1, Load1 and Disconnector connected to same ConnectivityNode
        assertLoadsOrBbsStarShapedConnectedWithFictitiousSwitches(network, "BBS1", "Load1");

        // Load2 and Disconnector connected to same ConnectivityNode
        assertEquals(1, getNodesConnectedWithFictitiousSwitchesToLoadOrBbs(network, "Load2").size());

        // Load3 and BBS6 connected to same ConnectivityNode
        assertLoadsOrBbsStarShapedConnectedWithFictitiousSwitches(network, "Load3", "BBS6");

        // Load4 and Load5 connected to same ConnectivityNode
        assertLoadsOrBbsStarShapedConnectedWithFictitiousSwitches(network, "Load4", "Load5");

        // Load6, Load7 and Load8 connected to same ConnectivityNode
        assertLoadsOrBbsStarShapedConnectedWithFictitiousSwitches(network, "Load6", "Load7", "Load8");

        // BBS4, BBS5 and Load9 connected to same ConnectivityNode
        assertLoadsOrBbsStarShapedConnectedWithFictitiousSwitches(network, "BBS4", "BBS5", "Load9");

        // Load10, BBS7 and Load11 connected to same ConnectivityNode
        assertLoadsOrBbsStarShapedConnectedWithFictitiousSwitches(network, "Load10", "BBS7", "Load11");
    }

    private static void assertLoadsOrBbsStarShapedConnectedWithInternalConnections(Network network, String... loadsOrBbs) {
        List<List<Integer>> nodesInternalConnectedTo = Stream.of(loadsOrBbs)
                .map(id -> getNodesConnectedWithInternalConnectionsToLoadOrBbs(network, id))
                .toList();
        assertEquals(List.of(1), nodesInternalConnectedTo.stream().map(List::size).distinct().toList());
        assertEquals(1, nodesInternalConnectedTo.stream().flatMap(List::stream).distinct().count());
    }

    private static void assertLoadsOrBbsStarShapedConnectedWithFictitiousSwitches(Network network, String... loadsOrBbs) {
        List<List<Integer>> nodesFSwitchConnectedTo = Stream.of(loadsOrBbs)
                .map(id -> getNodesConnectedWithFictitiousSwitchesToLoadOrBbs(network, id))
                .toList();
        assertEquals(List.of(1), nodesFSwitchConnectedTo.stream().map(List::size).distinct().toList());
        assertEquals(1, nodesFSwitchConnectedTo.stream().flatMap(List::stream).distinct().count());
    }

    private static List<Integer> getLoadOrBbsNodes(Network network, String... ids) {
        return Arrays.stream(ids)
                .map(id -> getLoadOrBbs(network, id))
                .map(injection -> injection.getTerminal().getNodeBreakerView().getNode())
                .toList();
    }

    private static List<Integer> getNodesConnectedWithInternalConnectionsToLoadOrBbs(Network network, String id) {
        int node = getLoadOrBbs(network, id).getTerminal().getNodeBreakerView().getNode();
        return network.getVoltageLevel("VoltageLevel").getNodeBreakerView().getNodesInternalConnectedTo(node);
    }

    private static List<Integer> getNodesConnectedWithFictitiousSwitchesToLoadOrBbs(Network network, String id) {
        int node = getLoadOrBbs(network, id).getTerminal().getNodeBreakerView().getNode();
        VoltageLevel.NodeBreakerView vlNbv = network.getVoltageLevel("VoltageLevel").getNodeBreakerView();
        return vlNbv.getSwitches(node)
                .stream()
                .filter(Switch::isFictitious)
                .map(Switch::getId)
                .map(sId -> vlNbv.getNode1(sId) == node ? vlNbv.getNode2(sId) : vlNbv.getNode1(sId))
                .toList();
    }

    private static Injection<? extends Injection<?>> getLoadOrBbs(Network network, String id) {
        Load load = network.getLoad(id);
        return load != null ? load : network.getBusbarSection(id);
    }
}
