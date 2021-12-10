/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TimeSeriesFilterTest {

    @Test
    public void test() {
        TimeSeriesFilter filter = new TimeSeriesFilter();
        assertTrue(filter.isIncludeDependencies());
        filter.setIncludeDependencies(false);
        assertFalse(filter.isIncludeDependencies());
    }
}
