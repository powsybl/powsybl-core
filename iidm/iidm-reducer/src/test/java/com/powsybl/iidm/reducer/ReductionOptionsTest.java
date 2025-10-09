/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class ReductionOptionsTest {

    @Test
    void test() {
        ReductionOptions options = new ReductionOptions();
        assertFalse(options.isWithDanglingLines());

        options.withDanglingLlines(true);
        assertTrue(options.isWithDanglingLines());
    }
}
