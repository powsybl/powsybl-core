/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.model;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.datasource.ZipArchiveDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
class CgmesOnDataSourceTest {

    static Stream<Arguments> provideArguments() {
        return Stream.of(
                Arguments.of("EQ cim14", "empty_cim14_EQ.xml", "14", true),
                Arguments.of("EQ cim16", "empty_cim16_EQ.xml", "16", true),
                Arguments.of("SV cim14", "empty_cim14_SV.xml", "14", false),
                Arguments.of("cim no rdf cim16", "validCim16InvalidContent_EQ.xml", "16", false),
                Arguments.of("cim no rdf cim14", "validCim14InvalidContent_EQ.xml", "14", false),
                Arguments.of("rdf no cim16", "validRdfInvalidContent_EQ.xml", "16", false),
                Arguments.of("rdf no cim14", "validRdfInvalidContent_EQ.xml", "14", false),
                Arguments.of("rdf cim16 not cim14", "empty_cim16_EQ.xml", "14", false),
                Arguments.of("rdf cim14 not cim16", "empty_cim14_EQ.xml", "16", false)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideArguments")
    void testExists(String testName, String filename, String cimVersion, boolean expectedExists) throws IOException {
        ReadOnlyDataSource dataSource = new ResourceDataSource("incomplete",
                new ResourceSet("/", filename));
        CgmesOnDataSource cgmesOnDataSource = new CgmesOnDataSource(dataSource);
        boolean exists = "14".equals(cimVersion) ? cgmesOnDataSource.existsCim14() : cgmesOnDataSource.exists();
        assertEquals(expectedExists, exists);
    }

    @Test
    void testFileDoesNotExist() throws IOException {
        Path testDir;
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            testDir = fileSystem.getPath("/tmp");
            Files.createDirectories(testDir);
            try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(testDir.resolve("foo.iidm.zip")))) {
                try {
                    ZipEntry e = new ZipEntry("foo.bar");
                    out.putNextEntry(e);
                    byte[] data = "Test String".getBytes();
                    out.write(data, 0, data.length);
                    out.closeEntry();
                    e = new ZipEntry("foo.xml");
                    out.putNextEntry(e);
                    data = getClass().getResourceAsStream("/empty_cim16_EQ.xml").readAllBytes();
                    out.write(data, 0, data.length);
                    out.closeEntry();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            ReadOnlyDataSource dataSource = new ZipArchiveDataSource(testDir, "foo.iidm.zip", "test", "xml", null);
            CgmesOnDataSource cgmesOnDataSource = new CgmesOnDataSource(dataSource);
            assertTrue(cgmesOnDataSource.exists());
            ReadOnlyDataSource dataSource2 = new ZipArchiveDataSource(testDir, "foo.iidm.zip", "test", "iidm", null);
            CgmesOnDataSource cgmesOnDataSource2 = new CgmesOnDataSource(dataSource2);
            assertTrue(cgmesOnDataSource2.exists());
            ReadOnlyDataSource dataSource3 = new ZipArchiveDataSource(testDir, "foo.iidm.zip", "foo", "bar", null);
            CgmesOnDataSource cgmesOnDataSource3 = new CgmesOnDataSource(dataSource3);
            assertFalse(cgmesOnDataSource3.exists());
        }
    }
}
