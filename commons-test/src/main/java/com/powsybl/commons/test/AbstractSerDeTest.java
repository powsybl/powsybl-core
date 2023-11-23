/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Function;
/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */

public abstract class AbstractSerDeTest {

    protected FileSystem fileSystem;
    protected Path tmpDir;

    @BeforeEach
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tmpDir = Files.createDirectory(fileSystem.getPath("tmp"));
    }

    @AfterEach
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    protected <T> T roundTripXmlTest(T data, BiConsumer<T, Path> out, Function<Path, T> in, String ref) throws IOException {
        return roundTripTest(data, out, in, ComparisonUtils::compareXml, ref);
    }

    protected <T> T roundTripTest(T data, BiConsumer<T, Path> out, Function<Path, T> in, String ref) throws IOException {
        return roundTripTest(data, out, in, ComparisonUtils::compareTxt, ref);
    }

    protected <T> Path writeXmlTest(T data, BiConsumer<T, Path> out, String ref) throws IOException {
        return writeTest(data, out, ComparisonUtils::compareXml, ref);
    }

    protected <T> Path writeTest(T data, BiConsumer<T, Path> out, BiConsumer<InputStream, InputStream> compare, String ref) throws IOException {
        Path xmlFile = tmpDir.resolve("data");
        out.accept(data, xmlFile);
        try (InputStream is = Files.newInputStream(xmlFile)) {
            compare.accept(getClass().getResourceAsStream(ref), is);
        }
        return xmlFile;
    }

    protected <T> T roundTripTest(T data, BiConsumer<T, Path> out, Function<Path, T> in, BiConsumer<InputStream, InputStream> compare, String ref) throws IOException {
        // Export the data and check the result with the reference
        Path xmlFile = writeTest(data, out, compare, ref);
        try (InputStream is1 = Files.newInputStream(xmlFile)) {
            compare.accept(getClass().getResourceAsStream(ref), is1);
        }
        // Read the exported data, export the retrieved data and check the result with the reference
        T data2 = in.apply(xmlFile);
        Path xmlFile2 = tmpDir.resolve("data2");
        out.accept(data2, xmlFile2);
        try (InputStream is2 = Files.newInputStream(xmlFile2)) {
            compare.accept(getClass().getResourceAsStream(ref), is2);
        }
        return data2;
    }

}
