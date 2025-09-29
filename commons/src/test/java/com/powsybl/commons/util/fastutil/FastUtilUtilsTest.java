/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.util.fastutil;

import org.junit.jupiter.api.Test;

import static com.powsybl.commons.util.fastutil.FastUtilUtils.grep;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class FastUtilUtilsTest {
    @Test
    void grepTest() {
        // Initialize
        ExtendedIntArrayList list = new ExtendedIntArrayList(new int[] {1, 2, 0, 4});

        // Get only some elements according to a predicate
        assertEquals(new ExtendedIntArrayList(new int[] {1, 0}), grep(list, (int i) -> i < 2));
    }
}
