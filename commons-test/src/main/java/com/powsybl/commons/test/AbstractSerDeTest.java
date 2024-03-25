/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    /**
     * XML roundtrip test on the given data. The following steps occur:
     * <ul>
     *     <li>Transform the given data with the transformer function provided. This might be used to add a roundtrip on
     *     other formats prior to the XML roundtrip.</li>
     *     <li>Write the transformed data to a temporary XML file thanks to the given write function.</li>
     *     <li>Compare the obtained XML file with the given reference file thanks to {@link ComparisonUtils#compareXml}.</li>
     *     <li>Read the same XML file with the given read function.</li>
     *     <li>Write the data read in the previous step to a new temporary XML file.</li>
     *     <li>Compare the new obtained XML file with the first obtained XML file byte by byte using {@link Files#mismatch}.</li>
     *     <li>Return the data read 3 steps above.</li>
     * </ul>
     */
    protected <T> T roundTripXmlTest(T data, BiFunction<T, Path, T> transformer, BiConsumer<T, Path> write, Function<Path, T> read, String ref) throws IOException {
        return roundTripTest(data, transformer, write, read, ComparisonUtils::compareXml, ref);
    }

    /**
     * Roundtrip test on the given data. The following steps occur:
     * <ul>
     *     <li>Write the given data to a temporary file thanks to the given write function.</li>
     *     <li>Compare the obtained file with the given reference file thanks to {@link ComparisonUtils#compareTxt}.</li>
     *     <li>Read the same file with the given read function.</li>
     *     <li>Write the data read in the previous step to a new temporary file.</li>
     *     <li>Compare the new obtained file with the first obtained file byte by byte using {@link Files#mismatch}.</li>
     *     <li>Return the data read 3 steps above.</li>
     * </ul>
     */
    protected <T> T roundTripTest(T data, BiConsumer<T, Path> write, Function<Path, T> read, String ref) throws IOException {
        return roundTripTest(data, write, read, ComparisonUtils::compareTxt, ref);
    }

    /**
     * Write the given data with given write function, and compare the resulting XML file to given reference.
     * @return the path of written file.
     */
    protected <T> Path writeXmlTest(T data, BiConsumer<T, Path> write, String ref) throws IOException {
        return writeTest(data, write, ComparisonUtils::compareXml, ref);
    }

    /**
     * Write the given data with given write function, and compare the resulting file to given reference with the
     * comparison method provided.
     * @return the path of written XML file.
     */
    protected <T> Path writeTest(T data, BiConsumer<T, Path> write, BiConsumer<InputStream, InputStream> compare, String ref) throws IOException {
        Path file = tmpDir.resolve("data");
        write.accept(data, file);
        try (InputStream is = Files.newInputStream(file)) {
            compare.accept(getClass().getResourceAsStream(ref), is);
        }
        return file;
    }

    protected <T> T roundTripTest(T data, BiConsumer<T, Path> write, Function<Path, T> read, BiConsumer<InputStream, InputStream> compare, String ref) throws IOException {
        return roundTripTest(data, (t, p) -> t, write, read, compare, ref);
    }

    protected <T> T roundTripTest(T data, BiFunction<T, Path, T> transformer, BiConsumer<T, Path> write, Function<Path, T> read, BiConsumer<InputStream, InputStream> compare, String ref) throws IOException {
        // Transform the data (used for cascading round trips)
        Path transformFile = tmpDir.resolve("data");
        T transformedData = transformer.apply(data, transformFile);

        // Export the data and check the result with the reference
        Path file1 = writeTest(transformedData, write, compare, ref);

        // Read the exported data
        T data2 = read.apply(file1);

        // Export the retrieved data and check the result with first written file
        // This is done to avoid the complexity of the DiffBuilder
        Path file2 = tmpDir.resolve("data2");
        write.accept(data, file2);
        assertEquals(-1, Files.mismatch(file1, file2),
                "File A written after transforming given data differs from " +
                        "file B obtained after reading A and writing the resulting data");

        return data2;
    }

}
