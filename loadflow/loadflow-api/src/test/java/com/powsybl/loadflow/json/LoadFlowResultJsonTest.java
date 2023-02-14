/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.json;

import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.loadflow.LoadFlowResultImpl;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 */
class LoadFlowResultJsonTest extends AbstractConverterTest {

    private static Map<String, String> createMetrics() {
        return ImmutableMap.<String, String>builder().put("nbiter", "4")
                .put("dureeCalcul", "0.02")
                .put("cause", "0")
                .put("contraintes", "0")
                .put("statut", "OK")
                .put("csprMarcheForcee", "0")
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

    @Test
    void roundTripVersion13Test() throws IOException {
        roundTripTest(createVersion13(), LoadFlowResultSerializer::write, LoadFlowResultDeserializer::read, "/LoadFlowResultVersion13.json");
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
    void readJsonVersion12Exception() throws IOException {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultVersion12Exception.json")));
        assertTrue(e.getMessage().contains("Connected component number field not found."));
    }

    @Test
    void readJsonVersion12Exception2() throws IOException {
        PowsyblException e = assertThrows(PowsyblException.class, () -> LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultVersion12Exception2.json")));
        assertTrue(e.getMessage().contains("com.powsybl.loadflow.json.LoadFlowResultDeserializer. componentNum is not valid for version 1.2. Version should be < 1.2 "));
    }

    @Test
    void handleErrorTest() throws IOException {
        try (var is = getClass().getResourceAsStream("/LoadFlowResultVersion10Error.json")) {
            AssertionError e = assertThrows(AssertionError.class, () -> LoadFlowResultDeserializer.read(is));
            assertEquals("Unexpected field: alienAttribute", e.getMessage());
        }
    }
}
