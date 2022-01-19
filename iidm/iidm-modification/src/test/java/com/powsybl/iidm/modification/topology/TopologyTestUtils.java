/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.joda.time.DateTime;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
final class TopologyTestUtils {

    static Network createNbNetwork() {
        Network network = createNetwork();
        VoltageLevel vl = network.newVoltageLevel().setId("NHV1_NHV2_1_VL#0").setNominalV(380).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl.getNodeBreakerView().newBusbarSection().setId("bbs").setNode(0).add();
        return network;
    }

    static Network createBbNetwork() {
        Network network = createNetwork();
        VoltageLevel vl = network.newVoltageLevel().setId("NHV1_NHV2_1_VL#0").setNominalV(380).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        vl.getBusBreakerView().newBus().setId("bus").add();
        return network;
    }

    private static Network createNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2021-08-27T14:44:56.567+02:00"));
        return network;
    }

    static LineAdder createLineAdder(Line line, Network network) {
        return network.newLine()
                .setId("testLine")
                .setR(line.getR())
                .setX(line.getX())
                .setB1(line.getB1())
                .setG1(line.getG1())
                .setB2(line.getB2())
                .setG2(line.getG2());
    }

    private TopologyTestUtils() {
    }
}
