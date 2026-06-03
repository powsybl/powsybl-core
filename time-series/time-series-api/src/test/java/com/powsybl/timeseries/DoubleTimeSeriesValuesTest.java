/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import org.junit.jupiter.api.Test;

import static java.lang.Double.NaN;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
class DoubleTimeSeriesValuesTest {

    @Test
    void testGet() {
        DoubleTimeSeriesValues values = new DoubleTimeSeriesValues(new double[] {1d, 2d, NaN, 4d}, 2);
        assertEquals(1d, values.get(2), 0d);
        assertEquals(2d, values.get(3), 0d);
        assertEquals(NaN, values.get(4), 0d);
        assertEquals(4d, values.get(5), 0d);
        assertEquals(NaN, values.get(6), 0d);
        assertEquals(NaN, values.get(7), 0d);
    }

    @Test
    void testEqualsAndToString() {
        DoubleTimeSeriesValues timeSeriesValues1 = new DoubleTimeSeriesValues(new double[] {1d, 2d}, 3);
        DoubleTimeSeriesValues timeSeriesValues2 = new DoubleTimeSeriesValues(new double[] {1d, 2d}, 3);
        assertEquals(timeSeriesValues1, timeSeriesValues2);
        assertEquals(timeSeriesValues1.hashCode(), timeSeriesValues2.hashCode());
        assertEquals("DoubleTimeSeriesValues{values=[1.0, 2.0], offset=3}", timeSeriesValues2.toString());
    }

}
