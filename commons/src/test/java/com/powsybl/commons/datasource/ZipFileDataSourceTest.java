/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class ZipFileDataSourceTest extends AbstractDataSourceTest {

    private static final String WORK_DIR = "/work/";
    private static final String MAIN_EXT = "xml";
    private static final String BASENAME = "network";
    private static final String MAIN_FILE = BASENAME + "." + MAIN_EXT;
    private static final String ZIP_FILENAME = MAIN_FILE + ".zip";
    private static final String ZIP_PATH = WORK_DIR + ZIP_FILENAME;
    private static final String ADDITIONAL_SUFFIX = "_mapping";
    private static final String ADDITIONAL_EXT = "csv";
    private static final String ADDITIONAL_FILE = BASENAME + ADDITIONAL_SUFFIX + "." + ADDITIONAL_EXT;
    private static final String UNRELATED_FILE = "other.de";

    @Override
    protected boolean appendTest() {
        return false;
    }

    @Override
    protected DataSource createDataSource() {
        return new ZipFileDataSource(testDir, getBaseName());
    }

    @Test
    void fakeZipTest() throws IOException {
        Files.createFile(testDir.resolve("fake.zip"));
        assertFalse(new ZipFileDataSource(testDir, "fake").exists("e"));
    }

    @Test
    void createZipDataSourceWithMoreThanOneDot() throws IOException {
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(fileSystem.getPath(ZIP_PATH)))) {
            // create an entry
            ZipEntry e = new ZipEntry(UNRELATED_FILE);
            out.putNextEntry(e);
            byte[] data = "Test String".getBytes();
            out.write(data, 0, data.length);

            e = new ZipEntry(MAIN_FILE);
            out.putNextEntry(e);
            data = "Test String 2".getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();

            e = new ZipEntry(ADDITIONAL_FILE);
            out.putNextEntry(e);
            data = "Test String 2".getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();

        }
        var workdirPath = fileSystem.getPath(WORK_DIR);
        DataSource dataSource = DataSourceUtil.createDataSource(workdirPath, ZIP_FILENAME, BASENAME, null);
        assertTrue(dataSource.exists(UNRELATED_FILE));
        assertFalse(dataSource.exists("not.zip"));
        assertTrue(dataSource.exists(null, MAIN_EXT));
        assertTrue(dataSource.exists(ADDITIONAL_SUFFIX, ADDITIONAL_EXT));
        assertFalse(dataSource.exists("-not", "there"));
        try (InputStream is = dataSource.newInputStream(UNRELATED_FILE)) {
            assertEquals("Test String", new String(is.readAllBytes()));
        }
    }

}
