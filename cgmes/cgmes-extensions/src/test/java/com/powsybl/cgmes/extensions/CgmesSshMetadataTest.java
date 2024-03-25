/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class CgmesSshMetadataTest {

    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(CgmesSshMetadataAdder.class)
                .setDescription("test description")
                .setModelingAuthoritySet("http://powsybl.org")
                .setSshVersion(1)
                .addDependency("http://dependency1")
                .addDependency("http://dependency2")
                .add();
        CgmesSshMetadata extension = network.getExtension(CgmesSshMetadata.class);
        assertNotNull(extension);
        assertEquals("test description", extension.getDescription());
        assertEquals("http://powsybl.org", extension.getModelingAuthoritySet());
        assertEquals(1, extension.getSshVersion());
        assertEquals(2, extension.getDependencies().size());
        assertTrue(extension.getDependencies().contains("http://dependency1"));
        assertTrue(extension.getDependencies().contains("http://dependency2"));
    }

    @Test
    void invalid() {
        Network network = EurostagTutorialExample1Factory.create();
        assertThrows(PowsyblException.class, () -> network.newExtension(CgmesSshMetadataAdder.class)
                .add());
    }
}
