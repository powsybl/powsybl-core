/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.test;

import eu.itesla_project.iidm.network.*;
import org.joda.time.DateTime;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class HvdcTestNetwork {

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
                .setName("Disconnector")
                .setNode1(0)
                .setNode2(1)
                .setOpen(false)
                .setRetained(true)
                .add();
        vl2.getNodeBreakerView().newBreaker()
                .setId("BK1")
                .setName("Breaker")
                .setNode1(1)
                .setNode2(2)
                .setOpen(false)
                .setRetained(false)
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
                .setMaxP(300)
                .setActivePowerSetPoint(280)
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
                .setVoltageSetPoint(405)
                .setVoltageRegulatorOn(true)
                .add();
        cs1.getTerminal()
                .setP(100.0f)
                .setQ(50.0f);
        cs1.newReactiveCapabilityCurve()
                .beginPoint()
                    .setP(5)
                    .setMinQ(0)
                    .setMaxQ(10)
                .endPoint()
                .beginPoint()
                    .setP(10)
                    .setMinQ(0)
                    .setMaxQ(10)
                .endPoint()
                .add();
        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        VscConverterStation cs2 = vl2.newVscConverterStation()
                .setId("C2")
                .setName("Converter2")
                .setNode(2)
                .setReactivePowerSetPoint(123)
                .setVoltageRegulatorOn(false)
                .add();
        cs2.newMinMaxReactiveLimits()
                .setMinQ(0)
                .setMaxQ(10)
                .add();
        createLine(network);
        return network;
    }

    public static Network createLcc() {
        Network network = createBase();
        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        LccConverterStation cs1 = vl1.newLccConverterStation()
                .setId("C1")
                .setName("Converter1")
                .setConnectableBus("B1")
                .setBus("B1")
                .setPowerFactor(0.5f)
                .add();
        cs1.getTerminal()
                .setP(100.0f)
                .setQ(50.0f);
        cs1.newFilter()
                .setB(0.00001f)
                .setConnected(true)
                .add();
        cs1.newFilter()
                .setB(0.00002f)
                .setConnected(false)
                .add();
        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        LccConverterStation cs2 = vl2.newLccConverterStation()
                .setId("C2")
                .setName("Converter2")
                .setNode(2)
                .setPowerFactor(0.6f)
                .add();
        cs2.getTerminal()
                .setP(75.0f)
                .setQ(25.0f);
        cs2.newFilter()
                .setB(0.00003f)
                .setConnected(true)
                .add();
        cs2.newFilter()
                .setB(0.00004f)
                .setConnected(true)
                .add();
        createLine(network);
        return network;
    }
}
