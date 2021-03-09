/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.iidm.network.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class BusRefTest {

    private final Bus bus = mock(Bus.class);

    @Test
    public void testIdBasedBusRef() throws JsonProcessingException {
        Network network = mock(Network.class);
        VoltageLevel vl = mock(VoltageLevel.class);
        when(network.getVoltageLevel(eq("vid"))).thenReturn(vl);
        VoltageLevel.BusView bv = mock(VoltageLevel.BusView.class);
        when(vl.getBusView()).thenReturn(bv);
        when(bv.getBus(eq("id"))).thenReturn(bus);

        final BusRef busRef = new IdBasedBusRef("vid", "id");
        assertEquals(bus, busRef.resolve(network).orElseThrow(AssertionError::new));
        assertFalse(new IdBasedBusRef("vid", "another").resolve(network).isPresent());

        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(busRef);
        assertEquals("{\"@c\":\".IdBasedBusRef\",\"voltageLevelId\":\"vid\",\"busId\":\"id\"}", json);
        final BusRef deserialized = objectMapper.readValue(json, BusRef.class);
        assertEquals(busRef, deserialized);
    }

    @Test
    public void testBranchBased() throws JsonProcessingException {
        Network network = mock(Network.class);
        Branch branch = mock(Branch.class);
        when(network.getBranch(eq("branchId"))).thenReturn(branch);
        Terminal terminal = mock(Terminal.class);
        Terminal.BusView bv = mock(Terminal.BusView.class);
        when(branch.getTerminal1()).thenReturn(terminal);
        when(terminal.getBusView()).thenReturn(bv);
        when(bv.getBus()).thenReturn(bus);
        final BusRef busRef = new BranchBasedBusRef("branchId", "ONE");
        assertEquals(bus, busRef.resolve(network).orElseThrow(AssertionError::new));

        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(busRef);
        assertEquals("{\"@c\":\".BranchBasedBusRef\",\"branchId\":\"branchId\",\"side\":\"ONE\"}", json);
        final BusRef deserialized = objectMapper.readValue(json, BusRef.class);
        assertEquals(busRef, deserialized);
    }

    @Test
    public void testInjectionBased() throws JsonProcessingException {
        Network network = mock(Network.class);
        Injection injection = mock(Injection.class);
        when(network.getIdentifiable(eq("injectionId"))).thenReturn(injection);
        Terminal terminal = mock(Terminal.class);
        Terminal.BusView bv = mock(Terminal.BusView.class);
        when(injection.getTerminal()).thenReturn(terminal);
        when(terminal.getBusView()).thenReturn(bv);
        when(bv.getBus()).thenReturn(bus);
        final InjectionBasedBusRef busRef = new InjectionBasedBusRef("injectionId");
        assertEquals(bus, busRef.resolve(network).orElseThrow(AssertionError::new));

        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(busRef);
        assertEquals("{\"@c\":\".InjectionBasedBusRef\",\"injectionId\":\"injectionId\"}", json);
        final InjectionBasedBusRef deserialized = objectMapper.readValue(json, InjectionBasedBusRef.class);
        assertEquals(busRef, deserialized);
    }

    @Test
    public void testNodeNumberBased() throws JsonProcessingException {
        Network network = mock(Network.class);
        VoltageLevel vl = mock(VoltageLevel.class);
        when(network.getVoltageLevel("vl")).thenReturn(vl);
        VoltageLevel.NodeBreakerView nbv = mock(VoltageLevel.NodeBreakerView.class);
        when(vl.getNodeBreakerView()).thenReturn(nbv);
        Terminal terminal = mock(Terminal.class);
        Terminal.BusView bv = mock(Terminal.BusView.class);
        when(terminal.getBusView()).thenReturn(bv);
        when(bv.getBus()).thenReturn(bus);
        when(nbv.getTerminal(eq(1))).thenReturn(terminal);

        BusRef busRef = new NodeNumberBasedBusRef("vl", 1);
        assertEquals(bus, busRef.resolve(network).orElseThrow(AssertionError::new));
        assertFalse(new NodeNumberBasedBusRef("vl", 2).resolve(network).isPresent());

        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(busRef);
        assertEquals("{\"@c\":\".NodeNumberBasedBusRef\",\"voltageLevelId\":\"vl\",\"node\":1}", json);
        assertEquals(busRef, objectMapper.readValue(json, NodeNumberBasedBusRef.class));
    }

}
