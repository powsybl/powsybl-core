/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import com.google.common.io.ByteStreams;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractDataSourceTest {

    private FileSystem fileSystem;

    protected Path testDir;

    private DataSource dataSource;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        testDir = fileSystem.getPath("/tmp");
        Files.createDirectories(testDir);
        dataSource = createDataSource();
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    protected abstract String getMainFileName();

    protected boolean appendTest() {
        return true;
    }

    protected abstract DataSource createDataSource();

    @Test
    public void fileNameTest() {
        assertEquals(dataSource.getMainFileName(), getMainFileName());
    }

    @Test
    public void writeThenReadTest() throws IOException {
        // check file does not exist
        assertFalse(dataSource.fileExists("dummy.txt"));

        // write file
        try (OutputStream os = dataSource.newOutputStream("dummy.txt", false)) {
            os.write("line1".getBytes(StandardCharsets.UTF_8));
        }
        if (appendTest()) {
            // write file in append mode
            try (OutputStream os = dataSource.newOutputStream("dummy.txt", true)) {
                os.write((System.lineSeparator() + "line2").getBytes(StandardCharsets.UTF_8));
            }
        }

        // check files exists
        assertTrue(dataSource.fileExists("dummy.txt"));

        // check all listed names exist and we can read them
        for (String fileName : dataSource.getFileNames(".*")) {
            assertTrue(dataSource.fileExists(fileName));
            try (InputStream is = dataSource.newInputStream(fileName)) {
                // Ok, some content is available
            }
            catch (IOException x) {
                fail(fileName);
            }
        }

        // check content is ok
        try (InputStream is = dataSource.newInputStream("dummy.txt")) {
            assertEquals("line1" + (appendTest() ? System.lineSeparator() + "line2" : ""),
                    new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        }
    }
}
