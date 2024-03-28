/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import org.junit.jupiter.params.provider.Arguments;

import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public class ZstdDataSourceTest extends DirectoryDataSourceTest {

    static Stream<Arguments> provideArgumentsForWriteThenReadTest() {
        return Stream.of(
            Arguments.of("foo", ".iidm", null, CompressionFormat.ZSTD),
            Arguments.of("foo", "", null, CompressionFormat.ZSTD),
            Arguments.of("foo", ".v3", null, CompressionFormat.ZSTD)
        );
    }

    static Stream<Arguments> provideArgumentsForClassAndListingTest() {
        return Stream.of(
            Arguments.of("foo", ".iidm", null, CompressionFormat.ZSTD, ZstdDataSource.class,
                Set.of("foo.iidm", "foo_bar.iidm", "foo.v3.iidm"),
                Set.of("foo_bar.iidm")),
            Arguments.of("foo", "", null, CompressionFormat.ZSTD, ZstdDataSource.class,
                Set.of("foo.txt", "foo.iidm", "foo.xiidm", "foo", "foo_bar.iidm", "foo.v3.iidm", "foo.v3", "foo_bar"),
                Set.of("foo_bar.iidm", "foo_bar")),
            Arguments.of("foo", ".v3", null, CompressionFormat.ZSTD, ZstdDataSource.class,
                Set.of("foo.v3"),
                Set.of())
        );
    }
}
