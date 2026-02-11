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

class JsonImporterTest extends AbstractIidmSerDeTest {

    private JsonImporter importer;

    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();  // initialise fileSystem
    }

    private static String unsupportedIidmVersion() {
        String[] currentIidmVersionSplit = CURRENT_IIDM_VERSION.toString().split("_");
        int unsupportedIidmVersionMajor = Integer.parseInt(currentIidmVersionSplit[1]);
        int unsupportedIidmVersionMinor = Integer.parseInt(currentIidmVersionSplit[2]) + 1;
        return String.format("%d.%d", unsupportedIidmVersionMajor, unsupportedIidmVersionMinor);
    }

    private Path createTempJson(String content) throws IOException {
        Path temp = Files.createTempFile("test-json-network", ".json");
        Files.writeString(temp, content);
        return temp;
    }

    @Test
    void testValidJsonVersionSupported() throws Exception {
        String json = "{ \"version\": \"1.15\" }";
        Path file = createTempJson(json);
        importer = new JsonImporter();
        DataSource dataSource = new DirectoryDataSource(tmpDir, file.getFileName().toString());
        assertDoesNotThrow(() -> importer.exists(dataSource));
    }

    @Test
    void testInvalidVersion() throws Exception {
        String json = "{ \"version\": \"99.0\" }";
        Path file = createTempJson(json);

        importer = new JsonImporter();
        DataSource dataSource = new DirectoryDataSource(fileSystem.getPath("/"), "test-json-network.json");

        String expectedError = "IIDM Version " + unsupportedIidmVersion()
                + " is not supported. Max supported version: "
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
}
