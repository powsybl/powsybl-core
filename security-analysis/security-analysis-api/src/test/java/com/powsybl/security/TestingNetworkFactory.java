/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.iidm.network.*;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class TestingNetworkFactory {

    private TestingNetworkFactory() {
    }

    public static Network create() {
        Network network = NetworkFactory.create("test", "test");

        Substation s1 = network.newSubstation()
            .setId("S1")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
            .setId("VL1")
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .setLowVoltageLimit(200.0)
            .setHighVoltageLimit(250.0)
            .setNominalV(220.)
            .add();
        vl1.getBusBreakerView().newBus()
            .setId("BUS1")
            .add();
        VoltageLevel vl2 = s1.newVoltageLevel()
            .setId("VL2")
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .setLowVoltageLimit(340.0)
            .setHighVoltageLimit(420.0)
            .setNominalV(380.)
            .add();
        vl2.getBusBreakerView().newBus()
            .setId("BUS2")
            .add();

        Substation s2 = network.newSubstation()
            .setId("S2")
            .setCountry(Country.BE)
            .add();
        VoltageLevel vl3 = s2.newVoltageLevel()
            .setId("VL3")
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .setLowVoltageLimit(200.0)
            .setHighVoltageLimit(250.0)
            .setNominalV(220.0)
            .add();
        vl3.getBusBreakerView().newBus()
            .setId("BUS3")
            .add();
        VoltageLevel vl4 = s2.newVoltageLevel()
            .setId("VL4")
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .setLowVoltageLimit(340.0)
            .setHighVoltageLimit(420.0)
            .setNominalV(380.)
            .add();
        vl4.getBusBreakerView().newBus()
            .setId("BUS4")
            .add();

        network.newLine()
            .setId("LINE1")
            .setVoltageLevel1("VL2")
            .setBus1("BUS2")
            .setConnectableBus1("BUS2")
            .setVoltageLevel2("VL4")
            .setConnectableBus2("BUS4")
            .setBus2("BUS4")
            .setR(0.0)
            .setX(0.0)
            .setB1(0.0)
            .setB2(0.0)
            .setG1(0.0)
            .setG2(0.0)
            .add();

        network.newLine()
            .setId("LINE2")
            .setVoltageLevel1("VL1")
            .setBus1("BUS1")
            .setConnectableBus1("BUS1")
            .setVoltageLevel2("VL3")
            .setBus2("BUS3")
            .setConnectableBus2("BUS3")
            .setR(0.0)
            .setX(0.0)
            .setB1(0.0)
            .setB2(0.0)
            .setG1(0.0)
            .setG2(0.0)
            .add();

        return network;
    }
}
