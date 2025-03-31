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

/**
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
class CgmesOnDataSourceTest {

    private static final String XIIDM_XML_NOT_CGMES = "<?xml version='1.0' encoding='UTF-8'?><some></some>";

    static Stream<Arguments> provideArguments() {
        return Stream.of(
                Arguments.of("EQ cim14", "empty_cim14_EQ.xml", false),
                Arguments.of("EQ cim16", "empty_cim16_EQ.xml", true),
                Arguments.of("cim16 no rdf", "validCim16InvalidContent_EQ.xml", false),
                Arguments.of("rdf no cim16", "validRdfInvalidContent_EQ.xml", false));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideArguments")
    void testExists(String testName, String filename, boolean expectedExists) throws IOException {
        ReadOnlyDataSource dataSource = new ResourceDataSource("incomplete",
                new ResourceSet("/", filename));
        CgmesOnDataSource cgmesOnDataSource = new CgmesOnDataSource(dataSource);
        assertEquals(expectedExists, cgmesOnDataSource.exists());
    }

    static Stream<Arguments> provideArgumentsForTestXmlMainFileXiidmZip() {
        return Stream.of(
                Arguments.of("foo", "xml"),
                Arguments.of("foo", null),
                Arguments.of("foo", ""),
                Arguments.of("foo", "notexists"),
                Arguments.of("bar", "xml"),
                Arguments.of("bar", null),
                Arguments.of("bar", ""),
                Arguments.of("bar", "notexists")
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestXmlMainFileXiidmZip")
    void testXmlMainFileXiidmZip(String basename, String dataextension) throws IOException {
        Path testDir;
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            testDir = fileSystem.getPath("/tmp");
            Files.createDirectories(testDir);
            try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(testDir.resolve("my.zip")))) {
                try {
                    ZipEntry e = new ZipEntry("foo.xml");
                    out.putNextEntry(e);
                    byte[] data = XIIDM_XML_NOT_CGMES.getBytes();
                    out.write(data, 0, data.length);
                    out.closeEntry();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            ReadOnlyDataSource dataSourceGoodBasenameGoodDataExtension = new ZipArchiveDataSource(
                    testDir, "my.zip", basename, dataextension, null);
            CgmesOnDataSource cgmesOnDataSourceGoodBasenameGoodDataExtension = new CgmesOnDataSource(
                    dataSourceGoodBasenameGoodDataExtension);
            assertFalse(cgmesOnDataSourceGoodBasenameGoodDataExtension.exists());

        }
    }

    static Stream<Arguments> provideArgumentsForTestXmlMainFileCgmesZip() {
        return Stream.of(
                Arguments.of("foo", "xml", true),
                Arguments.of("foo", "xiidm", false),
                Arguments.of("foo", "notexists", true),
                Arguments.of("foo", "", true),
                Arguments.of("foo", null, true),
                Arguments.of("bar", "xml", false),
                Arguments.of("bar", "xiidm", false),
                Arguments.of("bar", "notexists", true),
                Arguments.of("bar", "", true),
                Arguments.of("bar", null, true),
                Arguments.of("kop", "xml", true),
                Arguments.of("kop", "xiidm", false),
                Arguments.of("kop", "notexists", true),
                Arguments.of("kop", "", true),
                Arguments.of("kop", null, true),
                Arguments.of("notexist", "xml", true),
                Arguments.of("notexist", "xiidm", true),
                Arguments.of("notexist", "notexists", true),
                Arguments.of("notexist", "", true),
                Arguments.of("notexist", null, true)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestXmlMainFileCgmesZip")
    void testXmlMainFileCgmesZip(String basename, String dataextension, boolean expected) throws IOException {
        Path testDir;
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            testDir = fileSystem.getPath("/tmp");
            Files.createDirectories(testDir);
            try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(testDir.resolve("my.zip")))) {
                try {
                    // The cgmes file that justifies importing this as CGMES
                    ZipEntry e = new ZipEntry("foo.xml");
                    out.putNextEntry(e);
                    byte[] data = getClass().getResourceAsStream("/empty_cim16_EQ.xml").readAllBytes();
                    out.write(data, 0, data.length);
                    out.closeEntry();

                    // random other files, depending on the datasource basename and dataextension
                    // the cmgmes importer will refuse to import to allow other importers to import
                    e = new ZipEntry("foo.xiidm");
                    out.putNextEntry(e);
                    data = "same basename as the tested cgmes file foo.xml".getBytes();
                    out.write(data, 0, data.length);
                    out.closeEntry();
                    // Note: and no need to test prefix matching file names like "fooooo.xiidm" or
                    // "fooooo.xml"
                    // because the prefixing of the basename is not used for exists()

                    // different basename but xml may still be cgmes
                    e = new ZipEntry("bar.xml");
                    out.putNextEntry(e);
                    data = XIIDM_XML_NOT_CGMES.getBytes();
                    out.write(data, 0, data.length);
                    out.closeEntry();

                    e = new ZipEntry("bar.xiidm");
                    out.putNextEntry(e);
                    data = "different basename different extension".getBytes();
                    out.write(data, 0, data.length);
                    out.closeEntry();

                    e = new ZipEntry("kop.xiidm");
                    out.putNextEntry(e);
                    data = "nothing in common, there is no other file with the same basename and .xml extension"
                            .getBytes();
                    out.write(data, 0, data.length);
                    out.closeEntry();

                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            ReadOnlyDataSource dataSource = new ZipArchiveDataSource(testDir, "my.zip", basename, dataextension, null);
            CgmesOnDataSource cgmesOnDataSource = new CgmesOnDataSource(dataSource);
            assertEquals(expected, cgmesOnDataSource.exists());
        }
    }

}
