/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ReductionOptionsTest {

    @Test
    public void test() {
        ReductionOptions options = new ReductionOptions();
        assertFalse(options.isWithDanglingLines());

        options.withDanglingLlines(true);
        assertTrue(options.isWithDanglingLines());
    }
}
