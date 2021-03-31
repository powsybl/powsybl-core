/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class BusRefTest {

    private final Bus bvBus = mock(Bus.class);
    private final Bus bbvBus = mock(Bus.class);

    @Test
    public void testIdBasedBusRef() throws JsonProcessingException {
        Network network = mock(Network.class);
        Network.BusView bv = mock(Network.BusView.class);
        Network.BusBreakerView bbv = mock(Network.BusBreakerView.class);
        VoltageLevel vl = mock(VoltageLevel.class);
        when(bbvBus.getVoltageLevel()).thenReturn(vl);
        when(network.getBusView()).thenReturn(bv);
        when(network.getBusBreakerView()).thenReturn(bbv);
        when(bv.getBus(eq("busId"))).thenReturn(bvBus);
        when(bbv.getBus(eq("busId"))).thenReturn(bbvBus);

        final BusRef busRef = new IdBasedBusRef("busId");
        when(network.getIdentifiable(eq("busId"))).thenReturn((Identifiable) bvBus);
        when(bvBus.getId()).thenReturn("busId");
        when(bbvBus.getId()).thenReturn("busId");
        Terminal busTerminal = mock(Terminal.class);
        Terminal.BusView terBusView = mock(Terminal.BusView.class);
        Terminal.BusBreakerView terBusBreakView = mock(Terminal.BusBreakerView.class);
        when(busTerminal.getBusView()).thenReturn(terBusView);
        when(busTerminal.getBusBreakerView()).thenReturn(terBusBreakView);
        when(terBusView.getBus()).thenReturn(bvBus);
        when(terBusBreakView.getBus()).thenReturn(bbvBus);
        final Set<Terminal> singleton = Collections.singleton(busTerminal);
        final Stream mock = singleton.stream();
        final Stream mock2 = singleton.stream();
        when(bvBus.getConnectedTerminalStream()).thenReturn(mock);
        assertEquals(bvBus, busRef.resolve(network, TopologyLevel.BUS_BRANCH).orElseThrow(AssertionError::new));
        when(network.getIdentifiable(eq("busId"))).thenReturn((Identifiable) bbvBus);
        when(bbvBus.getConnectedTerminalStream()).thenReturn(mock2);
        assertEquals(bbvBus, busRef.resolve(network, TopologyLevel.BUS_BREAKER).orElseThrow(AssertionError::new));
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
        assertEquals(bvBus, busbarRef.resolve(network, TopologyLevel.BUS_BRANCH).orElseThrow(AssertionError::new));

        Identifiable branch = mock(Branch.class);
        when(network.getIdentifiable(eq("branchId"))).thenReturn(branch);
        try {
            new IdBasedBusRef("branchId").resolve(network, TopologyLevel.BUS_BRANCH);
            fail();
        } catch (Exception e) {
            assertEquals("branchId is not a bus or injection.", e.getMessage());
        }
    }

    @Test
    public void testBranch() throws JsonProcessingException {
        Network network = mock(Network.class);
        Branch branch = mock(Branch.class);
        when(network.getBranch(eq("branchId"))).thenReturn(branch);
        Terminal terminal = mock(Terminal.class);
        Terminal.BusView bv = mock(Terminal.BusView.class);
        when(branch.getTerminal1()).thenReturn(terminal);
        when(terminal.getBusView()).thenReturn(bv);
        when(bv.getBus()).thenReturn(bvBus);
        final BusRef busRef = new IdBasedBusRef("branchId", Branch.Side.ONE);
        assertEquals(bvBus, busRef.resolve(network, TopologyLevel.BUS_BRANCH).orElseThrow(AssertionError::new));
        Terminal terminal2 = mock(Terminal.class);
        Terminal.BusView bv2 = mock(Terminal.BusView.class);
        when(branch.getTerminal2()).thenReturn(terminal2);
        when(terminal2.getBusView()).thenReturn(bv2);
        Bus bus2 = mock(Bus.class);
        when(bv2.getBus()).thenReturn(bus2);
        final BusRef busRef2 = new IdBasedBusRef("branchId", Branch.Side.TWO);
        assertEquals(bus2, busRef2.resolve(network, TopologyLevel.BUS_BRANCH).orElseThrow(AssertionError::new));

        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(busRef);
        assertEquals("{\"@c\":\".IdBasedBusRef\",\"id\":\"branchId\",\"side\":\"ONE\"}", json);
        final BusRef deserialized = objectMapper.readValue(json, BusRef.class);
        assertEquals(busRef, deserialized);

        assertEquals(busRef, new IdBasedBusRef("branchId", Branch.Side.ONE));
    }

    @Test
    public void testNodeNumberBased() throws JsonProcessingException {
        Network network = mock(Network.class);
        VoltageLevel vl = mock(VoltageLevel.class);
        when(network.getVoltageLevel(eq("vl"))).thenReturn(vl);
        VoltageLevel.NodeBreakerView nbv = mock(VoltageLevel.NodeBreakerView.class);
        when(vl.getTopologyKind()).thenReturn(TopologyKind.NODE_BREAKER);
        when(vl.getNodeBreakerView()).thenReturn(nbv);
        Terminal terminal = mock(Terminal.class);
        Terminal.BusView bv = mock(Terminal.BusView.class);
        when(terminal.getBusView()).thenReturn(bv);
        when(bv.getBus()).thenReturn(bvBus);
        when(nbv.getTerminal(eq(1))).thenReturn(terminal);

        BusRef busRef = new NodeNumberBasedBusRef("vl", 1);
        assertEquals(bvBus, busRef.resolve(network, TopologyLevel.BUS_BRANCH).orElseThrow(AssertionError::new));
        assertFalse(new NodeNumberBasedBusRef("vl", 2).resolve(network, TopologyLevel.BUS_BRANCH).isPresent());

        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(busRef);
        assertEquals("{\"@c\":\".NodeNumberBasedBusRef\",\"voltageLevelId\":\"vl\",\"node\":1}", json);
        assertEquals(busRef, objectMapper.readValue(json, NodeNumberBasedBusRef.class));

        final Terminal.BusBreakerView bbv = mock(Terminal.BusBreakerView.class);
        when(terminal.getBusBreakerView()).thenReturn(bbv);
        when(bbv.getBus()).thenReturn(bbvBus);
        assertEquals(bbvBus, busRef.resolve(network, TopologyLevel.BUS_BREAKER).orElseThrow(AssertionError::new));

        when(vl.getTopologyKind()).thenReturn(TopologyKind.BUS_BREAKER);
        try {
            busRef.resolve(network, TopologyLevel.BUS_BRANCH);
            fail();
        } catch (Exception e) {
            assertEquals("Underlying topology not supported.", e.getMessage());
        }

        when(network.getVoltageLevel(eq("not_existing_vl"))).thenReturn(null);
        assertFalse(new NodeNumberBasedBusRef("not_existing_vl", 2).resolve(network, TopologyLevel.BUS_BRANCH).isPresent());

        assertEquals(busRef, new NodeNumberBasedBusRef("vl", 1));
    }

}
