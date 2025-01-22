/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import java.time.Instant;
import java.util.Objects;

import static com.powsybl.timeseries.TimeSeriesIndex.longToInstant;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class StringPoint extends AbstractPoint {

    private final String value;

    /**
     * @deprecated Replaced by {@link #StringPoint(int, Instant, String)}
     */
    @Deprecated(since = "6.7.0")
    public StringPoint(int index, long time, String value) {
        this(index, longToInstant(time, 1_000L), value);
    }

    public StringPoint(int index, Instant instant, String value) {
        super(index, instant);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, instant, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StringPoint other) {
            return index == other.index && instant.equals(other.instant) && Objects.equals(value, other.value);
        }
        return false;
    }

    @Override
    public String toString() {
        return "StringPoint(index=" + index + ", instant=" + instant + ", value=" + value + ")";
    }
}
