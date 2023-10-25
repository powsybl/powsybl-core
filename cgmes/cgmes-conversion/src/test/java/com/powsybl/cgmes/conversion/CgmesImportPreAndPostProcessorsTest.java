/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.GridModelReferenceResources;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.triplestore.api.TripleStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class CgmesImportPreAndPostProcessorsTest {

    class FakeCgmesImportPostProcessor implements CgmesImportPostProcessor {

        private final String name;

        FakeCgmesImportPostProcessor(String name) {
            this.name = Objects.requireNonNull(name);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void process(Network network, TripleStore tripleStore) {
            activatedPostProcessorNames.add(getName());
        }
    }

    class FakeCgmesImportPreProcessor implements CgmesImportPreProcessor {

        private final String name;

        FakeCgmesImportPreProcessor(String name) {
            this.name = Objects.requireNonNull(name);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void process(CgmesModel cgmes) {
            activatedPreProcessorNames.add(getName());
        }
    }

    private FileSystem fileSystem;

    private GridModelReferenceResources modelResources;

    private final List<String> activatedPreProcessorNames = new ArrayList<>();

    private final List<String> activatedPostProcessorNames = new ArrayList<>();

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        modelResources = CgmesConformity1Catalog.microGridBaseCaseBE();
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
        activatedPostProcessorNames.clear();
    }

    @Test
    void testParameters() {
        CgmesImport cgmesImport = new CgmesImport(Collections.emptyList(), List.of(new FakeCgmesImportPostProcessor("foo")));
        Parameter parameter = cgmesImport.getParameters().stream()
                .filter(p -> p.getName().equals("iidm.import.cgmes.post-processors"))
                .findFirst()
                .orElseThrow();
        assertEquals(List.of("foo"), parameter.getPossibleValues());
    }

    @Test
    void testEmpty() {
        CgmesImport cgmesImport = new CgmesImport(Collections.emptyList(), Collections.singletonList(new FakeCgmesImportPostProcessor("foo")));
        Properties properties = new Properties();
        cgmesImport.importData(modelResources.dataSource(), NetworkFactory.findDefault(), properties);
        assertTrue(activatedPostProcessorNames.isEmpty());
    }

    @Test
    void testList() {
        CgmesImport cgmesImport = new CgmesImport(Collections.emptyList(), Arrays.asList(new FakeCgmesImportPostProcessor("foo"),
                                                                                new FakeCgmesImportPostProcessor("bar"),
                                                                                new FakeCgmesImportPostProcessor("baz")));
        Properties properties = new Properties();
        properties.put(CgmesImport.POST_PROCESSORS, Arrays.asList("foo", "baz"));
        cgmesImport.importData(modelResources.dataSource(), NetworkFactory.findDefault(), properties);
        assertEquals(Arrays.asList("foo", "baz"), activatedPostProcessorNames);
    }

    @Test
    void testListPre() {
        CgmesImport cgmesImport = new CgmesImport(Arrays.asList(new FakeCgmesImportPreProcessor("foo"),
                new FakeCgmesImportPreProcessor("bar"),
                new FakeCgmesImportPreProcessor("baz")), Collections.emptyList());
        Properties properties = new Properties();
        properties.put(CgmesImport.PRE_PROCESSORS, Arrays.asList("foo", "baz"));
        cgmesImport.importData(modelResources.dataSource(), NetworkFactory.findDefault(), properties);
        assertEquals(Arrays.asList("foo", "baz"), activatedPreProcessorNames);
    }
}
