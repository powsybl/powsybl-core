/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class DataSourceObserverTest {

    private FileSystem fileSystem;

    protected Path testDir;

    @BeforeEach
    void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        testDir = fileSystem.getPath("/tmp");
        Files.createDirectories(testDir);
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    void test() throws IOException {
        String[] openedStream = new String[1];
        String[] closedStream = new String[1];
        DataSourceObserver observer = new DataSourceObserver() {
            @Override
            public void opened(String streamName) {
                openedStream[0] = streamName;
            }

            @Override
            public void closed(String streamName) {
                closedStream[0] = streamName;
            }
        };

        DataSource dataSource = new FileDataSource(testDir, "test", observer);

        try (OutputStream os = dataSource.newOutputStream(null, "txt", false)) {
        }
        assertEquals("/tmp/test.txt", openedStream[0]);
        assertEquals("/tmp/test.txt", closedStream[0]);

        openedStream[0] = null;
        closedStream[0] = null;
        try (InputStream is = dataSource.newInputStream(null, "txt")) {
        }
        assertEquals("/tmp/test.txt", openedStream[0]);
        assertEquals("/tmp/test.txt", closedStream[0]);
    }
}
