/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
final class MergingViewFactory {

    private MergingViewFactory() {
    }

    static MergingView createCGM(TopologyKind topologyKind) {
        MergingView cgm = MergingView.create("cgm", "test");
        if (topologyKind == TopologyKind.NODE_BREAKER) {
            cgm.merge(createIgmNodeBreaker(1), createIgmNodeBreaker(2));
        } else if (topologyKind == TopologyKind.BUS_BREAKER) {
            cgm.merge(createIgmBusBreaker(1), createIgmBusBreaker(2));
        } else {
            cgm.merge(createIgmNodeBreaker(1), createIgmBusBreaker(2));
        }

        return cgm;
    }

    private static Network createIgmNodeBreaker(int index) {
        Network network = Network.create("n" + index, "test");
        Substation s1 = network.newSubstation()
                .setId("S" + index)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL" + index)
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl1.getNodeBreakerView().newBusbarSection()
                .setNode(1)
                .setId("BBS" + index)
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("SW" + index + "_1")
                .setNode1(1)
                .setNode2(2)
                .add();
        vl1.newBoundaryLine()
                .setId("DL" + index)
                .setNode(2)
                .setR(0)
                .setX(0)
                .setB(0)
                .setG(0)
                .setP0(0)
                .setQ0(0)
                .setUcteXnodeCode("XNODE")
                .add();
        vl1.getNodeBreakerView().newBreaker()
                .setId("SW" + index + "_2")
                .setNode1(1)
                .setNode2(3)
                .add();
        vl1.newLoad()
                .setId("LOAD" + index)
                .setNode(3)
                .setP0(0)
                .setQ0(0)
                .add();

        return network;
    }

    private static Network createIgmBusBreaker(int index) {
        String bus1 = "BUS1_" + index;
        String bus2 = "BUS2_" + index;
        String bus3 = "BUS3_" + index;

        Network network = Network.create("n" + index, "test");
        Substation s1 = network.newSubstation()
                .setId("S" + index)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL" + index)
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId(bus1)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId(bus2)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId(bus3)
                .add();
        vl1.getBusBreakerView().newSwitch()
                .setId("SW" + index + "_1")
                .setBus1(bus1)
                .setBus2(bus2)
                .add();
        vl1.newBoundaryLine()
                .setId("DL" + index)
                .setBus(bus2)
                .setR(0)
                .setX(0)
                .setB(0)
                .setG(0)
                .setP0(0)
                .setQ0(0)
                .setUcteXnodeCode("XNODE")
                .add();
        vl1.getBusBreakerView().newSwitch()
                .setId("SW" + index + "_2")
                .setBus1(bus1)
                .setBus2(bus3)
                .add();
        vl1.newLoad()
                .setId("LOAD" + index)
                .setBus(bus3)
                .setConnectableBus(bus3)
                .setP0(0)
                .setQ0(0)
                .add();

        return network;
    }

}
