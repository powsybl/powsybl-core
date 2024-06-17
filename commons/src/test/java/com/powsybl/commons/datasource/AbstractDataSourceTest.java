/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
abstract class AbstractDataSourceTest {
    protected FileSystem fileSystem;
    protected Path testDir;
    protected boolean throwExceptionOnConsistency = false;

    protected boolean appendTest() {
        return true;
    }

    void writeThenReadTest(DataSource dataSource) throws IOException {
        writeThenReadTest(dataSource, null, "bar");
        writeThenReadTest(dataSource, "_baz", "bar");
        writeThenReadTest(dataSource, "_baz", null);
    }

    private void writeThenReadTest(DataSource dataSource, String suffix, String ext) throws IOException {
        // check file does not exist
        assertFalse(dataSource.exists(suffix, ext));

        // write file
        try (OutputStream os = dataSource.newOutputStream(suffix, ext, false)) {
            os.write("line1".getBytes(StandardCharsets.UTF_8));
        }
        if (appendTest()) {
            // write file in append mode
            try (OutputStream os = dataSource.newOutputStream(suffix, ext, true)) {
                os.write((System.lineSeparator() + "line2").getBytes(StandardCharsets.UTF_8));
            }
        }

        // write another file
        try (OutputStream os = dataSource.newOutputStream("dummy.txt", false)) {
            os.write("otherline1".getBytes(StandardCharsets.UTF_8));
        }

        // check files exists
        assertTrue(dataSource.exists(suffix, ext));
        assertTrue(dataSource.exists("dummy.txt"));

        // check content exists and is ok
        try (InputStream is = dataSource.newInputStream(suffix, ext)) {
            assertEquals("line1" + (appendTest() ? System.lineSeparator() + "line2" : ""),
                new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        } catch (IOException x) {
            fail();
        }
        try (InputStream is = dataSource.newInputStream("dummy.txt")) {
            assertEquals("otherline1", new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        } catch (IOException x) {
            fail();
        }
        try (InputStream is = dataSource.newInputStream("dummy.txt", true)) {
            if (throwExceptionOnConsistency) {
                fail();
            } else {
                assertNull(is);
            }
        } catch (PowsyblException exception) {
            if (throwExceptionOnConsistency) {
                assertEquals("File dummy.txt is inconsistent with the ArchiveDataSource",
                    exception.getMessage());
            } else {
                fail();
            }
        }
    }
}
