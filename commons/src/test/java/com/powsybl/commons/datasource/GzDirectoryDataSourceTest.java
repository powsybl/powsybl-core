/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class GzDirectoryDataSourceTest extends DirectoryDataSourceTest {

    @Override
    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
        compressionFormat = CompressionFormat.GZIP;
    }

    @Test
    @Override
    void testConstructors() {
        // Observer
        DataSourceObserver observer = new DefaultDataSourceObserver();

        // Check constructors
        checkDataSource(new GzDirectoryDataSource(testDir, "foo_bar", "iidm", false, observer), observer);
    }

    private void checkDataSource(DirectoryDataSource dataSource, DataSourceObserver observer) {
        assertEquals(testDir, dataSource.getDirectory());
        assertEquals("iidm", dataSource.getDataExtension());
        assertEquals(compressionFormat, dataSource.getCompressionFormat());
        assertEquals("foo_bar", dataSource.getBaseName());
        assertFalse(dataSource.allFiles());
        assertEquals(observer, dataSource.getObserver());
    }

    @Override
    protected DataSource createDataSource() {
        return new GzDirectoryDataSource(testDir, "foo", null, false, null);
    }

    @Override
    protected DataSource createDataSource(DataSourceObserver observer) {
        return new GzDirectoryDataSource(testDir, "foo", "iidm", false, observer);
    }

    static Stream<Arguments> provideArgumentsForWriteThenReadTest() {
        return Stream.of(
            Arguments.of("foo", "iidm", CompressionFormat.GZIP),
            Arguments.of("foo", "", CompressionFormat.GZIP),
            Arguments.of("foo", "v3", CompressionFormat.GZIP)
        );
    }

    static Stream<Arguments> provideArgumentsForClassAndListingTest() {
        Set<String> listedFiles = Set.of("foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar",
            "foo.bz2", "foo.txt.bz2", "foo.iidm.bz2", "foo.xiidm.bz2", "foo.v3.iidm.bz2", "foo.v3.bz2", "foo_bar.iidm.bz2", "foo_bar.bz2",
            "foo.xz", "foo.txt.xz", "foo.iidm.xz", "foo.xiidm.xz", "foo.v3.iidm.xz", "foo.v3.xz", "foo_bar.iidm.xz", "foo_bar.xz",
            "foo.zst", "foo.txt.zst", "foo.iidm.zst", "foo.xiidm.zst", "foo.v3.iidm.zst", "foo.v3.zst", "foo_bar.iidm.zst", "foo_bar.zst");
        Set<String> listedBarFiles = Set.of("foo_bar.iidm", "foo_bar", "foo_bar.iidm.bz2", "foo_bar.bz2", "foo_bar.iidm.xz", "foo_bar.xz", "foo_bar.iidm.zst", "foo_bar.zst");
        return Stream.of(
            Arguments.of(null, "foo", "iidm", CompressionFormat.GZIP, GzDirectoryDataSource.class,
                listedFiles,
                listedBarFiles),
            Arguments.of(null, "foo", "", CompressionFormat.GZIP, GzDirectoryDataSource.class,
                listedFiles,
                listedBarFiles),
            Arguments.of(null, "foo", "v3", CompressionFormat.GZIP, GzDirectoryDataSource.class,
                listedFiles,
                listedBarFiles)
        );
    }
}
