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
}
