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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
class MatpowerReaderWriterTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private FileSystem fileSystem;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    private void testMatpowerFile(MatpowerModel model) throws IOException {
        // write the model to a bin file
        Path file = fileSystem.getPath("/work/newmodel.mat");
        MatpowerWriter.write(model, file);

        // read the bin model back
        MatpowerModel model2 = MatpowerReader.read(file, model.getCaseName());

        // compare the two models
        String json = mapper.writeValueAsString(model);
        String json2 = mapper.writeValueAsString(model2);
        assertEquals(json, json2);
    }

    @Test
    void testCase9() throws IOException {
        testMatpowerFile(MatpowerModelFactory.create9());
    }

    @Test
    void testCase14() throws IOException {
        testMatpowerFile(MatpowerModelFactory.create14());
    }

    @Test
    void testCase30() throws IOException {
        testMatpowerFile(MatpowerModelFactory.create30());
    }

    @Test
    void testCase57() throws IOException {
        testMatpowerFile(MatpowerModelFactory.create57());
    }

    @Test
    void testCase118() throws IOException {
        testMatpowerFile(MatpowerModelFactory.create118());
    }

    @Test
    void testCase300() throws IOException {
        testMatpowerFile(MatpowerModelFactory.create300());
    }

    @Test
    void testInvalidModel() throws IOException {
        MatpowerModel model = new MatpowerModel("not-valid");
        model.setVersion("1");
        assertThrows(IllegalStateException.class, () -> testMatpowerFile(model));
    }

    @Test
    void testNonValidBus1() {
        MBus bus = new MBus();
        assertThrows(IllegalArgumentException.class, () -> bus.setType(MBus.Type.fromInt(5)));
    }

    @Test
    void testNonValidBus2() {
        MBus bus = new MBus();
        assertThrows(IllegalArgumentException.class, () -> bus.setType(MBus.Type.fromInt(0)));
    }

}
