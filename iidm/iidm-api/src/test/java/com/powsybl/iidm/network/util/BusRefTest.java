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
        VoltageLevel vl = mock(VoltageLevel.class);
        VoltageLevel.BusView bv = mock(VoltageLevel.BusView.class);
        when(vl.getBusView()).thenReturn(bv);
        when(bv.getBus(eq("id"))).thenReturn(bus);

        final BusRef busRef = new IdBasedBusRef(vl, "id");
        assertEquals(bus, busRef.resolve().orElseThrow(AssertionError::new));
        assertFalse(new IdBasedBusRef(vl, "another").resolve().isPresent());

        IdBasedBusBean idBasedBusBean = new IdBasedBusBean("vid", "bid");
        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(idBasedBusBean);
        assertEquals("{\"voltageLevelId\":\"vid\",\"busId\":\"bid\"}", json);
        final IdBasedBusBean deserialized = objectMapper.readValue(json, IdBasedBusBean.class);
        assertEquals(idBasedBusBean, deserialized);
    }

    @Test
    public void testBranchBased() throws JsonProcessingException {
        Branch branch = mock(Branch.class);
        Terminal terminal = mock(Terminal.class);
        Terminal.BusView bv = mock(Terminal.BusView.class);
        when(branch.getTerminal1()).thenReturn(terminal);
        when(terminal.getBusView()).thenReturn(bv);
        when(bv.getBus()).thenReturn(bus);
        final BusRef busRef = new BranchBasedBusRef(branch, Branch.Side.ONE);
        assertEquals(bus, busRef.resolve().orElseThrow(AssertionError::new));

        BranchBasedBusBean bean = new BranchBasedBusBean("branchId", "ONE");
        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(bean);
        assertEquals("{\"branchId\":\"branchId\",\"side\":\"ONE\"}", json);
        final BranchBasedBusBean deserialized = objectMapper.readValue(json, BranchBasedBusBean.class);
        assertEquals(bean, deserialized);
    }

    @Test
    public void testInjectionBased() throws JsonProcessingException {
        Injection injection = mock(Injection.class);
        Terminal terminal = mock(Terminal.class);
        Terminal.BusView bv = mock(Terminal.BusView.class);
        when(injection.getTerminal()).thenReturn(terminal);
        when(terminal.getBusView()).thenReturn(bv);
        when(bv.getBus()).thenReturn(bus);
        final InjectionBasedBusRef busRef = new InjectionBasedBusRef(injection);
        assertEquals(bus, busRef.resolve().orElseThrow(AssertionError::new));

        InjectionBasedBusBean bean = new InjectionBasedBusBean("injectionId");
        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(bean);
        assertEquals("{\"injectionId\":\"injectionId\"}", json);
        final InjectionBasedBusBean deserialized = objectMapper.readValue(json, InjectionBasedBusBean.class);
        assertEquals(bean, deserialized);
    }

    @Test
    public void testNodeNumberBased() throws JsonProcessingException {
        VoltageLevel vl = mock(VoltageLevel.class);
        VoltageLevel.NodeBreakerView nbv = mock(VoltageLevel.NodeBreakerView.class);
        when(vl.getNodeBreakerView()).thenReturn(nbv);
        Terminal terminal = mock(Terminal.class);
        Terminal.BusView bv = mock(Terminal.BusView.class);
        when(terminal.getBusView()).thenReturn(bv);
        when(bv.getBus()).thenReturn(bus);
        when(nbv.getTerminal(eq(1))).thenReturn(terminal);

        BusRef busRef = new NodeNumberBasedBusRef(vl, 1);
        assertEquals(bus, busRef.resolve().orElseThrow(AssertionError::new));
        assertFalse(new NodeNumberBasedBusRef(vl, 2).resolve().isPresent());

        NodeNumberBasedBusBean bean = new NodeNumberBasedBusBean("vl", 1);
        ObjectMapper objectMapper = new ObjectMapper();
        final String json = objectMapper.writeValueAsString(bean);
        assertEquals("{\"voltageLevelId\":\"vl\",\"node\":1}", json);
        assertEquals(bean, objectMapper.readValue(json, NodeNumberBasedBusBean.class));
    }

}
