/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.results;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.contingency.Contingency;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyComputationStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Riad BENRADI {@literal <riad.benradi_externe@ at rte-france.com>}
 */
class PhaseShifterResultsExtensionTest {

    private static final PhaseShifterResultsExtension.MovedPhaseShifterResult PHASE_SHIFTER_RESULT_1 = new PhaseShifterResultsExtension.MovedPhaseShifterResult("PS1", 1, 3);

    private static final PhaseShifterResultsExtension.MovedPhaseShifterResult PHASE_SHIFTER_RESULT_2 = new PhaseShifterResultsExtension.MovedPhaseShifterResult("PS2", 2, 4);

    @Test
    void testConstructorAndGetters() {
        PhaseShifterResultsExtension extension = new PhaseShifterResultsExtension(Arrays.asList(PHASE_SHIFTER_RESULT_1, PHASE_SHIFTER_RESULT_2));

        assertNotNull(extension);
        assertEquals(2, extension.getPhaseShifterResults().size());
        assertEquals(PHASE_SHIFTER_RESULT_1, extension.getPhaseShifterResult("PS1"));
        assertEquals(PHASE_SHIFTER_RESULT_2, extension.getPhaseShifterResult("PS2"));
        assertEquals(PhaseShifterResultsExtension.NAME, extension.getName());
    }

    @Test
    void testDefaultConstructor() {
        PhaseShifterResultsExtension extension = new PhaseShifterResultsExtension();

        assertNotNull(extension);
        assertNotNull(extension.getPhaseShifterResults());
        assertTrue(extension.getPhaseShifterResults().isEmpty());
    }

    @Test
    void testMovedPhaseShifterResultGettersAndToString() {
        PhaseShifterResultsExtension.MovedPhaseShifterResult result = new PhaseShifterResultsExtension.MovedPhaseShifterResult("PS6", 11, 12);

        assertEquals("PS6", result.getTransformerId());
        assertEquals(11, result.getInitialTap());
        assertEquals(12, result.getNewTap());
        assertEquals("MovedPhaseShifterResult{transformerId='PS6', initialTap=11, newTap=12}", result.toString());
    }

    @Test
    void testNullPhaseShifterResultsThrowsException() {
        assertThrows(NullPointerException.class, () -> new PhaseShifterResultsExtension(null));
    }

    @Test
    void testNullTransformerIdThrowsException() {
        assertThrows(NullPointerException.class, () -> new PhaseShifterResultsExtension.MovedPhaseShifterResult(null, 1, 2));
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
        PhaseShifterResultsExtension.MovedPhaseShifterResult phaseShifterResult1 = new PhaseShifterResultsExtension.MovedPhaseShifterResult("PS3", 5, 6);
        PhaseShifterResultsExtension.MovedPhaseShifterResult phaseShifterResult2 = new PhaseShifterResultsExtension.MovedPhaseShifterResult("PS4", 7, 8);
        PhaseShifterResultsExtension extension = new PhaseShifterResultsExtension(Arrays.asList(phaseShifterResult1, phaseShifterResult2));

        // Add the extension to the PostContingencyResult
        result.addExtension(PhaseShifterResultsExtension.class, extension);

        // Retrieve and verify the extension
        PhaseShifterResultsExtension retrievedExtension = result.getExtension(PhaseShifterResultsExtension.class);
        assertNotNull(retrievedExtension);
        assertEquals(phaseShifterResult1, retrievedExtension.getPhaseShifterResult("PS3"));
        assertEquals(phaseShifterResult2, retrievedExtension.getPhaseShifterResult("PS4"));
        assertEquals(extension, retrievedExtension);

        // Verify that the extendable reference is set
        assertEquals(result, retrievedExtension.getExtendable());
    }

    @Test
    void testToString() {
        PhaseShifterResultsExtension.MovedPhaseShifterResult phaseShifterResult = new PhaseShifterResultsExtension.MovedPhaseShifterResult("PS5", 9, 10);
        PhaseShifterResultsExtension extension = new PhaseShifterResultsExtension(Collections.singletonList(phaseShifterResult));
        String expectedToString = "PhaseShifterResultsExtension{phaseShifterResults={PS5=MovedPhaseShifterResult{transformerId='PS5', initialTap=9, newTap=10}}}";
        assertEquals(expectedToString, extension.toString());
    }

    @Test
    void testPreContingencyResultSupportsExtensions() {
        PreContingencyResult result = new PreContingencyResult(
                LoadFlowResult.ComponentResult.Status.CONVERGED,
                new LimitViolationsResult(Collections.emptyList()),
                NetworkResult.empty(),
                0.0
        );
        AbstractExtension<PreContingencyResult> extension = new AbstractExtension<>() {
            @Override
            public String getName() {
                return "test";
            }
        };

        result.addExtension(AbstractExtension.class, extension);

        assertSame(extension, result.getExtension(AbstractExtension.class));
        assertTrue(result.getExtensions().contains(extension));
        assertSame(result, extension.getExtendable());
        assertTrue(result.removeExtension(AbstractExtension.class));
        assertNull(result.getExtension(AbstractExtension.class));
        assertNull(extension.getExtendable());
    }
}
