/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import org.junit.jupiter.api.Test;
import org.threeten.extra.Interval;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class StringTimeSeriesLostColumnsIssueTest {

    @Test
    void test() throws IOException {
        String csv = String.join(System.lineSeparator(),
                "Time;Version;TITLE1;TITLE2",
                "2016-01-01T01:00:00Z;1;VALUE;VALUE",
                "2016-01-01T02:00:00Z;1;VALUE;VALUE") + System.lineSeparator();

        Map<Integer, List<TimeSeries>> tsMap = TimeSeries.parseCsv(csv);

        TimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2016-01-01T01:00:00Z/2016-01-01T02:00:00Z"), Duration.ofHours(1));
        TimeSeriesTable table = new TimeSeriesTable(1, 1, index);
        table.load(1, tsMap.get(1));
        String csv2 = table.toCsvString(new TimeSeriesCsvConfig(ZoneId.of("UTC")));

        assertEquals(csv, csv2);
    }
}
