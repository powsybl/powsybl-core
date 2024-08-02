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
import com.powsybl.commons.datasource.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
class CgmesOnDataSourceTest {

    private static void doTestExists(String filename, String cimVersion, boolean expectedExists) {
        ReadOnlyDataSource dataSource = new ResourceDataSource("incomplete",
                new ResourceSet("/", filename));
        CgmesOnDataSource cgmesOnDataSource = new CgmesOnDataSource(dataSource);
        boolean exists = "14".equals(cimVersion) ? cgmesOnDataSource.existsCim14() : cgmesOnDataSource.exists();
        assertEquals(expectedExists, exists);
    }

    private static void doTestExistsEmpty(String profile, String cimVersion, boolean expectedExists) {
        String filename = "empty_cim" + cimVersion + "_" + profile + ".xml";
        doTestExists(filename, cimVersion, expectedExists);
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
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            ReadOnlyDataSource dataSource = new ZipArchiveDataSource(testDir, "foo.iidm.zip", "test", "xml", null);
            CgmesOnDataSource cgmesOnDataSource = new CgmesOnDataSource(dataSource);
            assertFalse(cgmesOnDataSource.exists());
        }
    }

    @Test
    void testEQcim14() {
        doTestExistsEmpty("EQ", "14", true);
    }

    @Test
    void testEQcim16() {
        doTestExistsEmpty("EQ", "16", true);
    }

    @Test
    void testSVcim14() {
        doTestExistsEmpty("SV", "14", false);
    }

    @Test
    void testCimNoRdfcim16() {
        doTestExists("validCim16InvalidContent_EQ.xml", "16", false);
    }

    @Test
    void testCimNoRdfcim14() {
        doTestExists("validCim14InvalidContent_EQ.xml", "14", false);
    }

    @Test
    void testRdfNoCim16() {
        doTestExists("validRdfInvalidContent_EQ.xml", "16", false);
    }

    @Test
    void testRdfNoCim14() {
        doTestExists("validRdfInvalidContent_EQ.xml", "14", false);
    }

    @Test
    void testRdfCim16NotExistsCim14() {
        doTestExists("empty_cim16_EQ.xml", "14", false);
    }

    @Test
    void testRdfCim14NotExistsCim16() {
        doTestExists("empty_cim14_EQ.xml", "16", false);
    }
}
