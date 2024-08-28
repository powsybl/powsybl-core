/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.OperatingStatus;
import com.powsybl.iidm.network.impl.extensions.OperatingStatusImpl;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Nicolas Noir {@literal <nicolas.noir at rte-france.com>}
 */
class OperatingStatusXmlTest extends AbstractIidmSerDeTest {

    protected static Network createTestNetwork() {
        Network network = Network.create("test", "test");
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        network.newLine()
                .setId("L")
                .setVoltageLevel1("VL")
                .setNode1(2)
                .setVoltageLevel2("VL2")
                .setNode2(0)
                .setR(1)
                .setX(1)
                .setG1(0)
                .setG2(0)
                .setB1(0)
                .setB2(0)
                .add();
        return network;
    }

    @Test
    void test() throws IOException {
        Network network = createTestNetwork();

        // extend line
        Line line = network.getLine("L");
        assertNotNull(line);
        OperatingStatus<Line> lineOperatingStatus = new OperatingStatusImpl<>(line,
                OperatingStatus.Status.PLANNED_OUTAGE);
        line.addExtension(OperatingStatus.class, lineOperatingStatus);

        Network network2 = allFormatsRoundTripTest(network, "/operatingStatusRef.xml");

        Line line2 = network2.getLine("L");
        assertNotNull(line2);
        OperatingStatus<Line> lineOperatingStatus2 = line2.getExtension(OperatingStatus.class);
        assertNotNull(lineOperatingStatus2);
        assertEquals(lineOperatingStatus.getStatus(), lineOperatingStatus2.getStatus());

        lineOperatingStatus2.setStatus(OperatingStatus.Status.IN_OPERATION);
        assertEquals(OperatingStatus.Status.IN_OPERATION, lineOperatingStatus2.getStatus());
    }

    @Test
    void branchStatusCompatibilityTest() {
        Network network = NetworkSerDe.read(getClass().getResourceAsStream("/branchStatusRef.xml"));
        Line line = network.getLine("L");
        assertNotNull(line);
        OperatingStatus<Line> operatingStatus = line.getExtension(OperatingStatus.class);
        assertNotNull(operatingStatus);
        assertSame(OperatingStatus.Status.PLANNED_OUTAGE, operatingStatus.getStatus());
    }
}
