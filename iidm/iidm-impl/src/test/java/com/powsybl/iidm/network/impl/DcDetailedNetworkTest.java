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
        assertEquals(3, network.getSubnetworks().size());
        assertEquals(4, network.getDcNodeCount());
        assertEquals(1, network.getDcLineCount());
        assertEquals(2, network.getVoltageSourceConverterCount());
        assertEquals(2, network.getDcGroundCount());
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
        network.getVoltageLevel("VLDC-FR-xNodeDc1fr-150")
                .visitEquipments(topologyVisitor);
        assertEquals(2, visited.size());
        visited.forEach(c -> assertSame(network.getLineCommutatedConverter("CsFr"), c));
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
        network.getVoltageLevel("VLDC-FR-xNodeDc1fr-150")
                .visitEquipments(topologyVisitor);
        assertEquals(2, visited.size());
        var csFr = network.getLineCommutatedConverter("CsFr");
        visited.forEach(t -> assertSame(csFr, t.getConnectable()));
        var terminalBySide = visited.stream().collect(Collectors.toMap(Terminal::getSide, Function.identity()));
        assertSame(csFr.getTerminal1(), terminalBySide.get(ThreeSides.ONE));
        assertSame(csFr.getTerminal2().orElseThrow(), terminalBySide.get(ThreeSides.TWO));
    }
}
