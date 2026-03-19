/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ObservabilityArea;
import com.powsybl.iidm.network.extensions.ObservabilityAreaAdder;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class BusBreakerObservabilityAreaTest {

    @Test
    void shouldExposeAreasFromBusViewDefinition() {
        Network network = ObservabilityAreaTestSupport.createBusBreakerNetwork();
        VoltageLevel vlgen = network.getVoltageLevel("VLGEN");

        ObservabilityArea ext = ObservabilityAreaTestSupport.populateBusBreakerAreaFromBusView(vlgen);

        assertNotNull(ext);
        int n = 1;
        for (Bus bus : vlgen.getBusView().getBuses()) {
            ObservabilityArea.AreaCharacteristics area = ext.getBusView().getObservabilityArea(bus.getId());
            assertNotNull(area);
            assertEquals(n, area.getAreaNumber());
            assertEquals(ObservabilityAreaTestSupport.getStatus(n), area.getStatus());
            assertTrue(bus.getConnectedTerminalStream().allMatch(t -> area.getTerminals().contains(t)));
            assertTrue(area.getTerminals().containsAll(bus.getConnectedTerminalStream().collect(Collectors.toSet())));
            for (Bus busBreakerBus : vlgen.getBusBreakerView().getBusesFromBusViewBusId(bus.getId())) {
                ObservabilityArea.AreaCharacteristics busBreakerArea = ext.getBusBreakerView().getObservabilityArea(busBreakerBus.getId());
                assertNotNull(busBreakerArea);
                assertEquals(n, busBreakerArea.getAreaNumber());
                assertEquals(ObservabilityAreaTestSupport.getStatus(n), busBreakerArea.getStatus());
            }
            for (Terminal terminal : bus.getConnectedTerminals()) {
                assertNotNull(ext.getObservabilityArea(terminal));
                assertEquals(n, ext.getObservabilityArea(terminal).getAreaNumber());
                assertEquals(ObservabilityAreaTestSupport.getStatus(n), ext.getObservabilityArea(terminal).getStatus());
            }
            n++;
        }
        assertEquals(vlgen.getBusView().getBusStream().count(), ext.getObservabilityAreas().size());
        assertTrue(ext.isConsistentWithTopology());
    }

    @Test
    void shouldExposeAreasFromBusBreakerDefinition() {
        Network network = ObservabilityAreaTestSupport.createBusBreakerNetwork();
        VoltageLevel vlgen = network.getVoltageLevel("VLGEN");

        ObservabilityArea ext = vlgen.newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByBusBreakerViewBuses(Set.of("NGEN"), 10, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .withObservabilityAreaByBusBreakerViewBuses(Set.of("TEST"), 20, ObservabilityArea.ObservabilityStatus.BORDER)
                .withObservabilityAreaByBusBreakerViewBuses(Set.of("TEST2"), 30, ObservabilityArea.ObservabilityStatus.NON_OBSERVABLE)
                .add();

        assertEquals(10, ext.getBusBreakerView().getObservabilityArea("NGEN").getAreaNumber());
        assertEquals(20, ext.getBusBreakerView().getObservabilityArea("TEST").getAreaNumber());
        assertEquals(30, ext.getBusBreakerView().getObservabilityArea("TEST2").getAreaNumber());
        assertEquals(Set.of("NGEN"), ext.getBusBreakerView().getObservabilityArea("NGEN").getBusBreakerData().getBusIds());
        assertEquals(Set.of("TEST"), ext.getBusBreakerView().getObservabilityArea("TEST").getBusBreakerData().getBusIds());
        assertEquals(Set.of("TEST2"), ext.getBusBreakerView().getObservabilityArea("TEST2").getBusBreakerData().getBusIds());
        assertEquals(3, ext.getObservabilityAreas().size());
        assertFalse(ext.isConsistentWithTopology());
    }

    @Test
    void shouldReturnNullForMissingBusBreakerBus() {
        Network network = ObservabilityAreaTestSupport.createBusBreakerNetwork();
        ObservabilityArea ext = ObservabilityAreaTestSupport.populateBusBreakerAreaFromBusView(network.getVoltageLevel("VLGEN"));

        assertNull(ext.getBusBreakerView().getObservabilityArea("UNKNOWN"));
    }

    @Test
    void shouldExposeImmutableBusMapsAndTopologySpecificData() {
        Network network = ObservabilityAreaTestSupport.createBusBreakerNetwork();
        ObservabilityArea ext = ObservabilityAreaTestSupport.populateBusBreakerAreaFromBusView(network.getVoltageLevel("VLGEN"));

        Map<String, ObservabilityArea.AreaCharacteristics> busBreakerAreas = ext.getBusBreakerView().getObservabilityAreaByBus();
        assertEquals(Set.of("NGEN", "TEST", "TEST2"), busBreakerAreas.keySet());
        assertThrows(UnsupportedOperationException.class, () -> busBreakerAreas.put("OTHER", busBreakerAreas.get("NGEN")));

        ObservabilityArea.AreaCharacteristics ngenArea = busBreakerAreas.get("NGEN");
        assertEquals(Set.of("NGEN", "TEST"), ngenArea.getBusBreakerData().getBusIds());
        assertThrows(UnsupportedOperationException.class, () -> ngenArea.getBusBreakerData().getBusIds().add("OTHER"));

        UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class, () -> ngenArea.getNodeBreakerData().getNodes());
        assertEquals("Not supported in a bus breaker topology", e.getMessage());

        assertNotNull(ext.getBusView().getObservabilityAreaByBus().get("VLGEN_0"));
        assertNull(ext.getBusView().getObservabilityArea("UNKNOWN"));
    }

    @Test
    void shouldThrowForUnsupportedNodeBreakerViewAccess() {
        Network network = ObservabilityAreaTestSupport.createBusBreakerNetwork();
        ObservabilityArea ext = ObservabilityAreaTestSupport.populateBusBreakerAreaFromBusView(network.getVoltageLevel("VLGEN"));

        UnsupportedOperationException e1 = assertThrows(UnsupportedOperationException.class, () -> ext.getNodeBreakerView().getObservabilityAreaByNode());
        assertEquals("Not supported in a bus breaker topology", e1.getMessage());
        UnsupportedOperationException e2 = assertThrows(UnsupportedOperationException.class, () -> ext.getNodeBreakerView().getObservabilityArea(0));
        assertEquals("Not supported in a bus breaker topology", e2.getMessage());
    }

    @Test
    void shouldHandleTopologyAndStructureDriftInBusViewLookups() {
        Network network = ObservabilityAreaTestSupport.createBusBreakerNetwork();
        VoltageLevel vlgen = network.getVoltageLevel("VLGEN");
        ObservabilityArea ext = ObservabilityAreaTestSupport.populateBusBreakerAreaFromBusView(vlgen);

        vlgen.getBusBreakerView().getSwitch("TEST2_SW").setOpen(false);
        PowsyblException inconsistentAreas = assertThrows(PowsyblException.class, () -> ext.getBusView().getObservabilityArea("VLGEN_0"));
        assertEquals("Inconsistent observabilities areas: bus VLGEN_0 is associated to different area numbers and/or status", inconsistentAreas.getMessage());
        ObservabilityArea.AreaCharacteristics survivingArea = ext.getBusView().getObservabilityArea("VLGEN_0", false);
        assertNotNull(survivingArea);
        assertTrue(Set.of(1, 2).contains(survivingArea.getAreaNumber()));
        assertEquals(1, ext.getBusView().getObservabilityAreaByBus(false).size());
        assertTrue(Set.of(1, 2).contains(ext.getBusView().getObservabilityAreaByBus(false).get("VLGEN_0").getAreaNumber()));

        vlgen.getBusBreakerView().removeSwitch("TEST_SW");
        vlgen.getBusBreakerView().removeSwitch("TEST2_SW");
        vlgen.getBusBreakerView().removeBus("TEST");
        PowsyblException missingBus = assertThrows(PowsyblException.class, () -> ext.getBusView().getObservabilityArea("VLGEN_0"));
        assertEquals("Inconsistent observabilities areas: bus TEST does not exist anymore in bus-breaker view", missingBus.getMessage());
        ObservabilityArea.AreaCharacteristics areaAfterRemoval = ext.getBusView().getObservabilityArea("VLGEN_0", false);
        assertNotNull(areaAfterRemoval);
        assertEquals(1, areaAfterRemoval.getAreaNumber());
        assertEquals(ObservabilityArea.ObservabilityStatus.BORDER, areaAfterRemoval.getStatus());
        assertFalse(ext.isConsistentWithTopology());
    }

    @Test
    void shouldThrowWhenBusViewInputReferencesUnknownBus() {
        Network network = ObservabilityAreaTestSupport.createBusBreakerNetwork();

        PowsyblException e = assertThrows(PowsyblException.class, () -> network.getVoltageLevel("VLGEN").newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByBusViewBus("UNKNOWN", 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .add());

        assertEquals("Bus-view bus UNKNOWN does not exist", e.getMessage());
    }
}
