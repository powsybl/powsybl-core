/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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

/**
 * @author Olivier Bretteville {@literal <olivier.bretteville at rte-france.com>}
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class ZstdDirectoryDataSourceTest extends DirectoryDataSourceTest {

    @Override
    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
        compressionFormat = CompressionFormat.ZSTD;
    }

    @Test
    @Override
    void testConstructors() {
        // Observer
        DataSourceObserver observer = new DefaultDataSourceObserver();

        // Check constructors
        checkDataSource(new ZstdDirectoryDataSource(testDir, "foo_bar", "iidm", observer), observer);
    }

    private void checkDataSource(DirectoryDataSource dataSource, DataSourceObserver observer) {
        assertEquals(testDir, dataSource.getDirectory());
        assertEquals("iidm", dataSource.getDataExtension());
        assertEquals(compressionFormat, dataSource.getCompressionFormat());
        assertEquals("foo_bar", dataSource.getBaseName());
        assertEquals(observer, dataSource.getObserver());
    }

    @Override
    protected DataSource createDataSource() {
        return new ZstdDirectoryDataSource(testDir, "foo", null, null);
    }

    @Override
    protected DataSource createDataSource(DataSourceObserver observer) {
        return new ZstdDirectoryDataSource(testDir, "foo", "iidm", observer);
    }

    static Stream<Arguments> provideArgumentsForWriteThenReadTest() {
        return Stream.of(
            Arguments.of("foo", "iidm", CompressionFormat.ZSTD),
            Arguments.of("foo", "", CompressionFormat.ZSTD),
            Arguments.of("foo", "v3", CompressionFormat.ZSTD)
        );
    }

    static Stream<Arguments> provideArgumentsForClassAndListingTest() {
        Set<String> listedFiles = Set.of("foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar",
            "foo.bz2", "foo.txt.bz2", "foo.iidm.bz2", "foo.xiidm.bz2", "foo.v3.iidm.bz2", "foo.v3.bz2", "foo_bar.iidm.bz2", "foo_bar.bz2",
            "foo.xz", "foo.txt.xz", "foo.iidm.xz", "foo.xiidm.xz", "foo.v3.iidm.xz", "foo.v3.xz", "foo_bar.iidm.xz", "foo_bar.xz",
            "foo.gz", "foo.txt.gz", "foo.iidm.gz", "foo.xiidm.gz", "foo.v3.iidm.gz", "foo.v3.gz", "foo_bar.iidm.gz", "foo_bar.gz");
        Set<String> listedBarFiles = Set.of("foo_bar.iidm", "foo_bar", "foo_bar.iidm.bz2", "foo_bar.bz2", "foo_bar.iidm.xz", "foo_bar.xz", "foo_bar.iidm.gz", "foo_bar.gz");
        return Stream.of(
            Arguments.of("foo", "iidm", CompressionFormat.ZSTD, ZstdDirectoryDataSource.class,
                listedFiles,
                listedBarFiles),
            Arguments.of("foo", "", CompressionFormat.ZSTD, ZstdDirectoryDataSource.class,
                listedFiles,
                listedBarFiles),
            Arguments.of("foo", "v3", CompressionFormat.ZSTD, ZstdDirectoryDataSource.class,
                listedFiles,
                listedBarFiles)
        );
    }
}
