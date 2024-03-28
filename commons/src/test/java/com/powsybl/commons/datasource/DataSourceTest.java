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

import static com.powsybl.commons.datasource.DataSourceUtil.createNewDataSource;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class DataSourceTest {

    private FileSystem fileSystem;
    private Path directory;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        directory = fileSystem.getPath("/test/");
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    private static Stream<Arguments> provideArgumentsForTestClass() {
        return Stream.of(
            Arguments.of("foo.xml", DirectoryDataSource.class),
            Arguments.of("foo.xml.gz", GzDataSource.class),
            Arguments.of("foo.iidm.bz2", Bzip2DataSource.class)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestClass")
    void testClass(String fileName, Class<? extends NewDataSource> dataSourceClass) throws IOException {
        // Create the fake file
        Files.createFile(fileSystem.getPath(directory + fileName));

        // Create the datasource based on the file
        NewDataSource dataSource = createNewDataSource(directory, fileName);

        // Assert the class of the datasource
        assertInstanceOf(dataSourceClass, dataSource);
    }

//    @Test
//    void newTest() throws IOException {
//        Bzip2DataSource dataSource = (Bzip2DataSource) new DataSourceBuilder()
//            .withDirectory(Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("test/foo.txt")).getPath()).getParent())
//            .withSourceFormat(".iidm")
//            .withBaseName("foo")
//            .withCompressionFormat(CompressionFormat.BZIP2)
//            .build();
//
//        // write file
//        try (OutputStream os = dataSource.newOutputStream("test", "iidm", false)) {
//            os.write("line1".getBytes(StandardCharsets.UTF_8));
//        } catch (IOException e) {
//            fail();
//        }
//
//        for (String name : dataSource.listNames(".*")) {
//            assertTrue(dataSource.exists(name));
//        }
//    }
}
