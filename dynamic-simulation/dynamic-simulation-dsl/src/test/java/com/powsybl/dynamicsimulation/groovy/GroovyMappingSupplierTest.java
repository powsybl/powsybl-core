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
import com.powsybl.dynamicsimulation.DynamicModel;
import com.powsybl.dynamicsimulation.MappingSupplier;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class GroovyMappingSupplierTest {

    private FileSystem fileSystem;

    @Before
    public void setup() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());

        Files.copy(getClass().getResourceAsStream("/mapping.groovy"), fileSystem.getPath("/mapping.groovy"));
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();

        List<DynamicModelGroovyExtension> extensions = GroovyExtension.find(DynamicModelGroovyExtension.class, "dummy");
        assertEquals(1, extensions.size());
        assertTrue(extensions.get(0) instanceof DynamicModelGroovyExtension);

        MappingSupplier supplier = new GroovyMappingSupplier(fileSystem.getPath("/mapping.groovy"), extensions);

        List<DynamicModel> dynamicModels = supplier.get(network);
        assertEquals(2, dynamicModels.size());

        assertTrue(dynamicModels.get(0) instanceof DummyDynamicModel);
        DummyDynamicModel dynamicModel1 = (DummyDynamicModel) dynamicModels.get(0);
        assertEquals("id", dynamicModel1.getId());
        assertEquals("parameterSetId", dynamicModel1.getParameterSetId());
        assertEquals("parameterSetFile", dynamicModel1.getParameterSetFile());

        assertTrue(dynamicModels.get(1) instanceof DummyDynamicModel);
        DummyDynamicModel dynamicModel2 = (DummyDynamicModel) dynamicModels.get(1);
        assertEquals("LOAD", dynamicModel2.getId());
        assertEquals("LOAD", dynamicModel2.getParameterSetId());
        assertEquals("network.par", dynamicModel2.getParameterSetFile());
    }
}
