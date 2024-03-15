/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datasource;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class GzFileDataSourceTest extends AbstractDataSourceTest {

    private static final String WORK_DIR = "/work/";
    private static final String MAIN_EXT = "xml";
    private static final String BASENAME = "network";
    private static final String MAIN_FILE = BASENAME + "." + MAIN_EXT;
    private static final String GZ_FILENAME = MAIN_FILE + ".gz";
    private static final String GZ_PATH = WORK_DIR + GZ_FILENAME;
    private static final String ADDITIONAL_SUFFIX = "_mapping";
    private static final String ADDITIONAL_EXT = "csv";
    private static final String ADDITIONAL_FILE = BASENAME + ADDITIONAL_SUFFIX + "." + ADDITIONAL_EXT;
    private static final String UNRELATED_FILE = "other.de";

    @Override
    protected DataSource createDataSource() {
        return new GzFileDataSource(testDir, getBaseName());
    }

    @Test
    void createGzDataSourceWithMoreThanOneDot() throws IOException {


        try(GZIPOutputStream gos = new GZIPOutputStream(Files.newOutputStream(fileSystem.getPath(GZ_PATH)))) {
            Files.copy(Path.of(Objects.requireNonNull(getClass().getResource("/test/foo.txt")).getPath()), gos);
        }

        var workdirPath = fileSystem.getPath(WORK_DIR);
        DataSource dataSource = DataSourceUtil.createDataSource(workdirPath, GZ_FILENAME, null);
//        assertTrue(dataSource.exists(UNRELATED_FILE));
        assertFalse(dataSource.exists("not.gz"));
//        assertTrue(dataSource.exists(null, MAIN_EXT));
//        assertTrue(dataSource.exists(ADDITIONAL_SUFFIX, ADDITIONAL_EXT));
        assertFalse(dataSource.exists("-not", "there"));
//        try (InputStream is = dataSource.newInputStream(UNRELATED_FILE)) {
//            assertEquals("Test String", new String(is.readAllBytes()));
//        }
    }
}
