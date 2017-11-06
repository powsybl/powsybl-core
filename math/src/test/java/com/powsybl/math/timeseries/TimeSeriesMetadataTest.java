/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.timeseries;

import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.json.JsonUtil;
import org.junit.Test;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.time.Duration;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TimeSeriesMetadataTest {

    @Test
    public void test() throws IOException {
        RegularTimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-01-01T01:00:00Z"),
                                                                     Duration.ofMinutes(15), 1, 1);
        ImmutableMap<String, String> tags = ImmutableMap.of("var1", "value1");
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, tags, index);

        // test  getters
        assertEquals("ts1", metadata.getName());
        assertEquals(TimeSeriesDataType.DOUBLE, metadata.getDataType());
        assertEquals(tags, metadata.getTags());
        assertSame(index, metadata.getIndex());

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
                "    \"spacing\" : 900000,",
                "    \"firstVersion\" : 1,",
                "    \"versionCount\" : 1",
                "  }",
                "}");
        String json = JsonUtil.toJson(metadata::writeJson);
        assertEquals(jsonRef, json);
        TimeSeriesMetadata metadata2 = JsonUtil.parseJson(json, TimeSeriesMetadata::parseJson);
        assertTrue(metadata2 instanceof TimeSeriesMetadata);
        assertEquals(metadata, metadata2);
    }
}
