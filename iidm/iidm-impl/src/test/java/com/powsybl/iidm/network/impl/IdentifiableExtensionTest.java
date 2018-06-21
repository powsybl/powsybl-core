/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.LoadZipModel;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class IdentifiableExtensionTest {

    public static class LoadFooModel extends AbstractExtension<Load> {

        public LoadFooModel(Load load) {
            super(load);
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
        assertNotNull(zipModel);
        assertSame(zipModel, load.getExtension(LoadZipModel.class));
        assertNull(load.getExtension(LoadFooModel.class));
        assertEquals(1, load.getExtensions().size());
        assertArrayEquals(load.getExtensions().toArray(new Extension[0]), new Extension[] {zipModel});
    }
}
