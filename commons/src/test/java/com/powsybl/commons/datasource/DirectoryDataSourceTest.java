/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class DirectoryDataSourceTest extends AbstractFileSystemDataSourceTest {
    protected Set<String> unlistedFiles;
    private final Set<String> existingFiles = Set.of(
        "foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar", "bar.iidm", "bar",
        "foo.bz2", "foo.txt.bz2", "foo.iidm.bz2", "foo.xiidm.bz2", "foo.v3.iidm.bz2", "foo.v3.bz2", "foo_bar.iidm.bz2", "foo_bar.bz2", "bar.iidm.bz2", "bar.bz2",
        "foo.xz", "foo.txt.xz", "foo.iidm.xz", "foo.xiidm.xz", "foo.v3.iidm.xz", "foo.v3.xz", "foo_bar.iidm.xz", "foo_bar.xz", "bar.iidm.xz", "bar.xz",
        "foo.zst", "foo.txt.zst", "foo.iidm.zst", "foo.xiidm.zst", "foo.v3.iidm.zst", "foo.v3.zst", "foo_bar.iidm.zst", "foo_bar.zst", "bar.iidm.zst", "bar.zst",
        "foo.gz", "foo.txt.gz", "foo.iidm.gz", "foo.xiidm.gz", "foo.v3.iidm.gz", "foo.v3.gz", "foo_bar.iidm.gz", "foo_bar.gz", "bar.iidm.gz", "bar.gz"
    );

    @BeforeEach
    void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        testDir = fileSystem.getPath("/tmp");
        Files.createDirectories(testDir);

        // Create the test files
        existingFiles.forEach(fileName -> {
            try {
                Files.createFile(fileSystem.getPath(testDir + "/" + fileName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    private static Stream<Arguments> provideArgumentsForWriteThenReadTest() {
        return Stream.of(
            Arguments.of("foo", ".iidm", null, null),
            Arguments.of("foo", "", null, null),
            Arguments.of("foo", ".v3", null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForWriteThenReadTest")
    void writeThenReadTest(String baseName, String sourceFormat, ArchiveFormat archiveFormat,
                                   CompressionFormat compressionFormat) throws IOException {
        // Compute the full filename
        String fileName = testDir + "/" + baseName + sourceFormat
            + (archiveFormat == null ? "" : "." + archiveFormat.getExtension())
            + (compressionFormat == null ? "" : "." + compressionFormat.getExtension());

        // Create the datasource
        DataSource dataSource = DataSource.fromPath(fileSystem.getPath(fileName));

        writeThenReadTest(dataSource);
    }

    static Stream<Arguments> provideArgumentsForClassAndListingTest() {
        return Stream.of(
            Arguments.of("foo", ".iidm", null, null, DirectoryDataSource.class,
                Set.of("foo.iidm", "foo_bar.iidm", "foo.v3.iidm"),
                Set.of("foo_bar.iidm")),
            Arguments.of("foo", "", null, null, DirectoryDataSource.class,
                Set.of("foo.txt", "foo.iidm", "foo.xiidm", "foo", "foo_bar.iidm", "foo.v3.iidm", "foo.v3", "foo_bar"),
                Set.of("foo_bar.iidm", "foo_bar")),
            Arguments.of("foo", ".v3", null, null, DirectoryDataSource.class,
                Set.of("foo.v3"),
                Set.of())
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForClassAndListingTest")
    void testClassAndListing(String baseName, String sourceFormat, ArchiveFormat archiveFormat,
                  CompressionFormat compressionFormat, Class<? extends AbstractFileSystemDataSource> dataSourceClass,
                  Set<String> listedFiles, Set<String> listedBarFiles) throws IOException {
        // Compute the full filename
        String fileName = testDir + "/" + baseName + sourceFormat
            + (archiveFormat == null ? "" : "." + archiveFormat.getExtension())
            + (compressionFormat == null ? "" : "." + compressionFormat.getExtension());

        // Update the list of unlisted files
        unlistedFiles = existingFiles.stream().filter(name -> !listedFiles.contains(name)).collect(Collectors.toSet());

        // Create the datasource
        DataSource dataSource = DataSource.fromPath(fileSystem.getPath(fileName));

        // Check the class
        assertInstanceOf(dataSourceClass, dataSource);

        // List all the files in the datasource
        assertEquals(listedFiles, dataSource.listNames(".*"));
        assertEquals(listedBarFiles, dataSource.listNames(".*bar.*"));
    }
}
