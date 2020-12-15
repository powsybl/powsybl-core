/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cim;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import net.java.truevfs.comp.zip.ZipEntry;
import net.java.truevfs.comp.zip.ZipFile;
import net.java.truevfs.comp.zip.ZipOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CimAnonymizerTest {

    private FileSystem fileSystem;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void anonymizeZip() throws Exception {
        Path workDir = fileSystem.getPath("work");
        Path cimZipFile = workDir.resolve("sample.zip");
        Path anonymizedCimFileDir = workDir.resolve("result");
        Files.createDirectories(anonymizedCimFileDir);
        Path dictionaryFile = workDir.resolve("dic.csv");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(cimZipFile))) {
            zos.putNextEntry(new ZipEntry("sample_EQ.xml"));
            zos.write(ByteStreams.toByteArray(getClass().getResourceAsStream("/sample_EQ.xml")));
        }

        new CimAnonymizer().anonymizeZip(cimZipFile, anonymizedCimFileDir, dictionaryFile, new CimAnonymizer.DefaultLogger(), false);

        Path anonymizedCimZipFile = anonymizedCimFileDir.resolve("sample.zip");
        assertTrue(Files.exists(anonymizedCimZipFile));
        try (ZipFile anonymizedCimZipFileData = new ZipFile(anonymizedCimZipFile)) {
            assertNotNull(anonymizedCimZipFileData.entry("sample_EQ.xml"));
            Source control = Input.fromStream(getClass().getResourceAsStream("/sample_EQ_anonymized.xml")).build();
            try (InputStream is = anonymizedCimZipFileData.getInputStream("sample_EQ.xml")) {
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

        assertEquals(CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("/sample.csv"))),
                     new String(Files.readAllBytes(dictionaryFile), StandardCharsets.UTF_8));
    }
}
