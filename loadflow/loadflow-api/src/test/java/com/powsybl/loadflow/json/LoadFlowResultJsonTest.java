/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.json;

import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.loadflow.LoadFlowResultImpl;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.it>}
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class LoadFlowResultJsonTest extends AbstractSerDeTest {

    private static Map<String, String> createMetrics() {
        return ImmutableMap.<String, String>builder().put("nbiter", "4")
                .put("dureeCalcul", "0.02")
                .put("cause", "0")
                .put("contraintes", "0")
                .put("statut", "OK")
                .put("csprMarcheForcee", "0")
                .build();
    }

    private static Map<String, String> createComponentMetrics() {
        return ImmutableMap.<String, String>builder()
                .put("metric1", "aa")
                .put("metric2", "bb")
                .build();
    }

    private static LoadFlowResult createVersion10() {
        return new LoadFlowResultImpl(true, createMetrics(), "");
    }

    private static LoadFlowResult createVersion12() {
        return new LoadFlowResultImpl(true, createMetrics(), "", Collections.singletonList(new LoadFlowResultImpl.ComponentResultImpl(0, 0, LoadFlowResult.ComponentResult.Status.CONVERGED, 7, "bus1", 235.3, Double.NaN)));
    }

    private static LoadFlowResult createVersion13() {
        return new LoadFlowResultImpl(true, createMetrics(), "", Collections.singletonList(new LoadFlowResultImpl.ComponentResultImpl(0, 0, LoadFlowResult.ComponentResult.Status.CONVERGED, 7, "bus1", 235.3, 356.78)));
    }

    private static LoadFlowResult createVersion14() {
        return createVersion14(
                LoadFlowResult.ComponentResult.Status.CONVERGED,
                LoadFlowResult.ComponentResult.Status.CONVERGED,
                LoadFlowResult.ComponentResult.Status.NO_CALCULATION);
    }

    private static LoadFlowResult createVersion14(LoadFlowResult.ComponentResult.Status status1,
                                                  LoadFlowResult.ComponentResult.Status status2,
                                                  LoadFlowResult.ComponentResult.Status status3) {
        return new LoadFlowResultImpl(
                true,
                createMetrics(),
                "",
                List.of(new LoadFlowResultImpl.ComponentResultImpl(
                        0, 0,
                                status1, "",
                        createComponentMetrics(), 7, "bus0",
                        List.of(
                                new LoadFlowResultImpl.SlackBusResultImpl("bus1", 123.4),
                                new LoadFlowResultImpl.SlackBusResultImpl("bus2", 234.5)
                        ), 356.78),
                        new LoadFlowResultImpl.ComponentResultImpl(
                        0, 1,
                                status2, "",
                        createComponentMetrics(), 7, "busA",
                        List.of(
                                new LoadFlowResultImpl.SlackBusResultImpl("busB", 123.4)
                        ), 356.78),
                        new LoadFlowResultImpl.ComponentResultImpl(
                        1, 2,
                                status3, "",
                        createComponentMetrics(), 7, "busC",
                        List.of(
                                new LoadFlowResultImpl.SlackBusResultImpl("busC", 123.4)
                        ), 356.78)
                )
        );
    }

    @Test
    void roundTripVersion14Test() throws IOException {
        roundTripTest(createVersion14(), LoadFlowResultSerializer::write, LoadFlowResultDeserializer::read, "/LoadFlowResultVersion14.json");
    }

    @Test
    void testLoadFlowStatus() {
        var resultFc = createVersion14(
                LoadFlowResult.ComponentResult.Status.CONVERGED,
                LoadFlowResult.ComponentResult.Status.CONVERGED,
                LoadFlowResult.ComponentResult.Status.NO_CALCULATION);
        assertEquals(LoadFlowResult.Status.FULLY_CONVERGED, resultFc.getStatus());
        assertTrue(resultFc.isFullyConverged());
        assertFalse(resultFc.isPartiallyConverged());
        assertFalse(resultFc.isFailed());

        var resultPc1 = createVersion14(
                LoadFlowResult.ComponentResult.Status.CONVERGED,
                LoadFlowResult.ComponentResult.Status.MAX_ITERATION_REACHED,
                LoadFlowResult.ComponentResult.Status.NO_CALCULATION);
        assertEquals(LoadFlowResult.Status.PARTIALLY_CONVERGED, resultPc1.getStatus());
        assertFalse(resultPc1.isFullyConverged());
        assertTrue(resultPc1.isPartiallyConverged());
        assertFalse(resultPc1.isFailed());

        var resultPc2 = createVersion14(
                LoadFlowResult.ComponentResult.Status.CONVERGED,
                LoadFlowResult.ComponentResult.Status.FAILED,
                LoadFlowResult.ComponentResult.Status.NO_CALCULATION);
        assertEquals(LoadFlowResult.Status.PARTIALLY_CONVERGED, resultPc2.getStatus());
        assertFalse(resultPc2.isFullyConverged());
        assertTrue(resultPc2.isPartiallyConverged());
        assertFalse(resultPc2.isFailed());

        var resultF1 = createVersion14(
                LoadFlowResult.ComponentResult.Status.FAILED,
                LoadFlowResult.ComponentResult.Status.NO_CALCULATION,
                LoadFlowResult.ComponentResult.Status.FAILED);
        assertEquals(LoadFlowResult.Status.FAILED, resultF1.getStatus());
        assertFalse(resultF1.isFullyConverged());
        assertFalse(resultF1.isPartiallyConverged());
        assertTrue(resultF1.isFailed());

        var resultF2 = createVersion14(
                LoadFlowResult.ComponentResult.Status.NO_CALCULATION,
                LoadFlowResult.ComponentResult.Status.NO_CALCULATION,
                LoadFlowResult.ComponentResult.Status.NO_CALCULATION);
        assertEquals(LoadFlowResult.Status.FAILED, resultF2.getStatus());
        assertFalse(resultF2.isFullyConverged());
        assertFalse(resultF2.isPartiallyConverged());
        assertTrue(resultF2.isFailed());
    }

    @Test
    void readJsonVersion13() throws IOException {
        LoadFlowResult result = LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultVersion13.json"));
        LoadFlowResult.ComponentResult componentResult = result.getComponentResults().get(0);
        assertEquals("bus1", componentResult.getReferenceBusId());
        assertEquals("CONVERGED", componentResult.getStatusText());
        assertTrue(componentResult.getMetrics().isEmpty());
        assertEquals(1, componentResult.getSlackBusResults().size());
        LoadFlowResult.SlackBusResult slackBusResult = componentResult.getSlackBusResults().get(0);
        assertEquals("bus1", slackBusResult.getId());
        assertEquals(235.3, slackBusResult.getActivePowerMismatch());
    }

    @Test
    void readJsonVersion13SolverFailed() throws IOException {
        // LoadFlowResult.ComponentResult.Status.SOLVER_FAILED was removed in v1.4 and is now translated to FAILED
        // Info kept in statusText.
        LoadFlowResult result = LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultVersion13SolverFailed.json"));
        LoadFlowResult.ComponentResult componentResult = result.getComponentResults().get(0);
        assertEquals(LoadFlowResult.ComponentResult.Status.FAILED, componentResult.getStatus());
        assertEquals("SOLVER_FAILED", componentResult.getStatusText());
    }

    @Test
    void readJsonVersion12() throws IOException {
        LoadFlowResult result = LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultVersion12.json"));
        assertTrue(Double.isNaN(result.getComponentResults().get(0).getDistributedActivePower()));
    }

    @Test
    void readJsonVersion11() throws IOException {
        LoadFlowResult result11 = LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultVersion11.json"));
        assertTrue(result11.isOk());

        LoadFlowResult result12 = createVersion12();
        assertTrue(result12.isOk());

        LoadFlowResult.ComponentResult component11 = result11.getComponentResults().get(0);
        LoadFlowResult.ComponentResult component12 = result12.getComponentResults().get(0);

        assertEquals(component11.getConnectedComponentNum(), component12.getConnectedComponentNum());
        assertEquals(component11.getSynchronousComponentNum(), component12.getSynchronousComponentNum());
    }

    @Test
    void readJsonVersion10() throws IOException {
        LoadFlowResult result = LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultVersion10.json"));
        assertTrue(result.isOk());
        assertEquals(createMetrics(), result.getMetrics());
        assertNull(result.getLogs());
        assertTrue(result.getComponentResults().isEmpty());
    }

    @Test
    void readJsonVersion11Exception() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultVersion11Exception.json")));
        assertTrue(e.getMessage().contains("com.powsybl.loadflow.json.LoadFlowResultDeserializer. synchronousComponentNum is not valid for version 1.1. Version should be >= 1.2 "));
    }

    @Test
    void readJsonVersion12Exception() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultVersion12Exception.json")));
        assertTrue(e.getMessage().contains("Connected component number field not found."));
    }

    @Test
    void readJsonVersion12Exception2() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultVersion12Exception2.json")));
        assertTrue(e.getMessage().contains("com.powsybl.loadflow.json.LoadFlowResultDeserializer. componentNum is not valid for version 1.2. Version should be < 1.2 "));
    }

    @Test
    void version10apiBackwardCompatibility() {
        LoadFlowResult result = createVersion10();
        assertTrue(result.getComponentResults().isEmpty());
        assertTrue(result.isOk());
        assertEquals("", result.getLogs());
    }

    @Test
    void version13apiBackwardCompatibility() {
        LoadFlowResult result = createVersion13();
        LoadFlowResult.ComponentResult componentResult = result.getComponentResults().get(0);
        assertEquals("bus1", componentResult.getReferenceBusId());
        assertTrue(componentResult.getMetrics().isEmpty());
        assertEquals(1, componentResult.getSlackBusResults().size());
        LoadFlowResult.SlackBusResult slackBusResult = componentResult.getSlackBusResults().get(0);
        assertEquals("bus1", slackBusResult.getId());
        assertEquals(235.3, slackBusResult.getActivePowerMismatch());

    }

    @Test
    void version14UnsupportedIfMultipleSlackResults() {
        LoadFlowResult result = createVersion14();
        IllegalStateException ex1 = assertThrows(IllegalStateException.class, () -> result.getComponentResults().get(0).getSlackBusId());
        assertEquals("Deprecated method: cannot return a value in the case of multiple slack results. Please migrate to new API.", ex1.getMessage());
        IllegalStateException ex2 = assertThrows(IllegalStateException.class, () -> result.getComponentResults().get(0).getSlackBusActivePowerMismatch());
        assertEquals("Deprecated method: cannot return a value in the case of multiple slack results. Please migrate to new API.", ex2.getMessage());
    }

    @Test
    void handleErrorTest() throws IOException {
        try (var is = getClass().getResourceAsStream("/LoadFlowResultVersion10Error.json")) {
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> LoadFlowResultDeserializer.read(is));
            assertEquals("Unexpected field: alienAttribute", e.getMessage());
        }
    }
}
