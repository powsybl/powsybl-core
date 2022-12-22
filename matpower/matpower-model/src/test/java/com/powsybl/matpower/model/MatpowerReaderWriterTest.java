/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.matpower.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class MatpowerReaderWriterTest {

    private final ObjectWriter mapper = new ObjectMapper()
            .writerWithDefaultPrettyPrinter();

    private FileSystem fileSystem;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
    }

    @After
    public void tearDown() throws Exception {
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
    public void testCase9() throws IOException {
        testMatpowerFile(MatpowerModelFactory.create9());
    }

    @Test
    public void testCase14() throws IOException {
        testMatpowerFile(MatpowerModelFactory.create14());
    }

    @Test
    public void testCase30() throws IOException {
        testMatpowerFile(MatpowerModelFactory.create30());
    }

    @Test
    public void testCase57() throws IOException {
        testMatpowerFile(MatpowerModelFactory.create57());
    }

    @Test
    public void testCase118() throws IOException {
        testMatpowerFile(MatpowerModelFactory.create118());
    }

    @Test
    public void testCase300() throws IOException {
        testMatpowerFile(MatpowerModelFactory.create300());
    }

    @Test
    public void testCase9DcLine() throws IOException {
        testMatpowerFile(MatpowerModelFactory.create9Dcline());
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidModel() throws IOException {
        MatpowerModel model = new MatpowerModel("not-valid");
        model.setVersion("1");
        testMatpowerFile(model);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonValidBus1() {
        MBus bus = new MBus();
        bus.setType(MBus.Type.fromInt(5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonValidBus2() {
        MBus bus = new MBus();
        bus.setType(MBus.Type.fromInt(0));
    }

}
