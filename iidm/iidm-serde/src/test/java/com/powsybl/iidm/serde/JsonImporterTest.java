/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DirectoryDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class JsonImporterTest extends AbstractIidmSerDeTest {

    private JsonImporter importer;

    @BeforeEach
    @Override
    public void setUp() throws IOException {
        super.setUp();  // initialise fileSystem
        importer = new JsonImporter();
    }

    private void createTempJson(String content, String basename) throws IOException {
        Path temp = tmpDir.resolve(basename + ".json");
        Files.writeString(temp, content);
    }

    private static String unsupportedIidmVersion() {
        String[] currentIidmVersionSplit = CURRENT_IIDM_VERSION.toString().split("_");
        int supportedIidmVersionMajor = Integer.parseInt(currentIidmVersionSplit[1]);
        int unsupportedIidmVersionMinor = Integer.parseInt(currentIidmVersionSplit[2]) + 1;
        return String.format("%d.%d", supportedIidmVersionMajor, unsupportedIidmVersionMinor);
    }

    @Test
    void testValidVersion() throws Exception {
        String json = "{ \"version\": \"1.15\" }";
        String basename = "supportedVersion";
        createTempJson(json, basename);
        DataSource dataSource = new DirectoryDataSource(tmpDir, basename, "json", null);
        assertThatCode(() -> importer.exists(dataSource)).doesNotThrowAnyException();
        assertThat(importer.exists(dataSource)).isTrue();
    }

    @Test
    void testUnsupportedVersion() throws Exception {
        String json = String.format("{ \"version\": \"%s\" }", unsupportedIidmVersion());
        String basename = "unsupportedVersion";
        createTempJson(json, basename);
        DataSource dataSource = new DirectoryDataSource(tmpDir, basename, "json", null);
        assertThatCode(() -> importer.exists(dataSource)).doesNotThrowAnyException();
        assertThat(importer.exists(dataSource)).isFalse();
    }

    @Test
    void testEmptyVersion() throws Exception {
        String json = String.format("{ \"version\": \"%s\" }", "");
        String basename = "unsupportedVersion";
        createTempJson(json, basename);
        DataSource dataSource = new DirectoryDataSource(tmpDir, basename, "json", null);
        assertDoesNotThrow(() -> importer.exists(dataSource));
        assertThatCode(() -> importer.exists(dataSource)).doesNotThrowAnyException();
        assertThat(importer.exists(dataSource)).isFalse();
    }

    @Test
    void testImportDataWhenValidVersion() throws IOException {
        writeNetwork("/test1.json", "1.15");
        ReadOnlyDataSource dataSource = new DirectoryDataSource(fileSystem.getPath("/"), "test1");
        NetworkFactory networkFactory = NetworkFactory.findDefault();
        assertThatCode(() -> importer.importData(dataSource, networkFactory, null))
                .doesNotThrowAnyException();
    }

    @Test
    void testImportDataWhenUnsupportedVersion() throws IOException {
        writeNetwork("/test1.json", unsupportedIidmVersion());
        ReadOnlyDataSource dataSource = new DirectoryDataSource(fileSystem.getPath("/"), "test1");
        String expectedError = String.format("IIDM Version %s is not supported. Max supported version: %s",
                unsupportedIidmVersion(), CURRENT_IIDM_VERSION.toString("."));
        NetworkFactory networkFactory = NetworkFactory.findDefault();
        assertThatCode(() -> importer.importData(dataSource, networkFactory, null))
                .isInstanceOf(PowsyblException.class)
                .hasMessage(expectedError);
    }

    @Test
    void testMetaInfos() {
        assertEquals("JIIDM", importer.getFormat());
        assertEquals("IIDM JSON v " + CURRENT_IIDM_VERSION.toString(".") + " importer", importer.getComment());
        assertEquals(List.of("jiidm", "json"), importer.getSupportedExtensions());
        assertEquals(8, importer.getParameters().size());
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
