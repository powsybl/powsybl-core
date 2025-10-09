/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.dynamicsimulation.groovy;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.dynamicsimulation.DynamicModel;
import com.powsybl.dynamicsimulation.DynamicModelsSupplier;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class GroovyDynamicModelSupplierTest {

    private FileSystem fileSystem;

    @BeforeEach
    void setup() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());

        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/dynamicModels.groovy")), fileSystem.getPath("/dynamicModels.groovy"));
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();

        List<DynamicModelGroovyExtension> extensions = GroovyExtension.find(DynamicModelGroovyExtension.class, "dummy");
        assertEquals(1, extensions.size());
        assertNotNull(extensions.get(0));

        DynamicModelsSupplier supplier = new GroovyDynamicModelsSupplier(fileSystem.getPath("/dynamicModels.groovy"), extensions);
        testDynamicModels(supplier, network);
    }

    @Test
    void testWithInputStream() {
        Network network = EurostagTutorialExample1Factory.create();

        List<DynamicModelGroovyExtension> extensions = GroovyExtension.find(DynamicModelGroovyExtension.class, "dummy");
        assertEquals(1, extensions.size());
        assertNotNull(extensions.get(0));

        DynamicModelsSupplier supplier = new GroovyDynamicModelsSupplier(getClass().getResourceAsStream("/dynamicModels.groovy"), extensions);
        testDynamicModels(supplier, network);
    }

    void testDynamicModels(DynamicModelsSupplier supplier, Network network) {
        List<DynamicModel> dynamicModels = supplier.get(network);
        assertEquals(2, dynamicModels.size());

        assertInstanceOf(DummyDynamicModel.class, dynamicModels.get(0));
        DummyDynamicModel dynamicModel1 = (DummyDynamicModel) dynamicModels.get(0);
        assertEquals("id", dynamicModel1.getId());
        assertEquals("parameterSetId", dynamicModel1.getParameterSetId());

        assertInstanceOf(DummyDynamicModel.class, dynamicModels.get(1));
        DummyDynamicModel dynamicModel2 = (DummyDynamicModel) dynamicModels.get(1);
        assertEquals("LOAD", dynamicModel2.getId());
        assertEquals("LOAD", dynamicModel2.getParameterSetId());
    }
}
