/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import java.time.Instant;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DoublePoint extends AbstractPoint {

    private final double value;

    public DoublePoint(int index, long time, double value) {
        super(index, time);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, time, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DoublePoint) {
            DoublePoint other = (DoublePoint) obj;
            return index == other.index && time == other.time
                    && ((Double.isNaN(value) && Double.isNaN(other.value)) || value == other.value);
        }
        return false;
    }

    @Override
    public String toString() {
        return "DoublePoint(index=" + index + ", time=" + Instant.ofEpochMilli(time) + ", value=" + value + ")";
    }
}
