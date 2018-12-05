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
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import javax.xml.transform.Source;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
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

    protected static void compareXml(InputStream expected, InputStream actual) {
        Source control = Input.fromStream(expected).build();
        Source test = Input.fromStream(actual).build();
        Diff myDiff = DiffBuilder.compare(control).withTest(test).ignoreWhitespace().ignoreComments().build();
        boolean hasDiff = myDiff.hasDifferences();
        if (hasDiff) {
            System.err.println(myDiff.toString());
        }
        assertFalse(hasDiff);
    }

    private static String normalizeLineSeparator(String str) {
        return str.replace("\r\n", "\n")
                .replace("\r", "\n");
    }

    protected static void compareTxt(InputStream expected, InputStream actual) {
        try {
            String expectedStr = normalizeLineSeparator(new String(ByteStreams.toByteArray(expected), StandardCharsets.UTF_8));
            String actualStr = normalizeLineSeparator(new String(ByteStreams.toByteArray(actual), StandardCharsets.UTF_8));
            assertEquals(expectedStr, actualStr);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected <T> T roundTripXmlTest(T data, BiConsumer<T, Path> out, Function<Path, T> in, String ref) throws IOException {
        return roundTripTest(data, out, in, AbstractConverterTest::compareXml, ref);
    }

    protected <T> T roundTripTest(T data, BiConsumer<T, Path> out, Function<Path, T> in, String ref) throws IOException {
        return roundTripTest(data, out, in, AbstractConverterTest::compareTxt, ref);
    }

    protected <T> Path writeXmlTest(T data, BiConsumer<T, Path> out, String ref) throws IOException {
        return writeTest(data, out, AbstractConverterTest::compareXml, ref);
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
