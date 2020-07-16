/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PsseRawReaderTest {

    @Test
    public void ieee14BusTest() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/IEEE_14_bus.raw")))) {
            PsseRawModel rawData = new PsseRawReader().read(reader);
            assertNotNull(rawData);
            String jsonRef = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus.json")), StandardCharsets.UTF_8);
            String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(rawData);
            assertEquals(jsonRef, json);
        }
    }

    @Test
    public void minimalExampleRawxTest() throws IOException {
        String jsonFile = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/MinimalExample.rawx")), StandardCharsets.UTF_8);
        assertNotNull(jsonFile);
        PsseRawModel rawData = new PsseRawReader().readx(jsonFile);

        double tol = 0.000001;
        assertEquals("PSS(R)E MINIMUM RAWX CASE", rawData.getCaseIdentification().getTitle1());

        assertEquals(2, rawData.getBuses().size());
        assertEquals(101, rawData.getBuses().get(0).getI());
        assertEquals("Source", rawData.getBuses().get(0).getName());
        assertEquals(3, rawData.getBuses().get(0).getIde());
        assertEquals(102, rawData.getBuses().get(1).getI());
        assertEquals("Sink", rawData.getBuses().get(1).getName());
        assertEquals(1, rawData.getBuses().get(1).getIde());

        assertEquals(1, rawData.getLoads().size());
        assertEquals(102, rawData.getLoads().get(0).getI());
        assertEquals("1", rawData.getLoads().get(0).getId());
        assertEquals(500.0, rawData.getLoads().get(0).getPl(), tol);
        assertEquals(200.0, rawData.getLoads().get(0).getQl(), tol);

        assertEquals(1, rawData.getGenerators().size());
        assertEquals(101, rawData.getGenerators().get(0).getI());
        assertEquals("1", rawData.getGenerators().get(0).getId());

        assertEquals(1, rawData.getNonTransformerBranches().size());
        assertEquals(101, rawData.getNonTransformerBranches().get(0).getI());
        assertEquals(102, rawData.getNonTransformerBranches().get(0).getJ());
        assertEquals("1", rawData.getNonTransformerBranches().get(0).getCkt());
        assertEquals(0.01, rawData.getNonTransformerBranches().get(0).getX(), tol);
    }

    @Test
    public void ieee14BusRev35RawxTest() throws IOException {
        String jsonFile = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus_rev35.rawx")), StandardCharsets.UTF_8);
        assertNotNull(jsonFile);
        PsseRawModel rawData = new PsseRawReader().readx(jsonFile);

        String jsonRef = new String(ByteStreams.toByteArray(getClass().getResourceAsStream("/IEEE_14_bus_rev35.json")), StandardCharsets.UTF_8);
        String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(rawData);
        assertEquals(jsonRef, json);
    }
}
