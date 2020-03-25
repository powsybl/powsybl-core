/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class MatpowerReaderWriterTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private FileSystem fileSystem;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    private MatpowerModel readModelFromResources(String fileName) throws IOException {
        Objects.requireNonNull(fileName);
        MatpowerModel model;
        // read the source file in a model
        try (InputStream iStream = getClass().getResourceAsStream("/" + fileName)) {
            model = new MatpowerReader().read(iStream);
        }
        return model;
    }

    private void createMatCaseFile(MatpowerModel model, Path destMatFile) throws IOException {
        Objects.requireNonNull(model);
        Objects.requireNonNull(destMatFile);
        new MatpowerBinWriter(model).write(Files.newOutputStream(destMatFile));
    }

    private void testMatpowerFile(String fileName) throws IOException {
        MatpowerModel model = readModelFromResources(fileName);

        // write the model in a file
        Path file = fileSystem.getPath("/work/" + fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            new MatpowerWriter(model).write(writer);
        }

        // read the model
        MatpowerModel model2;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            model2 = new MatpowerReader().read(reader);
        }

        // compare the two models
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(model);
        String json2 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(model2);
        assertEquals(json, json2);
    }

    private void testMatpowerBinFile(String fileName) throws IOException {
        MatpowerModel model = readModelFromResources(fileName);

        // write the model in a bin file
        String fileBaseName = FilenameUtils.getBaseName(fileName);
        Path file = fileSystem.getPath("/work/" + fileBaseName + ".mat");
        createMatCaseFile(model, file);

        // read the bin model back
        MatpowerModel model2 = new MatpowerBinReader().read(Files.newInputStream(file), fileBaseName);

        // compare the two models
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(model);
        String json2 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(model2);
        assertEquals(json, json2);
    }

    @Test
    public void testCase9() throws IOException {
        testMatpowerFile("case9.m");
    }

    @Test
    public void testCase14() throws IOException {
        testMatpowerFile("case14.m");
    }

    @Test
    public void testCase30() throws IOException {
        testMatpowerFile("case30.m");
    }

    @Test
    public void testCase57() throws IOException {
        testMatpowerFile("case57.m");
    }

    @Test
    public void testCase118() throws IOException {
        testMatpowerFile("case118.m");
    }

    @Test
    public void testCase300() throws IOException {
        testMatpowerFile("case300.m");
    }

    @Test
    public void testBinCase9() throws IOException {
        testMatpowerBinFile("case9.m");
    }

    @Test
    public void testBinCase14() throws IOException {
        testMatpowerBinFile("case14.m");
    }

    @Test
    public void testBinCase30() throws IOException {
        testMatpowerBinFile("case30.m");
    }

    @Test
    public void testBinCase57() throws IOException {
        testMatpowerBinFile("case57.m");
    }

    @Test
    public void testBinCase118() throws IOException {
        testMatpowerBinFile("case118.m");
    }

    @Test
    public void testBinCase300() throws IOException {
        testMatpowerBinFile("case300.m");
    }
}
