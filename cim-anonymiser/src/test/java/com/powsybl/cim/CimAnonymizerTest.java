/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cim;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.test.TestUtil;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import javax.xml.transform.Source;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class CimAnonymizerTest {

    private FileSystem fileSystem;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    void anonymizeZip() throws Exception {
        Path workDir = fileSystem.getPath("work");
        Path cimZipFile = workDir.resolve("sample.zip");
        Path anonymizedCimFileDir = workDir.resolve("result");
        Files.createDirectories(anonymizedCimFileDir);
        Path dictionaryFile = workDir.resolve("dic.csv");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(cimZipFile))) {
            zos.putNextEntry(new ZipEntry("sample_EQ.xml"));
            zos.write(ByteStreams.toByteArray(getClass().getResourceAsStream("/sample_EQ.xml")));
            zos.closeEntry();
        }

        new CimAnonymizer().anonymizeZip(cimZipFile, anonymizedCimFileDir, dictionaryFile, new CimAnonymizer.DefaultLogger(), false);

        Path anonymizedCimZipFile = anonymizedCimFileDir.resolve("sample.zip");
        assertTrue(Files.exists(anonymizedCimZipFile));
        try (ZipFile anonymizedCimZipFileData = new ZipFile(Files.newByteChannel(anonymizedCimZipFile))) {
            assertNotNull(anonymizedCimZipFileData.getEntry("sample_EQ.xml"));
            Source control = Input.fromStream(getClass().getResourceAsStream("/sample_EQ_anonymized.xml")).build();
            try (InputStream is = anonymizedCimZipFileData.getInputStream(anonymizedCimZipFileData.getEntry("sample_EQ.xml"))) {
                Source test = Input.fromStream(is).build();
                Diff myDiff = DiffBuilder.compare(control)
                        .withTest(test)
                        .ignoreWhitespace()
                        .ignoreComments()
                        .build();
                boolean hasDiff = myDiff.hasDifferences();
                if (hasDiff) {
                    System.err.println(myDiff.toString());
                }
                assertFalse(hasDiff);
            }
        }

        assertEquals(TestUtil.normalizeLineSeparator(CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("/sample.csv")))),
                TestUtil.normalizeLineSeparator(Files.readString(dictionaryFile, StandardCharsets.UTF_8)));
    }
}
