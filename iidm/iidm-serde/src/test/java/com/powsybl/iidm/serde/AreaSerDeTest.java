/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
class AreaSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void testNetworkAreas() throws IOException {
        Network network = createBaseNetworkWithAreas();
        allFormatsRoundTripTest(network, "/areaRoundTripRef.xml", CURRENT_IIDM_VERSION);
        // backward compatibility (checks versions 11 and 12)
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("/areaRoundTripRef.xml", IidmVersion.V_1_11);
    }

    @Test
    void testUnknownAreaType() {
        ImportOptions options = new ImportOptions();
        Throwable e = assertThrows(PowsyblException.class,
                    () -> NetworkSerDe.read(getVersionedNetworkAsStream("areaUnknownType.xml", CURRENT_IIDM_VERSION), options, null));
        assertTrue(e.getMessage().contains("Area Type Identifiable 'ControlArea' not found"));
    }

    private static Network createBaseNetworkWithAreas() {
        // Create a base network
        Network network = Network.create("test", "test");
        network.setCaseDate(ZonedDateTime.parse("2020-03-04T13:20:30.476+01:00"));
        Substation s1 = network.newSubstation().setId("sub1").add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL1").setNominalV(1).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        network.newSubstation().setId("sub2").add();
        vl1.getBusBreakerView().newBus().setId("N1").add();
        vl1.getBusBreakerView().newBus().setId("N2").add();
        final Load load1 = vl1.newLoad().setId("L1").setBus("N1").setP0(0).setQ0(0).add();
        final Load load2 = vl1.newLoad().setId("L2").setBus("N2").setP0(0).setQ0(0).add();

        // Add area types and areas to the network
        final AreaType biddingZoneType = network.newAreaType().setId("BiddingZone").add();
        final AreaType controlAreaType = network.newAreaType().setId("ControlArea").add();
        network.newArea().setAreaType(biddingZoneType).setId("BidZoneId1").setName("BidZoneName1").addBoundaryTerminal(load1.getTerminal(), true)
               .addBoundaryTerminal(load2.getTerminal(), false).add();
        network.newArea().setAreaType(biddingZoneType).setId("BidZoneId2").setName("BidZoneName2").addBoundaryTerminal(load1.getTerminal(), true)
               .setAcNetInterchangeTarget(100.).setAcNetInterchangeTolerance(1.).add();
        network.newArea().setAreaType(controlAreaType).setId("ControlAreaId1").setName("ControlAreaName1").add();
        vl1.addArea(network.getArea("BidZoneId1"));
        vl1.addArea(network.getArea("ControlAreaId1"));
        return network;
    }

}
