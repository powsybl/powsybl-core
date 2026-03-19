/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ObservabilityArea;
import com.powsybl.iidm.network.extensions.ObservabilityAreaAdder;
import com.powsybl.iidm.network.util.Networks;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class NodeBreakerObservabilityAreaTest {

    @Test
    void shouldExposeAreasFromNodeDefinition() {
        Network network = ObservabilityAreaTestSupport.createNodeBreakerNetwork();
        VoltageLevel vlC = network.getVoltageLevel("C");
        ObservabilityArea ext = ObservabilityAreaTestSupport.populateNodeBreakerAreaFromNodes(vlC);
        Map<String, Set<Integer>> nodesByBus = Networks.getNodesByBus(vlC);

        int n = 1;
        for (Bus bus : vlC.getBusView().getBuses()) {
            ObservabilityArea.AreaCharacteristics area = ext.getBusView().getObservabilityArea(bus.getId());
            assertNotNull(area);
            assertEquals(n, area.getAreaNumber());
            assertEquals(ObservabilityAreaTestSupport.getStatus(n), area.getStatus());
            assertTrue(bus.getConnectedTerminalStream().allMatch(t -> area.getTerminals().contains(t)));
            assertTrue(area.getTerminals().containsAll(bus.getConnectedTerminalStream().collect(Collectors.toSet())));
            for (Bus busBreakerBus : vlC.getBusBreakerView().getBusesFromBusViewBusId(bus.getId())) {
                ObservabilityArea.AreaCharacteristics busBreakerArea = ext.getBusBreakerView().getObservabilityArea(busBreakerBus.getId());
                assertNotNull(busBreakerArea);
                assertEquals(n, busBreakerArea.getAreaNumber());
            }
            for (int node : nodesByBus.get(bus.getId())) {
                ObservabilityArea.AreaCharacteristics nodeArea = ext.getNodeBreakerView().getObservabilityArea(node);
                assertNotNull(nodeArea);
                assertEquals(n, nodeArea.getAreaNumber());
            }
            for (Terminal terminal : bus.getConnectedTerminals()) {
                assertEquals(n, ext.getObservabilityArea(terminal).getAreaNumber());
            }
            n++;
        }
        assertEquals(vlC.getBusView().getBusStream().count(), ext.getObservabilityAreas().size());
        assertTrue(ext.isConsistentWithTopology());
    }

    @Test
    void shouldExposeImmutableNodeMapsAndTopologySpecificData() {
        Network network = ObservabilityAreaTestSupport.createNodeBreakerNetwork();
        VoltageLevel vlC = network.getVoltageLevel("C");
        ObservabilityArea ext = ObservabilityAreaTestSupport.populateNodeBreakerAreaFromNodes(vlC);

        Map<Integer, ObservabilityArea.AreaCharacteristics> nodeAreas = ext.getNodeBreakerView().getObservabilityAreaByNode();
        assertEquals(Set.of(0, 1, 2, 3, 4), nodeAreas.keySet());
        assertThrows(UnsupportedOperationException.class, () -> nodeAreas.put(99, nodeAreas.get(0)));

        ObservabilityArea.AreaCharacteristics nodeArea = nodeAreas.get(0);
        assertEquals(Set.of(0, 1, 2, 3, 4), nodeArea.getNodeBreakerData().getNodes());
        assertThrows(UnsupportedOperationException.class, () -> nodeArea.getNodeBreakerData().getNodes().add(99));

        UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> nodeArea.getBusBreakerData().getBusIds());
        assertEquals("Not supported in a node breaker topology", e.getMessage());

        assertTrue(ext.getBusBreakerView().getObservabilityAreaByBus().values().stream().anyMatch(Objects::nonNull));
        assertNull(ext.getBusBreakerView().getObservabilityArea(null, false));
    }

    @Test
    void shouldExposeAreasFromBusViewDefinition() {
        Network network = ObservabilityAreaTestSupport.createNodeBreakerNetwork();
        VoltageLevel vlC = network.getVoltageLevel("C");
        ObservabilityAreaAdder adder = vlC.newExtension(ObservabilityAreaAdder.class);
        int n = 1;
        for (Bus bus : vlC.getBusView().getBuses()) {
            adder.withObservabilityAreaByBusViewBus(bus.getId(), 10 * n, ObservabilityAreaTestSupport.getStatus(n));
            n++;
        }

        ObservabilityArea ext = adder.add();

        n = 1;
        for (Bus bus : vlC.getBusView().getBuses()) {
            assertEquals(10 * n, ext.getBusView().getObservabilityArea(bus.getId()).getAreaNumber());
            assertEquals(ObservabilityAreaTestSupport.getStatus(n), ext.getBusView().getObservabilityArea(bus.getId()).getStatus());
            n++;
        }
        assertTrue(ext.isConsistentWithTopology());
    }

    @Test
    void shouldReturnNullForNewBusAfterTopologyChange() {
        Network network = ObservabilityAreaTestSupport.createNodeBreakerNetwork();
        VoltageLevel vlC = network.getVoltageLevel("C");
        ObservabilityArea ext = ObservabilityAreaTestSupport.populateNodeBreakerAreaFromNodes(vlC);

        vlC.getNodeBreakerView().getSwitch("TEST_SW").setOpen(false);

        assertNotNull(vlC.getBusView().getBus("C_9"));
        assertNull(ext.getBusView().getObservabilityArea("C_9"));
        assertNull(ext.getBusView().getObservabilityAreaByBus(false).get("C_9"));
        assertNull(ext.getBusBreakerView().getObservabilityAreaByBus(false).get("TEST_BBS"));
        assertFalse(ext.isConsistentWithTopology());
    }

    @Test
    void shouldHandlePartiallyDefinedAndConflictingCurrentBuses() {
        Network network = ObservabilityAreaTestSupport.createNodeBreakerNetwork();
        VoltageLevel vlC = network.getVoltageLevel("C");
        ObservabilityArea ext = ObservabilityAreaTestSupport.populateNodeBreakerAreaFromNodes(vlC);

        vlC.getNodeBreakerView().newSwitch().setId("TEST2_SW").setNode1(0).setNode2(9).setOpen(false).setKind(SwitchKind.DISCONNECTOR).add();
        PowsyblException partiallyDefined = assertThrows(PowsyblException.class, () -> ext.getBusView().getObservabilityArea("C_0"));
        assertEquals("Inconsistent observability areas: only part of nodes of bus-view bus C_0 are defined", partiallyDefined.getMessage());
        ObservabilityArea.AreaCharacteristics survivingArea = ext.getBusView().getObservabilityArea("C_0", false);
        assertNotNull(survivingArea);
        assertEquals(1, survivingArea.getAreaNumber());
        assertEquals(1, ext.getBusView().getObservabilityAreaByBus(false).get("C_0").getAreaNumber());
        assertTrue(ext.getBusBreakerView().getObservabilityAreaByBus(false).values().stream()
                .filter(Objects::nonNull)
                .anyMatch(area -> area.getAreaNumber() == 1));

        Network conflictingNetwork = ObservabilityAreaTestSupport.createNodeBreakerNetwork();
        VoltageLevel conflictingVl = conflictingNetwork.getVoltageLevel("C");
        ObservabilityArea conflictingArea = conflictingVl.newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByNodes(Set.of(0, 1), 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .withObservabilityAreaByNodes(Set.of(2, 3, 4), 2, ObservabilityArea.ObservabilityStatus.BORDER)
                .add();

        PowsyblException conflicting = assertThrows(PowsyblException.class, () -> conflictingArea.getBusView().getObservabilityArea("C_0"));
        assertEquals("Inconsistent observability areas: bus-view bus C_0 has different area numbers and/or status", conflicting.getMessage());
        assertNotNull(conflictingArea.getBusView().getObservabilityArea("C_0", false));
        assertNotNull(conflictingArea.getBusView().getObservabilityAreaByBus(false).get("C_0"));
        assertTrue(conflictingArea.getBusBreakerView().getObservabilityAreaByBus(false).values().stream().anyMatch(Objects::nonNull));
    }

    @Test
    void shouldThrowWhenBusViewInputReferencesUnknownBus() {
        Network network = ObservabilityAreaTestSupport.createNodeBreakerNetwork();

        PowsyblException e = assertThrows(PowsyblException.class, () -> network.getVoltageLevel("C").newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByBusViewBus("UNKNOWN", 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .add());

        assertEquals("Bus-view bus UNKNOWN does not exist", e.getMessage());
    }
}
