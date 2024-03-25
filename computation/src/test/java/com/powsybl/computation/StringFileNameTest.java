/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class StringFileNameTest {

    @Test
    void test() {
        StringFileName fileName = new StringFileName("file");
        assertEquals("file", fileName.getName(0));
        assertFalse(fileName.dependsOnExecutionNumber());
    }

    @Test
    void test2() {
        StringFileName fileName = new StringFileName("file-" + CommandConstants.EXECUTION_NUMBER_PATTERN);
        assertEquals("file-0", fileName.getName(0));
        assertEquals("file-*", fileName.getName(-1));
        assertTrue(fileName.dependsOnExecutionNumber());
    }

}
