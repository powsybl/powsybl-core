/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DirectoryDataSource;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import org.junit.jupiter.api.Test;

import static com.powsybl.commons.test.ComparisonUtils.assertTxtEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Properties;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class PsseFullExportTest extends AbstractSerDeTest {

    private void exportTest(Network network, String baseName, String extensionName) throws IOException {
        String pathName = "/work/";
        String fileName = baseName + "." + extensionName;
        Path path = fileSystem.getPath(pathName);
        Path file = fileSystem.getPath(pathName + fileName);

        Properties properties = null;
        if (extensionName.equals("rawx")) {
            properties = new Properties();
            properties.put("psse.export.raw-format", false);
        }
        DataSource dataSource = new DirectoryDataSource(path, baseName);
        new PsseExporter().export(network, properties, dataSource);

        try (InputStream is = Files.newInputStream(file)) {
            assertTxtEquals(getClass().getResourceAsStream("/" + fileName), is);
        }
    }

    @Test
    void fullExportRawTest() throws IOException {
        Network network = createNetwork();
        exportTest(network, "full_export", "raw");
    }

    @Test
    void fullExportRawxTest() throws IOException {
        Network network = createNetwork();
        exportTest(network, "full_export", "rawx");
    }

    private Network createNetwork() {
        Network network = Network.create("Psse.fullExport", "no-format");
        network.setCaseDate(ZonedDateTime.parse("2016-01-01T10:00:00.000+02:00"));

        Substation sub1 = createSubstation(network, "Sub1");
        VoltageLevel vl1S1 = createVoltageLevel(sub1, "Vl1-Sub1", 400.0, TopologyKind.NODE_BREAKER);
        createSwitch(vl1S1, "Sw1-Vl1-Sub1", 1, 2, false);
        createSwitch(vl1S1, "Sw-Line-Vl1-Sub1-Sub2", 1, 3, false);
        createSwitch(vl1S1, "Sw-Gen-Vl1-Sub1", 2, 4, false);
        createSwitch(vl1S1, "Sw-Line-Vl1-Sub1-Sub7", 2, 5, false);
        createSwitch(vl1S1, "Sw-Line-Vl1-Sub1-Sub4", 2, 6, false);
        createSwitch(vl1S1, "Sw-Line-Vl1-Sub1-Sub5", 2, 7, false);
        Generator gen = createGenerator(vl1S1, "Gen-Vl1-Sub1", 4, 50.0, 10.0, 405.0, true);
        // define the slack bus
        vl1S1.newExtension(SlackTerminalAdder.class).withTerminal(gen.getTerminal()).add();

        Substation sub2 = createSubstation(network, "Sub2");
        VoltageLevel vl1S2 = createVoltageLevel(sub2, "Vl1-Sub2", 400.0, TopologyKind.NODE_BREAKER);
        createSwitch(vl1S2, "Sw-Line-Vl1-Sub2-Sub1", 1, 2, false);
        createSwitch(vl1S2, "Sw-Line-Vl1-Sub2-Sub3", 1, 3, false);
        createSwitch(vl1S2, "Sw-Vsc-Vl1-Sub2-Sub4", 1, 4, false);
        createSwitch(vl1S2, "Sw-Lcc-Vl1-Sub2-Sub5", 1, 5, false);
        createSwitch(vl1S2, "Sw-DanglingLine-Vl1-Sub2", 1, 6, false);
        DanglingLine dlVl1S2 = createDanglingLine(vl1S2, "DanglingLine-Vl1-Sub2", 6, 5.0, 2.0, "TieLine");

        Substation sub3 = createSubstation(network, "Sub3");
        VoltageLevel vl1S3 = createVoltageLevel(sub3, "Vl1-Sub3", 400.0, TopologyKind.NODE_BREAKER);
        createSwitch(vl1S3, "Sw-Line-Vl1-Sub3-Sub2", 1, 2, false);
        createSwitch(vl1S3, "Sw-Load-Vl1-Sub3", 1, 3, false);
        createSwitch(vl1S3, "Sw-T2w-Vl1-Sub3", 1, 4, false);
        createLoad(vl1S3, "Load-Vl1-Sub3", 3, 25.0, 5.0);

        VoltageLevel vl2S3 = createVoltageLevel(sub3, "Vl2-Sub3", 25.0, TopologyKind.BUS_BREAKER);
        Bus busVl2S3 = createBus(vl2S3, "Bus-Vl2-Sub3");
        createLoad(vl2S3, "Load-Vl2-Sub3", busVl2S3.getId(), 10.0, 2.0);

        TwoWindingsTransformer t2w = sub3.newTwoWindingsTransformer()
                .setId("T2w-Vl1-Vl2-Sub3")
                .setName("T2w-Vl1-Vl2-Sub3")
                .setVoltageLevel1(vl1S3.getId())
                .setVoltageLevel2(vl2S3.getId())
                .setNode1(4)
                .setBus2(busVl2S3.getId())
                .setConnectableBus2(busVl2S3.getId())
                .setR(0.001)
                .setX(0.01)
                .setG(0.0001)
                .setB(-0.0002)
                .setRatedU1(vl1S3.getNominalV())
                .setRatedU2(vl2S3.getNominalV() * 1.01)
                .setRatedS(100.0)
                .add();
        t2w.newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(2)
                .beginStep().setRho(1.05).endStep()
                .beginStep().setRho(1.0).endStep()
                .beginStep().setRho(0.95).endStep()
                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
                .setTargetV(vl2S3.getNominalV() * 1.02)
                .setRegulationTerminal(t2w.getTerminal2());
        t2w.newOperationalLimitsGroup1("ApparentPowerLimits")
                .newApparentPowerLimits()
                .setPermanentLimit(95.0)
                .beginTemporaryLimit()
                .setName("TemporaryApparentPowerLimit")
                .setAcceptableDuration(60)
                .setValue(115.0)
                .endTemporaryLimit()
                .add();

        Substation sub4 = createSubstation(network, "Sub4");
        VoltageLevel vl1S4 = createVoltageLevel(sub4, "Vl1-Sub4", 400.0, TopologyKind.NODE_BREAKER);
        createSwitch(vl1S4, "Sw-Vsc-Vl1-Sub4-Sub2", 1, 2, false);
        createSwitch(vl1S4, "Sw-FixedShunt-Vl1-Sub4", 1, 3, false);
        createSwitch(vl1S4, "Sw-T3w-Vl1-Sub4", 1, 4, false);
        createSwitch(vl1S4, "Sw-Line-Vl1-Sub4-sub1", 1, 5, false);
        ShuntCompensator shunt = vl1S4.newShuntCompensator()
                .setId("FixedShunt-Vl1-Sub4")
                .setName("FixedShunt-Vl1-Sub4")
                .setNode(3)
                .setSectionCount(1)
                .newLinearModel()
                .setMaximumSectionCount(1)
                .setGPerSection(0.001)
                .setBPerSection(0.1)
                .add()
                .setVoltageRegulatorOn(false)
                .add();

        VoltageLevel vl2S4 = createVoltageLevel(sub4, "Vl2-Sub4", 110.0, TopologyKind.NODE_BREAKER);
        createSwitch(vl2S4, "Sw-T3w-Vl2-Sub4", 1, 2, false);
        createSwitch(vl2S4, "Sw-SwitchedShunt-Vl2-Sub4", 1, 3, false);
        createSwitch(vl2S4, "Sw-Load-Vl2-Sub4", 1, 4, false);
        vl2S4.newShuntCompensator()
                .setId("SwitchedShunt-Vl2-Sub4")
                .setName("SwitchedShunt-Vl2-Sub4")
                .setNode(3)
                .setSectionCount(1)
                .newLinearModel()
                .setMaximumSectionCount(2)
                .setGPerSection(0.001)
                .setBPerSection(0.1)
                .add()
                .setTargetV(vl2S4.getNominalV() * 1.01)
                .setTargetDeadband(0.5)
                .setVoltageRegulatorOn(true)
                .add();
        createLoad(vl2S4, "Load-Vl2-Sub4", 4, 12.0, 4.0);

        VoltageLevel vl3S4 = createVoltageLevel(sub4, "Vl3-Sub4", 25.0, TopologyKind.NODE_BREAKER);
        createSwitch(vl3S4, "Sw-T3w-Vl3-Sub4", 1, 2, false);
        createSwitch(vl3S4, "Sw-Gen-Vl3-Sub4", 1, 3, false);
        createSwitch(vl3S4, "Sw-StaticVar-Vl3-Sub4", 1, 4, false);
        createGenerator(vl3S4, "Gen-Vl3-Sub4", 3, 0.5, 5.5, 25.0, false);
        vl3S4.newStaticVarCompensator()
                .setId("StaticVar-Vl3-Sub4")
                .setName("StaticVar-Vl3-Sub4")
                .setNode(4)
                .setBmin(0.0)
                .setBmax(10.0)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setRegulatingTerminal(shunt.getTerminal())
                .setVoltageSetpoint(vl1S4.getNominalV() * 1.02)
                .setReactivePowerSetpoint(0.0)
                .add();

        ThreeWindingsTransformer t3w = sub4.newThreeWindingsTransformer()
                .setId("T3w-Vl1-Vl2-Vl3-Sub4")
                .setName("T3w-Vl1-Vl2-Vl3-Sub4")
                .setRatedU0(vl1S4.getNominalV())
                .newLeg1()
                .setVoltageLevel(vl1S4.getId())
                .setNode(4)
                .setR(0.0)
                .setX(0.1)
                .setG(0.0001)
                .setB(-0.002)
                .setRatedU(vl1S4.getNominalV() * 1.03)
                .setRatedS(200.0)
                .add()
                .newLeg2()
                .setVoltageLevel(vl2S4.getId())
                .setNode(2)
                .setR(0.001)
                .setX(0.2)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(vl2S4.getNominalV())
                .setRatedS(150.0)
                .add()
                .newLeg3()
                .setVoltageLevel(vl3S4.getId())
                .setNode(2)
                .setR(0.002)
                .setX(0.25)
                .setG(0.0)
                .setB(-0.0025)
                .setRatedU(vl3S4.getNominalV())
                .setRatedS(50)
                .add()
                .add();
        t3w.getLeg1().newRatioTapChanger()
                .setTapPosition(0)
                .setLowTapPosition(0)
                .beginStep()
                .setRho(1.02)
                .endStep()
                .beginStep()
                .setRho(1.0)
                .endStep()
                .beginStep()
                .setRho(0.98)
                .endStep()
                .setRegulationTerminal(t3w.getLeg2().getTerminal())
                .setTargetV(vl2S4.getNominalV() * 0.99)
                .setTargetDeadband(0.5)
                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE);
        t3w.getLeg1().newOperationalLimitsGroup("ActivePowerLimits")
                .newActivePowerLimits()
                .setPermanentLimit(210.0)
                .beginTemporaryLimit()
                .setName("TemporaryActivePowerLimit")
                .setAcceptableDuration(120)
                .setValue(225.0)
                .endTemporaryLimit()
                .add();

        Substation sub5 = createSubstation(network, "Sub5");
        VoltageLevel vl1S5 = createVoltageLevel(sub5, "Vl1-Sub5", 400.0, TopologyKind.NODE_BREAKER);
        createSwitch(vl1S5, "Sw-Lcc-Vl1-Sub5-Sub2", 1, 2, false);
        createSwitch(vl1S5, "Sw-DanglingLine-Vl1-Sub5", 1, 3, false);
        createSwitch(vl1S5, "Sw-Gen-Vl1-Sub5", 1, 4, true);
        createSwitch(vl1S5, "Sw-Load-Vl1-Sub5", 1, 5, false);
        createSwitch(vl1S5, "Sw-Battery-Vl1-Sub5", 1, 6, false);
        createSwitch(vl1S5, "Sw-Line-Vl1-Sub5-Sub1", 1, 7, false);
        createDanglingLine(vl1S5, "DanglingLine-Vl1-Sub5", 3, 7.0, 1.25, "");
        createGenerator(vl1S5, "Gen-Vl1-Sub5", 4, 2.0, 2.0, vl1S5.getNominalV(), false);
        createLoad(vl1S5, "Load-Vl1-Sub5", 5, 2.0, 0.25);
        vl1S5.newBattery()
                .setId("Battery-Vl1-Sub5")
                .setName("Battery-Vl1-Sub5")
                .setNode(6)
                .setMinP(0.0)
                .setMaxP(50.0)
                .setTargetP(14.0)
                .setTargetQ(3.5)
                .add();

        Substation sub6 = createSubstation(network, "Sub6");
        VoltageLevel vl1S6 = createVoltageLevel(sub6, "Vl1-Sub6", 400.0, TopologyKind.NODE_BREAKER);
        createSwitch(vl1S6, "Sw-DanglingLine-Vl1-Sub6", 1, 2, false);
        createSwitch(vl1S6, "Sw-Load-Vl1-Sub6", 1, 3, false);
        DanglingLine dlVl1S6 = createDanglingLine(vl1S6, "DanglingLine-Vl1-Sub6", 2, -5.0, -2.0, "TieLine");
        createLoad(vl1S6, "Load-Vl1-Sub6", 3, 5.0, 2.0);

        Substation sub7 = createSubstation(network, "Sub7");
        VoltageLevel vl1S7 = createVoltageLevel(sub7, "Vl1-Sub7", 400.0, TopologyKind.BUS_BREAKER);
        Bus busVl1S7 = createBus(vl1S7, "Bus-Vl1-Sub7");
        createLoad(vl1S7, "Load-Vl1-Sub7", busVl1S7.getId(), 5.0, 0.5);

        // Lines
        createLine(network, "Line-Vl1-Sub1-Vl1-Sub2", vl1S1, vl1S2, 3, 2);
        createLine(network, "Line-Vl1-Sub2-Vl1-Sub3", vl1S2, vl1S3, 3, 2);
        createLine(network, "Line-Vl1-Sub1-Vl1-Sub7", vl1S1, vl1S7, 5, busVl1S7);
        createLine(network, "Line-Vl1-Sub1-Vl1-Sub4", vl1S1, vl1S4, 6, 5);
        createLine(network, "Line-Vl1-Sub1-Vl1-Sub5", vl1S1, vl1S5, 7, 7);

        // HvdcLines
        VscConverterStation vsc1 = vl1S2.newVscConverterStation()
                .setId("Vsc-Vl1-Sub2")
                .setName("Vsc-Vl1-Sub2")
                .setNode(4)
                .setLossFactor(0.001f)
                .setReactivePowerSetpoint(0.0)
                .setVoltageSetpoint(vl1S2.getNominalV())
                .setVoltageRegulatorOn(true)
                .add();
        vsc1.newMinMaxReactiveLimits().setMinQ(-250.0).setMaxQ(300.0).add();
        VscConverterStation vsc2 = vl1S4.newVscConverterStation()
                .setId("Vsc-Vl1-Sub4")
                .setName("Vsc-Vl1-Sub4")
                .setNode(2)
                .setLossFactor(0.002f)
                .setReactivePowerSetpoint(0.1)
                .setVoltageSetpoint(vl1S4.getNominalV())
                .setVoltageRegulatorOn(false)
                .add();
        vsc2.newMinMaxReactiveLimits().setMinQ(-260.0).setMaxQ(310.0).add();
        network.newHvdcLine()
                .setId("Vsc-Vl1-Sub2-Vl1-Sub4")
                .setName("Vsc-Vl1-Sub2-Vl1-Sub4")
                .setNominalV(vl1S2.getNominalV())
                .setConverterStationId1(vsc1.getId())
                .setConverterStationId2(vsc2.getId())
                .setR(0.001)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)
                .setMaxP(310.0)
                .setActivePowerSetpoint(25.0)
                .add();

        LccConverterStation lcc1 = vl1S2.newLccConverterStation()
                .setId("Lcc-Vl1-Sub2")
                .setName("Lcc-Vl1-Sub2")
                .setNode(5)
                .setLossFactor(0.002f)
                .setPowerFactor(0.95f)
                .add();
        LccConverterStation lcc2 = vl1S5.newLccConverterStation()
                .setId("Lcc-Vl1-Sub5")
                .setName("Lcc-Vl1-Sub5")
                .setNode(2)
                .setLossFactor(0.0021f)
                .setPowerFactor(0.98f)
                .add();
        network.newHvdcLine()
                .setId("Lcc-Vl1-Sub2-Vl1-Sub5")
                .setName("Lcc-Vl1-Sub2-Vl1-Sub5")
                .setNominalV(vl1S2.getNominalV())
                .setConverterStationId1(lcc1.getId())
                .setConverterStationId2(lcc2.getId())
                .setR(0.0015)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)
                .setMaxP(210.0)
                .setActivePowerSetpoint(35.0)
                .add();

        // TieLine
        network.newTieLine()
                .setId("TieLine-Vl1-Sub2-Vl1-Sub6")
                .setName("TieLine-Vl1-Sub2-Vl1-Sub6")
                .setDanglingLine1(dlVl1S2.getId())
                .setDanglingLine2(dlVl1S6.getId())
                .add();

        return network;
    }

    private static Substation createSubstation(Network network, String substationId) {
        return network.newSubstation()
                .setId(substationId)
                .setName(substationId)
                .add();
    }

    private static VoltageLevel createVoltageLevel(Substation substation, String voltageLevelId, double nominalV, TopologyKind topologyKind) {
        return substation.newVoltageLevel()
                .setId(voltageLevelId)
                .setName(voltageLevelId)
                .setNominalV(nominalV)
                .setTopologyKind(topologyKind)
                .add();
    }

    private static void createSwitch(VoltageLevel voltageLevel, String switchId, int node1, int node2, boolean isOpen) {
        assertSame(TopologyKind.NODE_BREAKER, voltageLevel.getTopologyKind());
        voltageLevel.getNodeBreakerView().newSwitch()
                .setId(switchId)
                .setName(switchId)
                .setKind(SwitchKind.BREAKER)
                .setNode1(node1)
                .setNode2(node2)
                .setOpen(isOpen)
                .add();
    }

    private static Generator createGenerator(VoltageLevel voltageLevel, String generatorId, int node, double targetP, double targetQ, double targetV, boolean isRegulating) {
        assertSame(TopologyKind.NODE_BREAKER, voltageLevel.getTopologyKind());
        Generator gen = voltageLevel.newGenerator()
                .setId(generatorId)
                .setName(generatorId)
                .setNode(node)
                .setMinP(1.0)
                .setMaxP(100.0)
                .setRatedS(125.0)
                .setTargetP(targetP)
                .setTargetQ(targetQ)
                .setTargetV(targetV)
                .setVoltageRegulatorOn(false)
                .add();
        gen.setRegulatingTerminal(gen.getTerminal())
                .setVoltageRegulatorOn(isRegulating);
        gen.newMinMaxReactiveLimits().setMinQ(-225.0).setMaxQ(230.0).add();

        return gen;
    }

    private static void createLoad(VoltageLevel voltageLevel, String loadId, int node, double p, double q) {
        assertSame(TopologyKind.NODE_BREAKER, voltageLevel.getTopologyKind());
        voltageLevel.newLoad()
                .setId(loadId)
                .setName(loadId)
                .setNode(node)
                .setP0(p)
                .setQ0(q)
                .setLoadType(LoadType.UNDEFINED)
                .add();
    }

    private static Bus createBus(VoltageLevel voltageLevel, String busId) {
        assertSame(TopologyKind.BUS_BREAKER, voltageLevel.getTopologyKind());
        return voltageLevel.getBusBreakerView().newBus()
                .setId(busId)
                .setName(busId)
                .add();
    }

    private static void createLoad(VoltageLevel voltageLevel, String loadId, String busId, double p, double q) {
        assertSame(TopologyKind.BUS_BREAKER, voltageLevel.getTopologyKind());
        voltageLevel.newLoad()
                .setId(loadId)
                .setName(loadId)
                .setBus(busId)
                .setConnectableBus(busId)
                .setP0(p)
                .setQ0(q)
                .setLoadType(LoadType.UNDEFINED)
                .add();
    }

    private static DanglingLine createDanglingLine(VoltageLevel voltageLevel, String danglingLineId, int node, double p0, double q0, String pairingKey) {
        return voltageLevel.newDanglingLine()
                .setId(danglingLineId)
                .setName(danglingLineId)
                .setNode(node)
                .setR(10.0)
                .setX(90.0)
                .setG(0.02)
                .setB(1.2)
                .setP0(p0)
                .setQ0(q0)
                .setPairingKey(pairingKey)
                .add();
    }

    private static void createLine(Network network, String lineId, VoltageLevel vl1, VoltageLevel vl2, int node1, int node2) {
        createLine(network, lineId, vl1, vl2, node1, node2, null);
    }

    private static void createLine(Network network, String lineId, VoltageLevel vl1, VoltageLevel vl2, int node1, Bus bus2) {
        createLine(network, lineId, vl1, vl2, node1, 0, bus2);
    }

    private static void createLine(Network network, String lineId, VoltageLevel vl1, VoltageLevel vl2, int node1, int node2, Bus bus2) {
        Line line;
        if (bus2 != null) {
            line = network.newLine()
                    .setId(lineId)
                    .setName(lineId)
                    .setVoltageLevel1(vl1.getId())
                    .setVoltageLevel2(vl2.getId())
                    .setNode1(node1)
                    .setBus2(bus2.getId())
                    .setConnectableBus2(bus2.getId())
                    .setR(15.0)
                    .setX(100.0)
                    .add();
        } else {
            line = network.newLine()
                    .setId(lineId)
                    .setName(lineId)
                    .setVoltageLevel1(vl1.getId())
                    .setVoltageLevel2(vl2.getId())
                    .setNode1(node1)
                    .setNode2(node2)
                    .setR(10.0)
                    .setX(90.0)
                    .add();
        }
        line.setG1(0.0)
                .setB1(0.0)
                .setG2(0.0)
                .setB2(0.0);
        line.newCurrentLimits1()
                .setPermanentLimit(500.0)
                .beginTemporaryLimit()
                .setName("TemporaryCurrentLimits")
                .setAcceptableDuration(180)
                .setValue(550.0)
                .endTemporaryLimit()
                .add();
    }
}
