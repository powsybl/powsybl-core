/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io.mmap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MemoryMapFileMockTest {

    private MemoryMappedFile memoryMappedFile;

    @Before
    public void setUp() {
        memoryMappedFile = new MemoryMapFileMock();
    }

    @After
    public void tearDown() throws IOException {
        memoryMappedFile.close();
    }

    @Test
    public void exists() {
        assertFalse(memoryMappedFile.exists());
    }

    @Test
    public void getBuffer() throws IOException {
        ByteBuffer buffer = memoryMappedFile.getBuffer(100);
        assertNotNull(buffer);
        assertEquals(100, buffer.remaining());
    }

}
