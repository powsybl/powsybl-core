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

import static org.junit.Assert.assertFalse;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ZipFileDataSourceTest extends AbstractDataSourceTest {

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
}
