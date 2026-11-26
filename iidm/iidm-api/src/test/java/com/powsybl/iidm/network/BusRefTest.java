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
import com.powsybl.commons.PowsyblException;
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

    @Test
    void testTerminalBasedBusRefForInjection() throws JsonProcessingException {
        Bus bus = mock(Bus.class);
        Injection<?> laterInjection = mock(Injection.class);
        Injection<?> anchorInjection = mock(Injection.class);
        Injection<?> fictitiousInjection = mock(Injection.class);
        when(laterInjection.getId()).thenReturn("z-load");
        when(anchorInjection.getId()).thenReturn("a-load");
        when(fictitiousInjection.getId()).thenReturn("0-generated");
        when(fictitiousInjection.isFictitious()).thenReturn(true);

        Terminal laterTerminal = mock(Terminal.class);
        Terminal anchorTerminal = mock(Terminal.class);
        Terminal fictitiousTerminal = mock(Terminal.class);
        when(laterTerminal.getConnectable()).thenReturn(laterInjection);
        when(anchorTerminal.getConnectable()).thenReturn(anchorInjection);
        when(fictitiousTerminal.getConnectable()).thenReturn(fictitiousInjection);
        doAnswer(invocation -> Stream.of(laterTerminal, fictitiousTerminal, anchorTerminal)).when(bus).getConnectedTerminalStream();

        TerminalBasedBusRef busRef = TerminalBasedBusRef.of(bus).orElseThrow();
        assertEquals(new TerminalBasedBusRef("a-load"), busRef);
        assertEquals(new TerminalBasedBusRef("a-load").hashCode(), busRef.hashCode());

        Network network = mock(Network.class);
        doReturn(anchorInjection).when(network).getIdentifiable("a-load");
        when(anchorInjection.getTerminal()).thenReturn(anchorTerminal);
        Terminal.BusView busView = mock(Terminal.BusView.class);
        when(anchorTerminal.getBusView()).thenReturn(busView);
        when(busView.getBus()).thenReturn(bvBus);
        Terminal.BusBreakerView busBreakerView = mock(Terminal.BusBreakerView.class);
        when(anchorTerminal.getBusBreakerView()).thenReturn(busBreakerView);
        when(busBreakerView.getBus()).thenReturn(bbvBus);
        assertEquals(bvBus, busRef.resolve(network, TopologyLevel.BUS_BRANCH).orElseThrow());
        assertEquals(bbvBus, busRef.resolve(network, TopologyLevel.BUS_BREAKER).orElseThrow());
        when(busView.getBus()).thenReturn(null);
        assertTrue(busRef.resolve(network, TopologyLevel.BUS_BRANCH).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> busRef.resolve(network, TopologyLevel.NODE_BREAKER));

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(busRef);
        assertEquals("{\"@c\":\".TerminalBasedBusRef\",\"id\":\"a-load\"}", json);
        assertEquals(busRef, objectMapper.readValue(json, BusRef.class));

        when(network.getIdentifiable("missing")).thenReturn(null);
        assertTrue(new TerminalBasedBusRef("missing").resolve(network, TopologyLevel.BUS_BRANCH).isEmpty());
    }

    @Test
    void testTerminalBasedBusRefForBranchAndConverter() throws JsonProcessingException {
        Network network = mock(Network.class);
        Line branch = mock(Line.class);
        when(branch.getId()).thenReturn("branchId");
        Terminal branchTerminalOne = mock(Terminal.class);
        Terminal branchTerminalTwo = mock(Terminal.class);
        when(branchTerminalOne.getConnectable()).thenReturn(branch);
        when(branchTerminalTwo.getConnectable()).thenReturn(branch);
        when(branch.getSide(branchTerminalOne)).thenReturn(TwoSides.ONE);
        when(branch.getSide(branchTerminalTwo)).thenReturn(TwoSides.TWO);
        Bus bus = mock(Bus.class);
        doAnswer(invocation -> Stream.of(branchTerminalTwo, branchTerminalOne)).when(bus).getConnectedTerminalStream();
        assertEquals(new TerminalBasedBusRef("branchId", ThreeSides.ONE, null), TerminalBasedBusRef.of(bus).orElseThrow());
        TerminalBasedBusRef branchRef = new TerminalBasedBusRef("branchId", ThreeSides.TWO, null);
        assertEquals(new TerminalBasedBusRef("branchId", ThreeSides.TWO, null), branchRef);

        doReturn(branch).when(network).getIdentifiable("branchId");
        when(branch.getTerminal(TwoSides.TWO)).thenReturn(branchTerminalTwo);
        Terminal.BusView branchBusView = mock(Terminal.BusView.class);
        when(branchTerminalTwo.getBusView()).thenReturn(branchBusView);
        when(branchBusView.getBus()).thenReturn(bvBus);
        assertEquals(bvBus, branchRef.resolve(network, TopologyLevel.BUS_BRANCH).orElseThrow());

        ObjectMapper objectMapper = new ObjectMapper();
        String branchJson = objectMapper.writeValueAsString(branchRef);
        assertEquals("{\"@c\":\".TerminalBasedBusRef\",\"id\":\"branchId\",\"side\":\"TWO\"}", branchJson);
        assertEquals(branchRef, objectMapper.readValue(branchJson, BusRef.class));

        AcDcConverter<?> converter = mock(AcDcConverter.class);
        when(converter.getId()).thenReturn("converter");
        Terminal converterTerminalOne = mock(Terminal.class);
        Terminal converterTerminalTwo = mock(Terminal.class);
        when(converterTerminalOne.getConnectable()).thenReturn(converter);
        when(converterTerminalTwo.getConnectable()).thenReturn(converter);
        when(converter.getTerminalNumber(converterTerminalOne)).thenReturn(TerminalNumber.ONE);
        when(converter.getTerminalNumber(converterTerminalTwo)).thenReturn(TerminalNumber.TWO);
        doAnswer(invocation -> Stream.of(converterTerminalTwo, converterTerminalOne)).when(bus).getConnectedTerminalStream();
        assertEquals(new TerminalBasedBusRef("converter", null, TerminalNumber.ONE), TerminalBasedBusRef.of(bus).orElseThrow());
        TerminalBasedBusRef converterRef = new TerminalBasedBusRef("converter", null, TerminalNumber.TWO);
        doReturn(converter).when(network).getIdentifiable("converter");
        when(converter.getTerminal(TerminalNumber.TWO)).thenReturn(Optional.of(converterTerminalTwo));
        when(converterTerminalTwo.getBusView()).thenReturn(branchBusView);
        assertEquals(bvBus, converterRef.resolve(network, TopologyLevel.BUS_BRANCH).orElseThrow());
    }

    @Test
    void testTerminalBasedBusRefEligibilityAndInvalidReferences() {
        Bus bus = mock(Bus.class);
        BusbarSection busbar = mock(BusbarSection.class);
        when(busbar.getId()).thenReturn("busbar");
        Terminal busbarTerminal = mock(Terminal.class);
        doReturn(busbar).when(busbarTerminal).getConnectable();
        doAnswer(invocation -> Stream.of(busbarTerminal)).when(bus).getConnectedTerminalStream();
        assertTrue(TerminalBasedBusRef.of(bus).isEmpty());
        assertEquals(new TerminalBasedBusRef("busbar"),
                TerminalBasedBusRef.of(bus, BusbarSection.class::isInstance).orElseThrow());
        assertThrows(NullPointerException.class, () -> TerminalBasedBusRef.of(bus, null));

        doReturn(Stream.empty()).when(bus).getConnectedTerminalStream();
        assertTrue(TerminalBasedBusRef.of(bus).isEmpty());
        assertThrows(PowsyblException.class, () -> new TerminalBasedBusRef("id", ThreeSides.ONE, TerminalNumber.ONE));

        Network network = mock(Network.class);
        Injection<?> injection = mock(Injection.class);
        doReturn(injection).when(network).getIdentifiable("injection");
        TerminalBasedBusRef busRefInjection = new TerminalBasedBusRef("injection", ThreeSides.ONE, null);
        assertThrows(PowsyblException.class, () -> busRefInjection.resolve(network, TopologyLevel.BUS_BRANCH));

        Branch<?> branch = mock(Branch.class);
        doReturn(branch).when(network).getIdentifiable("branch");
        TerminalBasedBusRef busRefBranch = new TerminalBasedBusRef("branch", null, TerminalNumber.ONE);
        assertThrows(PowsyblException.class, () -> busRefBranch.resolve(network, TopologyLevel.BUS_BRANCH));
        TerminalBasedBusRef busRefBranchThree = new TerminalBasedBusRef("branch", ThreeSides.THREE, null);
        assertThrows(PowsyblException.class, () -> busRefBranchThree.resolve(network, TopologyLevel.BUS_BRANCH));

        AcDcConverter<?> converter = mock(AcDcConverter.class);
        doReturn(converter).when(network).getIdentifiable("converter");
        when(converter.getTerminal(TerminalNumber.TWO)).thenReturn(Optional.empty());
        TerminalBasedBusRef busRefConverter = new TerminalBasedBusRef("converter", null, TerminalNumber.TWO);
        assertThrows(PowsyblException.class, () -> busRefConverter.resolve(network, TopologyLevel.BUS_BRANCH));
    }
}
