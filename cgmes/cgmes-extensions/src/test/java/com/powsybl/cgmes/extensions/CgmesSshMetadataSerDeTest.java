/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class CgmesSshMetadataSerDeTest extends AbstractCgmesExtensionTest {

    @Test
    void test() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2020-09-07T15:44:10.209+02:00"));
        network.newExtension(CgmesSshMetadataAdder.class)
                .setDescription("test description")
                .setModelingAuthoritySet("http://powsybl.org")
                .setSshVersion(1)
                .addDependency("http://dependency1")
                .addDependency("http://dependency2")
                .add();
        allFormatsRoundTripTest(network, "/eurostag_cgmes_ssh_metadata.xml");
    }
}
