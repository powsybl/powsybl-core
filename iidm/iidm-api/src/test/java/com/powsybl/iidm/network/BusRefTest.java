/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
class BusRefTest {

    private final Bus bvBus = mock(Bus.class);
    private final Bus bbvBus = mock(Bus.class);

    @Test
    void testIdBasedBusRef() throws JsonProcessingException {
        Network network = mock(Network.class);
        Network.BusView bv = mock(Network.BusView.class);

        VoltageLevel vl = mock(VoltageLevel.class);
        when(bbvBus.getVoltageLevel()).thenReturn(vl);
        when(network.getBusView()).thenReturn(bv);
        when(bv.getBus(eq("busId"))).thenReturn(bvBus);

        final BusRef busRef = new IdBasedBusRef("busId");
        when(network.getIdentifiable(eq("busId"))).thenReturn((Identifiable) bvBus);
        when(bvBus.getId()).thenReturn("busId");
        Terminal busTerminal = mock(Terminal.class);
        Terminal.BusView terBusView = mock(Terminal.BusView.class);
        when(busTerminal.getBusView()).thenReturn(terBusView);
        when(terBusView.getBus()).thenReturn(bvBus);
        final Set<Terminal> singleton = Collections.singleton(busTerminal);
        final Stream mock = singleton.stream();
        when(bvBus.getConnectedTerminalStream()).thenReturn(mock);
        assertEquals(bvBus, busRef.resolve(network, TopologyLevel.BUS_BRANCH).orElseThrow(IllegalStateException::new));

        when(network.getIdentifiable(eq("busId"))).thenReturn((Identifiable) bbvBus);
        assertEquals(bbvBus, busRef.resolve(network, TopologyLevel.BUS_BREAKER).orElseThrow(IllegalStateException::new));

        assertFalse(new IdBasedBusRef("another").resolve(network, TopologyLevel.BUS_BRANCH).isPresent());

        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(busRef);
        assertEquals("{\"@c\":\".IdBasedBusRef\",\"id\":\"busId\"}", json);
        final BusRef deserialized = objectMapper.readValue(json, BusRef.class);
        assertEquals(busRef, deserialized);

        Identifiable busbarSection = mock(BusbarSection.class);
        when(network.getIdentifiable(eq("busbarId"))).thenReturn(busbarSection);
        final BusbarSection bbs = (BusbarSection) busbarSection;
        Terminal terminal = mock(Terminal.class);
        when(bbs.getTerminal()).thenReturn(terminal);
        Terminal.BusView tbv = mock(Terminal.BusView.class);
        when(terminal.getBusView()).thenReturn(tbv);
        when(tbv.getBus()).thenReturn(bvBus);
        final IdBasedBusRef busbarRef = new IdBasedBusRef("busbarId");
        assertEquals(bvBus, busbarRef.resolve(network, TopologyLevel.BUS_BRANCH).orElseThrow(IllegalStateException::new));

        Terminal.BusBreakerView bbv = mock(Terminal.BusBreakerView.class);
        when(terminal.getBusBreakerView()).thenReturn(bbv);
        when(bbv.getBus()).thenReturn(bbvBus);
        assertEquals(bbvBus, busbarRef.resolve(network, TopologyLevel.BUS_BREAKER).orElseThrow(IllegalStateException::new));

        Identifiable branch = mock(Branch.class);
        when(network.getIdentifiable(eq("branchId"))).thenReturn(branch);
        try {
            new IdBasedBusRef("branchId").resolve(network, TopologyLevel.BUS_BRANCH);
            fail();
        } catch (Exception e) {
            assertEquals("branchId is not a bus or injection.", e.getMessage());
        }

        try {
            new IdBasedBusRef("branchId").resolve(network, TopologyLevel.NODE_BREAKER);
            fail();
        } catch (Exception e) {
            assertEquals("NODE_BREAKER is not supported in resolve a BusRef.", e.getMessage());
        }
    }

    @Test
    void testBranch() throws JsonProcessingException {
        Network network = mock(Network.class);
        Branch branch = mock(Branch.class);
        when(network.getIdentifiable("branchId")).thenReturn(branch);
        Terminal terminal = mock(Terminal.class);
        Terminal.BusView bv = mock(Terminal.BusView.class);
        when(branch.getTerminal(TwoSides.ONE)).thenReturn(terminal);
        when(terminal.getBusView()).thenReturn(bv);
        when(bv.getBus()).thenReturn(bvBus);
        final BusRef busRef = new IdBasedBusRef("branchId", TwoSides.ONE);
        assertEquals(bvBus, busRef.resolve(network, TopologyLevel.BUS_BRANCH).orElseThrow(IllegalStateException::new));
        Terminal terminal2 = mock(Terminal.class);
        Terminal.BusView bv2 = mock(Terminal.BusView.class);
        when(branch.getTerminal(TwoSides.TWO)).thenReturn(terminal2);
        when(terminal2.getBusView()).thenReturn(bv2);
        Bus bus2 = mock(Bus.class);
        when(bv2.getBus()).thenReturn(bus2);
        final BusRef busRef2 = new IdBasedBusRef("branchId", TwoSides.TWO);
        assertEquals(bus2, busRef2.resolve(network, TopologyLevel.BUS_BRANCH).orElseThrow(IllegalStateException::new));

        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(busRef);
        assertEquals("{\"@c\":\".IdBasedBusRef\",\"id\":\"branchId\",\"side\":\"ONE\"}", json);
        final BusRef deserialized = objectMapper.readValue(json, BusRef.class);
        assertEquals(busRef, deserialized);

        assertEquals(busRef, new IdBasedBusRef("branchId", TwoSides.ONE));
    }

    @Test
    void testInvalidBranch() {
        Network network = mock(Network.class);
        when(network.getBranch("branchId")).thenReturn(null);

        BusRef busRef = new IdBasedBusRef("branchId", TwoSides.TWO);
        Optional<Bus> bus = busRef.resolve(network, TopologyLevel.BUS_BRANCH);
        assertTrue(bus.isEmpty());
    }
}
