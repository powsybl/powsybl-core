/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class IeeeCdfReaderWriterTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private FileSystem fileSystem;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    private void testIeeeFile(String fileName) throws IOException {
        IeeeCdfModel model;
        // read
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/" + fileName)))) {
            model = new IeeeCdfReader().read(reader);
        }

        // write
        Path file = fileSystem.getPath("/work/" + fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            new IeeeCdfWriter(model).write(writer);
        }

        // read
        IeeeCdfModel model2;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            model2 = new IeeeCdfReader().read(reader);
        }

        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(model);
        String json2 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(model2);
        assertEquals(json, json2);
    }

    @Test
    void testIeee9() throws IOException {
        testIeeeFile("ieee9cdf.txt");
    }

    @Test
    void testIeee14() throws IOException {
        testIeeeFile("ieee14cdf.txt");
    }

    @Test
    void testIeee30() throws IOException {
        testIeeeFile("ieee30cdf.txt");
    }

    @Test
    void testIeee57() throws IOException {
        testIeeeFile("ieee57cdf.txt");
    }

    @Test
    void testIeee118() throws IOException {
        testIeeeFile("ieee118cdf.txt");
    }

    @Test
    void testIeee300() throws IOException {
        testIeeeFile("ieee300cdf.txt");
    }

    @Test
    void testIeee9zeroimpedance() throws IOException {
        testIeeeFile("ieee9zeroimpedancecdf.txt");
    }

    @Test
    void testTieLine() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/tieline.txt")))) {
            IeeeCdfModel model = new IeeeCdfReader().read(reader);
            assertEquals(1, model.getTieLines().size());
            IeeeCdfTieLine tieLine = model.getTieLines().get(0);
            assertEquals(1, tieLine.getMeteredBusNumber());
            assertEquals(2, tieLine.getMeteredAreaNumber());
            assertEquals(3, tieLine.getNonMeteredBusNumber());
            assertEquals(4, tieLine.getNonMeteredAreaNumber());
            assertEquals(5, tieLine.getCircuitNumber());
        }
    }

    @Test
    void testInvalidFile() throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(""))) {
            IeeeCdfReader r = new IeeeCdfReader();
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> r.read(reader));
            assertEquals("Failed to parse the IeeeCdfModel", e.getMessage());
        }
    }
}
