/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.model.test.TestGridModelResources;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.triplestore.api.TripleStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CgmesImportPostProcessorTest {

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
        public void process(Network network, TripleStore tripleStore, Profiling profiling) {
            activatedPostProcessorNames.add(getName());
        }
    }

    private FileSystem fileSystem;

    private TestGridModelResources modelResources;

    private InMemoryPlatformConfig platformConfig;

    private final List<String> activatedPostProcessorNames = new ArrayList<>();

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        modelResources = new CgmesConformity1Catalog().microGridBaseCaseBE();
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
        activatedPostProcessorNames.clear();
    }

    @Test
    public void testParameters() {
        CgmesImport cgmesImport = new CgmesImport(platformConfig, Collections.emptyList());
        assertTrue(cgmesImport.getParameters().stream().map(Parameter::getName).collect(Collectors.toSet()).contains("iidm.import.cgmes.post-processors"));
    }

    @Test
    public void testEmpty() {
        CgmesImport cgmesImport = new CgmesImport(platformConfig, Collections.singletonList(new FakeCgmesImportPostProcessor("foo")));
        Properties properties = new Properties();
        cgmesImport.importData(modelResources.dataSource(), properties);
        assertTrue(activatedPostProcessorNames.isEmpty());
    }

    @Test
    public void testList() {
        CgmesImport cgmesImport = new CgmesImport(platformConfig, Arrays.asList(new FakeCgmesImportPostProcessor("foo"),
                                                                                new FakeCgmesImportPostProcessor("bar"),
                                                                                new FakeCgmesImportPostProcessor("baz")));
        Properties properties = new Properties();
        properties.put(CgmesImport.POST_PROCESSORS, Arrays.asList("foo", "baz"));
        cgmesImport.importData(modelResources.dataSource(), properties);
        assertEquals(Arrays.asList("foo", "baz"), activatedPostProcessorNames);
    }
}
