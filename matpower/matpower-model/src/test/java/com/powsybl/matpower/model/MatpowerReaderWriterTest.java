/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.matpower.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.PowsyblException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import static com.powsybl.matpower.model.MatpowerReader.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.eu>}
 */
class MatpowerReaderWriterTest {

    private final ObjectWriter mapper = new ObjectMapper()
            .writerWithDefaultPrettyPrinter();

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
        MatpowerWriter.write(model, file, true);

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
    void testCase9DcLine() throws IOException {
        testMatpowerFile(MatpowerModelFactory.create9Dcline());
    }

    @Test
    void testInvalidModel() {
        MatpowerModel model = new MatpowerModel("not-valid");
        model.setVersion(MatpowerFormatVersion.V1);
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

    @Test
    void testRequiredColumns() {
        int generatorV2Columns = MatpowerFormatVersion.V2.getGeneratorColumns();
        assertEquals(MatpowerFormatVersion.V2,
                     MatpowerReader.checkNumberOfColumns(MATPOWER_BUSES_COLUMNS, generatorV2Columns, MATPOWER_BRANCHES_COLUMNS, MATPOWER_DCLINES_COLUMNS).generatorVersion());
        assertEquals(MatpowerFormatVersion.V1,
                MatpowerReader.checkNumberOfColumns(MATPOWER_BUSES_COLUMNS, MatpowerFormatVersion.V1.getGeneratorColumns(), MATPOWER_BRANCHES_COLUMNS, MATPOWER_DCLINES_COLUMNS).generatorVersion());
        assertEquals(MatpowerFormatVersion.V1,
                MatpowerReader.checkNumberOfColumns(MATPOWER_BUSES_COLUMNS, 12, MATPOWER_BRANCHES_COLUMNS, MATPOWER_DCLINES_COLUMNS).generatorVersion());
        assertEquals(MatpowerFormatVersion.V2,
                MatpowerReader.checkNumberOfColumns(MATPOWER_BUSES_COLUMNS, 23, MATPOWER_BRANCHES_COLUMNS, MATPOWER_DCLINES_COLUMNS).generatorVersion());
        var e = assertThrows(PowsyblException.class, () -> MatpowerReader.checkNumberOfColumns(MATPOWER_BUSES_COLUMNS, 5, MATPOWER_BRANCHES_COLUMNS, MATPOWER_DCLINES_COLUMNS));
        assertEquals("Unexpected number of columns for generators, expected at least 10 columns, but got 5", e.getMessage());
        e = assertThrows(PowsyblException.class, () -> MatpowerReader.checkNumberOfColumns(4, generatorV2Columns, MATPOWER_BRANCHES_COLUMNS, MATPOWER_DCLINES_COLUMNS));
        assertEquals("Unexpected number of columns for buses, expected at least 13 columns, but got 4", e.getMessage());
        e = assertThrows(PowsyblException.class, () -> MatpowerReader.checkNumberOfColumns(MATPOWER_BUSES_COLUMNS, generatorV2Columns, 7, MATPOWER_DCLINES_COLUMNS));
        assertEquals("Unexpected number of columns for branches, expected at least 13 columns, but got 7", e.getMessage());
        e = assertThrows(PowsyblException.class, () -> MatpowerReader.checkNumberOfColumns(MATPOWER_BUSES_COLUMNS, generatorV2Columns, MATPOWER_BRANCHES_COLUMNS, 13));
        assertEquals("Unexpected number of columns for DC lines, expected at least 17 columns, but got 13", e.getMessage());
    }
}
