/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.google.common.testing.EqualsTester;
import com.powsybl.commons.json.JsonUtil;
import org.junit.Test;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class IrregularTimeSeriesIndexTest {

    @Test
    public void test() {
        IrregularTimeSeriesIndex index = IrregularTimeSeriesIndex.create(Instant.parse("2015-01-01T00:00:00Z"),
                                                                         Instant.parse("2015-01-01T01:00:00Z"));
        assertEquals(IrregularTimeSeriesIndex.TYPE, index.getType());

        // test getters
        assertEquals("2015-01-01T00:00:00Z", Instant.ofEpochMilli(index.getTimeAt(0)).toString());
        assertEquals("2015-01-01T01:00:00Z", Instant.ofEpochMilli(index.getTimeAt(1)).toString());
        assertEquals(2, index.getPointCount());

        // test to string
        assertEquals("IrregularTimeSeriesIndex(times=[2015-01-01T00:00:00Z, 2015-01-01T01:00:00Z])",
                     index.toString());

        // test json
        String jsonRef = "[ 1420070400000, 1420074000000 ]";
        String json = JsonUtil.toJson(index::writeJson);
        assertEquals(jsonRef, json);
        IrregularTimeSeriesIndex index2 = JsonUtil.parseJson(json, IrregularTimeSeriesIndex::parseJson);
        assertNotNull(index2);
        assertEquals(index, index2);
    }

    @Test
    public void testEquals() {
        new EqualsTester()
                .addEqualityGroup(IrregularTimeSeriesIndex.create(Instant.parse("2015-01-01T00:00:00Z")),
                                  IrregularTimeSeriesIndex.create(Instant.parse("2015-01-01T00:00:00Z")))
                .addEqualityGroup(IrregularTimeSeriesIndex.create(Instant.parse("2015-01-01T01:00:00Z")),
                                  IrregularTimeSeriesIndex.create(Instant.parse("2015-01-01T01:00:00Z")))
                .testEquals();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testContructorError() {
        IrregularTimeSeriesIndex.create();
    }
}
