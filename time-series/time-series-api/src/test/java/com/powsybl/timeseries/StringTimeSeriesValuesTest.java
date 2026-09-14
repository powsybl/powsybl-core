/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
class StringTimeSeriesValuesTest {

    @Test
    void testGet() {
        StringTimeSeriesValues values = new StringTimeSeriesValues(new String[] {"A", "B", null, "D"}, 2);
        assertEquals("A", values.get(2));
        assertEquals("B", values.get(3));
        assertEquals(null, values.get(4));
        assertEquals("D", values.get(5));
        assertEquals(null, values.get(6));
        assertEquals(null, values.get(7));
    }

    @Test
    void testEqualsAndToString() {
        StringTimeSeriesValues timeSeriesValues1 = new StringTimeSeriesValues(new String[] {"A", "B"}, 3);
        StringTimeSeriesValues timeSeriesValues2 = new StringTimeSeriesValues(new String[] {"A", "B"}, 3);
        assertEquals(timeSeriesValues1, timeSeriesValues2);
        assertEquals(timeSeriesValues1.hashCode(), timeSeriesValues2.hashCode());
        assertEquals("StringTimeSeriesValues{values=[A, B], offset=3}", timeSeriesValues2.toString());
    }

}
