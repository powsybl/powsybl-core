/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public final class TestingNetworkFactory {

    private TestingNetworkFactory() {
    }

    public static Network createFromEurostag() {
        Network network = EurostagTutorialExample1Factory.create();

        network.getSubstation("P2").setCountry(Country.BE);

        Generator generator = network.getVoltageLevel("VLGEN").newGenerator()
            .setId("GEN2")
            .setBus("NGEN")
            .setConnectableBus("NGEN")
            .setMinP(-9999.99f)
            .setMaxP(9999.99f)
            .setVoltageRegulatorOn(true)
            .setTargetV(24.5f)
            .setTargetP(607f)
            .setTargetQ(301f)
            .add();

        ((Bus) network.getIdentifiable("NHV1")).setV(380f).getVoltageLevel().setLowVoltageLimit(400f).setHighVoltageLimit(500f);
        ((Bus) network.getIdentifiable("NHV2")).setV(380f).getVoltageLevel().setLowVoltageLimit(300f).setHighVoltageLimit(500f);
        network.getLine("NHV1_NHV2_1").getTerminal1().setP(560f).setQ(550f);
        network.getLine("NHV1_NHV2_1").getTerminal2().setP(560f).setQ(550f);
        network.getLine("NHV1_NHV2_1").newCurrentLimits1().setPermanentLimit(500f).add();
        network.getLine("NHV1_NHV2_1").newCurrentLimits2()
            .setPermanentLimit(1100f)
            .beginTemporaryLimit()
            .setName("10'")
            .setAcceptableDuration(10 * 60)
            .setValue(1200)
            .endTemporaryLimit()
            .add();

        network.getLine("NHV1_NHV2_2").getTerminal1().setP(560f).setQ(550f);
        network.getLine("NHV1_NHV2_2").getTerminal2().setP(560f).setQ(550f);
        network.getLine("NHV1_NHV2_2").newCurrentLimits1()
            .setPermanentLimit(1100f)
            .beginTemporaryLimit()
            .setName("20'")
            .setAcceptableDuration(20 * 60)
            .setValue(1200)
            .endTemporaryLimit()
            .add();
        network.getLine("NHV1_NHV2_2").newCurrentLimits2().setPermanentLimit(500f).add();

        return network;
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
            .setLowVoltageLimit(200.0f)
            .setHighVoltageLimit(250.0f)
            .setNominalV(220.f)
            .add();
        vl1.getBusBreakerView().newBus()
            .setId("BUS1")
            .add();
        VoltageLevel vl2 = s1.newVoltageLevel()
            .setId("VL2")
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .setLowVoltageLimit(340.0f)
            .setHighVoltageLimit(420.0f)
            .setNominalV(380.f)
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
            .setLowVoltageLimit(200.0f)
            .setHighVoltageLimit(250.0f)
            .setNominalV(220.0f)
            .add();
        vl3.getBusBreakerView().newBus()
            .setId("BUS3")
            .add();
        VoltageLevel vl4 = s2.newVoltageLevel()
            .setId("VL4")
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .setLowVoltageLimit(340.0f)
            .setHighVoltageLimit(420.0f)
            .setNominalV(380.f)
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
            .setR(0.0f)
            .setX(0.0f)
            .setB1(0.0f)
            .setB2(0.0f)
            .setG1(0.0f)
            .setG2(0.0f)
            .add();

        network.newLine()
            .setId("LINE2")
            .setVoltageLevel1("VL1")
            .setBus1("BUS1")
            .setConnectableBus1("BUS1")
            .setVoltageLevel2("VL3")
            .setBus2("BUS3")
            .setConnectableBus2("BUS3")
            .setR(0.0f)
            .setX(0.0f)
            .setB1(0.0f)
            .setB2(0.0f)
            .setG1(0.0f)
            .setG2(0.0f)
            .add();

        return network;
    }
}
