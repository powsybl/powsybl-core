/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.AreaType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
class AreaSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void testNetworkAreas() throws IOException {
        Network network = Network.create("test", "test");
        network.setCaseDate(ZonedDateTime.parse("2020-03-04T13:20:30.476+01:00"));
        final AreaType biddingZoneType = network.newAreaType().setId("BiddingZone").add();
        final AreaType controlAreaType = network.newAreaType().setId("ControlArea").add();
        network.newArea().setAreaType(biddingZoneType).setId("BidZoneId1").setName("BidZoneName1").add();
        network.newAicArea().setAreaType(biddingZoneType).setId("BidZoneId2").setName("BidZoneName2")
               .setAcNetInterchangeTarget(100).setAcNetInterchangeTolerance(1).add();
        network.newArea().setAreaType(controlAreaType).setId("ControlAreaId1").setName("ControlAreaName1").add();

        Substation s1 = network.newSubstation().setId("sub1").add();
        s1.newVoltageLevel().setId("VL1").setNominalV(1).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        network.newSubstation().setId("sub2").add();

        allFormatsRoundTripTest(network, "/areaRoundTripRef.xml", CURRENT_IIDM_VERSION);

        // TODO Backward compatibility, area and areaType model is skipped
        // allFormatsRoundTripAllPreviousVersionedXmlTest("areaRoundTripRef.xml");
    }

}
