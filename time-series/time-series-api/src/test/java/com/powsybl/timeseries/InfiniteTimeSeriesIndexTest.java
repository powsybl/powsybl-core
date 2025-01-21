/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.powsybl.commons.json.JsonUtil;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class InfiniteTimeSeriesIndexTest {

    @Test
    void testInfiniteIndex() {
        assertEquals("infiniteIndex", InfiniteTimeSeriesIndex.INSTANCE.getType());
        assertEquals(2, InfiniteTimeSeriesIndex.INSTANCE.getPointCount());
        assertEquals(-31557014167219200L, InfiniteTimeSeriesIndex.INSTANCE.getTimeAt(0));
        assertEquals(31556889864403199L, InfiniteTimeSeriesIndex.INSTANCE.getTimeAt(1));
        assertEquals(InfiniteTimeSeriesIndex.START_INSTANT, InfiniteTimeSeriesIndex.INSTANCE.getInstantAt(0));
        assertEquals(InfiniteTimeSeriesIndex.END_INSTANT, InfiniteTimeSeriesIndex.INSTANCE.getInstantAt(1));
        assertEquals("InfiniteTimeSeriesIndex()", InfiniteTimeSeriesIndex.INSTANCE.toString());
        TimeSeriesMetadata metadata = new TimeSeriesMetadata("ts1", TimeSeriesDataType.DOUBLE, Collections.emptyMap(), InfiniteTimeSeriesIndex.INSTANCE);
        TimeSeriesMetadata metadata2 = JsonUtil.parseJson(JsonUtil.toJson(metadata::writeJson), TimeSeriesMetadata::parseJson);
        assertEquals(metadata, metadata2);
    }
}
