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
import com.powsybl.matpower.model.io.MReader;
import com.powsybl.matpower.model.io.MWriter;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
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
            model = MReader.read(iStream);
        }
        return model;
    }

    private void testMFile(String fileName) throws IOException {
        MatpowerModel model = readModelFromResources(fileName);

        // write the model in a file
        Path file = fileSystem.getPath("/work/" + fileName);
        MWriter.write(model, file);

        // read the model
        MatpowerModel model2 = MReader.read(file);

        // compare the two models
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(model);
        String json2 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(model2);
        assertEquals(json, json2);
    }

    private void testMatpowerFile(String fileName) throws IOException {
        MatpowerModel model = readModelFromResources(fileName);

        // write the model in a bin file
        String fileBaseName = FilenameUtils.getBaseName(fileName);
        Path file = fileSystem.getPath("/work/" + fileBaseName + ".mat");
        MatpowerWriter.write(model, file);

        // read the bin model back
        MatpowerModel model2 = MatpowerReader.read(file, fileBaseName);

        // compare the two models
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(model);
        String json2 = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(model2);
        assertEquals(json, json2);
    }

    @Test
    public void testMCase9() throws IOException {
        testMFile("case9.m");
    }

    @Test
    public void testMCase14() throws IOException {
        testMFile("case14.m");
    }

    @Test
    public void testMCase30() throws IOException {
        testMFile("case30.m");
    }

    @Test
    public void testMCase57() throws IOException {
        testMFile("case57.m");
    }

    @Test
    public void testMCase118() throws IOException {
        testMFile("case118.m");
    }

    @Test
    public void testMCase300() throws IOException {
        testMFile("case300.m");
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
}
