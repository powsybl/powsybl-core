/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 */
class CompressedDoubleDataChunkTest {

    @Test
    void testConsistence() {
        try {
            new CompressedDoubleDataChunk(0, 5, new double[]{3.0, 4.0}, new int[]{1, 1});
            fail();
        } catch (IllegalArgumentException e) {
            // do nothing
        }
    }
}
