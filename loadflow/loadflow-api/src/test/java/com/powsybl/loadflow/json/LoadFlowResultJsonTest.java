/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.json;

import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.PowsyblException;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.loadflow.LoadFlowResultImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 */
public class LoadFlowResultJsonTest extends AbstractConverterTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

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
        return new LoadFlowResultImpl(true, createMetrics(), "", Collections.singletonList(new LoadFlowResultImpl.ComponentResultImpl(0, 0, LoadFlowResult.ComponentResult.Status.CONVERGED, 7, "bus1", 235.3)));
    }

    @Test
    public void roundTripVersion12Test() throws IOException {
        roundTripTest(createVersion12(), LoadFlowResultSerializer::write, LoadFlowResultDeserializer::read, "/LoadFlowResultVersion12.json");
    }

    @Test
    public void readJsonVersion11() throws IOException {
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
    public void readJsonVersion10() throws IOException {
        LoadFlowResult result = LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultVersion10.json"));
        assertTrue(result.isOk());
        assertEquals(createMetrics(), result.getMetrics());
        assertNull(result.getLogs());
        assertTrue(result.getComponentResults().isEmpty());
    }

    @Test
    public void readJsonVersion11Exception() throws IOException {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("synchronousComponentNum field unexpected in version < 1.2");
        LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultVersion11Exception.json"));
    }

    @Test
    public void readJsonVersion12Exception() throws IOException {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Connected component number field not found.");
        LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultVersion12Exception.json"));
    }

    @Test
    public void handleErrorTest() throws IOException {
        expected.expect(AssertionError.class);
        expected.expectMessage("Unexpected field: alienAttribute");
        LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultVersion10Error.json"));
    }
}
