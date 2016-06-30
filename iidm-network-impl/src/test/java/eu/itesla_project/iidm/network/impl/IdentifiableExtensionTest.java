/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.Identifiable;
import eu.itesla_project.iidm.network.Load;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
import eu.itesla_project.iidm.network.test.LoadZipModel;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class IdentifiableExtensionTest {

    public static class LoadFooModel implements Identifiable.Extension<Load> {

        private final Load load;

        public LoadFooModel(Load load) {
            this.load = Objects.requireNonNull(load);
        }

        @Override
        public Load getIdentifiable() {
            return load;
        }

        @Override
        public String getName() {
            return "loadFooModel";
        }
    }

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        Load load = network.getLoad("LOAD");
        assertTrue(load.getExtensions().isEmpty());
        LoadZipModel zipModel = new LoadZipModel(load, 1, 2, 3, 4, 5, 6, 380);
        load.addExtension(LoadZipModel.class, zipModel);
        assertTrue(zipModel != null);
        assertTrue(load.getExtension(LoadZipModel.class) == zipModel);
        assertTrue(load.getExtension(LoadFooModel.class) == null);
        assertTrue(load.getExtensions().size() == 1);
        assertArrayEquals(load.getExtensions().toArray(new Identifiable.Extension[0]), new Identifiable.Extension[] {zipModel});
    }
}
