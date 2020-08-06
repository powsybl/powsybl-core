/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.dynamicsimulation.groovy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.dynamicsimulation.DynamicEventModel;
import com.powsybl.dynamicsimulation.DynamicEventModelsSupplier;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class GroovyDynamicEventModelSupplierTest {

    private FileSystem fileSystem;

    @Before
    public void setup() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());

        Files.copy(getClass().getResourceAsStream("/dynamicEventModels.groovy"), fileSystem.getPath("/dynamicEventModels.groovy"));
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();

        List<DynamicEventModelGroovyExtension> extensions = GroovyExtension.find(DynamicEventModelGroovyExtension.class, "dummy");
        assertEquals(1, extensions.size());
        assertTrue(extensions.get(0) instanceof DynamicEventModelGroovyExtension);

        DynamicEventModelsSupplier supplier = new GroovyDynamicEventModelsSupplier(fileSystem.getPath("/dynamicEventModels.groovy"), extensions);

        List<DynamicEventModel> dynamicEventModels = supplier.get(network);
        assertEquals(2, dynamicEventModels.size());

        assertTrue(dynamicEventModels.get(0) instanceof DummyDynamicEventModel);
        DummyDynamicEventModel dynamicModel1 = (DummyDynamicEventModel) dynamicEventModels.get(0);
        assertEquals("NHV1_NHV2_1", dynamicModel1.getId());

        assertTrue(dynamicEventModels.get(1) instanceof DummyDynamicEventModel);
        DummyDynamicEventModel dynamicModel2 = (DummyDynamicEventModel) dynamicEventModels.get(1);
        assertEquals("NHV1_NHV2_2", dynamicModel2.getId());
    }
}
