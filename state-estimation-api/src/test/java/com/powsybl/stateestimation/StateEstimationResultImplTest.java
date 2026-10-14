/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
class StateEstimationResultImplTest {

    private static StateEstimationResult.ComponentResult component() {
        return new StateEstimationResultImpl.ComponentResultImpl(
                StateEstimationResult.ComponentResult.Status.ESTIMATED, "converged", 4, "BUS", 12.5);
    }

    @Test
    void aFailedEstimationHoldsNothing() {
        StateEstimationResultImpl result = StateEstimationResultImpl.failed();

        assertEquals(StateEstimationResult.Status.FAILED, result.getStatus());
        assertTrue(result.getComponentResults().isEmpty());
        assertTrue(result.getBusResults().isEmpty());
        assertTrue(result.getMeasurementResults().isEmpty());
    }

    @Test
    void doesNotFollowALaterChangeToTheListsItWasGiven() {
        List<StateEstimationResult.ComponentResult> components = new ArrayList<>(List.of(component()));

        StateEstimationResultImpl result = new StateEstimationResultImpl(
                StateEstimationResult.Status.FULLY_ESTIMATED, components, List.of(), List.of());
        components.clear();

        assertEquals(1, result.getComponentResults().size());
        assertThrows(UnsupportedOperationException.class, () -> result.getComponentResults().clear());
    }

    @Test
    void aComponentReportsWhatTheEstimationDidToIt() {
        StateEstimationResult.ComponentResult result = component();

        assertEquals("converged", result.getStatusText());
        assertEquals("BUS", result.getReferenceBusId());
        assertEquals(12.5, result.getObjectiveFunctionValue());
    }

    @Test
    void anEstimatorInAnotherFamilyReportsNoObjectiveFunctionValue() {
        assertEquals(Double.NaN, new StateEstimationResultImpl.ComponentResultImpl(
                StateEstimationResult.ComponentResult.Status.ESTIMATED, null, 1, "BUS", Double.NaN)
                .getObjectiveFunctionValue());
    }

    @Test
    void aBusCarriesTheStateInTheUnitsIidmUses() {
        StateEstimationResult.BusResult result = new StateEstimationResultImpl.BusResultImpl("BUS", 398.5, -2.75);

        assertEquals("BUS", result.getBusId());
        assertEquals(398.5, result.getV());
        assertEquals(-2.75, result.getAngle());
    }

    @Test
    void aMeasurementDerivesItsResidualFromTheTwoValues() {
        StateEstimationResult.MeasurementResult above = new StateEstimationResultImpl.MeasurementResultImpl(
                "M1", 100.0, 99.5, 2.5, false);

        assertEquals("M1", above.getMeasurementId());
        assertEquals(100.0, above.getMeasuredValue());
        assertEquals(99.5, above.getEstimatedValue());
        assertEquals(0.5, above.getResidual(), 1e-12);
        assertEquals(2.5, above.getNormalizedResidual());
        assertFalse(above.isFlagged());

        StateEstimationResult.MeasurementResult below = new StateEstimationResultImpl.MeasurementResultImpl(
                "M2", 98.0, 100.0, -4.0, true);

        assertEquals(-2.0, below.getResidual(), 1e-12);
        assertTrue(below.isFlagged(), "flagging compares the absolute value, so a low reading can be flagged too");
    }
}
