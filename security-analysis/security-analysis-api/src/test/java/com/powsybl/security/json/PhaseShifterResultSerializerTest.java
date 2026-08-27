/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.security.results.MovedPhaseShifterResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PhaseShifterResultSerializerTest {

    @Test
    void testWriteSortedByTransformerId() throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(writer);
        generator.writeStartObject();
        PhaseShifterResultSerializer.write(Map.of(
            "B", new MovedPhaseShifterResult("B", 1, 2),
            "A", new MovedPhaseShifterResult("A", 3, 4)), generator);
        generator.writeEndObject();
        generator.close();

        String result = writer.toString();
        assertTrue(result.contains("\"phaseShifterResults\""));
        assertTrue(result.indexOf("A") < result.indexOf("B"));
    }

    @Test
    void testWriteEmptyMap() throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(writer);
        generator.writeStartObject();
        PhaseShifterResultSerializer.write(Map.of(), generator);
        generator.writeEndObject();
        generator.close();

        assertEquals("{}", writer.toString());
    }

    @Test
    void testRecordValidation() {
        assertNotNull(new MovedPhaseShifterResult("T1", 2, 4).transformerId());
        assertThrows(NullPointerException.class, () -> new MovedPhaseShifterResult(null, 0, 1));
    }

    @Test
    void testWriteSingleEntry() throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(writer);
        generator.writeStartObject();
        PhaseShifterResultSerializer.write(
                Map.of("T1", new MovedPhaseShifterResult("T1", 0, 2)), generator);
        generator.writeEndObject();
        generator.close();

        String result = writer.toString();
        assertTrue(result.contains("\"transformerId\":\"T1\""));
        assertTrue(result.contains("\"initialTap\":0"));
        assertTrue(result.contains("\"newTap\":2"));
    }
}
