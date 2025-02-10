/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.fasterxml.jackson.core.JsonGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface TimeSeriesIndex extends Iterable<Instant> {

    int getPointCount();

    enum ExportFormat {
        MILLISECONDS,
        NANOSECONDS
    }

    /**
     * @deprecated Replaced by {@link TimeSeriesIndex#getInstantAt(int)}}
     */
    @Deprecated(since = "6.7.0")
    long getTimeAt(int point);

    Instant getInstantAt(int point);

    String getType();

    /**
     * Write the TimeSeriesIndex as JSON using {@code ExportFormat.MILLISECONDS} format
     */
    void writeJson(JsonGenerator generator);

    void writeJson(JsonGenerator generator, ExportFormat format);

    /**
     * Export the TimeSeriesIndex as JSON using {@code ExportFormat.MILLISECONDS} format
     */
    String toJson();

    String toJson(ExportFormat format);

    Stream<Instant> stream();

    Iterator<Instant> iterator();
}
