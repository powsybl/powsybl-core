/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.triplestore.test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DirectoryDataSource;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Objects identified by urn:uuid IRIs keep their identifiers when written,
 * so that references to them resolve after the written data is read again.
 *
 * @author Hantao Cui, Grid Inno LLC
 */
class UrnUuidExportTest {

    private static final String BASE = "http://urn-uuid";
    private static final String CONTEXT = "urn-uuid_EQ.xml";
    private static final String CIM_NAMESPACE = "http://iec.ch/TC57/CIM100#";
    private static final String NODE = "urn:uuid:5b8e1c3a-2d47-4f9b-8c61-7e0a9d3f2b14";
    private static final String TERMINAL = "urn:uuid:a3d9f6e2-81c4-4b5a-9e07-6c2f1b8d4e93";
    private static final String QUERY = String.join(System.lineSeparator(),
            "SELECT ?terminal ?node",
            "{",
            "    ?terminal a cim:Terminal ;",
            "        cim:Terminal.ConnectivityNode ?node .",
            "    ?node a cim:ConnectivityNode .",
            "}");

    private FileSystem fileSystem;
    private Path exportFolder;

    @BeforeEach
    void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        exportFolder = Files.createDirectory(fileSystem.getPath("/export"));
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void referencesResolveAfterWrite() throws IOException {
        for (String implementation : TripleStoreFactory.allImplementations()) {
            TripleStore source = TripleStoreFactory.create(implementation);
            try (InputStream is = getClass().getResourceAsStream("/urn-uuid/" + CONTEXT)) {
                source.read(is, BASE, CONTEXT);
            }
            DataSource dataSource = new DirectoryDataSource(exportFolder, implementation);
            source.write(dataSource);

            TripleStore written = TripleStoreFactory.create(implementation);
            try (InputStream is = dataSource.newInputStream(CONTEXT)) {
                written.read(is, BASE, CONTEXT);
            }
            written.defineQueryPrefix("cim", CIM_NAMESPACE);
            PropertyBags results = written.query(QUERY);
            assertEquals(1, results.size());
            assertEquals(TERMINAL, results.get(0).get("terminal"));
            assertEquals(NODE, results.get(0).get("node"));
        }
    }
}
