/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.powsybl.iidm.network.ThreeSides;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
class ObservabilityResultImplTest {

    @Test
    void oneConnectedComponentCanHoldSeveralObservableIslands() {
        ObservabilityResult.ObservableIsland observable = new ObservabilityResultImpl.ObservableIslandImpl(
                0, ObservabilityResult.IslandStatus.OBSERVABLE, Set.of("BUS1", "BUS2"));
        ObservabilityResult.ObservableIsland dark = new ObservabilityResultImpl.ObservableIslandImpl(
                1, ObservabilityResult.IslandStatus.NON_OBSERVABLE, Set.of("BUS3"));

        ObservabilityResultImpl result = new ObservabilityResultImpl(ObservabilityResult.Status.COMPLETED,
                "two islands", List.of(observable, dark), List.of());

        assertEquals(2, result.getObservableIslands().size());
        assertEquals("two islands", result.getStatusText());
        assertEquals(ObservabilityResult.IslandStatus.OBSERVABLE, result.getIsland("BUS1").orElseThrow().getStatus());
        assertEquals(1, result.getIsland("BUS3").orElseThrow().getIslandNumber());
    }

    @Test
    void aBranchEntryNamesItsSideAndAnInjectionEntryDoesNot() {
        ObservabilityResult.QuantityRedundancy branchSide = new ObservabilityResultImpl.QuantityRedundancyImpl(
                Quantity.REACTIVE_POWER, ThreeSides.TWO, true);
        ObservabilityResult.QuantityRedundancy injection = new ObservabilityResultImpl.QuantityRedundancyImpl(
                Quantity.VOLTAGE, null, null);

        assertEquals(ThreeSides.TWO, branchSide.getSide().orElseThrow());
        assertEquals(Quantity.REACTIVE_POWER, branchSide.getQuantity());

        assertTrue(injection.getSide().isEmpty());
        assertEquals(Quantity.VOLTAGE, injection.getQuantity());
    }

    @Test
    void aVoltageRedundancyRefusesABranchSide() {
        assertThrows(IllegalArgumentException.class, () -> new ObservabilityResultImpl.QuantityRedundancyImpl(
                Quantity.VOLTAGE, ThreeSides.ONE, true));
    }

    @Test
    void anUnsetRedundancyIsEmptyAndACriticalQuantityIsFalse() {
        ObservabilityResult.QuantityRedundancy notEstablished = new ObservabilityResultImpl.QuantityRedundancyImpl(
                Quantity.ACTIVE_POWER, null, null);
        ObservabilityResult.QuantityRedundancy critical = new ObservabilityResultImpl.QuantityRedundancyImpl(
                Quantity.ACTIVE_POWER, null, false);

        assertTrue(notEstablished.isRedundant().isEmpty());
        assertFalse(critical.isRedundant().orElseThrow());
    }

    @Test
    void aFailedAnalysisReadsAsEmptyRatherThanNull() {
        ObservabilityResultImpl result = new ObservabilityResultImpl(
                ObservabilityResult.Status.FAILED, null, List.of(), List.of());

        assertEquals(ObservabilityResult.Status.FAILED, result.getStatus());
        assertEquals("", result.getStatusText());
        assertTrue(result.getObservableIslands().isEmpty());
        assertTrue(result.getElementObservabilities().isEmpty());
    }

    @Test
    void doesNotFollowALaterChangeToTheSetItWasGiven() {
        Set<String> busIds = new HashSet<>(Set.of("BUS1"));

        ObservabilityResult.ObservableIsland area = new ObservabilityResultImpl.ObservableIslandImpl(
                0, ObservabilityResult.IslandStatus.OBSERVABLE, busIds);
        busIds.clear();

        assertEquals(Set.of("BUS1"), area.getBusIds());
    }
}
