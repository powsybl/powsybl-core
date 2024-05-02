/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
class DistanceCalculatorTest {

    @Test
    void test() {
        double zeroDistance = DistanceCalculator.distance(1, 0, 1, 0);
        assertEquals(0, zeroDistance);

        double nonZeroDistance = DistanceCalculator.distance(10, 10, 20, 20);
        assertEquals(1546488.0483491954, nonZeroDistance);
    }
}
