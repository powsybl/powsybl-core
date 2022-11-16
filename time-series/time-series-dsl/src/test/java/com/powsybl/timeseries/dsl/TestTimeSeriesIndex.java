/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries.dsl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.timeseries.AbstractTimeSeriesIndex;

import java.time.Instant;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TestTimeSeriesIndex extends AbstractTimeSeriesIndex {

    private int pointCount;

    private long startTime;

    public TestTimeSeriesIndex(long startTime, int pointCount) {
        this.startTime = startTime;
        this.pointCount = pointCount;
    }

    @Override
    public int getPointCount() {
        return pointCount;
    }

    @Override
    public long getTimeAt(int point) {
        return startTime + point;
    }

    @Override
    public String getType() {
        return "testIndex";
    }

    @Override
    public void writeJson(JsonGenerator generator) {
        throw new AssertionError();
    }

    @Override
    public Iterator<Instant> iterator() {
        throw new AssertionError();
    }

    @Override
    public Stream<Instant> stream() {
        throw new AssertionError();
    }
}
