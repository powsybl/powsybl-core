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
import com.fasterxml.jackson.databind.JsonNode;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.results.ChangedPhaseTapChanger;
import com.powsybl.security.results.NetworkResult;
import com.powsybl.security.results.PreContingencyResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChangedPhaseTapChangerSerializerUtilTest {

    @Test
    void testWriteSortedByTransformerId() throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(writer);
        generator.writeStartObject();
        ChangedPhaseTapChangerSerializerUtil.write(List.of(
            new ChangedPhaseTapChanger("B", ThreeSides.TWO, 1, 2),
            new ChangedPhaseTapChanger("A", ThreeSides.ONE, 3, 4)), generator);
        generator.writeEndObject();
        generator.close();

        String result = writer.toString();
        assertTrue(result.contains("\"changedPhaseShifters\""));
        assertTrue(result.indexOf("A") < result.indexOf("B"));
    }

    @Test
    void testWriteEmptyMap() throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(writer);
        generator.writeStartObject();
        ChangedPhaseTapChangerSerializerUtil.write(List.of(), generator);
        generator.writeEndObject();
        generator.close();

        assertEquals("{}", writer.toString());
    }

    @Test
    void testRecordValidation() {
        assertNotNull(new ChangedPhaseTapChanger("T1", ThreeSides.ONE, 2, 4).transformerId());
        assertThrows(NullPointerException.class, () -> new ChangedPhaseTapChanger(null, ThreeSides.ONE, 0, 1));
    }

    @Test
    void testWriteSingleEntry() throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(writer);
        generator.writeStartObject();
        ChangedPhaseTapChangerSerializerUtil.write(
                List.of(new ChangedPhaseTapChanger("T1", ThreeSides.THREE, 0, 2)), generator);
        generator.writeEndObject();
        generator.close();

        String result = writer.toString();
        assertTrue(result.contains("\"transformerId\":\"T1\""));
        assertTrue(result.contains("\"side\":\"THREE\""));
        assertTrue(result.contains("\"initialTap\":0"));
        assertTrue(result.contains("\"finalTap\":2"));
    }

    @Test
    void testNullSideSerializationAndDeserialization() throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(writer);
        generator.writeStartObject();
        ChangedPhaseTapChangerSerializerUtil.write(
                List.of(new ChangedPhaseTapChanger("T1", null, 0, 2)), generator);
        generator.writeEndObject();
        generator.close();

        JsonNode json = JsonUtil.createObjectMapper().readTree(writer.toString());
        JsonNode changedPhaseShifter = json.path("changedPhaseShifters").get(0);
        assertFalse(changedPhaseShifter.has("side"));

        ChangedPhaseTapChanger deserialized = JsonUtil.createObjectMapper()
                .readValue(changedPhaseShifter.toString(), ChangedPhaseTapChanger.class);
        assertEquals(new ChangedPhaseTapChanger("T1", null, 0, 2), deserialized);
    }

    @Test
    void testGetChangedPhaseShifterBothGetters() {
        var pre = new PreContingencyResult(
            LoadFlowResult.ComponentResult.Status.CONVERGED,
            null,
            new NetworkResult(Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
            0.0,
            List.of(
                new ChangedPhaseTapChanger("T1", null, 0, 2),
                new ChangedPhaseTapChanger("T2", ThreeSides.ONE, 1, 3))
        );
        assertEquals(new ChangedPhaseTapChanger("T1", null, 0, 2), pre.getChangedPhaseShifter("T1"));
        assertEquals(new ChangedPhaseTapChanger("T2", ThreeSides.ONE, 1, 3), pre.getChangedPhaseShifter("T2", ThreeSides.ONE));
    }

}
