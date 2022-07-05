/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.*;
import org.joda.time.DateTime;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public final class Networks {

    private Networks() {
    }

    public static Network createNetworkWithBusbar() {
        Network network = NetworkFactory.create("Network", "test");
        network.setCaseDate(DateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation substation = network.newSubstation()
                .setId("Substation")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId("VoltageLevel")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        voltageLevel.getNodeBreakerView().newBusbarSection()
                .setId("Busbar")
                .setNode(0)
                .add();
        return network;
    }

    public static Network createNetworkWithBus() {
        Network network = NetworkFactory.create("Network", "test");
        network.setCaseDate(DateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation substation = network.newSubstation()
                .setId("Substation")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId("VoltageLevel")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("Bus")
                .add();
        return network;
    }

    public static Network createNetworkWithGenerator() {
        Network network = NetworkFactory.create("test", "test");
        network.setCaseDate(DateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation substation = network.newSubstation()
                .setId("Substation")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId("VoltageLevel")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("Bus")
                .add();
        voltageLevel.newGenerator()
                .setId("Generator")
                .setBus("Bus")
                .setConnectableBus("Bus")
                .setTargetP(100)
                .setTargetV(380)
                .setVoltageRegulatorOn(true)
                .setMaxP(100)
                .setMinP(0)
                .add();
        return network;
    }

    public static Network createNetworkWithLine() {
        Network network = NetworkFactory.create("Network", "test");
        network.setCaseDate(DateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation substation1 = network.newSubstation()
                .setId("Substation1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId("VoltageLevel1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel1.getBusBreakerView().newBus()
                .setId("Bus1")
                .add();
        Substation substation2 = network.newSubstation()
                .setId("Substation2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel2 = substation2.newVoltageLevel()
                .setId("VoltageLevel2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel2.getBusBreakerView().newBus()
                .setId("Bus2")
                .add();
        network.newLine()
                .setId("Line")
                .setVoltageLevel1(voltageLevel1.getId())
                .setBus1("Bus1")
                .setConnectableBus1("Bus1")
                .setVoltageLevel2(voltageLevel2.getId())
                .setBus2("Bus2")
                .setConnectableBus2("Bus2")
                .setR(3.0)
                .setX(33.0)
                .setG1(0.0)
                .setB1(386E-6 / 2)
                .setG2(0.0)
                .setB2(386E-6 / 2)
                .add();
        return network;
    }

    public static Network createNetworkWithLoad() {
        Network network = NetworkFactory.create("test", "test");
        network.setCaseDate(DateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation substation = network.newSubstation()
                .setId("Substation")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId("VoltageLevel")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("Bus")
                .add();
        voltageLevel.newLoad()
                .setId("Load")
                .setBus("Bus")
                .setConnectableBus("Bus")
                .setP0(100)
                .setQ0(50)
                .add();
        return network;
    }

    public static Network createNetworkWithShuntCompensator() {
        Network network = NetworkFactory.create("test", "test");
        network.setCaseDate(DateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation substation = network.newSubstation()
                .setId("Substation")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId("VoltageLevel")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("Bus")
                .add();
        voltageLevel.newShuntCompensator()
                .setId("Shunt")
                .setBus("Bus")
                .setConnectableBus("Bus")
                .setSectionCount(1)
                .newLinearModel()
                    .setBPerSection(1e-5)
                    .setMaximumSectionCount(1)
                .add()
                .add();
        return network;
    }

    public static Network createNetworkWithStaticVarCompensator() {
        Network network = NetworkFactory.create("test", "test");
        network.setCaseDate(DateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation substation = network.newSubstation()
                .setId("Substation")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId("VoltageLevel")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("Bus")
                .add();
        voltageLevel.newStaticVarCompensator()
                .setId("Svc")
                .setConnectableBus("Bus")
                .setBus("Bus")
                .setBmin(0.0002)
                .setBmax(0.0008)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetPoint(390.0)
                .setReactivePowerSetPoint(1.0)
                .add();
        return network;
    }

    public static Network createNetworkWithSwitch() {
        Network network = NetworkFactory.create("Network", "test");
        network.setCaseDate(DateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation substation = network.newSubstation()
                .setId("Substation")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId("VoltageLevel")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("Bus1")
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("Bus2")
                .add();
        voltageLevel.getBusBreakerView().newSwitch()
                .setId("Switch")
                .setBus1("Bus1")
                .setBus2("Bus2")
                .setOpen(false)
                .add();
        return network;
    }

    public static Network createNetworkWithThreeWindingsTransformer() {
        Network network = NetworkFactory.create("Network", "test");
        network.setCaseDate(DateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation substation = network.newSubstation()
                .setId("Substation")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel1 = substation.newVoltageLevel()
                .setId("VoltageLevel1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel1.getBusBreakerView().newBus()
                .setId("Bus1")
                .add();
        VoltageLevel voltageLevel2 = substation.newVoltageLevel()
                .setId("VoltageLevel2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel2.getBusBreakerView().newBus()
                .setId("Bus2")
                .add();
        VoltageLevel voltageLevel3 = substation.newVoltageLevel()
                .setId("VoltageLevel3")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel3.getBusBreakerView().newBus()
                .setId("Bus3")
                .add();
        substation.newThreeWindingsTransformer()
                .setId("Transformer3w")
                .newLeg1()
                    .setR(17.424)
                    .setX(1.7424)
                    .setB(0.000573921028466483)
                    .setG(0.00573921028466483)
                    .setRatedU(132.0)
                    .setVoltageLevel(voltageLevel1.getId())
                    .setBus("Bus1")
                    .add()
                .newLeg2()
                    .setR(1.089)
                    .setX(0.1089)
                    .setRatedU(33.0)
                    .setVoltageLevel(voltageLevel2.getId())
                    .setBus("Bus2")
                    .add()
                .newLeg3()
                    .setR(0.121)
                    .setX(0.0121)
                    .setRatedU(11.0)
                    .setVoltageLevel(voltageLevel3.getId())
                    .setBus("Bus3")
                    .add()
                .add();
        return network;
    }

    public static Network createNetworkWithTwoWindingsTransformer() {
        Network network = NetworkFactory.create("Network", "test");
        network.setCaseDate(DateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation substation = network.newSubstation()
                .setId("Substation")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel1 = substation.newVoltageLevel()
                .setId("VoltageLevel1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel1.getBusBreakerView().newBus()
                .setId("Bus1")
                .add();
        VoltageLevel voltageLevel2 = substation.newVoltageLevel()
                .setId("VoltageLevel2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel2.getBusBreakerView().newBus()
                .setId("Bus2")
                .add();
        int zb380 = 380 * 380 / 100;
        substation.newTwoWindingsTransformer()
                .setId("Transformer")
                .setVoltageLevel1(voltageLevel1.getId())
                .setBus1("Bus1")
                .setConnectableBus1("Bus1")
                .setRatedU1(24.0)
                .setVoltageLevel2(voltageLevel2.getId())
                .setBus2("Bus2")
                .setConnectableBus2("Bus2")
                .setRatedU2(400.0)
                .setR(0.24 / 1300 * zb380)
                .setX(Math.sqrt(10 * 10 - 0.24 * 0.24) / 1300 * zb380)
                .setG(0.0)
                .setB(0.0)
                .add();
        return network;
    }

    public static Network createNetworkWithDanglingLine() {
        Network network = NetworkFactory.create("Network", "test");
        network.setCaseDate(DateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation substation = network.newSubstation()
                .setId("Substation")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                .setId("VoltageLevel")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel.getBusBreakerView().newBus()
                .setId("Bus")
                .add();
        voltageLevel.newDanglingLine()
                .setId("DanglingLine")
                .setBus("Bus")
                .setR(10.0)
                .setX(1.0)
                .setB(10e-6)
                .setG(10e-5)
                .setP0(50.0)
                .setQ0(30.0)
                .add();
        return network;
    }

    public static Network createNetworkWithHvdcLine() {
        Network network = NetworkFactory.create("Network", "test");
        network.setCaseDate(DateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation substation1 = network.newSubstation()
                .setId("Substation1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId("VoltageLevel1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel1.getBusBreakerView().newBus()
                .setId("Bus1")
                .add();
        VscConverterStation cs1 = voltageLevel1.newVscConverterStation()
                .setId("Converter1")
                .setConnectableBus("Bus1")
                .setBus("Bus1")
                .setLossFactor(0.011f)
                .setVoltageSetpoint(405.0)
                .setVoltageRegulatorOn(true)
                .add();
        Substation substation2 = network.newSubstation()
                .setId("Substation2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel2 = substation2.newVoltageLevel()
                .setId("VoltageLevel2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        voltageLevel2.getBusBreakerView().newBus()
                .setId("Bus2")
                .add();
        VscConverterStation cs2 = voltageLevel2.newVscConverterStation()
                .setId("Converter2")
                .setConnectableBus("Bus2")
                .setBus("Bus2")
                .setLossFactor(0.011f)
                .setReactivePowerSetpoint(123)
                .setVoltageRegulatorOn(false)
                .add();
        network.newHvdcLine()
                .setId("HvdcLine")
                .setConverterStationId1("Converter1")
                .setConverterStationId2("Converter2")
                .setR(1)
                .setNominalV(400)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setMaxP(300.0)
                .setActivePowerSetpoint(280)
                .add();
        return network;
    }

    public static Network createNetworkWithBusbarAndSwitch() {
        Network network = Network.create("Network", "test");
        network.setCaseDate(DateTime.parse("2018-01-01T00:30:00.000+01:00"));
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS1")
                .setNode(0)
                .add();
        vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS2")
                .setNode(1)
                .add();
        vl.getNodeBreakerView().newBreaker()
                .setId("B")
                .setNode1(0)
                .setNode2(1)
                .setOpen(false)
                .setRetained(true)
                .add();
        return network;
    }

    public static Network createNetworkWithDoubleBusbarSections() {
        int countNodes = 0;

        int bbN1 = countNodes++;
        int bbN2 = countNodes++;
        int iN1 = countNodes++;
        int gN1 = countNodes++;

        Network network = Network.create("network1", "test");

        Substation substation1 = network.newSubstation()
                .setId("Substation1")
                .setCountry(Country.FR)
                .add();

        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId("VoltageLevel1")
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setNominalV(400)
                .add();

        voltageLevel1.getNodeBreakerView().newBusbarSection()
                .setId("BusbarSection1")
                .setNode(bbN1)
                .add();
        voltageLevel1.getNodeBreakerView().newBusbarSection()
                .setId("BusbarSection2")
                .setNode(bbN2)
                .add();

        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("Disconnector1")
                .setNode1(bbN1)
                .setNode2(iN1)
                .setOpen(true)
                .add();

        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("Disconnector2")
                .setNode1(bbN2)
                .setNode2(iN1)
                .setOpen(false)
                .add();

        voltageLevel1.newGenerator()
                .setId("Generator1")
                .setNode(gN1)
                .setTargetP(100)
                .setTargetV(380)
                .setVoltageRegulatorOn(true)
                .setMaxP(100)
                .setMinP(0)
                .add();

        voltageLevel1.getNodeBreakerView().newBreaker()
                .setId("Breaker1")
                .setNode1(gN1)
                .setNode2(iN1)
                .add();
        return network;
    }

    public static Network createNetworkWithPhaseShiftTransformer() {
        Network network = Networks.createNetworkWithTwoWindingsTransformer();
        TwoWindingsTransformer twt = network.getTwoWindingsTransformerStream().findFirst().get();
        twt.newPhaseTapChanger()
                .setTapPosition(1)
                .setRegulationTerminal(twt.getTerminal2())
                .setRegulationMode(PhaseTapChanger.RegulationMode.FIXED_TAP)
                .setRegulationValue(200)
                .beginStep()
                .setAlpha(-20.0)
                .setRho(1.0)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .beginStep()
                .setAlpha(0.0)
                .setRho(1.0)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .beginStep()
                .setAlpha(20.0)
                .setRho(1.0)
                .setR(0.0)
                .setX(0.0)
                .setG(0.0)
                .setB(0.0)
                .endStep()
                .add();
        return network;
    }

    public static Network createNetworkWithBridge() {
        Network network = NetworkFactory.findDefault().createNetwork("Network", "test");
        network.setCaseDate(DateTime.parse("2020-01-01T00:30:00.000+01:00"));

        Substation substation1 = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId("V1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        voltageLevel1.getNodeBreakerView().newBusbarSection()
                .setId("Busbar1_1")
                .setNode(11)
                .add();
        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("D1_0")
                .setOpen(false)
                .setNode1(17)
                .setNode2(18)
                .add();
        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("D1_1")
                .setOpen(false)
                .setNode1(19)
                .setNode2(20)
                .add();
        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("D1_2")
                .setOpen(false)
                .setNode1(23)
                .setNode2(24)
                .add();
        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("D1_3")
                .setOpen(false)
                .setNode1(31)
                .setNode2(32)
                .add();
        voltageLevel1.getNodeBreakerView().newDisconnector()
                .setId("D1_6_BRIDGE")
                .setOpen(true)
                .setNode1(15)
                .setNode2(16)
                .add();
        voltageLevel1.getNodeBreakerView().newInternalConnection()
                .setNode1(11)
                .setNode2(1)
                .add();
        voltageLevel1.getNodeBreakerView().newInternalConnection()
                .setNode1(17)
                .setNode2(1)
                .add();
        voltageLevel1.getNodeBreakerView().newInternalConnection()
                .setNode1(19)
                .setNode2(1)
                .add();
        voltageLevel1.getNodeBreakerView().newInternalConnection()
                .setNode1(18)
                .setNode2(4)
                .add();
        voltageLevel1.getNodeBreakerView().newInternalConnection()
                .setNode1(20)
                .setNode2(3)
                .add();
        voltageLevel1.getNodeBreakerView().newInternalConnection()
                .setNode1(23)
                .setNode2(4)
                .add();
        voltageLevel1.getNodeBreakerView().newInternalConnection()
                .setNode1(31)
                .setNode2(3)
                .add();
        voltageLevel1.getNodeBreakerView().newInternalConnection()
                .setNode1(24)
                .setNode2(7)
                .add();
        voltageLevel1.getNodeBreakerView().newInternalConnection()
                .setNode1(32)
                .setNode2(6)
                .add();
        voltageLevel1.getNodeBreakerView().newInternalConnection()
                .setNode1(15)
                .setNode2(7)
                .add();
        voltageLevel1.getNodeBreakerView().newInternalConnection()
                .setNode1(16)
                .setNode2(6)
                .add();

        Substation substation2 = network.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel2 = substation2.newVoltageLevel()
                .setId("V2")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        voltageLevel2.getNodeBreakerView().newBusbarSection()
                .setId("Busbar2_1")
                .setNode(7)
                .add();
        voltageLevel2.getNodeBreakerView().newBreaker()
                .setId("Breaker2_0")
                .setOpen(false)
                .setNode1(12)
                .setNode2(13)
                .add();
        voltageLevel2.getNodeBreakerView().newDisconnector()
                .setId("D2_0")
                .setOpen(false)
                .setNode1(16)
                .setNode2(17)
                .add();
        voltageLevel2.getNodeBreakerView().newInternalConnection()
                .setNode1(13)
                .setNode2(6)
                .add();
        voltageLevel2.getNodeBreakerView().newInternalConnection()
                .setNode1(12)
                .setNode2(3)
                .add();
        voltageLevel2.getNodeBreakerView().newInternalConnection()
                .setNode1(17)
                .setNode2(3)
                .add();
        voltageLevel2.getNodeBreakerView().newInternalConnection()
                .setNode1(16)
                .setNode2(0)
                .add();
        voltageLevel2.getNodeBreakerView().newInternalConnection()
                .setNode1(7)
                .setNode2(0)
                .add();
        Substation substation3 = network.newSubstation()
                .setId("S3")
                .setCountry(Country.FR)
                .add();
        VoltageLevel voltageLevel3 = substation3.newVoltageLevel()
                .setId("V3")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        voltageLevel3.getNodeBreakerView().newBusbarSection()
                .setId("Busbar3_1")
                .setNode(16)
                .add();
        voltageLevel3.getNodeBreakerView().newBreaker()
                .setId("Breaker3_0")
                .setOpen(false)
                .setNode1(27)
                .setNode2(28)
                .add();
        voltageLevel3.getNodeBreakerView().newDisconnector()
                .setId("D3_0")
                .setOpen(false)
                .setNode1(45)
                .setNode2(46)
                .add();
        voltageLevel3.getNodeBreakerView().newInternalConnection()
                .setNode1(28)
                .setNode2(4)
                .add();
        voltageLevel3.getNodeBreakerView().newInternalConnection()
                .setNode1(27)
                .setNode2(9)
                .add();
        voltageLevel3.getNodeBreakerView().newInternalConnection()
                .setNode1(46)
                .setNode2(9)
                .add();
        voltageLevel3.getNodeBreakerView().newInternalConnection()
                .setNode1(45)
                .setNode2(7)
                .add();
        voltageLevel3.getNodeBreakerView().newInternalConnection()
                .setNode1(16)
                .setNode2(7)
                .add();

        network.newLine()
                .setId("Line1")
                .setVoltageLevel1(voltageLevel1.getId())
                .setVoltageLevel2(voltageLevel2.getId())
                .setR(5.0)
                .setX(32.0)
                .setG1(2.0)
                .setB1(386E-6 / 2)
                .setG2(2.0)
                .setB2(386E-6 / 2)
                .setNode1(34)
                .setNode2(22)
                .add();
        network.newLine()
                .setId("Line2")
                .setVoltageLevel1(voltageLevel1.getId())
                .setVoltageLevel2(voltageLevel3.getId())
                .setR(4.0)
                .setX(34.0)
                .setG1(1.0)
                .setB1(386E-6 / 2)
                .setG2(1.0)
                .setB2(386E-6 / 2)
                .setNode1(33)
                .setNode2(51)
                .add();
        voltageLevel1.getNodeBreakerView().newInternalConnection()
                .setNode1(34)
                .setNode2(7)
                .add();
        voltageLevel1.getNodeBreakerView().newInternalConnection()
                .setNode1(33)
                .setNode2(6)
                .add();
        voltageLevel2.getNodeBreakerView().newInternalConnection()
                .setNode1(22)
                .setNode2(6)
                .add();
        voltageLevel2.getNodeBreakerView().newInternalConnection()
                .setNode1(13)
                .setNode2(6)
                .add();
        voltageLevel3.getNodeBreakerView().newInternalConnection()
                .setNode1(51)
                .setNode2(4)
                .add();
        voltageLevel3.getNodeBreakerView().newInternalConnection()
                .setNode1(28)
                .setNode2(4)
                .add();

        return network;
    }
}
