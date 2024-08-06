/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class DataSourceTest {

    private FileSystem fileSystem;
    private Path directory;

    @BeforeEach
    void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        directory = fileSystem.getPath("/test/");
        Files.createDirectories(directory);
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    private static Stream<Arguments> provideArgumentsForTestClass() {
        return Stream.of(
            Arguments.of("foo.xml", DirectoryDataSource.class),
            Arguments.of("foo.xml.gz", GzDirectoryDataSource.class),
            Arguments.of("foo.iidm.bz2", Bzip2DirectoryDataSource.class)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestClass")
    void testClass(String fileName, Class<? extends DataSource> dataSourceClass) throws IOException {
        // File path
        Path filePath = directory.resolve(fileName);

        // Assert exception when the file does not exist
        PowsyblException exception = assertThrows(PowsyblException.class, () -> DataSource.fromPath(filePath));
        assertEquals("File " + filePath + " does not exist or is not a regular file", exception.getMessage());

        // Create the fake file
        Files.createFile(filePath);

        // Create the datasource based on the file
        DataSource dataSource = DataSource.fromPath(filePath);

        // Assert the class of the datasource
        assertInstanceOf(dataSourceClass, dataSource);
    }
}
