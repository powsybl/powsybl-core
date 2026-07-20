/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
public record StringTimeSeriesValues(String[] values, int offset) {

    public String get(int globalIndex) {
        int localIndex = globalIndex - offset;
        if (localIndex < 0 || localIndex >= values.length) {
            return null;
        }
        return values[localIndex];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StringTimeSeriesValues that = (StringTimeSeriesValues) o;
        return offset == that.offset && Arrays.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(values), offset);
    }

    @Override
    @NonNull
    public String toString() {
        return "StringTimeSeriesValues{" +
                "values=" + Arrays.toString(values) +
                ", offset=" + offset +
                '}';
    }
}
