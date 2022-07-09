/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons;

import com.google.common.io.ByteStreams;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractConverterTest {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    protected FileSystem fileSystem;
    protected Path tmpDir;

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        tmpDir = Files.createDirectory(fileSystem.getPath("tmp"));
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }
    protected static void compareTxt(InputStream expected, String actual) {
        try {
            String expectedStr = TestUtil.normalizeLineSeparator(new String(ByteStreams.toByteArray(expected), StandardCharsets.UTF_8));
            String actualStr = TestUtil.normalizeLineSeparator(actual);
            assertEquals(expectedStr, actualStr);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
        Path xmlFile = writeTest(data, out, compare, ref);
        T data2 = in.apply(xmlFile);
        Path xmlFile2 = tmpDir.resolve("data2");
        out.accept(data2, xmlFile2);
        try (InputStream is = Files.newInputStream(xmlFile2)) {
            compare.accept(getClass().getResourceAsStream(ref), is);
        }
        return data2;
    }

}
