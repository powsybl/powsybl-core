/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class CgmesSvMetadataTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(CgmesSvMetadataAdder.class)
                .setDescription("test description")
                .setModelingAuthoritySet("http://powsybl.org")
                .setSvVersion(1)
                .addDependency("http://dependency1")
                .addDependency("http://dependency2")
                .add();
        CgmesSvMetadata extension = network.getExtension(CgmesSvMetadata.class);
        assertNotNull(extension);
        assertEquals("test description", extension.getDescription());
        assertEquals("http://powsybl.org", extension.getModelingAuthoritySet());
        assertEquals(1, extension.getSvVersion());
        assertEquals(2, extension.getDependencies().size());
        assertTrue(extension.getDependencies().contains("http://dependency1"));
        assertTrue(extension.getDependencies().contains("http://dependency2"));
    }

    @Test(expected = PowsyblException.class)
    public void invalid() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(CgmesSvMetadataAdder.class)
                .add();
    }
}
