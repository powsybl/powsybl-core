/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import com.powsybl.commons.PowsyblException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class FunctionFileNameTest {

    @Test
    void test() {
        FunctionFileName fileName = new FunctionFileName(executionNumber -> "file-" + executionNumber, null);
        assertEquals("file-0", fileName.getName(0));
        assertTrue(fileName.dependsOnExecutionNumber());
    }

    @Test
    void checkTest() {
        FunctionFileName fileName = new FunctionFileName(executionNumber -> "file-" + executionNumber,
            s -> {
                throw new PowsyblException("error");
            }
        );
        assertThrows(RuntimeException.class, () -> fileName.getName(0));
    }
}
