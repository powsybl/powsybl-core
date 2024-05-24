/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable;

import com.powsybl.iidm.network.*;

/**
 * @author Ameni Walha {@literal <ameni.walha at rte-france.com>}
 */
final class ScalableTestNetwork {
    private ScalableTestNetwork() {
        throw new IllegalStateException("No default constructor in utility class");
    }

    static Network createNetwork() {
        Network network = Network.create("network", "test");
        Substation s = network.newSubstation()
                .setId("s")
                .setCountry(Country.US)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("vl1")
                .setNominalV(380.0)
                .setLowVoltageLimit(0.8 * 380.0)
                .setHighVoltageLimit(1.2 * 380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl.getBusBreakerView().newBus()
                .setId("bus1")
                .add();
        vl.newGenerator()
                .setId("g1")
                .setBus("bus1")
                .setConnectableBus("bus1")
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(0.0)
                .setVoltageRegulatorOn(false)
                .setTargetQ(0.0)
                .add();
        vl.newGenerator()
                .setId("g2")
                .setBus("bus1")
                .setConnectableBus("bus1")
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(0.0)
                .setVoltageRegulatorOn(false)
                .setTargetQ(0.0)
                .add();
        vl.newGenerator()
                .setId("g3")
                .setBus("bus1")
                .setConnectableBus("bus1")
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(0.0)
                .setVoltageRegulatorOn(true)
                .setTargetV(1.0)
                .add();
        vl.newLoad()
                .setId("l1")
                .setBus("bus1")
                .setConnectableBus("bus1")
                .setP0(100.0)
                .setQ0(0.0)
                .setLoadType(LoadType.UNDEFINED)
                .add();

        VoltageLevel vl2 = s.newVoltageLevel()
                .setId("vl2")
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setNominalV(380)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("bus2")
                .add();
        network.newLine()
                .setId("l12")
                .setVoltageLevel1("vl1")
                .setConnectableBus1("bus1")
                .setBus1("bus1")
                .setVoltageLevel2("vl2")
                .setConnectableBus2("bus2")
                .setBus2("bus2")
                .setR(1)
                .setX(1)
                .setG1(0)
                .setG2(0)
                .setB1(0)
                .setB2(0)
                .add();
        VoltageLevel vl3 = s.newVoltageLevel()
                .setId("vl3")
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setNominalV(380)
                .add();
        vl3.getBusBreakerView().newBus()
                .setId("bus3")
                .add();
        vl3.newGenerator()
                .setId("g4")
                .setBus("bus3")
                .setConnectableBus("bus3")
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(0.0)
                .setVoltageRegulatorOn(false)
                .setTargetQ(0.0)
                .add();

        return network;
    }

    static Network createNetworkWithDanglingLine() {
        Network network = Network.create("network", "test");
        Substation s = network.newSubstation()
                .setId("s")
                .setCountry(Country.US)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("vl1")
                .setNominalV(380.0)
                .setLowVoltageLimit(0.8 * 380.0)
                .setHighVoltageLimit(1.2 * 380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl.getBusBreakerView().newBus()
                .setId("bus1")
                .add();
        vl.newGenerator()
                .setId("g1")
                .setBus("bus1")
                .setConnectableBus("bus1")
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(50.0)
                .setVoltageRegulatorOn(false)
                .setTargetQ(0.0)
                .add();
        vl.newGenerator()
                .setId("g2")
                .setBus("bus1")
                .setConnectableBus("bus1")
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(50.0)
                .setVoltageRegulatorOn(false)
                .setTargetQ(0.0)
                .add();
        vl.newGenerator()
                .setId("g3")
                .setBus("bus1")
                .setConnectableBus("bus1")
                .setMinP(0.0)
                .setMaxP(100.0)
                .setTargetP(50.0)
                .setVoltageRegulatorOn(true)
                .setTargetV(1.0)
                .add();
        vl.newLoad()
                .setId("l1")
                .setBus("bus1")
                .setConnectableBus("bus1")
                .setP0(100.0)
                .setQ0(0.0)
                .setLoadType(LoadType.UNDEFINED)
                .add();

        VoltageLevel vl2 = s.newVoltageLevel()
                .setId("vl2")
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setNominalV(380)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("bus2")
                .add();
        vl2.newDanglingLine()
                .setId("dl2")
                .setBus("bus2")
                .setConnectableBus("bus2")
                .setP0(50.0)
                .setQ0(0.0)
                .setR(0.0)
                .setX(10.)
                .setB(0.0)
                .setG(0.0)
                .add();

        network.newLine()
                .setId("l12")
                .setVoltageLevel1("vl1")
                .setConnectableBus1("bus1")
                .setBus1("bus1")
                .setVoltageLevel2("vl2")
                .setConnectableBus2("bus2")
                .setBus2("bus2")
                .setR(1)
                .setX(1)
                .setG1(0)
                .setG2(0)
                .setB1(0)
                .setB2(0)
                .add();
        return network;
    }

    static Network createNetworkwithDanglingLineAndBattery() {
        Network network = Network.create("network", "test");

        // Substation
        Substation s = network.newSubstation()
            .setId("s")
            .setCountry(Country.US)
            .add();

        // First voltage level
        VoltageLevel vl = s.newVoltageLevel()
            .setId("vl1")
            .setNominalV(380.0)
            .setLowVoltageLimit(0.8 * 380.0)
            .setHighVoltageLimit(1.2 * 380.0)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        vl.getBusBreakerView().newBus()
            .setId("bus1")
            .add();
        vl.newGenerator()
            .setId("g1")
            .setBus("bus1")
            .setConnectableBus("bus1")
            .setMinP(0.0)
            .setMaxP(150.0)
            .setTargetP(80.0)
            .setVoltageRegulatorOn(false)
            .setTargetQ(0.0)
            .add();
        vl.newGenerator()
            .setId("g2")
            .setBus("bus1")
            .setConnectableBus("bus1")
            .setMinP(10.0)
            .setMaxP(100.0)
            .setTargetP(50.0)
            .setVoltageRegulatorOn(false)
            .setTargetQ(0.0)
            .add();
        vl.newGenerator()
            .setId("g3")
            .setBus("bus1")
            .setConnectableBus("bus1")
            .setMinP(20.0)
            .setMaxP(80.0)
            .setTargetP(30.0)
            .setVoltageRegulatorOn(true)
            .setTargetV(1.0)
            .add();
        vl.newLoad()
            .setId("l1")
            .setBus("bus1")
            .setConnectableBus("bus1")
            .setP0(100.0)
            .setQ0(0.0)
            .setLoadType(LoadType.UNDEFINED)
            .add();
        vl.newLoad()
            .setId("l2")
            .setBus("bus1")
            .setConnectableBus("bus1")
            .setP0(80.0)
            .setQ0(0.0)
            .setLoadType(LoadType.UNDEFINED)
            .add();
        vl.newLoad()
            .setId("l3")
            .setBus("bus1")
            .setConnectableBus("bus1")
            .setP0(50.0)
            .setQ0(0.0)
            .setLoadType(LoadType.UNDEFINED)
            .add();

        // Second Voltage level
        VoltageLevel vl2 = s.newVoltageLevel()
            .setId("vl2")
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .setNominalV(380)
            .add();
        vl2.getBusBreakerView().newBus()
            .setId("bus2")
            .add();
        vl2.newDanglingLine()
            .setId("dl1")
            .setBus("bus2")
            .setConnectableBus("bus2")
            .setP0(50.0)
            .setQ0(0.0)
            .setR(0.0)
            .setX(10.)
            .setB(0.0)
            .setG(0.0)
            .add();
        Battery battery = vl2.newBattery()
            .setId("BAT")
            .setBus("bus2")
            .setConnectableBus("bus2")
            .setTargetP(9999.99)
            .setTargetQ(9999.99)
            .setMinP(-9999.99)
            .setMaxP(9999.99)
            .add();
        battery.newMinMaxReactiveLimits()
            .setMinQ(-9999.99)
            .setMaxQ(9999.99)
            .add();
        battery.getTerminal().setP(-605);
        battery.getTerminal().setQ(-225);

        // Line between the two voltage levels
        network.newLine()
            .setId("l12")
            .setVoltageLevel1("vl1")
            .setConnectableBus1("bus1")
            .setBus1("bus1")
            .setVoltageLevel2("vl2")
            .setConnectableBus2("bus2")
            .setBus2("bus2")
            .setR(1)
            .setX(1)
            .setG1(0)
            .setG2(0)
            .setB1(0)
            .setB2(0)
            .add();
        return network;
    }
}
