/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class MultipleReadOnlyDataSourceTest {

    private FileSystem fileSystem;

    private Path testDir;

    @BeforeEach
    void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        testDir = fileSystem.getPath("/tmp");
        Files.createDirectories(testDir);
        Files.createFile(testDir.resolve("a.txt"));
        Files.createFile(testDir.resolve("b.txt"));
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    void test() throws IOException {
        ReadOnlyDataSource dataSource = new MultipleReadOnlyDataSource(DataSourceUtil.createDataSource(testDir, "a", ""),
                                                                       DataSourceUtil.createDataSource(testDir, "b", ""));
        assertEquals("a", dataSource.getBaseName());
        assertTrue(dataSource.exists(null, "txt"));
        assertFalse(dataSource.exists(null, "json"));
        assertTrue(dataSource.exists("a.txt"));
        assertTrue(dataSource.exists("b.txt"));
        assertFalse(dataSource.exists("c.txt"));
        assertEquals(Set.of("a.txt", "b.txt"), dataSource.listNames(".*"));
        try (var is = dataSource.newInputStream("a.txt")) {
            assertNotNull(is);
        }
        try (var is = dataSource.newInputStream("x.txt")) {
            assertNull(is);
        }
        try (var is = dataSource.newInputStream(null, "txt")) {
            assertNotNull(is);
        }
    }

    @Test
    void testEmpty() {
        PowsyblException e = assertThrows(PowsyblException.class, MultipleReadOnlyDataSource::new);
        assertEquals("Empty data source list", e.getMessage());
    }
}
