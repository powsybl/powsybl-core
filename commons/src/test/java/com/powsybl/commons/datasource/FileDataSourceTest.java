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
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class FileDataSourceTest extends AbstractDataSourceTest {

    @Override
    protected DataSource createDataSource() {
        return new FileDataSource(testDir, getBaseName());
    }

    @Test
    void listNamesTest() throws IOException {
        // Create a couple of files in the test folder
        // One using the basename
        String validFilename = getBaseName() + ".txt";
        try (OutputStream os = Files.newOutputStream(testDir.resolve(validFilename))) {
            os.write("basename_line".getBytes(StandardCharsets.UTF_8));
        }
        // Other that has a different name
        String otherFilename = "other.txt";
        try (OutputStream os = Files.newOutputStream(testDir.resolve(otherFilename))) {
            os.write("other_line".getBytes(StandardCharsets.UTF_8));
        }

        // A file data source created using the complete filename does not return other filenames
        Set<String> names = new FileDataSource(testDir, getBaseName(), ".txt").listNames(".*");
        assertEquals(1, names.size());
        assertTrue(names.contains(validFilename));
        assertFalse(names.contains(otherFilename));

        // A file data source created using the test folder and the basename sees all the files
        names = new FileDataSource(testDir, getBaseName()).listNames(".*");
        assertEquals(2, names.size());
        assertTrue(names.contains(validFilename));
        assertTrue(names.contains(otherFilename));
    }

    @Test
    void createNewFilesTest() throws IOException {
        DataSource ds = createDataSource();

        // use the data source to write a file that contains basename
        String suffix = "suffix";
        String ext = "ext";
        try (OutputStream os = ds.newOutputStream(suffix, ext, false)) {
            os.write("line".getBytes(StandardCharsets.UTF_8));
        }

        // it is allowed to use the data source to write a file that does not contain the basename
        try (OutputStream os = ds.newOutputStream("dummy.txt", false)) {
            os.write("dummy_line".getBytes(StandardCharsets.UTF_8));
        }

        // write another file in the same directory of data source that does not contain the basename
        // do not use the data source, just write in the same directory
        try (OutputStream os = Files.newOutputStream(testDir.resolve("dummy2.txt"))) {
            os.write("dummy2_line".getBytes(StandardCharsets.UTF_8));
        }

        // check the three files exists when checked through the data source
        assertTrue(ds.exists(suffix, ext));
        assertTrue(ds.exists("dummy.txt"));
        assertTrue(ds.exists("dummy2.txt"));

        // all the files can be accessed through list names for basename datasource (no extension)
        Set<String> names = ds.listNames(".*");
        assertEquals(3, names.size());
        assertTrue(names.contains(getBaseName() + suffix + "." + ext));
        assertTrue(names.contains("dummy.txt"));
        assertTrue(names.contains("dummy2.txt"));
    }
}
