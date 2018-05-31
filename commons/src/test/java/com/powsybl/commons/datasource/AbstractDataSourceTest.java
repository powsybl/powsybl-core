/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.ByteStreams;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

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

    protected String getBaseName() {
        return "foo";
    }

    protected boolean appendTest() {
        return true;
    }

    protected abstract DataSource createDataSource();

    @Test
    public void baseNameTest() {
        assertEquals(dataSource.getBaseName(), getBaseName());
    }

    private void writeThenReadTest(String suffix, String ext) throws IOException {
        // check file does not exist
        assertFalse(dataSource.exists(suffix, ext));

        // write file
        try (OutputStream os = dataSource.newOutputStream(suffix, ext, false)) {
            os.write("line1".getBytes(StandardCharsets.UTF_8));
        }
        if (appendTest()) {
            // write file in append mode
            try (OutputStream os = dataSource.newOutputStream(suffix, ext, true)) {
                os.write((System.lineSeparator() + "line2").getBytes(StandardCharsets.UTF_8));
            }
        }

        // write another file
        try (OutputStream os = dataSource.newOutputStream("dummy.txt", false)) {
            os.write("otherline1".getBytes(StandardCharsets.UTF_8));
        }

        // check files exists
        assertTrue(dataSource.exists(suffix, ext));
        assertTrue(dataSource.exists("dummy.txt"));

        // check all listed names exist and we can read them
        for (String name : dataSource.listNames(".*")) {
            assertTrue(dataSource.exists(name));
            try (InputStream is = dataSource.newInputStream(name)) {
                // Ok, some content is available
            }
            catch (IOException x) {
                fail(name);
            }
        }

        // check content is ok
        try (InputStream is = dataSource.newInputStream(suffix, ext)) {
            assertEquals("line1" + (appendTest() ? System.lineSeparator() + "line2" : ""),
                    new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        }
        try (InputStream is = dataSource.newInputStream("dummy.txt")) {
            assertEquals("otherline1", new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        }
    }

    @Test
    public void writeThenReadTest() throws IOException {
        writeThenReadTest(null, "bar");
        writeThenReadTest("_baz", "bar");
        writeThenReadTest("_baz", null);
    }

}
