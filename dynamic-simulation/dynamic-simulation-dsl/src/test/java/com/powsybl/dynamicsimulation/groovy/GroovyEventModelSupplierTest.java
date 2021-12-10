/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.dynamicsimulation.groovy;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.dynamicsimulation.EventModel;
import com.powsybl.dynamicsimulation.EventModelsSupplier;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class GroovyEventModelSupplierTest {

    private FileSystem fileSystem;

    @Before
    public void setup() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());

        Files.copy(getClass().getResourceAsStream("/eventModels.groovy"), fileSystem.getPath("/eventModels.groovy"));
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();

        List<EventModelGroovyExtension> extensions = GroovyExtension.find(EventModelGroovyExtension.class, "dummy");
        assertEquals(1, extensions.size());
        assertTrue(extensions.get(0) instanceof EventModelGroovyExtension);

        EventModelsSupplier supplier = new GroovyEventModelsSupplier(fileSystem.getPath("/eventModels.groovy"), extensions);

        List<EventModel> eventModels = supplier.get(network);
        assertEquals(2, eventModels.size());

        assertTrue(eventModels.get(0) instanceof DummyEventModel);
        DummyEventModel eventModel1 = (DummyEventModel) eventModels.get(0);
        assertEquals("NHV1_NHV2_1", eventModel1.getId());

        assertTrue(eventModels.get(1) instanceof DummyEventModel);
        DummyEventModel eventModel2 = (DummyEventModel) eventModels.get(1);
        assertEquals("NHV1_NHV2_2", eventModel2.getId());
    }
}
