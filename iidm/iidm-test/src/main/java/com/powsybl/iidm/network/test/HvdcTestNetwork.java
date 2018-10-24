/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public final class HvdcTestNetwork {

    private static final String DISCONNECTOR_NAME = "Disconnector";
    private static final String BREAKER_NAME = "Breaker";

    private HvdcTestNetwork() {
    }

    private static Network createBase() {
        Network network = NetworkFactory.create("hvdctest", "test");
        network.setCaseDate(DateTime.parse("2016-06-27T16:34:55.930+02:00"));
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B1")
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
        vl2.getNodeBreakerView().setNodeCount(3);
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setName("BusbarSection")
                .setNode(0)
                .add();
        vl2.getNodeBreakerView().newDisconnector()
                .setId("DISC_BBS1_BK1")
                .setName(DISCONNECTOR_NAME)
                .setNode1(0)
                .setNode2(1)
                .setOpen(false)
                .setRetained(false)
                .add();
        vl2.getNodeBreakerView().newBreaker()
                .setId("BK1")
                .setName(BREAKER_NAME)
                .setNode1(1)
                .setNode2(2)
                .setOpen(false)
                .setRetained(true)
                .add();
        return network;
    }

    private static void createLine(Network network) {
        network.newHvdcLine()
                .setId("L")
                .setName("HVDC")
                .setConverterStationId1("C1")
                .setConverterStationId2("C2")
                .setR(1)
                .setNominalV(400)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setMaxP(300.0)
                .setActivePowerSetpoint(280)
                .add();
    }

    public static Network createVsc() {
        Network network = createBase();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        VscConverterStation cs1 = vl1.newVscConverterStation()
                .setId("C1")
                .setName("Converter1")
                .setConnectableBus("B1")
                .setBus("B1")
                .setLossFactor(0.011f)
                .setVoltageSetpoint(405.0)
                .setVoltageRegulatorOn(true)
                .add();
        cs1.getTerminal()
                .setP(100.0)
                .setQ(50.0);
        cs1.newReactiveCapabilityCurve()
                .beginPoint()
                    .setP(5.0)
                    .setMinQ(0.0)
                    .setMaxQ(10.0)
                .endPoint()
                .beginPoint()
                    .setP(10.0)
                    .setMinQ(0.0)
                    .setMaxQ(10.0)
                .endPoint()
                .add();
        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        VscConverterStation cs2 = vl2.newVscConverterStation()
                .setId("C2")
                .setName("Converter2")
                .setNode(2)
                .setLossFactor(0.011f)
                .setReactivePowerSetpoint(123)
                .setVoltageRegulatorOn(false)
                .add();
        cs2.newMinMaxReactiveLimits()
                .setMinQ(0.0)
                .setMaxQ(10.0)
                .add();
        createLine(network);
        return network;
    }

    public static Network createLcc() {
        Network network = createBase();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        ShuntCompensator shunt1 = vl1.newShuntCompensator()
                .setId("C1_Filter1")
                .setName("Filter 1")
                .setConnectableBus("B1")
                .setBus("B1")
                .setbPerSection(1e-5)
                .setCurrentSectionCount(1)
                .setMaximumSectionCount(1)
                .add();
        shunt1.getTerminal()
                .setQ(25.0);
        ShuntCompensator shunt2 = vl1.newShuntCompensator()
                .setId("C1_Filter2")
                .setName("Filter 2")
                .setConnectableBus("B1")
                .setbPerSection(2e-5)
                .setCurrentSectionCount(0)
                .setMaximumSectionCount(1)
                .add();
        shunt2.getTerminal()
                .setQ(25.0);
        LccConverterStation cs1 = vl1.newLccConverterStation()
                .setId("C1")
                .setName("Converter1")
                .setConnectableBus("B1")
                .setBus("B1")
                .setLossFactor(0.011f)
                .setPowerFactor(0.5f)
                .add();
        cs1.getTerminal()
                .setP(100.0)
                .setQ(50.0);
        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        vl2.getNodeBreakerView().setNodeCount(7);
        vl2.getNodeBreakerView().newDisconnector()
                .setId("DISC_BBS1_BK2")
                .setName(DISCONNECTOR_NAME)
                .setNode1(0)
                .setNode2(3)
                .setOpen(false)
                .setRetained(false)
                .add();
        vl2.getNodeBreakerView().newBreaker()
                .setId("BK2")
                .setName(BREAKER_NAME)
                .setNode1(3)
                .setNode2(4)
                .setOpen(false)
                .setRetained(true)
                .add();
        vl2.getNodeBreakerView().newDisconnector()
                .setId("DISC_BBS1_BK3")
                .setName(DISCONNECTOR_NAME)
                .setNode1(0)
                .setNode2(5)
                .setOpen(false)
                .setRetained(false)
                .add();
        vl2.getNodeBreakerView().newBreaker()
                .setId("BK3")
                .setName(BREAKER_NAME)
                .setNode1(5)
                .setNode2(6)
                .setOpen(false)
                .setRetained(true)
                .add();
        ShuntCompensator shunt3 = vl2.newShuntCompensator()
                .setId("C2_Filter1")
                .setName("Filter 3")
                .setNode(4)
                .setbPerSection(3e-5)
                .setCurrentSectionCount(1)
                .setMaximumSectionCount(1)
                .add();
        shunt3.getTerminal()
                .setQ(12.5);
        ShuntCompensator shunt4 = vl2.newShuntCompensator()
                .setId("C2_Filter2")
                .setName("Filter 4")
                .setNode(6)
                .setbPerSection(4e-5)
                .setCurrentSectionCount(1)
                .setMaximumSectionCount(1)
                .add();
        shunt4.getTerminal()
                .setQ(12.5);
        LccConverterStation cs2 = vl2.newLccConverterStation()
                .setId("C2")
                .setName("Converter2")
                .setNode(2)
                .setLossFactor(0.011f)
                .setPowerFactor(0.6f)
                .add();
        cs2.getTerminal()
                .setP(75.0)
                .setQ(25.0);
        createLine(network);
        return network;
    }
}
