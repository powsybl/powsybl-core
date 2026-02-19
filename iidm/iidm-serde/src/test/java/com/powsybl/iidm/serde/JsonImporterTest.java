/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DirectoryDataSource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.NetworkFactory;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonImporterTest extends AbstractIidmSerDeTest {

    private JsonImporter importer;

    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();  // initialise fileSystem
    }

    private void createTempJson(String content, String basename) throws IOException {
        Path temp = tmpDir.resolve(basename + ".json");
        Files.writeString(temp, content);
    }

    @Test
    void testValidJsonVersionSupported() throws Exception {
        String json = "{ \"version\": \"1.15\" }";
        String basename = "supportedVersion";
        createTempJson(json, basename);
        importer = new JsonImporter();
        DataSource dataSource = new DirectoryDataSource(tmpDir, basename, "json", null);
        assertDoesNotThrow(() -> importer.exists(dataSource));
    }

    @Test
    void testInvalidVersion() throws Exception {
        String json = "{ \"version\": \"99.0\" }";
        String basename = "unsupportedVersion";
        createTempJson(json, basename);

        importer = new JsonImporter();
        DataSource dataSource = new DirectoryDataSource(tmpDir, basename, "json", null);

        String expectedError = "IIDM Version 99.0 is not supported. Max supported version: "
                + CURRENT_IIDM_VERSION.toString(".");

        PowsyblException exception = assertThrows(PowsyblException.class, () -> importer.exists(dataSource));
        assertEquals(expectedError, exception.getMessage());
    }

    @Test
    void testMetaInfos() {
        var importer = new JsonImporter();
        assertEquals("JIIDM", importer.getFormat());
        assertEquals("IIDM JSON v " + CURRENT_IIDM_VERSION.toString(".") + " importer", importer.getComment());
        assertEquals(List.of("jiidm", "json"), importer.getSupportedExtensions());
        assertEquals(6, importer.getParameters().size());
    }

    @Test
    void testUnsupportedIidmVersion() throws IOException {
        writeNetwork("/test1.json", "1.16");
        importer = new JsonImporter();
        ReadOnlyDataSource dataSource = new DirectoryDataSource(fileSystem.getPath("/"), "test1");
        assertThatCode(() -> importer.importData(dataSource, NetworkFactory.findDefault(), null))
                .isInstanceOf(PowsyblException.class)
                .hasMessage("IIDM Version 1.16 is not supported. Max supported version: 1.15");
    }

    private void writeNetwork(String fileName, String version) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        ObjectNode root = mapper.createObjectNode();
        root.put("version", version);
        root.put("id", "shuntTestCase");
        root.put("caseDate", "2019-09-30T16:29:18.263+02:00");
        root.put("forecastDistance", 0);
        root.put("sourceFormat", "test");
        root.put("minimumValidationLevel", "STEADY_STATE_HYPOTHESIS");
        try (BufferedWriter writer = Files.newBufferedWriter(fileSystem.getPath(fileName), StandardCharsets.UTF_8)) {
            mapper.writeValue(writer, root);
        }
    }

}
