/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.triplestore.test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.triplestore.api.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
class ExportTest {

    private final String networkId = "network-id";
    private final String cimNamespace = "http://iec.ch/TC57/2013/CIM-schema-cim16#";
    private final String baseNamespace = "http://" + networkId + "/#";
    private final String bvName = "380kV";
    private final double nominalVoltage = 380;
    private final String vl1Name = "S1 380kV";
    private final String vl2Name = "S2 380kV";
    private final String substation1Id = "af9a4ae3-ba2e-4c34-8e47-5af894ee20f4";
    private final String substation2Id = "d6056127-34f1-43a9-b029-23fddb913bd5";
    private final String query = "SELECT ?voltageLevel ?vlName ?substation ?baseVoltage ?bvName ?nominalVoltage" + System.lineSeparator() +
            "{" + System.lineSeparator() +
            "    ?voltageLevel" + System.lineSeparator() +
            "        a cim:VoltageLevel ;" + System.lineSeparator() +
            "        cim:IdentifiedObject.name ?vlName ;" + System.lineSeparator() +
            "        cim:VoltageLevel.Substation ?substation ;" + System.lineSeparator() +
            "        cim:VoltageLevel.BaseVoltage ?baseVoltage." + System.lineSeparator() +
            "    ?baseVoltage" + System.lineSeparator() +
            "        a cim:BaseVoltage ;" + System.lineSeparator() +
            "        cim:IdentifiedObject.name ?bvName ;" + System.lineSeparator() +
            "        cim:BaseVoltage.nominalVoltage ?nominalVoltage ." + System.lineSeparator() +
            "}";
    private final String exportFolderName = "/export";
    private FileSystem fileSystem;
    private Path exportFolder;

    @BeforeEach
    void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        exportFolder = Files.createDirectory(fileSystem.getPath(exportFolderName));
    }

    @AfterEach
    void tearDown() throws Exception {
        fileSystem.close();
    }

    private PropertyBag createBaseVoltageProperties() {
        PropertyBag baseVoltageProperties = new PropertyBag(Arrays.asList("IdentifiedObject.name", "nominalVoltage"), true);
        baseVoltageProperties.setClassPropertyNames(Arrays.asList("IdentifiedObject.name"));
        baseVoltageProperties.put("IdentifiedObject.name", bvName);
        baseVoltageProperties.put("nominalVoltage", Double.toString(nominalVoltage));
        return baseVoltageProperties;
    }

    private PropertyBag createVoltageLevelProperties(String baseVoltageId, String vlName, String substationId) {
        PropertyBag voltageLevelProperties = new PropertyBag(Arrays.asList("IdentifiedObject.name", "Substation", "BaseVoltage"), true);
        voltageLevelProperties.setResourceNames(Arrays.asList("Substation", "BaseVoltage"));
        voltageLevelProperties.setClassPropertyNames(Arrays.asList("IdentifiedObject.name"));
        voltageLevelProperties.put("IdentifiedObject.name", vlName);
        voltageLevelProperties.put("Substation", substationId);
        voltageLevelProperties.put("BaseVoltage", baseVoltageId);
        return voltageLevelProperties;
    }

    @Test
    void test() throws IOException {
        for (String implementation : TripleStoreFactory.allImplementations()) {
            // create export triple store
            TripleStore exportTripleStore = TripleStoreFactory.create(implementation);
            // add namespaces to triple store
            exportTripleStore.addNamespace("data", baseNamespace);
            exportTripleStore.addNamespace("cim", cimNamespace);
            // create context
            String contextName = networkId + "_" + "EQ" + "_" + implementation + ".xml";
            // add statements to triple stores
            // add base voltage statements
            String baseVoltageRdfId = exportTripleStore.add(contextName, cimNamespace, "BaseVoltage", createBaseVoltageProperties());
            assertEquals('_', baseVoltageRdfId.charAt(0));
            // add voltage levels statements
            PropertyBags voltageLevelsProperties = new PropertyBags();
            voltageLevelsProperties.add(createVoltageLevelProperties(baseVoltageRdfId, vl1Name, substation1Id));
            voltageLevelsProperties.add(createVoltageLevelProperties(baseVoltageRdfId, vl2Name, substation2Id));
            exportTripleStore.add(contextName, cimNamespace, "VoltageLevel", voltageLevelsProperties);
            String baseVoltageMasterResourceId = baseVoltageRdfId.substring(1);
            checkRepository(exportTripleStore, baseVoltageMasterResourceId);

            // export triple store
            DataSource dataSource = new FileDataSource(exportFolder, networkId + "_" + implementation);
            exportTripleStore.write(dataSource);

            // create import triple store
            TripleStore importTripleStore = TripleStoreFactory.create(implementation);
            // import data into triple store
            try (InputStream is = dataSource.newInputStream(contextName)) {
                importTripleStore.read(is, "http://" + networkId, contextName);
            }
            checkRepository(importTripleStore, baseVoltageMasterResourceId);
        }
    }

    private void checkRepository(TripleStore tripleStore, String baseVoltageMasterResourceId) {
        String baseVoltageRdfId = "_" + baseVoltageMasterResourceId;
        // check namespaces
        assertTrue(tripleStore.getNamespaces().contains(new PrefixNamespace("data", baseNamespace)));
        assertTrue(tripleStore.getNamespaces().contains(new PrefixNamespace("cim", cimNamespace)));
        // query import triple store
        tripleStore.defineQueryPrefix("cim", cimNamespace);
        PropertyBags results = tripleStore.query(query);
        // check query results
        assertEquals(2, results.size());
        results.forEach(result -> {
            assertTrue(Arrays.asList(vl1Name, vl2Name).contains(result.getId("vlName")));
            assertTrue(Arrays.asList(baseNamespace + substation1Id, baseNamespace + substation2Id).contains(result.get("substation")));
            assertTrue(Arrays.asList(substation1Id, substation2Id).contains(result.getId("substation")));
            assertEquals(baseNamespace + baseVoltageRdfId, result.get("baseVoltage"));
            assertEquals(baseVoltageMasterResourceId, result.getId("baseVoltage"));
            assertEquals(bvName, result.get("bvName"));
            assertEquals(nominalVoltage, result.asDouble("nominalVoltage"), 0);
        });
    }

}
