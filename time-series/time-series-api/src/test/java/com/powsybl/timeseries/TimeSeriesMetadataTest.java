/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.timeseries.json.TimeSeriesJsonModule;
import org.junit.jupiter.api.Test;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TimeSeriesMetadataTest {

    @Test
    void test() throws IOException {
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:00:00Z"),
                                                                     Duration.ofMinutes(15));
        ImmutableMap<String, String> tags = ImmutableMap.of("var1", "value1");
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, tags, index);

        // test  getters
        assertEquals("ts1", metadata.getName());
        assertEquals(TimeSeriesDataType.DOUBLE, metadata.getDataType());
        assertEquals(tags, metadata.getTags());
        assertSame(index, metadata.getIndex());

        // toString test
        assertEquals("TimeSeriesMetadata(name=ts1, dataType=DOUBLE, tags={var1=value1}, index=RegularTimeSeriesIndex(startInstant=2015-01-01T00:00:00Z, endInstant=2015-01-01T01:00:00Z, deltaT=PT15M))",
                     metadata.toString());

        // test json
        String jsonRef = String.join(System.lineSeparator(),
                "{",
                "  \"name\" : \"ts1\",",
                "  \"dataType\" : \"DOUBLE\",",
                "  \"tags\" : [ {",
                "    \"var1\" : \"value1\"",
                "  } ],",
                "  \"regularIndex\" : {",
                "    \"startTime\" : 1420070400000,",
                "    \"endTime\" : 1420074000000,",
                "    \"spacing\" : 900000",
                "  }",
                "}");
        String json = JsonUtil.toJson(metadata::writeJson);
        assertEquals(jsonRef, json);
        TimeSeriesMetadata metadata2 = JsonUtil.parseJson(json, TimeSeriesMetadata::parseJson);
        assertNotNull(metadata2);
        assertEquals(metadata, metadata2);

        // test json with object mapper
        ObjectMapper objectMapper = JsonUtil.createObjectMapper()
                .registerModule(new TimeSeriesJsonModule());

        assertEquals(metadata, objectMapper.readValue(objectMapper.writeValueAsString(metadata), TimeSeriesMetadata.class));

        // test with a list of metadata
        List<TimeSeriesMetadata> metadataList = objectMapper.readValue(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(Arrays.asList(metadata, metadata)),
                                                                       TypeFactory.defaultInstance().constructCollectionType(List.class, TimeSeriesMetadata.class));
        assertEquals(2, metadataList.size());
        assertEquals(metadata, metadataList.get(0));
        assertEquals(metadata, metadataList.get(1));
    }

    @Test
    void testIrregularIndex() throws IOException {
        TimeSeriesIndex index = IrregularTimeSeriesIndex.create(
            Instant.parse("2015-01-01T00:00:00Z"),
            Instant.parse("2015-01-02T00:00:00Z"),
            Instant.parse("2015-01-04T00:00:00Z"),
            Instant.parse("2015-01-10T00:00:00Z"));
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, Collections.emptyMap(), index);
        TimeSeriesMetadata metadata2 = JsonUtil.parseJson(JsonUtil.toJson(metadata::writeJson), TimeSeriesMetadata::parseJson);
        assertEquals(metadata, metadata2);
    }

    @Test
    void jsonErrorMetadataTests() {

        // Initialisation
        IllegalStateException e0;

        // Both parameters (left, right) are missing
        final String jsonRegularIndexError = String.join(System.lineSeparator(),
            "[ {",
            "  \"metadata\" : {",
            "    \"name\" : \"ts\",",
            "    \"dataType\" : \"DOUBLE\",",
            "    \"tags\" : [ ],",
            "    \"regularIndex\" : {",
            "      \"error\" : 1420070400000,",
            "      \"endTime\" : 1437350400000,",
            "      \"spacing\" : 17280000000",
            "    }",
            "  },",
            "  \"chunks\" : [ {",
            "    \"offset\" : 0,",
            "    \"values\" : [ 1.0, 2.0 ]",
            "  } ]",
            "}, {",
            "  \"name\" : \"ts_calc_min\",",
            "  \"expr\" : {",
            "    \"binaryMin\" : {",
            "    }",
            "  }",
            "} ]");
        e0 = assertThrows(IllegalStateException.class, () -> TimeSeries.parseJson(jsonRegularIndexError));
        assertEquals("Unexpected field error", e0.getMessage());

        // Both parameters (left, right) are missing
        final String jsonMetadataError = String.join(System.lineSeparator(),
            "[ {",
            "  \"metadata\" : {",
            "    \"name\" : \"ts\",",
            "    \"dataType\" : \"DOUBLE\",",
            "    \"tags\" : [ ],",
            "    \"error\" : {",
            "      \"error\" : 1420070400000,",
            "      \"endTime\" : 1437350400000,",
            "      \"spacing\" : 17280000000",
            "    }",
            "  },",
            "  \"chunks\" : [ {",
            "    \"offset\" : 0,",
            "    \"values\" : [ 1.0, 2.0 ]",
            "  } ]",
            "}, {",
            "  \"name\" : \"ts_calc_min\",",
            "  \"expr\" : {",
            "    \"binaryMin\" : {",
            "    }",
            "  }",
            "} ]");
        e0 = assertThrows(IllegalStateException.class, () -> TimeSeries.parseJson(jsonMetadataError));
        assertEquals("Unexpected field name error", e0.getMessage());

        // Both parameters (left, right) are missing
        final String jsontemp = String.join(System.lineSeparator(),
            "[ {",
            "  \"metadata\" : {",
            "    \"name\" : \"ts\",",
            "    \"dataType\" : \"DOUBLE\",",
            "    \"tags\" : [ ],",
            "    \"error\" : {",
            "      \"error\" : 1420070400000,",
            "      \"endTime\" : 1437350400000,",
            "      \"spacing\" : 17280000000",
            "    }",
            "  },",
            "  \"chunks\" : [ {",
            "    \"offset\" : 0,",
            "    \"values\" : [ 1.0, 2.0 ]",
            "  } ]",
            "}, {",
            "  \"name\" : \"ts_calc_min\",",
            "  \"expr\" : {",
            "    \"binaryMin\" : {",
            "    }",
            "  }",
            "} ]");
        e0 = assertThrows(IllegalStateException.class, () -> TimeSeries.parseJson(jsontemp));
        assertEquals("Unexpected field name error", e0.getMessage());
    }
}
