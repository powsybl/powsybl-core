/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class CgmesMetadataModelsSerDeTest extends AbstractCgmesExtensionTest {

    @Test
    void test() throws IOException {
        Network network = NetworkTest1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2020-09-07T15:44:10.209+02:00"));
        network.newExtension(CgmesMetadataModelsAdder.class)
                .newModel()
                .setSubset(CgmesSubset.EQUIPMENT)
                .setId("eqId")
                .setDescription("EQ description")
                .setModelingAuthoritySet("http://powsybl.org")
                .addProfile("http://equipment-core")
                .addProfile("http://equipment-operation")
                .setVersion(1)
                .addDependentOn("eq-dependency1")
                .addDependentOn("eq-dependency2")
                .add()
                .newModel()
                .setSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS)
                .setId("sshId")
                .setDescription("SSH description")
                .setModelingAuthoritySet("http://powsybl.org")
                .addProfile("http://steady-state-hypothesis")
                .setVersion(1)
                .addDependentOn("ssh-dependency1")
                .addDependentOn("ssh-dependency2")
                .addSupersedes("ssh-superseded1")
                .add()
                .newModel()
                .setSubset(CgmesSubset.STATE_VARIABLES)
                .setId("svId")
                .setDescription("SV description")
                .setModelingAuthoritySet("http://powsybl.org")
                .addProfile("http://state-variables")
                .setVersion(1)
                .addDependentOn("sv-dependency1")
                .addDependentOn("sv-dependency2")
                .add()
                .add();
        allFormatsRoundTripTest(network, "/network_test1_cgmes_metadata_models.xml");
    }
}
