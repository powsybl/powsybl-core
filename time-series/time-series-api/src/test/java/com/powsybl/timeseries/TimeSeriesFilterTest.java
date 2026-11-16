/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TimeSeriesFilterTest {

    @Test
    void test() {
        TimeSeriesFilter filter = new TimeSeriesFilter();
        assertTrue(filter.isIncludeDependencies());
        filter.setIncludeDependencies(false);
        assertFalse(filter.isIncludeDependencies());
    }
}
