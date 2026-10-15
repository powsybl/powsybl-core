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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
class ObservabilityResultImplTest {

    @Test
    void oneConnectedComponentCanHoldSeveralObservableIslands() {
        ObservabilityResult.Area observable = new ObservabilityResultImpl.AreaImpl(
                0, ObservabilityResult.AreaStatus.OBSERVABLE, Set.of("BUS1", "BUS2"));
        ObservabilityResult.Area dark = new ObservabilityResultImpl.AreaImpl(
                1, ObservabilityResult.AreaStatus.NON_OBSERVABLE, Set.of("BUS3"));

        ObservabilityResultImpl result = new ObservabilityResultImpl(ObservabilityResult.Status.COMPLETED,
                "two islands", List.of(observable, dark), List.of());

        assertEquals(2, result.getAreas().size());
        assertEquals("two islands", result.getStatusText());
        assertEquals(ObservabilityResult.AreaStatus.OBSERVABLE, result.getArea("BUS1").orElseThrow().getStatus());
        assertEquals(1, result.getArea("BUS3").orElseThrow().getAreaNumber());
    }

    @Test
    void aBranchQualityNamesItsSideAndAnInjectionQualityDoesNot() {
        ObservabilityResult.Quality branchSide = new ObservabilityResultImpl.QualityImpl(
                ObservabilityResult.Quantity.REACTIVE_POWER, ThreeSides.TWO, 1.5, true);
        ObservabilityResult.Quality injection = new ObservabilityResultImpl.QualityImpl(
                ObservabilityResult.Quantity.VOLTAGE, null, 0.2, null);

        assertEquals(ThreeSides.TWO, branchSide.getSide().orElseThrow());
        assertEquals(ObservabilityResult.Quantity.REACTIVE_POWER, branchSide.getQuantity());
        assertEquals(1.5, branchSide.getStandardDeviation());

        assertTrue(injection.getSide().isEmpty());
        assertEquals(0.2, injection.getStandardDeviation());
    }

    @Test
    void anUnsetRedundancyIsEmptyAndACriticalQuantityIsFalse() {
        ObservabilityResult.Quality notEstablished = new ObservabilityResultImpl.QualityImpl(
                ObservabilityResult.Quantity.ACTIVE_POWER, null, 0.5, null);
        ObservabilityResult.Quality critical = new ObservabilityResultImpl.QualityImpl(
                ObservabilityResult.Quantity.ACTIVE_POWER, null, 0.5, false);

        assertTrue(notEstablished.isRedundant().isEmpty());
        assertFalse(critical.isRedundant().orElseThrow());
    }

    @Test
    void aFailedAnalysisReadsAsEmptyRatherThanNull() {
        ObservabilityResultImpl result = new ObservabilityResultImpl(
                ObservabilityResult.Status.FAILED, null, List.of(), List.of());

        assertEquals(ObservabilityResult.Status.FAILED, result.getStatus());
        assertEquals("", result.getStatusText());
        assertTrue(result.getAreas().isEmpty());
        assertTrue(result.getElementObservabilities().isEmpty());
    }

    @Test
    void doesNotFollowALaterChangeToTheSetItWasGiven() {
        Set<String> busIds = new HashSet<>(Set.of("BUS1"));

        ObservabilityResult.Area area = new ObservabilityResultImpl.AreaImpl(
                0, ObservabilityResult.AreaStatus.OBSERVABLE, busIds);
        busIds.clear();

        assertEquals(Set.of("BUS1"), area.getBusIds());
    }
}
