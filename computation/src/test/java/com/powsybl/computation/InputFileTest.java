/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class InputFileTest {

    @Test
    void test() {
        InputFile file = new InputFile("test");
        assertEquals("test", file.getName(1));
        assertFalse(file.dependsOnExecutionNumber());
        assertNull(file.getPreProcessor());
    }

    @Test
    void test2() {
        InputFile file = new InputFile("test" + CommandConstants.EXECUTION_NUMBER_PATTERN);
        assertEquals("test1", file.getName(1));
        assertTrue(file.dependsOnExecutionNumber());
        assertNull(file.getPreProcessor());
    }

    @Test
    void test3() {
        InputFile file = new InputFile(integer -> "test" + integer, null);
        assertEquals("test1", file.getName(1));
        assertTrue(file.dependsOnExecutionNumber());
        assertNull(file.getPreProcessor());
    }

    @Test
    void test4() {
        InputFile file = new InputFile("test.zip", FilePreProcessor.ARCHIVE_UNZIP);
        assertEquals(FilePreProcessor.ARCHIVE_UNZIP, file.getPreProcessor());
    }

    void test5() {
        assertThrows(RuntimeException.class, () -> new InputFile("test", FilePreProcessor.ARCHIVE_UNZIP));
    }

    @Test
    void test6() {
        InputFile file = new InputFile("test.gz", FilePreProcessor.FILE_GUNZIP);
        assertEquals(FilePreProcessor.FILE_GUNZIP, file.getPreProcessor());
    }

    @Test
    void test7() {
        assertThrows(RuntimeException.class, () -> new InputFile("test", FilePreProcessor.FILE_GUNZIP));
    }

    @Test
    void test8() {
        assertThrows(RuntimeException.class, () -> new InputFile("test" + File.separator + "test", null));
    }
}
