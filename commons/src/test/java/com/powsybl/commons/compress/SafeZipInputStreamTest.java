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
        safeZipInputStream = new SafeZipInputStream(zipInputStream, 1, 100);
        assertNotNull(safeZipInputStream);
    }

    @Test
    void testGetNextEntry() throws IOException {
        when(zipInputStream.getNextEntry()).thenReturn(new ZipEntry("entry1"), new ZipEntry("entry2"), null);
        safeZipInputStream = new SafeZipInputStream(zipInputStream, 3, 100);
        ZipEntry entry1 = safeZipInputStream.getNextEntry();
        assertNotNull(entry1);
        assertEquals("entry1", entry1.getName());
        ZipEntry entry2 = safeZipInputStream.getNextEntry();
        assertNotNull(entry2);
        assertEquals("entry2", entry2.getName());
        ZipEntry entry3 = safeZipInputStream.getNextEntry();
        assertNull(entry3);
    }

    @Test
    void testExceedMaxEntries() throws IOException {
        when(zipInputStream.getNextEntry()).thenReturn(new ZipEntry("entry1"), new ZipEntry("entry2"), null);
        safeZipInputStream = new SafeZipInputStream(zipInputStream, 1, 100);
        ZipEntry entry1 = safeZipInputStream.getNextEntry();
        assertNotNull(entry1);
        assertEquals("entry1", entry1.getName());
        IOException exception = assertThrows(IOException.class, () -> {
            safeZipInputStream.getNextEntry();
        });
        assertEquals("Max entries to read exceeded", exception.getMessage());
    }

    @Test
    void testReadExceedsMaxBytes() throws IOException {
        when(zipInputStream.read()).thenReturn(1, 1, 1, -1);

        safeZipInputStream = new SafeZipInputStream(zipInputStream, 1, 2);
        safeZipInputStream.getNextEntry();
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

        safeZipInputStream = new SafeZipInputStream(zipInputStream, 1, 5);
        safeZipInputStream.getNextEntry();

        IOException exception = assertThrows(IOException.class, () -> {
            safeZipInputStream.read(buffer, 0, 10);
        });
        assertEquals("Max bytes to read exceeded", exception.getMessage());
    }
}
