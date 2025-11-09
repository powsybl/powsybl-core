/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.DcGround;
import com.powsybl.iidm.network.DcNode;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class DcGroundSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void testNetworkDcGround() throws IOException {
        Network network = createBaseNetwork();

        // Test for the current version
        allFormatsRoundTripTest(network, "/dcGroundRoundTripRef.xml", CURRENT_IIDM_VERSION);
    }

    private static Network createBaseNetwork() {
        Network network = Network.create("dcGroundTest", "code");
        network.setCaseDate(ZonedDateTime.parse("2025-01-02T03:04:05.000+01:00"));
        DcNode dcNode = network.newDcNode()
                .setId("dcNode")
                .setNominalV(500.)
                .add();
        DcGround dcGround1 = network.newDcGround()
                .setId("dcGroundWithoutSolvedV")
                .setName("A DC Ground without solved values")
                .setFictitious(true)
                .setDcNode(dcNode.getId())
                .setConnected(false)
                .setR(0.1)
                .add();
        dcGround1.setProperty("prop name", "prop value");
        dcGround1.addAlias("someAlias");
        network.newDcGround()
                .setId("dcGroundWithSolvedValues")
                .setName("A DC Ground with solved values")
                .setDcNode(dcNode.getId())
                .setR(2.0)
                .add().getDcTerminal().setP(1.1).setI(2.2);
        return network;
    }

}
