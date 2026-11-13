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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
class BadDataResultImplTest {

    private static BadDataResult.SuspectMeasurement suspect(String id, double normalizedResidual) {
        return new BadDataResultImpl.SuspectMeasurementImpl(id, normalizedResidual);
    }

    @Test
    void listsTheSuspectsInTheOrderItWasGiven() {
        BadDataResultImpl result = new BadDataResultImpl(BadDataResult.Status.BAD_DATA_DETECTED, "two readings disagree",
                List.of(suspect("M1", 9.4), suspect("M2", -4.1)), Set.of("M3"));

        assertEquals(BadDataResult.Status.BAD_DATA_DETECTED, result.getStatus());
        assertEquals("two readings disagree", result.getStatusText());
        assertEquals(List.of("M1", "M2"),
                result.getSuspectMeasurements().stream().map(BadDataResult.SuspectMeasurement::getMeasurementId).toList());
        assertEquals(9.4, result.getSuspectMeasurements().get(0).getNormalizedResidual());
        assertEquals(-4.1, result.getSuspectMeasurements().get(1).getNormalizedResidual(),
                "the residual is signed, so a reading below the estimate is suspect too");
        assertEquals(Set.of("M3"), result.getUncheckableMeasurementIds());
    }

    @Test
    void anInconclusiveTestReadsAsEmptyRatherThanNull() {
        BadDataResultImpl result = new BadDataResultImpl(BadDataResult.Status.INCONCLUSIVE, null,
                List.of(), Set.of());

        assertEquals(BadDataResult.Status.INCONCLUSIVE, result.getStatus());
        assertEquals("", result.getStatusText());
        assertTrue(result.getSuspectMeasurements().isEmpty());
    }

    @Test
    void doesNotFollowALaterChangeToTheCollectionsItWasGiven() {
        List<BadDataResult.SuspectMeasurement> suspects = new ArrayList<>(List.of(suspect("M1", 9.4)));

        BadDataResultImpl result = new BadDataResultImpl(BadDataResult.Status.BAD_DATA_DETECTED, "",
                suspects, Set.of("M3"));
        suspects.clear();

        assertEquals(1, result.getSuspectMeasurements().size());
        assertThrows(UnsupportedOperationException.class, () -> result.getSuspectMeasurements().clear());
    }
}
