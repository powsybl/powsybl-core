/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.results;

import com.powsybl.contingency.Contingency;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyComputationStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Riad BENRADI {@literal <riad.benradi_externe@ at rte-france.com>}
 */
class PhaseShifterResultsExtensionTest {

    @Test
    void testConstructorAndGetters() {
        List<String> movedIds = Arrays.asList("PS1", "PS2");
        PhaseShifterResultsExtension extension = new PhaseShifterResultsExtension(movedIds);

        assertNotNull(extension);
        assertEquals(movedIds, extension.getMovedPhaseShifterIds());
        assertEquals(PhaseShifterResultsExtension.NAME, extension.getName());
    }

    @Test
    void testDefaultConstructor() {
        PhaseShifterResultsExtension extension = new PhaseShifterResultsExtension();

        assertNotNull(extension);
        assertNotNull(extension.getMovedPhaseShifterIds());
        assertTrue(extension.getMovedPhaseShifterIds().isEmpty());
    }

    @Test
    void testNullMovedPhaseShifterIdsThrowsException() {
        assertThrows(NullPointerException.class, () -> new PhaseShifterResultsExtension(null));
    }

    @Test
    void testAddExtensionToPostContingencyResult() {
        // Create a dummy PostContingencyResult
        Contingency dummyContingency = Contingency.builder("dummy").build();
        PostContingencyResult result = new PostContingencyResult(
                dummyContingency,
                PostContingencyComputationStatus.CONVERGED,
                new LimitViolationsResult(Collections.emptyList()),
                NetworkResult.empty(),
                ConnectivityResult.empty(),
                0.0
        );

        // Create the extension
        List<String> movedIds = Arrays.asList("PS3", "PS4");
        PhaseShifterResultsExtension extension = new PhaseShifterResultsExtension(movedIds);

        // Add the extension to the PostContingencyResult
        result.addExtension(PhaseShifterResultsExtension.class, extension);

        // Retrieve and verify the extension
        PhaseShifterResultsExtension retrievedExtension = result.getExtension(PhaseShifterResultsExtension.class);
        assertNotNull(retrievedExtension);
        assertEquals(movedIds, retrievedExtension.getMovedPhaseShifterIds());
        assertEquals(extension, retrievedExtension);

        // Verify that the extendable reference is set
        assertEquals(result, retrievedExtension.getExtendable());
    }

    @Test
    void testToString() {
        List<String> movedIds = List.of("PS5");
        PhaseShifterResultsExtension extension = new PhaseShifterResultsExtension(movedIds);
        String expectedToString = "PhaseShifterResultsExtension{movedPhaseShifterIds=[PS5]}";
        assertEquals(expectedToString, extension.toString());
    }
}