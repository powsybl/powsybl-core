/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.BranchStatus;
import com.powsybl.iidm.network.impl.extensions.BranchStatusImpl;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Nicolas Noir {@literal <nicolas.noir at rte-france.com>}
 */
class BranchStatusXmlTest extends AbstractSerDeTest {

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
        Substation s2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
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
        BranchStatus<Line> lineBranchStatus = new BranchStatusImpl<>(line,
                BranchStatus.Status.PLANNED_OUTAGE);
        line.addExtension(BranchStatus.class, lineBranchStatus);

        Network network2 = roundTripXmlTest(network,
                NetworkSerDe::writeAndValidate,
                NetworkSerDe::read,
                "/branchStatusRef.xml");

        Line line2 = network2.getLine("L");
        assertNotNull(line2);
        BranchStatus lineBranchStatus2 = line2.getExtension(BranchStatus.class);
        assertNotNull(lineBranchStatus2);
        assertEquals(lineBranchStatus.getStatus(), lineBranchStatus2.getStatus());

        lineBranchStatus2.setStatus(BranchStatus.Status.IN_OPERATION);
        assertEquals(BranchStatus.Status.IN_OPERATION, lineBranchStatus2.getStatus());
    }
}
