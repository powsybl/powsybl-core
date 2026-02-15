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
}
