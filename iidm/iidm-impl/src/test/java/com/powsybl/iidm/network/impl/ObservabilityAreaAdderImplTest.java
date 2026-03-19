/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.ObservabilityArea;
import com.powsybl.iidm.network.extensions.ObservabilityAreaAdder;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ObservabilityAreaAdderImplTest {

    @Test
    void shouldCreateNodeBreakerImplementationFromBusViewAndNodes() {
        Network network = ObservabilityAreaTestSupport.createNodeBreakerNetwork();
        VoltageLevel vl = network.getVoltageLevel("C");

        ObservabilityArea fromBusView = vl.newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByBusViewBus("C_0", 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .add();
        assertEquals("NodeBreakerObservabilityArea", fromBusView.getClass().getSimpleName());

        ObservabilityArea fromNodes = network.getVoltageLevel("N").newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByNodes(Set.of(0), 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .add();
        assertEquals("NodeBreakerObservabilityArea", fromNodes.getClass().getSimpleName());
    }

    @Test
    void shouldCreateBusBreakerImplementationFromBusViewAndBusBreakerBuses() {
        Network network = ObservabilityAreaTestSupport.createBusBreakerNetwork();
        VoltageLevel vl = network.getVoltageLevel("VLGEN");

        ObservabilityArea fromBusView = vl.newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByBusViewBus("VLGEN_0", 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .add();
        assertEquals("BusBreakerObservabilityArea", fromBusView.getClass().getSimpleName());

        ObservabilityArea fromBusBreaker = vl.newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByBusBreakerViewBuses(Set.of("NGEN"), 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .add();
        assertEquals("BusBreakerObservabilityArea", fromBusBreaker.getClass().getSimpleName());
    }

    @Test
    void shouldRejectUnsupportedTopologyInputs() {
        Network nodeBreakerNetwork = ObservabilityAreaTestSupport.createNodeBreakerNetwork();
        PowsyblException nodeBreakerFailure = assertThrows(PowsyblException.class, () -> nodeBreakerNetwork.getVoltageLevel("C").newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByBusBreakerViewBuses(Set.of("C"), 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .add());
        assertEquals("Observability areas must be exclusively filled by bus-view buses or nodes in node-breaker voltage levels", nodeBreakerFailure.getMessage());

        Network busBreakerNetwork = ObservabilityAreaTestSupport.createBusBreakerNetwork();
        PowsyblException busBreakerFailure = assertThrows(PowsyblException.class, () -> busBreakerNetwork.getVoltageLevel("VLGEN").newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByNodes(Set.of(1), 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .add());
        assertEquals("Observability areas must be exclusively filled by bus-view buses or bus-breaker-view buses in bus-breaker voltage levels", busBreakerFailure.getMessage());
    }

    @Test
    void shouldRejectMixedPopulationStrategies() {
        Network nodeBreakerNetwork = ObservabilityAreaTestSupport.createNodeBreakerNetwork();
        PowsyblException nodeBreakerFailure = assertThrows(PowsyblException.class, () -> nodeBreakerNetwork.getVoltageLevel("C").newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByBusViewBus("C_0", 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .withObservabilityAreaByNodes(Set.of(0), 2, ObservabilityArea.ObservabilityStatus.BORDER)
                .add());
        assertEquals("Observability areas must be exclusively filled by bus-view buses OR nodes, not both", nodeBreakerFailure.getMessage());

        Network busBreakerNetwork = ObservabilityAreaTestSupport.createBusBreakerNetwork();
        PowsyblException busBreakerFailure = assertThrows(PowsyblException.class, () -> busBreakerNetwork.getVoltageLevel("VLGEN").newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByBusViewBus("VLGEN_0", 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .withObservabilityAreaByBusBreakerViewBuses(Set.of("NGEN"), 2, ObservabilityArea.ObservabilityStatus.BORDER)
                .add());
        assertEquals("Observability areas must be exclusively filled by bus-view buses OR nodes, not both", busBreakerFailure.getMessage());
    }

    @Test
    void shouldPreserveCurrentOverwriteBehavior() {
        Network busBreakerNetwork = ObservabilityAreaTestSupport.createBusBreakerNetwork();
        ObservabilityArea busBreakerArea = busBreakerNetwork.getVoltageLevel("VLGEN").newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByBusViewBus("VLGEN_0", 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .withObservabilityAreaByBusViewBus("VLGEN_0", 2, ObservabilityArea.ObservabilityStatus.BORDER)
                .add();
        assertEquals(2, busBreakerArea.getBusView().getObservabilityArea("VLGEN_0").getAreaNumber());
        assertEquals(ObservabilityArea.ObservabilityStatus.BORDER, busBreakerArea.getBusView().getObservabilityArea("VLGEN_0").getStatus());

        Network nodeBreakerNetwork = ObservabilityAreaTestSupport.createNodeBreakerNetwork();
        ObservabilityArea nodeBreakerArea = nodeBreakerNetwork.getVoltageLevel("C").newExtension(ObservabilityAreaAdder.class)
                .withObservabilityAreaByNodes(Set.of(0), 1, ObservabilityArea.ObservabilityStatus.OBSERVABLE)
                .withObservabilityAreaByNodes(Set.of(0), 2, ObservabilityArea.ObservabilityStatus.NON_OBSERVABLE)
                .add();
        assertEquals(2, nodeBreakerArea.getNodeBreakerView().getObservabilityArea(0).getAreaNumber());
        assertEquals(ObservabilityArea.ObservabilityStatus.NON_OBSERVABLE, nodeBreakerArea.getNodeBreakerView().getObservabilityArea(0).getStatus());
    }

    @Test
    void shouldAllowEmptyExtensionCreation() {
        Network nodeBreakerNetwork = ObservabilityAreaTestSupport.createNodeBreakerNetwork();
        ObservabilityArea nodeBreakerArea = nodeBreakerNetwork.getVoltageLevel("C").newExtension(ObservabilityAreaAdder.class).add();
        assertNotNull(nodeBreakerArea);
        assertTrue(nodeBreakerArea.getObservabilityAreas().isEmpty());
        assertFalse(nodeBreakerArea.isConsistentWithTopology());

        Network busBreakerNetwork = ObservabilityAreaTestSupport.createBusBreakerNetwork();
        ObservabilityArea busBreakerArea = busBreakerNetwork.getVoltageLevel("VLGEN").newExtension(ObservabilityAreaAdder.class).add();
        assertNotNull(busBreakerArea);
        assertTrue(busBreakerArea.getObservabilityAreas().isEmpty());
        assertFalse(busBreakerArea.isConsistentWithTopology());
    }
}
