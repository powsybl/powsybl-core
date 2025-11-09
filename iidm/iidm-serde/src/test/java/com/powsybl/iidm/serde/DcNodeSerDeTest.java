/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class DcNodeSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void testNetworkDcNode() throws IOException {
        Network network = createBaseNetwork();

        // Test for the current version
        allFormatsRoundTripTest(network, "/dcNodeRoundTripRef.xml", CURRENT_IIDM_VERSION);
    }

    private static Network createBaseNetwork() {
        Network network = Network.create("dcNodeTest", "code");
        network.setCaseDate(ZonedDateTime.parse("2025-01-02T03:04:05.000+01:00"));
        var n = network.newDcNode()
                .setId("dcNodeWithoutSolvedV")
                .setName("A DC Node without solved V")
                .setNominalV(500.)
                .setFictitious(true)
                .add();
        n.setProperty("prop name", "prop value");
        n.addAlias("someAlias");
        network.newDcNode()
                .setId("dcNodeWithSolvedV")
                .setName("A DC Node with solved V")
                .setNominalV(500.)
                .add()
                .setV(498.);
        return network;
    }

}
