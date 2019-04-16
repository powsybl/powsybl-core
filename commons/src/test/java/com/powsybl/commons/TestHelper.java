/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class TestHelper {

    public static void assertInvalidInvocation(ShouldThrowExceptionRunnable r, String expectedErrMsg) {
        try {
            r.run();
            fail();
        } catch (Exception e) {
            assertEquals(expectedErrMsg, e.getMessage());
        }
    }

    private TestHelper() {
    }
}
