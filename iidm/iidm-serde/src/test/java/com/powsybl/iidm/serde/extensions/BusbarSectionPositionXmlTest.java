/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BusbarSectionPosition;
import com.powsybl.iidm.network.impl.extensions.BusbarSectionPositionImpl;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class BusbarSectionPositionXmlTest extends AbstractIidmSerDeTest {

    private static Network createTestNetwork() {
        Network network = Network.create("test", "test");
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS")
                .setNode(0)
                .add();
        return network;
    }

    @Test
    void test() throws IOException {
        Network network = createTestNetwork();

        // extends busbar section
        BusbarSection busbarSection = network.getVoltageLevel("VL").getNodeBreakerView().getBusbarSection("BBS");
        BusbarSectionPositionImpl busbarSectionPosition = new BusbarSectionPositionImpl(busbarSection, 0, 1);
        busbarSection.addExtension(BusbarSectionPosition.class, busbarSectionPosition);

        Network network2 = allFormatsRoundTripTest(network, "/busbarSectionPositionRef.xml");

        BusbarSection busbarSection2 = network2.getVoltageLevel("VL").getNodeBreakerView().getBusbarSection("BBS");
        BusbarSectionPositionImpl busbarSectionPosition2 = busbarSection2.getExtension(BusbarSectionPosition.class);
        assertNotNull(busbarSectionPosition2);
        assertEquals(busbarSectionPosition.getBusbarIndex(), busbarSectionPosition2.getBusbarIndex());
        assertEquals(busbarSectionPosition.getSectionIndex(), busbarSectionPosition2.getSectionIndex());
    }
}
