/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class DataSourceUtilTest {
    private FileSystem fileSystem;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @Test
    void testGetBaseName() {
        assertEquals("dummy", DataSourceUtil.getBaseName("dummy.xml.gz"));
        assertEquals("dummy", DataSourceUtil.getBaseName("dummy.gz"));
        assertEquals("dummy", DataSourceUtil.getBaseName("dummy"));
    }

    @Test
    void testCreateDataSource() {

        // Observer
        DataSourceObserver dataSourceObserver = new DefaultDataSourceObserver();

        // Create a datasource
        DataSource dataSource = DataSourceUtil.createDataSource(
            fileSystem.getPath("/tmp"),
            "foo",
            ".bar",
            ArchiveFormat.ZIP,
            CompressionFormat.ZIP,
            dataSourceObserver);

        // The data source should be an instance of AbstractDataSource
        assertInstanceOf(AbstractDataSource.class, dataSource);

        // Check the datasource values
        assertEquals(fileSystem.getPath("/tmp"), ((AbstractDataSource) dataSource).getDirectory());
        assertEquals("foo", dataSource.getBaseName());
        assertEquals(".bar", ((AbstractDataSource) dataSource).getSourceFormat());
        assertEquals(ArchiveFormat.ZIP, ((AbstractDataSource) dataSource).getArchiveFormat());
        assertEquals(CompressionFormat.ZIP, ((AbstractDataSource) dataSource).getCompressionFormat());
        assertEquals(dataSourceObserver, ((AbstractDataSource) dataSource).getObserver());
    }
}
