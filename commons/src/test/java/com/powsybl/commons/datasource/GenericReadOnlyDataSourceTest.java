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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class GenericReadOnlyDataSourceTest {

    protected FileSystem fileSystem;
    protected Path testDir;
    protected Set<String> unlistedFiles;
    protected Set<String> existingFiles;
    protected Set<String> filesInArchive;

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
        filesInArchive = Set.of(
            "foo_in_archive", "foo_in_archive.txt", "foo_in_archive.iidm", "foo_in_archive.xiidm",
            "foo_in_archive.v3.iidm", "foo_in_archive.v3", "foo_bar_in_archive.iidm", "foo_bar_in_archive",
            "bar_in_archive.iidm", "bar_in_archive");
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    void testConstructors() {
        // Observer
        DataSourceObserver observer = new DefaultDataSourceObserver();

        // Check constructors
        checkDataSource(new GenericReadOnlyDataSource(testDir), "", null);
        checkDataSource(new GenericReadOnlyDataSource(testDir, "foo_bar"), "foo_bar", null);
        checkDataSource(new GenericReadOnlyDataSource(testDir, "foo_bar", "iidm"), "foo_bar", "iidm");
        checkDataSource(new GenericReadOnlyDataSource(testDir, "foo_bar", "iidm", observer), "foo_bar", "iidm");
    }

    private void checkDataSource(GenericReadOnlyDataSource dataSource, String baseName, String mainExtension) {
        assertEquals(baseName, dataSource.getBaseName());
        assertEquals(mainExtension, dataSource.getMainExtension());
    }

    private String getFileName(String baseName, String mainExtension) {
        return testDir + "/" + baseName + (mainExtension == null || mainExtension.isEmpty() ? "" : "." + mainExtension);
    }

    private void createFiles(String archiveName) throws IOException{
        // Create the test files in the directory
        existingFiles.forEach(fileName -> {
            try {
                Files.createFile(fileSystem.getPath(testDir + "/" + fileName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Create the Zip archive and add the files
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(fileSystem.getPath(archiveName + ".zip")))) {
            filesInArchive.forEach(fileInArchive -> {
                try {
                    ZipEntry e = new ZipEntry(fileInArchive);
                    out.putNextEntry(e);
                    byte[] data = "Test String".getBytes();
                    out.write(data, 0, data.length);
                    out.closeEntry();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }

    // Currently, the files are not filtered in the zip archive
    static Stream<Arguments> provideArgumentsForClassAndListingTest() {
        Set<String> listedBarFiles = Set.of("foo_bar.iidm", "foo_bar", "foo_bar.iidm.bz2", "foo_bar.bz2", "foo_bar.iidm.xz", "foo_bar.xz",
            "foo_bar.iidm.zst", "foo_bar.zst", "foo_bar.iidm.gz", "foo_bar.gz", "foo_bar_in_archive.iidm", "foo_bar_in_archive",
            "bar_in_archive.iidm", "bar_in_archive");
        return Stream.of(
            Arguments.of("foo", "iidm", GenericReadOnlyDataSource.class,
                Set.of("foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar",
                    "foo.bz2", "foo.txt.bz2", "foo.iidm.bz2", "foo.xiidm.bz2", "foo.v3.iidm.bz2", "foo.v3.bz2", "foo_bar.iidm.bz2", "foo_bar.bz2",
                    "foo.xz", "foo.txt.xz", "foo.iidm.xz", "foo.xiidm.xz", "foo.v3.iidm.xz", "foo.v3.xz", "foo_bar.iidm.xz", "foo_bar.xz",
                    "foo.zst", "foo.txt.zst", "foo.iidm.zst", "foo.xiidm.zst", "foo.v3.iidm.zst", "foo.v3.zst", "foo_bar.iidm.zst", "foo_bar.zst",
                    "foo.gz", "foo.txt.gz", "foo.iidm.gz", "foo.xiidm.gz", "foo.v3.iidm.gz", "foo.v3.gz", "foo_bar.iidm.gz", "foo_bar.gz",
                    "foo_in_archive", "foo_in_archive.txt", "foo_in_archive.iidm", "foo_in_archive.xiidm",
                    "foo_in_archive.v3.iidm", "foo_in_archive.v3", "foo_bar_in_archive.iidm", "foo_bar_in_archive",
                    "bar_in_archive.iidm", "bar_in_archive", "foo.iidm.zip"),
                listedBarFiles),
            Arguments.of("foo", "", GenericReadOnlyDataSource.class,
                Set.of("foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar",
                    "foo.bz2", "foo.txt.bz2", "foo.iidm.bz2", "foo.xiidm.bz2", "foo.v3.iidm.bz2", "foo.v3.bz2", "foo_bar.iidm.bz2", "foo_bar.bz2",
                    "foo.xz", "foo.txt.xz", "foo.iidm.xz", "foo.xiidm.xz", "foo.v3.iidm.xz", "foo.v3.xz", "foo_bar.iidm.xz", "foo_bar.xz",
                    "foo.zst", "foo.txt.zst", "foo.iidm.zst", "foo.xiidm.zst", "foo.v3.iidm.zst", "foo.v3.zst", "foo_bar.iidm.zst", "foo_bar.zst",
                    "foo.gz", "foo.txt.gz", "foo.iidm.gz", "foo.xiidm.gz", "foo.v3.iidm.gz", "foo.v3.gz", "foo_bar.iidm.gz", "foo_bar.gz",
                    "foo_in_archive", "foo_in_archive.txt", "foo_in_archive.iidm", "foo_in_archive.xiidm",
                    "foo_in_archive.v3.iidm", "foo_in_archive.v3", "foo_bar_in_archive.iidm", "foo_bar_in_archive",
                    "bar_in_archive.iidm", "bar_in_archive", "foo.zip"),
                listedBarFiles),
            Arguments.of("foo", "v3", GenericReadOnlyDataSource.class,
                Set.of("foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar",
                    "foo.bz2", "foo.txt.bz2", "foo.iidm.bz2", "foo.xiidm.bz2", "foo.v3.iidm.bz2", "foo.v3.bz2", "foo_bar.iidm.bz2", "foo_bar.bz2",
                    "foo.xz", "foo.txt.xz", "foo.iidm.xz", "foo.xiidm.xz", "foo.v3.iidm.xz", "foo.v3.xz", "foo_bar.iidm.xz", "foo_bar.xz",
                    "foo.zst", "foo.txt.zst", "foo.iidm.zst", "foo.xiidm.zst", "foo.v3.iidm.zst", "foo.v3.zst", "foo_bar.iidm.zst", "foo_bar.zst",
                    "foo.gz", "foo.txt.gz", "foo.iidm.gz", "foo.xiidm.gz", "foo.v3.iidm.gz", "foo.v3.gz", "foo_bar.iidm.gz", "foo_bar.gz",
                    "foo_in_archive", "foo_in_archive.txt", "foo_in_archive.iidm", "foo_in_archive.xiidm",
                    "foo_in_archive.v3.iidm", "foo_in_archive.v3", "foo_bar_in_archive.iidm", "foo_bar_in_archive",
                    "bar_in_archive.iidm", "bar_in_archive", "foo.v3.zip"),
                listedBarFiles)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForClassAndListingTest")
    void testClassAndListing(String baseName, String mainExtension,
                             Class<? extends AbstractFileSystemDataSource> dataSourceClass,
                             Set<String> listedFiles, Set<String> listedBarFiles) throws IOException {
        // Compute the full filename
        String fileName = getFileName(baseName, mainExtension);

        // Update the list of unlisted files
        unlistedFiles = existingFiles.stream().filter(name -> !listedFiles.contains(name)).collect(Collectors.toSet());

        // Create the files
        createFiles(fileName);

        // Create the datasource
        GenericReadOnlyDataSource dataSource = new GenericReadOnlyDataSource(testDir, baseName, mainExtension);

        // Check the class
        assertInstanceOf(dataSourceClass, dataSource);

        // List all the files in the datasource
        assertEquals(listedFiles, dataSource.listNames(".*"));
        assertEquals(listedBarFiles, dataSource.listNames(".*bar.*"));
    }

    @Test
    void testIsMainExtension() {
        GenericReadOnlyDataSource dataSource = new GenericReadOnlyDataSource(testDir, "foo_bar");
        assertTrue(dataSource.isMainExtension("test"));
        assertTrue(dataSource.isMainExtension("iidm"));

        dataSource = new GenericReadOnlyDataSource(testDir, "foo_bar", "iidm");
        assertTrue(dataSource.isMainExtension("test"));
        assertTrue(dataSource.isMainExtension("iidm"));

    }


}
