/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class InputFileTest {

    @Test
    public void test() {
        InputFile file = new InputFile("test");
        assertEquals("test", file.getName(1));
        assertFalse(file.dependsOnExecutionNumber());
        assertNull(file.getPreProcessor());
    }

    @Test
    public void test2() {
        InputFile file = new InputFile("test" + CommandConstants.EXECUTION_NUMBER_PATTERN);
        assertEquals("test1", file.getName(1));
        assertTrue(file.dependsOnExecutionNumber());
        assertNull(file.getPreProcessor());
    }

    @Test
    public void test3() {
        InputFile file = new InputFile(integer -> "test" + integer, null);
        assertEquals("test1", file.getName(1));
        assertTrue(file.dependsOnExecutionNumber());
        assertNull(file.getPreProcessor());
    }

    @Test
    public void test4() {
        InputFile file = new InputFile("test.zip", FilePreProcessor.ARCHIVE_UNZIP);
        assertEquals(FilePreProcessor.ARCHIVE_UNZIP, file.getPreProcessor());
    }

    @Test(expected = RuntimeException.class)
    public void test5() {
        new InputFile("test", FilePreProcessor.ARCHIVE_UNZIP);
    }

    @Test
    public void test6() {
        InputFile file = new InputFile("test.gz", FilePreProcessor.FILE_GUNZIP);
        assertEquals(FilePreProcessor.FILE_GUNZIP, file.getPreProcessor());
    }

    @Test(expected = RuntimeException.class)
    public void test7() {
        new InputFile("test", FilePreProcessor.FILE_GUNZIP);
    }

    @Test(expected = RuntimeException.class)
    public void test8() {
        new InputFile("test" + File.separator + "test", null);
    }
}
