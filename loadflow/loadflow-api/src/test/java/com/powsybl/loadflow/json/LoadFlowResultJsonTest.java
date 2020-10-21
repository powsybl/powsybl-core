/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.loadflow.LoadFlowResultImpl;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 */
public class LoadFlowResultJsonTest extends AbstractConverterTest {

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

    private static LoadFlowResult createVersion11() {
        return new LoadFlowResultImpl(true, createMetrics(), "", Collections.singletonList(new LoadFlowResultImpl.ComponentResultImpl(0, LoadFlowResult.ComponentResult.Status.CONVERGED, 7, "bus1", 235.3)));
    }

    private static LoadFlowResult createVersion12() {
        return new LoadFlowResultImpl(true, createMetrics(), "logs", Collections.singletonList(new LoadFlowResultImpl.ComponentResultImpl(0, LoadFlowResult.ComponentResult.Status.CONVERGED, 7, "bus1", 235.3)));
    }

    @Test
    public void roundTripVersion12Test() throws IOException {
        roundTripTest(createVersion12(), LoadFlowResultSerializer::write, LoadFlowResultDeserializer::read, "/LoadFlowResultVersion12.json");
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
    public void readJsonVersion11() throws IOException {
        LoadFlowResult result = LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultVersion11.json"));
        assertTrue(result.isOk());
        assertEquals(createMetrics(), result.getMetrics());
        assertNull(result.getLogs());
        assertFalse(result.getComponentResults().isEmpty());
    }

    @Test
    public void handleErrorTest() throws IOException {
        expected.expect(AssertionError.class);
        expected.expectMessage("Unexpected field: alienAttribute");
        LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultVersion10Error.json"));
    }

    @Test
    public void handleErrorTest12() throws IOException {
        expected.expect(PowsyblException.class);
        expected.expectMessage("LoadFlowResult. Tag: logs is not valid for version 1.1. Version should be > 1.1");
        LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultVersion12Error.json"));
    }

    @Test
    public void loadFlowResultJsonModuleTest() throws IOException {
        ObjectMapper objectMapper = JsonUtil.createObjectMapper()
                .registerModule(new LoadFlowResultJsonModule());
        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        writer.writeValue(os, createVersion12());
        String result = new String(os.toByteArray(), StandardCharsets.UTF_8);
        compareTxt(getClass().getResourceAsStream("/LoadFlowResultVersion12.json"), result);
    }

}
