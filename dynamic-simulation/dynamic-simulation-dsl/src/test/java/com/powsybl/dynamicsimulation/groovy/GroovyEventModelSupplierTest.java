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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.dynamicsimulation.EventModel;
import com.powsybl.dynamicsimulation.EventModelsSupplier;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class GroovyEventModelSupplierTest {

    private FileSystem fileSystem;

    @BeforeEach
    void setup() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());

        Files.copy(getClass().getResourceAsStream("/eventModels.groovy"), fileSystem.getPath("/eventModels.groovy"));
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();

        List<EventModelGroovyExtension> extensions = GroovyExtension.find(EventModelGroovyExtension.class, "dummy");
        assertEquals(1, extensions.size());
        assertNotNull(extensions.get(0));

        EventModelsSupplier supplier = new GroovyEventModelsSupplier(fileSystem.getPath("/eventModels.groovy"), extensions);

        testEventModelsSupplier(network, supplier);
    }

    @Test
    void testWithInputStream() {
        Network network = EurostagTutorialExample1Factory.create();

        List<EventModelGroovyExtension> extensions = GroovyExtension.find(EventModelGroovyExtension.class, "dummy");
        assertEquals(1, extensions.size());
        assertNotNull(extensions.get(0));

        EventModelsSupplier supplier = new GroovyEventModelsSupplier(getClass().getResourceAsStream("/eventModels.groovy"), extensions);

        testEventModelsSupplier(network, supplier);
    }

    private static void testEventModelsSupplier(Network network, EventModelsSupplier supplier) {
        List<EventModel> eventModels = supplier.get(network);
        assertEquals(2, eventModels.size());

        assertTrue(eventModels.get(0) instanceof DummyEventModel);
        DummyEventModel eventModel1 = (DummyEventModel) eventModels.get(0);
        assertEquals("NHV1_NHV2_1", eventModel1.getId());
        assertEquals(50, eventModel1.getStartTime(), 0);

        assertTrue(eventModels.get(1) instanceof DummyEventModel);
        DummyEventModel eventModel2 = (DummyEventModel) eventModels.get(1);
        assertEquals("NHV1_NHV2_2", eventModel2.getId());
        assertEquals(50, eventModel2.getStartTime(), 0);
    }
}
