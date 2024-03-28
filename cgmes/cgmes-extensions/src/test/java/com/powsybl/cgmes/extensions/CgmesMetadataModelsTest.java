/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class CgmesMetadataModelsTest {

    @Test
    void test() {
        Network network = NetworkTest1Factory.create();
        network.newExtension(CgmesMetadataModelsAdder.class)
                .newModel()
                .setSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS)
                .setId("sshId")
                .setDescription("SSH description")
                .setModelingAuthoritySet("http://powsybl.org")
                .setVersion(1)
                .addProfile("http://steady-state-hypothesis")
                .addDependentOn("ssh-dependency1")
                .addDependentOn("ssh-dependency2")
                .add()
                .newModel()
                .setSubset(CgmesSubset.STATE_VARIABLES)
                .setId("svId")
                .setDescription("SV description")
                .setModelingAuthoritySet("http://powsybl.org")
                .setVersion(2)
                .addProfile("http://state-variables")
                .addDependentOn("sv-dependency")
                .add()
                .add();

        CgmesMetadataModels extension = network.getExtension(CgmesMetadataModels.class);
        assertNotNull(extension);

        CgmesMetadataModel ssh = extension.getModelForSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS).orElseThrow();
        assertEquals("SSH description", ssh.getDescription());
        assertEquals("http://powsybl.org", ssh.getModelingAuthoritySet());
        assertEquals(1, ssh.getVersion());
        assertEquals(Set.of("ssh-dependency1", "ssh-dependency2"), ssh.getDependentOn());

        CgmesMetadataModel sv = extension.getModelForSubset(CgmesSubset.STATE_VARIABLES).orElseThrow();
        assertEquals("SV description", sv.getDescription());
        assertEquals("http://powsybl.org", sv.getModelingAuthoritySet());
        assertEquals(2, sv.getVersion());
        assertEquals(Set.of("sv-dependency"), sv.getDependentOn());
    }

    @Test
    void invalid() {
        Network network = NetworkTest1Factory.create();
        CgmesMetadataModelsAdder adder = network.newExtension(CgmesMetadataModelsAdder.class);
        // Expected an exception because the list of models is empty
        assertThrows(PowsyblException.class, adder::add);
    }
}
