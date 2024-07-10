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

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class Bzip2DirectoryDataSourceTest extends DirectoryDataSourceTest {

    @Override
    @BeforeEach
    void setUp() throws Exception {
        super.setUp();
        compressionFormat = CompressionFormat.BZIP2;
    }

    @Test
    @Override
    void testConstructors() {
        // Observer
        DataSourceObserver observer = new DefaultDataSourceObserver();

        // Check constructors
        checkDataSource(new Bzip2DirectoryDataSource(testDir, "foo_bar", observer), observer);
        checkDataSource(new Bzip2DirectoryDataSource(testDir, "foo_bar"), null);
    }

    private void checkDataSource(DirectoryDataSource dataSource, DataSourceObserver observer) {
        assertEquals(testDir, dataSource.getDirectory());
        assertEquals(compressionFormat, dataSource.getCompressionFormat());
        assertEquals("foo_bar", dataSource.getBaseName());
        assertEquals(observer, dataSource.getObserver());
    }

    @Override
    protected boolean appendTest() {
        // Append does not work with bzip2 compression
        return false;
    }

    @Override
    protected DataSource createDataSource() {
        return new Bzip2DirectoryDataSource(testDir, "foo");
    }

    @Override
    protected DataSource createDataSource(DataSourceObserver observer) {
        return new Bzip2DirectoryDataSource(testDir, "foo", observer);
    }

    static Stream<Arguments> provideArgumentsForWriteThenReadTest() {
        return Stream.of(
            Arguments.of("foo", ".iidm", CompressionFormat.BZIP2),
            Arguments.of("foo", "", CompressionFormat.BZIP2),
            Arguments.of("foo", ".v3", CompressionFormat.BZIP2)
        );
    }

    static Stream<Arguments> provideArgumentsForClassAndListingTest() {
        return Stream.of(
            Arguments.of("foo", ".iidm", CompressionFormat.BZIP2, Bzip2DirectoryDataSource.class,
                Set.of("foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar",
                    "foo.xz", "foo.txt.xz", "foo.iidm.xz", "foo.xiidm.xz", "foo.v3.iidm.xz", "foo.v3.xz", "foo_bar.iidm.xz", "foo_bar.xz",
                    "foo.zst", "foo.txt.zst", "foo.iidm.zst", "foo.xiidm.zst", "foo.v3.iidm.zst", "foo.v3.zst", "foo_bar.iidm.zst", "foo_bar.zst",
                    "foo.gz", "foo.txt.gz", "foo.iidm.gz", "foo.xiidm.gz", "foo.v3.iidm.gz", "foo.v3.gz", "foo_bar.iidm.gz", "foo_bar.gz"),
                Set.of("foo_bar.iidm", "foo_bar", "foo_bar.iidm.xz", "foo_bar.xz", "foo_bar.iidm.zst", "foo_bar.zst", "foo_bar.iidm.gz", "foo_bar.gz")),
            Arguments.of("foo", "", CompressionFormat.BZIP2, Bzip2DirectoryDataSource.class,
                Set.of("foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar",
                    "foo.xz", "foo.txt.xz", "foo.iidm.xz", "foo.xiidm.xz", "foo.v3.iidm.xz", "foo.v3.xz", "foo_bar.iidm.xz", "foo_bar.xz",
                    "foo.zst", "foo.txt.zst", "foo.iidm.zst", "foo.xiidm.zst", "foo.v3.iidm.zst", "foo.v3.zst", "foo_bar.iidm.zst", "foo_bar.zst",
                    "foo.gz", "foo.txt.gz", "foo.iidm.gz", "foo.xiidm.gz", "foo.v3.iidm.gz", "foo.v3.gz", "foo_bar.iidm.gz", "foo_bar.gz"),
                Set.of("foo_bar.iidm", "foo_bar", "foo_bar.iidm.xz", "foo_bar.xz", "foo_bar.iidm.zst", "foo_bar.zst", "foo_bar.iidm.gz", "foo_bar.gz")),
            Arguments.of("foo", ".v3", CompressionFormat.BZIP2, Bzip2DirectoryDataSource.class,
                Set.of("foo", "foo.txt", "foo.iidm", "foo.xiidm", "foo.v3.iidm", "foo.v3", "foo_bar.iidm", "foo_bar",
                    "foo.xz", "foo.txt.xz", "foo.iidm.xz", "foo.xiidm.xz", "foo.v3.iidm.xz", "foo.v3.xz", "foo_bar.iidm.xz", "foo_bar.xz",
                    "foo.zst", "foo.txt.zst", "foo.iidm.zst", "foo.xiidm.zst", "foo.v3.iidm.zst", "foo.v3.zst", "foo_bar.iidm.zst", "foo_bar.zst",
                    "foo.gz", "foo.txt.gz", "foo.iidm.gz", "foo.xiidm.gz", "foo.v3.iidm.gz", "foo.v3.gz", "foo_bar.iidm.gz", "foo_bar.gz"),
                Set.of("foo_bar.iidm", "foo_bar", "foo_bar.iidm.xz", "foo_bar.xz", "foo_bar.iidm.zst", "foo_bar.zst", "foo_bar.iidm.gz", "foo_bar.gz"))
        );
    }
}
