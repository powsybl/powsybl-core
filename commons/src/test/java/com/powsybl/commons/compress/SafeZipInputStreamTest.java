/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.compress;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SafeZipInputStreamTest {

    private ZipInputStream zipInputStream;
    private SafeZipInputStream safeZipInputStream;

    @BeforeEach
    void setUp() {
        zipInputStream = mock(ZipInputStream.class);
    }

    @Test
    void testConstructorWithValidEntryNumber() throws IOException {
        when(zipInputStream.getNextEntry()).thenReturn(new ZipEntry("entry1"), new ZipEntry("entry2"));
        safeZipInputStream = new SafeZipInputStream(zipInputStream, 100);
        assertNotNull(safeZipInputStream);
    }

    @Test
    void testGetNextEntry() throws IOException {
        when(zipInputStream.getNextEntry()).thenReturn(new ZipEntry("entry1"), new ZipEntry("entry2"), null);
        safeZipInputStream = new SafeZipInputStream(zipInputStream, 100);
        ZipEntry entry1 = safeZipInputStream.getNextEntry();
        assertEquals("entry1", entry1.getName());
        assertNotNull(entry1);
        ZipEntry entry2 = safeZipInputStream.getNextEntry();
        assertNotNull(entry2);
        assertEquals("entry2", entry2.getName());
        ZipEntry entry3 = safeZipInputStream.getNextEntry();
        assertNull(entry3);
    }

    @Test
    void testReadExceedsMaxBytes() throws IOException {
        when(zipInputStream.read()).thenReturn(1, 1, 1, -1);

        safeZipInputStream = new SafeZipInputStream(zipInputStream, 2);
        assertEquals(1, safeZipInputStream.read());
        assertEquals(1, safeZipInputStream.read());
        IOException exception = assertThrows(IOException.class, () -> {
            safeZipInputStream.read();
        });

        assertEquals("Max bytes to read exceeded", exception.getMessage());
    }

    @Test
    void testReadByteArrayExceedsMaxBytes() throws IOException {
        byte[] buffer = new byte[10];
        when(zipInputStream.read(buffer, 0, 10)).thenReturn(10);

        safeZipInputStream = new SafeZipInputStream(zipInputStream, 5);

        IOException exception = assertThrows(IOException.class, () -> {
            safeZipInputStream.read(buffer, 0, 10);
        });

        assertEquals("Max bytes to read exceeded", exception.getMessage());
    }
}
