/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ZipFileDataSourceTest extends AbstractDataSourceTest {

    private static final String WORK_DIR = "/work/";
    private static final String ZIP_TST = "multiple.dot.tst";
    private static final String INSIDE_ZIP_TST_TOP = "insideZip.tst.top";

    @Override
    protected boolean appendTest() {
        return false;
    }

    @Override
    protected DataSource createDataSource() {
        return new ZipFileDataSource(testDir, getBaseName());
    }

    @Test
    public void fakeZipTest() throws IOException {
        Files.createFile(testDir.resolve("fake.zip"));
        assertFalse(new ZipFileDataSource(testDir, "fake").exists("e"));
    }

    @Test
    public void createZipDataSourceWithMoreThanOneDot() throws IOException {
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(fileSystem.getPath(WORK_DIR + ZIP_TST + ".zip")));) {
            // create an entry
            ZipEntry e = new ZipEntry(INSIDE_ZIP_TST_TOP);
            out.putNextEntry(e);
            byte[] data = "Test String".getBytes();
            out.write(data, 0, data.length);

            e = new ZipEntry(ZIP_TST);
            out.putNextEntry(e);
            data = "Test String 2".getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();

            e = new ZipEntry(ZIP_TST + "-xml.gz");
            out.putNextEntry(e);
            data = "Test String 2".getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();

        }
        var zipPath = fileSystem.getPath(WORK_DIR);
        DataSource dataSource = DataSourceUtil.createDataSource(zipPath, ZIP_TST + ".zip", null);
        assertTrue(dataSource.exists(INSIDE_ZIP_TST_TOP));
        assertFalse(dataSource.exists("not.zip"));
        assertTrue(dataSource.exists("", "dot.tst"));
        assertTrue(dataSource.exists("-xml", "gz"));
        assertFalse(dataSource.exists("-not", "there"));
        assertEquals("Test String", new String(dataSource.newInputStream("insideZip.tst.top").readAllBytes()));
    }

}
