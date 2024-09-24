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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class DirectoryDataSourceTest extends AbstractFileSystemDataSourceTest {

    @BeforeEach
    void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        testDir = fileSystem.getPath("/tmp");
        Files.createDirectories(testDir);

        // Files
        existingFiles = Set.of(
            "foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar", "bar.iidm", "bar",
            "foo.bz2", "foo.txt.bz2", "foo.iidm.bz2", "foo.xiidm.bz2", "foo.v3.iidm.bz2", "foo.v3.bz2", "foo_bar.iidm.bz2", "foo_bar.bz2", "bar.iidm.bz2", "bar.bz2",
            "foo.xz", "foo.txt.xz", "foo.iidm.xz", "foo.xiidm.xz", "foo.v3.iidm.xz", "foo.v3.xz", "foo_bar.iidm.xz", "foo_bar.xz", "bar.iidm.xz", "bar.xz",
            "foo.zst", "foo.txt.zst", "foo.iidm.zst", "foo.xiidm.zst", "foo.v3.iidm.zst", "foo.v3.zst", "foo_bar.iidm.zst", "foo_bar.zst", "bar.iidm.zst", "bar.zst",
            "foo.gz", "foo.txt.gz", "foo.iidm.gz", "foo.xiidm.gz", "foo.v3.iidm.gz", "foo.v3.gz", "foo_bar.iidm.gz", "foo_bar.gz", "bar.iidm.gz", "bar.gz"
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @Override
    protected String getContainerPath(String containerFileName, String baseName, String dataExtension,
                                      CompressionFormat compressionFormat) {
        return testDir + (containerFileName == null ? "" : "/" + containerFileName);
    }

    @Override
    protected void createFiles(String fileName) throws IOException {
        Files.createDirectories(fileSystem.getPath(fileName));
        // Create the test files
        existingFiles.forEach(eachFileName -> {
            try {
                Files.createFile(fileSystem.getPath(fileName + "/" + eachFileName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    protected String getDatasourcePath(String containerFileName, String baseName, String dataExtension,
                                       CompressionFormat compressionFormat) {
        return baseName == null ?
                getContainerPath(containerFileName, baseName, dataExtension, compressionFormat) :
                testDir + "/" + getFileName(baseName, dataExtension, compressionFormat);
    }

    @Test
    @Override
    void testConstructors() {
        // Observer
        DataSourceObserver observer = new DefaultDataSourceObserver();

        // Check constructors
        checkDataSource(new DirectoryDataSource(testDir, "foo_bar"), null, null, null);
        checkDataSource(new DirectoryDataSource(testDir, "foo_bar", observer), null, null, observer);
        checkDataSource(new DirectoryDataSource(testDir, "foo_bar", "iidm", observer), "iidm", null, observer);
        checkDataSource(new DirectoryDataSource(testDir, "foo_bar", "iidm", CompressionFormat.GZIP, observer), "iidm", CompressionFormat.GZIP, observer);
    }

    private void checkDataSource(DirectoryDataSource dataSource, String dataExtension, CompressionFormat compressionFormat, DataSourceObserver observer) {
        assertEquals(testDir, dataSource.getDirectory());
        assertEquals(dataExtension, dataSource.getDataExtension());
        assertEquals(compressionFormat, dataSource.getCompressionFormat());
        assertEquals("foo_bar", dataSource.getBaseName());
        assertEquals(observer, dataSource.getObserver());
    }

    @Override
    protected DataSource createDataSource() {
        return new DirectoryDataSource(testDir, "foo");
    }

    @Override
    protected DataSource createDataSource(DataSourceObserver observer) {
        return new DirectoryDataSource(testDir, "foo", "iidm", observer);
    }

    static Stream<Arguments> provideArgumentsForWriteThenReadTest() {
        return Stream.of(
            Arguments.of("foo", "iidm", null),
            Arguments.of("foo", "", null),
            Arguments.of("foo", "v3", null)
        );
    }

    static Stream<Arguments> provideArgumentsForClassAndListingTest() {
        Set<String> listedFiles = Set.of("foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar",
            "foo.bz2", "foo.txt.bz2", "foo.iidm.bz2", "foo.xiidm.bz2", "foo.v3.iidm.bz2", "foo.v3.bz2", "foo_bar.iidm.bz2", "foo_bar.bz2",
            "foo.xz", "foo.txt.xz", "foo.iidm.xz", "foo.xiidm.xz", "foo.v3.iidm.xz", "foo.v3.xz", "foo_bar.iidm.xz", "foo_bar.xz",
            "foo.zst", "foo.txt.zst", "foo.iidm.zst", "foo.xiidm.zst", "foo.v3.iidm.zst", "foo.v3.zst", "foo_bar.iidm.zst", "foo_bar.zst",
            "foo.gz", "foo.txt.gz", "foo.iidm.gz", "foo.xiidm.gz", "foo.v3.iidm.gz", "foo.v3.gz", "foo_bar.iidm.gz", "foo_bar.gz");
        Set<String> listedBarFiles = Set.of("foo_bar.iidm", "foo_bar", "foo_bar.iidm.bz2", "foo_bar.bz2", "foo_bar.iidm.xz", "foo_bar.xz",
            "foo_bar.iidm.zst", "foo_bar.zst", "foo_bar.iidm.gz", "foo_bar.gz");
        Set<String> barFiles = Set.of(
            "bar.iidm", "bar",
            "bar.iidm.bz2", "bar.bz2",
            "bar.iidm.xz", "bar.xz",
            "bar.iidm.zst", "bar.zst",
            "bar.iidm.gz", "bar.gz");
        Set<String> curatedListedFiles = Stream.concat(listedFiles.stream(), barFiles.stream()).collect(Collectors.toSet());
        Set<String> curatedListedBarFiles = Stream.concat(listedBarFiles.stream(), barFiles.stream()).collect(Collectors.toSet());
        return Stream.of(
            Arguments.of(null, "foo", "iidm", null, DirectoryDataSource.class,
                listedFiles,
                listedBarFiles),
            Arguments.of(null, "foo", "", null, DirectoryDataSource.class,
                listedFiles,
                listedBarFiles),
            Arguments.of(null, "foo", "v3", null, DirectoryDataSource.class,
                listedFiles,
                listedBarFiles),
            Arguments.of("tmp", null, null, null, DirectoryDataSource.class,
                curatedListedFiles,
                curatedListedBarFiles),
            Arguments.of("foo", null, null, null, DirectoryDataSource.class,
                curatedListedFiles,
                curatedListedBarFiles),
            Arguments.of("foo.xiidm", null, null, null, DirectoryDataSource.class,
                    curatedListedFiles,
                    curatedListedBarFiles)
        );
    }

    @Test
    void testExceptionListNames() {

        // Create the datasource
        DataSource dataSource = new DirectoryDataSource(fileSystem.getPath("/foo"), "baz");

        // An exception is thrown because the directory does not exist
        assertThrows(IOException.class, () -> dataSource.listNames(".*"));
    }

}
