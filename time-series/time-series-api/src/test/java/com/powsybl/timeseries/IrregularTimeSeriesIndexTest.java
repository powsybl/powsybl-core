/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import com.powsybl.commons.json.JsonUtil;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class IrregularTimeSeriesIndexTest {

    @Test
    void test() {
        List<Instant> instants = Arrays.asList(Instant.parse("2015-01-01T00:00:00Z"),
                                               Instant.parse("2015-01-01T01:00:00Z"));
        IrregularTimeSeriesIndex index = IrregularTimeSeriesIndex.create(instants);
        assertEquals(IrregularTimeSeriesIndex.TYPE, index.getType());

        // test getters
        assertEquals("2015-01-01T00:00:00Z", index.getInstantAt(0).toString());
        assertEquals("2015-01-01T01:00:00Z", index.getInstantAt(1).toString());
        assertEquals(2, index.getPointCount());

        // test iterator and stream
        assertEquals(instants, index.stream().toList());
        assertEquals(instants, Lists.newArrayList(index.iterator()));

        // test to string
        assertEquals("IrregularTimeSeriesIndex(times=[2015-01-01T00:00:00Z, 2015-01-01T01:00:00Z])",
                     index.toString());

        // test json
        String jsonRefMillis = "[ 1420070400000, 1420074000000 ]";
        String jsonRef = "[ 1420070400000000000, 1420074000000000000 ]";
        String jsonMillis = index.toJson();
        String json = index.toJson(TimeSeries.TimeFormat.NANO);
        assertEquals(jsonRefMillis, jsonMillis);
        assertEquals(jsonRef, json);
        IrregularTimeSeriesIndex index2 = JsonUtil.parseJson(json, jsonParser -> IrregularTimeSeriesIndex.parseJson(jsonParser, TimeSeries.TimeFormat.NANO));
        assertNotNull(index2);
        assertEquals(index, index2);
        IrregularTimeSeriesIndex index3 = JsonUtil.parseJson(jsonMillis, IrregularTimeSeriesIndex::parseJson);
        assertNotNull(index3);
        assertEquals(index, index3);
    }

    @Test
    void testEquals() {
        new EqualsTester()
                .addEqualityGroup(IrregularTimeSeriesIndex.create(Instant.parse("2015-01-01T00:00:00Z")),
                                  IrregularTimeSeriesIndex.create(Instant.parse("2015-01-01T00:00:00Z")))
                .addEqualityGroup(IrregularTimeSeriesIndex.create(Instant.parse("2015-01-01T01:00:00Z")),
                                  IrregularTimeSeriesIndex.create(Instant.parse("2015-01-01T01:00:00Z")))
                .testEquals();
    }

    @Test
    void testContructorError() {
        assertThrows(IllegalArgumentException.class, IrregularTimeSeriesIndex::create);
        long[] duplicates = {0L, 1L, 1L};
        assertThrows(IllegalArgumentException.class, () -> new IrregularTimeSeriesIndex(duplicates));
        long[] unordered = {0L, 2L, 1L};
        assertThrows(IllegalArgumentException.class, () -> new IrregularTimeSeriesIndex(unordered));
    }
}
