/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class SubstationTest extends AbstractIidmSerDeTest {

    @Test
    void testGeographicalTags() throws IOException {
        Network n = Network.create("test", "test");
        n.setCaseDate(ZonedDateTime.parse("2020-03-04T13:20:30.476+01:00"));
        Substation s1 = n.newSubstation().setId("sub1").setGeographicalTags("geoTag1", "geoTag2").add();
        s1.newVoltageLevel().setId("VL1").setNominalV(1).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        n.newSubstation().setId("sub2").setGeographicalTags("geoTag3", "geoTag4").add();
        allFormatsRoundTripTest(n, "/geographicalTags.xml");
    }
}
