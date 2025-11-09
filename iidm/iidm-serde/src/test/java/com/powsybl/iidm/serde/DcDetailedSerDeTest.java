/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class DcDetailedSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void testNetworkDcDetailed() throws IOException {
        Network network = createBaseNetwork();
//        network.write("XIIDM", new Properties(), Paths.get("D:\\powsybl-dev\\powsybl-core\\iidm\\iidm-serde\\src\\test\\resources\\V1_15\\dcDetailedRoundTripRef.xml"));
        allFormatsRoundTripTest(network, "/dcDetailedRoundTripRef.xml", CURRENT_IIDM_VERSION);
    }

    @Test
    void testNetworkDcDetailed2() throws IOException {
        Network network = createBaseNetwork2();
//        network.write("XIIDM", new Properties(), Paths.get("D:\\powsybl-dev\\powsybl-core\\iidm\\iidm-serde\\src\\test\\resources\\V1_15\\dcDetailedRoundTripRef2.xml"));
        allFormatsRoundTripTest(network, "/dcDetailedRoundTripRef2.xml", CURRENT_IIDM_VERSION);
    }

    private static Network createBaseNetwork() {
        // Create a base network
        Network network = DcDetailedNetworkFactory.createLccBipoleGroundReturnWithDcLineSegments();
        ZonedDateTime caseDate = ZonedDateTime.parse("2025-01-02T03:04:05.000+01:00");
        network.getDcNode(DcDetailedNetworkFactory.DC_NODE_FR_POS).setV(502.34);
        network.getDcGround(DcDetailedNetworkFactory.DC_GROUND_FR)
                .setR(0.3).getDcTerminal().setP(1.1).setI(1000.);
        network.getDcGround(DcDetailedNetworkFactory.DC_GROUND_GB)
                .setR(0.6).getDcTerminal().setConnected(false);
        network.getDcLine("dcLineSegmentFrPosA")
                        .getDcTerminal1().setI(1.1).setP(2.2);
        network.getDcLine("dcLineSegmentFrPosA")
                        .getDcTerminal2().setConnected(false);
        network.getLineCommutatedConverter("LccFrPos")
                .getDcTerminal1().setP(2.2).setI(750.);
        network.getLineCommutatedConverter("LccFrNeg")
                .getDcTerminal2().setP(3.3).setI(550.);
        network.setCaseDate(caseDate);
        network.getSubnetworks().forEach(subnetwork -> subnetwork.setCaseDate(caseDate));
        return network;
    }

    private static Network createBaseNetwork2() {
        // Create a base network
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();
        ZonedDateTime caseDate = ZonedDateTime.parse("2025-01-02T03:04:05.000+01:00");
        network.setCaseDate(caseDate);
        network.getSubnetworks().forEach(subnetwork -> subnetwork.setCaseDate(caseDate));
        return network;
    }

}
