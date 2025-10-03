/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class DcDetailedNetworkTest {

    @Test
    void testLccMonopoleGroundReturn() {
        Network network = DcDetailedNetworkFactory.createLccMonopoleGroundReturn();
        assertEquals(2, network.getBusView().getSynchronousComponents().size());
        assertEquals(1, network.getDcComponents().size());
        assertEquals(1, network.getBusView().getConnectedComponents().size());
        assertEquals(3, network.getSubnetworks().size());
        assertEquals(4, network.getDcNodeCount());
        assertEquals(1, network.getDcLineCount());
        assertEquals(2, network.getLineCommutatedConverterCount());
        assertEquals(2, network.getDcGroundCount());
        assertTrue(network.getDcGround("dcGroundFr").getDcTerminal().isConnected());
        assertTrue(network.getDcGround("dcGroundGb").getDcTerminal().isConnected());
    }

    @Test
    void testLccMonopoleMetallicReturn() {
        Network network = DcDetailedNetworkFactory.createLccMonopoleMetallicReturn();
        assertEquals(2, network.getBusView().getSynchronousComponents().size());
        assertEquals(1, network.getDcComponents().size());
        assertEquals(1, network.getBusView().getConnectedComponents().size());
        assertEquals(3, network.getSubnetworks().size());
        assertEquals(4, network.getDcNodeCount());
        assertEquals(2, network.getDcLineCount());
        assertEquals(2, network.getLineCommutatedConverterCount());
        assertEquals(2, network.getDcGroundCount());
        assertTrue(network.getDcGround("dcGroundFr").getDcTerminal().isConnected());
        assertFalse(network.getDcGround("dcGroundGb").getDcTerminal().isConnected());
    }

    @Test
    void testVscSymmetricalMonopole() {
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();
        assertEquals(2, network.getBusView().getSynchronousComponents().size());
        assertEquals(1, network.getDcComponents().size());
        assertEquals(1, network.getBusView().getConnectedComponents().size());
        assertEquals(3, network.getSubnetworks().size());
        assertEquals(4, network.getDcNodeCount());
        assertEquals(2, network.getDcLineCount());
        assertEquals(2, network.getVoltageSourceConverterCount());
        assertEquals(0, network.getDcGroundCount());
    }

    @Test
    void testVscAsymmetricalMonopole() {
        Network network = DcDetailedNetworkFactory.createVscAsymmetricalMonopole();
        assertEquals(2, network.getBusView().getSynchronousComponents().size());
        assertEquals(1, network.getDcComponents().size());
        assertEquals(1, network.getBusView().getConnectedComponents().size());
        assertEquals(3, network.getSubnetworks().size());
        assertEquals(4, network.getDcNodeCount());
        assertEquals(1, network.getDcLineCount());
        assertEquals(2, network.getVoltageSourceConverterCount());
        assertEquals(2, network.getDcGroundCount());
    }

    @Test
    void testLccBipoleGroundReturn() {
        Network network = DcDetailedNetworkFactory.createLccBipoleGroundReturn();
        assertEquals(2, network.getBusView().getSynchronousComponents().size());
        assertEquals(1, network.getDcComponents().size());
        assertEquals(1, network.getBusView().getConnectedComponents().size());
        assertEquals(3, network.getSubnetworks().size());
        assertEquals(6, network.getDcNodeCount());
        assertEquals(4, network.getDcSwitchCount());
        assertEquals(2, network.getDcLineCount());
        assertEquals(4, network.getLineCommutatedConverterCount());
        assertEquals(2, network.getDcGroundCount());
        assertEquals(6, network.getDcBusCount());
    }

    @Test
    void testLccBipoleGroundReturnNegativePoleOutage() {
        Network network = DcDetailedNetworkFactory.createLccBipoleGroundReturnNegativePoleOutage();
        assertEquals(2, network.getBusView().getSynchronousComponents().size());
        List<Component> dcComponents = List.copyOf(network.getDcComponents());
        assertEquals(1, dcComponents.size());
        assertEquals(1, network.getBusView().getConnectedComponents().size());
        assertEquals(3, network.getSubnetworks().size());
        assertEquals(6, network.getDcNodeCount());
        assertEquals(4, network.getDcSwitchCount());
        assertEquals(2, network.getDcLineCount());
        assertEquals(4, network.getLineCommutatedConverterCount());
        assertEquals(2, network.getDcGroundCount());
        assertEquals(4, network.getDcBusCount());
        Component dc0 = dcComponents.get(0);
        assertEquals(4, dc0.getSize());
    }

    @Test
    void testLccBipoleGroundReturnWithDcLineSegments() {
        Network network = DcDetailedNetworkFactory.createLccBipoleGroundReturnWithDcLineSegments();
        assertEquals(2, network.getBusView().getSynchronousComponents().size());
        List<Component> dcComponents = List.copyOf(network.getDcComponents());
        assertEquals(1, dcComponents.size());
        assertEquals(1, network.getBusView().getConnectedComponents().size());
        assertEquals(3, network.getSubnetworks().size());
        assertEquals(22, network.getDcNodeCount());
        assertEquals(12, network.getDcSwitchCount());
        assertEquals(12, network.getDcLineCount());
        assertEquals(4, network.getLineCommutatedConverterCount());
        assertEquals(2, network.getDcGroundCount());
        assertEquals(14, network.getDcBusCount());
        Component dc0 = dcComponents.get(0);
        assertEquals(14, dc0.getSize());
    }

    @Test
    void testEquipmentTopologyVisitor() {
        Network network = DcDetailedNetworkFactory.createLccMonopoleGroundReturn();
        List<Connectable<?>> visited = new ArrayList<>();
        TopologyVisitor topologyVisitor = new AbstractEquipmentTopologyVisitor() {
            @Override
            public <I extends Connectable<I>> void visitEquipment(Connectable<I> eq) {
                if (eq instanceof AcDcConverter<?>) {
                    visited.add(eq);
                }
            }
        };
        network.getVoltageLevel(DcDetailedNetworkFactory.getVoltageLevelId(Country.FR, DcDetailedNetworkFactory.X_NODE_DC_1_FR, DcDetailedNetworkFactory.SUFFIX_150))
                .visitEquipments(topologyVisitor);
        assertEquals(2, visited.size());
        visited.forEach(c -> assertSame(network.getLineCommutatedConverter("LccFr"), c));
    }

    @Test
    void testTerminalTopologyVisitor() {
        Network network = DcDetailedNetworkFactory.createLccMonopoleGroundReturn();
        List<Terminal> visited = new ArrayList<>();
        TopologyVisitor topologyVisitor = new AbstractTerminalTopologyVisitor() {
            @Override
            public void visitTerminal(Terminal t) {
                if (t.getConnectable() instanceof AcDcConverter<?>) {
                    visited.add(t);
                }
            }
        };
        network.getVoltageLevel(DcDetailedNetworkFactory.getVoltageLevelId(Country.FR, DcDetailedNetworkFactory.X_NODE_DC_1_FR, DcDetailedNetworkFactory.SUFFIX_150))
                .visitEquipments(topologyVisitor);
        assertEquals(2, visited.size());
        var lccFr = network.getLineCommutatedConverter("LccFr");
        visited.forEach(t -> assertSame(lccFr, t.getConnectable()));
        var terminalBySide = visited.stream().collect(Collectors.toMap(Terminal::getTerminalNumber, Function.identity()));
        assertSame(lccFr.getTerminal1(), terminalBySide.get(TerminalNumber.ONE));
        assertSame(lccFr.getTerminal2().orElseThrow(), terminalBySide.get(TerminalNumber.TWO));
    }
}
