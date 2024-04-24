/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.util;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.powsybl.commons.test.ComparisonUtils.compareTxt;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class StringToIntMapperTest {

    private enum TestSubset implements IntCounter {

        TYPE(1);

        private final int initialValue;

        TestSubset(int initialValue) {
            this.initialValue = initialValue;
        }

        @Override
        public int getInitialValue() {
            return initialValue;
        }
    }

    @Test
    void test() throws IOException {
        StringToIntMapper<TestSubset> mapper = new StringToIntMapper<>(TestSubset.class);
        testAddMapping(mapper);
        mapper.reset(TestSubset.TYPE);
        testAddMapping(mapper);
        try {
            mapper.reset(null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    void testDump() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path testDir = fileSystem.getPath("/tmp");
            Files.createDirectories(testDir);
            Path outputFile = testDir.resolve("output.txt");

            StringToIntMapper<TestSubset> mapper = new StringToIntMapper<>(TestSubset.class);
            String value = "value1";
            mapper.newInt(TestSubset.TYPE, value);
            mapper.dump(outputFile);

            String expectedStr = "TYPE;value1;1" + System.lineSeparator();
            try (InputStream in = Files.newInputStream(outputFile)) {
                compareTxt(expectedStr, in);
            } catch (Exception e) {
                fail(e);
            }
        }
    }

    @Test
    void testLoad() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path testDir = fileSystem.getPath("/tmp");
            Files.createDirectories(testDir);
            Path outputFile = testDir.resolve("output.txt");
            try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
                writer.write("TYPE;value1;1" + System.lineSeparator());
            }

            StringToIntMapper<TestSubset> mapper = new StringToIntMapper<>(TestSubset.class);
            mapper.load(outputFile);

            assertEquals(1, mapper.getInt(TestSubset.TYPE, "value1"));
        }
    }

    @Test
    void testLoadException() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path testDir = fileSystem.getPath("/tmp");
            Files.createDirectories(testDir);
            Path outputFile = testDir.resolve("output.txt");
            try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
                writer.write("TYPE;value1;1;5" + System.lineSeparator());
            }

            StringToIntMapper<TestSubset> mapper = new StringToIntMapper<>(TestSubset.class);
            PowsyblException exception = assertThrows(PowsyblException.class, () -> mapper.load(outputFile));
            assertEquals("Bad format: TYPE;value1;1;5", exception.getMessage());
        }
    }

    private void testAddMapping(StringToIntMapper<TestSubset> mapper) throws IOException {
        String value = "value1";
        assertFalse(mapper.isMapped(TestSubset.TYPE, value));
        try {
            mapper.getId(TestSubset.TYPE, 1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            mapper.getInt(TestSubset.TYPE, value);
            fail();
        } catch (IllegalStateException ignored) {
        }

        int num = mapper.newInt(TestSubset.TYPE, value);
        assertTrue(mapper.isMapped(TestSubset.TYPE, value));
        assertEquals(1, num);
        assertEquals(value, mapper.getId(TestSubset.TYPE, 1));
        assertEquals(1, mapper.getInt(TestSubset.TYPE, value));

        value = "value2";
        num = mapper.newInt(TestSubset.TYPE, value);
        assertTrue(mapper.isMapped(TestSubset.TYPE, value));
        assertEquals(2, num);
        assertEquals(value, mapper.getId(TestSubset.TYPE, 2));
        assertEquals(2, mapper.getInt(TestSubset.TYPE, value));

        String content = String.join(System.lineSeparator(), "TYPE;value1;1", "TYPE;value2;2");
        try (Writer writer = new StringWriter()) {
            mapper.dump(writer);
            assertEquals(content, writer.toString().trim());
        }

        try {
            mapper.newInt(null, value);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            mapper.newInt(TestSubset.TYPE, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            mapper.getId(null, 1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            mapper.getId(TestSubset.TYPE, 0);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            mapper.getId(TestSubset.TYPE, 3);
            fail();
        } catch (IllegalArgumentException ignored) {
        }

        try {
            mapper.getInt(null, value);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            mapper.getInt(TestSubset.TYPE, null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

}
