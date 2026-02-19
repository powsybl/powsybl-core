/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DirectoryDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.test.TestUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.NetworkFactoryImpl;

import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

import com.powsybl.iidm.serde.XMLImporter;
import com.powsybl.psse.converter.extensions.PsseModelExtension;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import com.powsybl.psse.model.pf.PssePowerFlowModel;
import org.junit.jupiter.api.Test;

import static com.powsybl.commons.test.ComparisonUtils.assertTxtEquals;
import static com.powsybl.psse.model.PsseVersion.fromRevision;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class PsseExporterTest extends AbstractSerDeTest {

    private Network importTest(String basename, String filename, boolean ignoreBaseVoltage) {
        Properties properties = new Properties();
        properties.put("psse.import.ignore-base-voltage", ignoreBaseVoltage);

        ReadOnlyDataSource dataSource = new ResourceDataSource(basename, new ResourceSet("/", filename));
        Network network = new PsseImporter().importData(dataSource, new NetworkFactoryImpl(), properties);
        network.setCaseDate(ZonedDateTime.parse("2016-01-01T10:00:00.000+02:00"));
        return network;
    }

    private void exportTest(Network network, String baseName, String fileName) throws IOException {
        String pathName = "/work/";
        Path path = fileSystem.getPath(pathName);
        Path file = fileSystem.getPath(pathName + fileName);

        DataSource dataSource = new DirectoryDataSource(path, baseName);
        new PsseExporter().export(network, null, dataSource);

        try (InputStream is = Files.newInputStream(file)) {
            assertTxtEquals(getClass().getResourceAsStream("/" + fileName), is);
        }
    }

    @Test
    void importExportTest14() throws IOException {
        Network network = importTest("IEEE_14_bus", "IEEE_14_bus.raw", false);
        exportTest(network, "IEEE_14_bus_exported", "IEEE_14_bus_exported.raw");
    }

    @Test
    void importExportTest14Completed() throws IOException {
        Network network = importTest("IEEE_14_bus_completed", "IEEE_14_bus_completed.raw", false);
        exportTest(network, "IEEE_14_bus_completed_exported", "IEEE_14_bus_completed_exported.raw");
    }

    @Test
    void importExportTest24() throws IOException {
        Network network = importTest("IEEE_24_bus", "IEEE_24_bus.raw", false);
        changeIEEE24BusNetwork(network);
        exportTest(network, "IEEE_24_bus_updated_exported", "IEEE_24_bus_updated_exported.raw");

        // check that the psseModel associated with the network has not been changed
        PssePowerFlowModel psseModel = network.getExtension(PsseModelExtension.class).getPsseModel();
        String jsonRef = loadJsonReference("IEEE_24_bus.json");
        assertEquals(jsonRef, toJsonString(psseModel));
    }

    private static void changeIEEE24BusNetwork(Network network) {
        network.getBusBreakerView().getBuses().forEach(bus -> {
            bus.setV(bus.getV() * 1.01);
            bus.setAngle(bus.getAngle() * 1.01);
        });

        Load load21 = network.getLoad("B2-L1 ");
        load21.getTerminal().disconnect();

        Load load31 = network.getLoad("B3-L1 ");
        load31.getTerminal().setP(Double.NaN);
        load31.getTerminal().setQ(Double.NaN);

        Generator gen11 = network.getGenerator("B1-G1 ");
        gen11.getTerminal().disconnect();

        Generator gen21 = network.getGenerator("B2-G1 ");
        gen21.getTerminal().setP(Double.NaN);
        gen21.getTerminal().setQ(Double.NaN);

        ShuntCompensator sh11 = network.getShuntCompensator("B1-SwSH1");
        sh11.getTerminal().disconnect();

        Line line = network.getLine("L-1-2-1 ");
        line.getTerminal1().disconnect();
        line.getTerminal2().disconnect();

        TwoWindingsTransformer tw2t = network.getTwoWindingsTransformer("T-24-3-1 ");
        tw2t.getTerminal1().disconnect();
        tw2t.getTerminal2().disconnect();
    }

    private static String toJsonString(PssePowerFlowModel rawData) throws JsonProcessingException {
        PsseVersion version = fromRevision(rawData.getCaseIdentification().getRev());
        SimpleBeanPropertyFilter filter = new SimpleBeanPropertyFilter() {
            @Override
            protected boolean include(PropertyWriter writer) {
                Revision rev = writer.getAnnotation(Revision.class);
                return rev == null || PsseVersioned.isValidVersion(version, rev);
            }
        };
        FilterProvider filters = new SimpleFilterProvider().addFilter("PsseVersionFilter", filter);
        String json = new ObjectMapper().writerWithDefaultPrettyPrinter().with(filters).writeValueAsString(rawData);
        return TestUtil.normalizeLineSeparator(json);
    }

    private String loadJsonReference(String fileName) {
        try {
            InputStream is = getClass().getResourceAsStream("/" + fileName);
            return TestUtil.normalizeLineSeparator(new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void importExportTest30() throws IOException {
        Network network = importTest("IEEE_30_bus", "IEEE_30_bus.raw", false);
        exportTest(network, "IEEE_30_bus_updated_exported", "IEEE_30_bus_updated_exported.raw");
    }

    @Test
    void importExportTest57() throws IOException {
        Network network = importTest("IEEE_57_bus", "IEEE_57_bus.raw", false);
        exportTest(network, "IEEE_57_bus_updated_exported", "IEEE_57_bus_updated_exported.raw");
    }

    @Test
    void importExportTest118() throws IOException {
        Network network = importTest("IEEE_118_bus", "IEEE_118_bus.raw", false);
        exportTest(network, "IEEE_118_bus_updated_exported", "IEEE_118_bus_updated_exported.raw");
    }

    @Test
    void importExportTestT3W() throws IOException {
        Network network = importTest("ThreeMIB_T3W_modified", "ThreeMIB_T3W_modified.raw", false);
        exportTest(network, "ThreeMIB_T3W_modified_exported", "ThreeMIB_T3W_modified_exported.raw");
    }

    @Test
    void importExportTestT3Wphase() throws IOException {
        Network network = importTest("ThreeMIB_T3W_phase", "ThreeMIB_T3W_phase.raw", false);
        exportTest(network, "ThreeMIB_T3W_phase_exported", "ThreeMIB_T3W_phase_exported.raw");
    }

    @Test
    void importExportRemoteControl() throws IOException {
        Network network = importTest("remoteControl", "remoteControl.raw", false);
        exportTest(network, "remoteControl_updated_exported", "remoteControl_updated_exported.raw");
    }

    @Test
    void importExportExampleVersion32() throws IOException {
        Network network = importTest("ExampleVersion32", "ExampleVersion32.raw", false);
        exportTest(network, "ExampleVersion32_exported", "ExampleVersion32_exported.raw");
    }

    @Test
    void importExportSwitchedShunt() throws IOException {
        Network network = importTest("SwitchedShunt", "SwitchedShunt.raw", false);
        exportTest(network, "SwitchedShunt_exported", "SwitchedShunt_exported.raw");
    }

    @Test
    void importExportTwoTerminalDc() throws IOException {
        Network network = importTest("twoTerminalDc", "twoTerminalDc.raw", false);
        exportTest(network, "twoTerminalDc_updated_exported", "twoTerminalDc_updated_exported.raw");
    }

    @Test
    void importExportParallelTwoTerminalDcBetweenSameAcBuses() throws IOException {
        Network network = importTest("parallelTwoTerminalDcBetweenSameAcBuses", "parallelTwoTerminalDcBetweenSameAcBuses.raw", false);
        exportTest(network, "parallelTwoTerminalDcBetweenSameAcBuses_updated_exported", "parallelTwoTerminalDcBetweenSameAcBuses_updated_exported.raw");
    }

    @Test
    void importExportIEEE14BusRev35() throws IOException {
        Network network = importTest("IEEE_14_bus_rev35", "IEEE_14_bus_rev35.raw", false);
        exportTest(network, "IEEE_14_bus_rev35_exported", "IEEE_14_bus_rev35_exported.raw");
    }

    @Test
    void importExportIEEE14BusRev35x() throws IOException {
        Network network = importTest("IEEE_14_bus_rev35", "IEEE_14_bus_rev35.rawx", false);
        exportTest(network, "IEEE_14_bus_rev35_exported", "IEEE_14_bus_rev35_exported.rawx");
    }

    @Test
    void importExportTwoWindingsTransformerPhase() throws IOException {
        Network network = importTest("TwoWindingsTransformerPhase", "TwoWindingsTransformerPhase.raw", false);
        exportTest(network, "TwoWindingsTransformerPhase_exported", "TwoWindingsTransformerPhase_exported.raw");
    }

    @Test
    void importExportRawCaseWithSpecialCharacters() throws IOException {
        Network network = importTest("RawCaseWithSpecialCharacters", "RawCaseWithSpecialCharacters.raw", false);
        exportTest(network, "RawCaseWithSpecialCharacters_exported", "RawCaseWithSpecialCharacters_exported.raw");
    }

    @Test
    void importExportRawxCaseWithSpecialCharacters() throws IOException {
        Network network = importTest("RawxCaseWithSpecialCharacters", "RawxCaseWithSpecialCharacters.rawx", false);
        exportTest(network, "RawxCaseWithSpecialCharacters_exported", "RawxCaseWithSpecialCharacters_exported.rawx");
    }

    @Test
    void importExportTestRaw14NodeBreaker() throws IOException {
        Network network = importTest("IEEE_14_bus_nodeBreaker_rev35", "IEEE_14_bus_nodeBreaker_rev35.raw", false);
        exportTest(network, "IEEE_14_bus_nodeBreaker_rev35_exported", "IEEE_14_bus_nodeBreaker_rev35_exported.raw");
    }

    @Test
    void importExportTestRaw14NodeBreakerSplitBus() throws IOException {
        Network network = importTest("IEEE_14_bus_nodeBreaker_rev35", "IEEE_14_bus_nodeBreaker_rev35.raw", false);

        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        vl1.getNodeBreakerView().getSwitch("VL1-Sw-1-2-1 ").setOpen(true);
        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        vl2.getNodeBreakerView().getSwitch("VL2-Sw-1-2-1 ").setOpen(true);

        exportTest(network, "IEEE_14_bus_nodeBreaker_rev35_split_bus_exported", "IEEE_14_bus_nodeBreaker_rev35_split_bus_exported.raw");
    }

    @Test
    void importExportTestRawFiveBusNodeBreaker() throws IOException {
        Network network = importTest("five_bus_nodeBreaker_rev35", "five_bus_nodeBreaker_rev35.raw", false);
        exportTest(network, "five_bus_nodeBreaker_rev35_exported", "five_bus_nodeBreaker_rev35_exported.raw");
    }

    @Test
    void importExportTestRawFiveBusNodeBreakerSplitBus() throws IOException {
        Network network = importTest("five_bus_nodeBreaker_rev35", "five_bus_nodeBreaker_rev35.raw", false);

        VoltageLevel vl1 = network.getVoltageLevel("VL1");
        vl1.getNodeBreakerView().getSwitch("VL1-Sw-1-2-1 ").setOpen(true);

        VoltageLevel vl2 = network.getVoltageLevel("VL2");
        vl2.getNodeBreakerView().getSwitch("VL2-Sw-1-2-1 ").setOpen(true);
        vl2.getNodeBreakerView().getSwitch("VL2-Sw-1-4-1 ").setOpen(true);

        VoltageLevel vl3 = network.getVoltageLevel("VL3");
        vl3.getNodeBreakerView().getSwitch("VL3-Sw-1-2-1 ").setOpen(true);
        vl3.getNodeBreakerView().getSwitch("VL3-Sw-2-5-1 ").setOpen(true);

        VoltageLevel vl4 = network.getVoltageLevel("VL4");
        vl4.getNodeBreakerView().getSwitch("VL4-Sw-1-2-1 ").setOpen(true);
        vl4.getNodeBreakerView().getSwitch("VL4-Sw-2-4-1 ").setOpen(true);

        VoltageLevel vl5 = network.getVoltageLevel("VL5");
        vl5.getNodeBreakerView().getSwitch("VL5-Sw-1-2-1 ").setOpen(true);
        vl5.getNodeBreakerView().getSwitch("VL5-Sw-1-4-1 ").setOpen(true);

        exportTest(network, "five_bus_nodeBreaker_rev35_split_buses_exported", "five_bus_nodeBreaker_rev35_split_buses_exported.raw");
    }

    @Test
    void exportDataTest() {
        PsseExporter psseExporter = new PsseExporter();

        assertEquals("Update IIDM to PSS/E ", psseExporter.getComment());
        assertEquals("PSS/E", psseExporter.getFormat());
        assertEquals(2, psseExporter.getParameters().size());
        assertEquals("psse.export.update", psseExporter.getParameters().get(0).getName());
        assertEquals("psse.export.raw-format", psseExporter.getParameters().get(1).getName());
    }

    @Test
    void rawExportFailingTest() throws IOException {
        ReadOnlyDataSource ds = new ResourceDataSource("raw-export-failing", new ResourceSet("/", "raw-export-failing.xiidm"));
        Network network = new XMLImporter().importData(ds, new NetworkFactoryImpl(), null);
        exportTest(network, "raw-export-failing", "raw-export-failing.raw");
    }

    @Test
    void exportBusWithoutInjectionInBusBreakerModelTest() throws IOException {
        Network network = createBusBreakerModel();
        exportTest(network, "busWithoutInjectionInBusBreakerModel", "busWithoutInjectionInBusBreakerModel.raw");
    }

    @Test
    void exportBusWithoutInjectionInNodeBreakerModelWithoutSwitchesTest() throws IOException {
        Network network = createNodeBreakerModelWithoutSwitches();
        exportTest(network, "busWithoutInjectionInNodeBreakerModelWithoutSwitches", "busWithoutInjectionInNodeBreakerModelWithoutSwitches.raw");
    }

    @Test
    void exportBusWithoutInjectionInNodeBreakerModelWithSwitchesTest() throws IOException {
        Network network = createNodeBreakerModelWithSwitches();
        exportTest(network, "busWithoutInjectionInNodeBreakerModelWithSwitches", "busWithoutInjectionInNodeBreakerModelWithSwitches.raw");
    }

    @Test
    void exportBusWithoutInjectionInNodeBreakerModelWithIsolatedInternalConnectionTest() throws IOException {
        Network network = createNodeBreakerModelWithSwitches();
        network.getVoltageLevel("voltageLevel3").getNodeBreakerView().newInternalConnection().setNode1(4).setNode2(40).add();

        exportTest(network, "busWithoutInjectionInNodeBreakerModelWithIsolatedInternalConnection", "busWithoutInjectionInNodeBreakerModelWithIsolatedInternalConnection.raw");
    }

    private static Network createBusBreakerModel() {
        Network network = NetworkFactory.findDefault().createNetwork("network", "busBreakerModelTest")
                .setCaseDate(ZonedDateTime.parse("2026-02-16T10:00:00.000+02:00"));
        Substation substation1 = createSubstation(network, "substation1");
        VoltageLevel voltageLevel1 = createVoltageLevel(substation1, "voltageLevel1", TopologyKind.BUS_BREAKER);
        Substation substation2 = createSubstation(network, "substation2");
        VoltageLevel voltageLevel2 = createVoltageLevel(substation2, "voltageLevel2", TopologyKind.BUS_BREAKER);
        Substation substation3 = createSubstation(network, "substation3");
        VoltageLevel voltageLevel3 = createVoltageLevel(substation3, "voltageLevel3", TopologyKind.BUS_BREAKER);

        voltageLevel1.getBusBreakerView().newBus().setId("bus1").add();
        voltageLevel2.getBusBreakerView().newBus().setId("bus2").add();
        voltageLevel3.getBusBreakerView().newBus().setId("bus3").add();

        createGeneratorInBusBreakerModel(voltageLevel1);
        createLoadInBusBreakerModel(voltageLevel2);

        createLineInBusBreakerModel(network, "Line1", voltageLevel1.getId(), "bus1", voltageLevel2.getId(), "bus2");
        Line line2 = createLineInBusBreakerModel(network, "Line2", voltageLevel2.getId(), "bus2", voltageLevel3.getId(), "bus3");
        line2.getTerminal2().disconnect();

        return network;
    }

    private static Network createNodeBreakerModelWithoutSwitches() {
        Network network = NetworkFactory.findDefault().createNetwork("network", "nodeBreakerModelWithoutSwitchesTest")
                .setCaseDate(ZonedDateTime.parse("2026-02-16T10:00:00.000+02:00"));
        Substation substation1 = createSubstation(network, "substation1");
        VoltageLevel voltageLevel1 = createVoltageLevel(substation1, "voltageLevel1", TopologyKind.NODE_BREAKER);
        Substation substation2 = createSubstation(network, "substation2");
        VoltageLevel voltageLevel2 = createVoltageLevel(substation2, "voltageLevel2", TopologyKind.NODE_BREAKER);
        Substation substation3 = createSubstation(network, "substation3");
        VoltageLevel voltageLevel3 = createVoltageLevel(substation3, "voltageLevel3", TopologyKind.NODE_BREAKER);

        voltageLevel1.getNodeBreakerView().newBusbarSection().setId("bus1").setNode(1);
        voltageLevel1.getNodeBreakerView().newInternalConnection().setNode1(1).setNode2(10).add();
        voltageLevel1.getNodeBreakerView().newInternalConnection().setNode1(1).setNode2(11).add();

        voltageLevel2.getNodeBreakerView().newBusbarSection().setId("bus2").setNode(2);
        voltageLevel2.getNodeBreakerView().newInternalConnection().setNode1(2).setNode2(20).add();
        voltageLevel2.getNodeBreakerView().newInternalConnection().setNode1(2).setNode2(21).add();
        voltageLevel2.getNodeBreakerView().newInternalConnection().setNode1(2).setNode2(22).add();

        voltageLevel3.getNodeBreakerView().newBusbarSection().setId("bus3").setNode(3);
        voltageLevel3.getNodeBreakerView().newInternalConnection().setNode1(3).setNode2(30).add();

        createGeneratorInNodeBreakerModel(voltageLevel1);
        createLoadInNodeBreakerModel(voltageLevel2);

        createLineInNodeBreakerModel(network, "Line1", voltageLevel1.getId(), 11, voltageLevel2.getId(), 21);
        Line line2 = createLineInNodeBreakerModel(network, "Line2", voltageLevel2.getId(), 22, voltageLevel3.getId(), 30);
        line2.getTerminal2().disconnect();

        return network;
    }

    private static Network createNodeBreakerModelWithSwitches() {
        Network network = NetworkFactory.findDefault().createNetwork("network", "nodeBreakerModelWithSwitchesTest")
                .setCaseDate(ZonedDateTime.parse("2026-02-16T10:00:00.000+02:00"));
        Substation substation1 = createSubstation(network, "substation1");
        VoltageLevel voltageLevel1 = createVoltageLevel(substation1, "voltageLevel1", TopologyKind.NODE_BREAKER);
        Substation substation2 = createSubstation(network, "substation2");
        VoltageLevel voltageLevel2 = createVoltageLevel(substation2, "voltageLevel2", TopologyKind.NODE_BREAKER);
        Substation substation3 = createSubstation(network, "substation3");
        VoltageLevel voltageLevel3 = createVoltageLevel(substation3, "voltageLevel3", TopologyKind.NODE_BREAKER);

        voltageLevel1.getNodeBreakerView().newBusbarSection().setId("bus1").setNode(1);
        voltageLevel1.getNodeBreakerView().newBreaker().setId("Sw-Gen-Line1-from").setNode1(1).setNode2(10).setOpen(false).add();
        voltageLevel1.getNodeBreakerView().newInternalConnection().setNode1(10).setNode2(11).add();

        voltageLevel2.getNodeBreakerView().newBusbarSection().setId("bus2").setNode(2);
        voltageLevel2.getNodeBreakerView().newBreaker().setId("Sw-Load").setNode1(2).setNode2(20).setOpen(false).add();
        voltageLevel2.getNodeBreakerView().newBreaker().setId("Sw-Line1-To").setNode1(2).setNode2(21).setOpen(false).add();
        voltageLevel2.getNodeBreakerView().newBreaker().setId("Sw-Line2-From").setNode1(2).setNode2(22).setOpen(false).add();

        voltageLevel3.getNodeBreakerView().newBusbarSection().setId("bus3").setNode(3);
        voltageLevel3.getNodeBreakerView().newBreaker().setId("Sw-Line2-To").setNode1(3).setNode2(30).setOpen(true).add();

        createGeneratorInNodeBreakerModel(voltageLevel1);
        createLoadInNodeBreakerModel(voltageLevel2);

        createLineInNodeBreakerModel(network, "Line1", voltageLevel1.getId(), 11, voltageLevel2.getId(), 21);
        createLineInNodeBreakerModel(network, "Line2", voltageLevel2.getId(), 22, voltageLevel3.getId(), 30);

        return network;
    }

    private static Substation createSubstation(Network network, String substationId) {
        return network.newSubstation()
                .setId(substationId)
                .add();
    }

    private static VoltageLevel createVoltageLevel(Substation substation, String voltageLevelId, TopologyKind topologyKind) {
        return substation.newVoltageLevel()
                .setId(voltageLevelId)
                .setNominalV(400.0)
                .setTopologyKind(topologyKind)
                .add();
    }

    private static void createGeneratorInBusBreakerModel(VoltageLevel voltageLevel) {
        Generator generator = voltageLevel.newGenerator()
                .setId("Gen")
                .setTargetP(10.0)
                .setTargetQ(0.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .setMinP(0.0)
                .setMaxP(25.0)
                .setBus("bus1")
                .setConnectableBus("bus1")
                .add();
        generator.newMinMaxReactiveLimits().setMinQ(-10.0).setMaxQ(15.0).add();

    }

    private static void createGeneratorInNodeBreakerModel(VoltageLevel voltageLevel) {
        Generator generator = voltageLevel.newGenerator()
                .setId("Gen")
                .setTargetP(10.0)
                .setTargetQ(0.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .setMinP(0.0)
                .setMaxP(25.0)
                .setNode(10)
                .add();
        generator.newMinMaxReactiveLimits().setMinQ(-10.0).setMaxQ(15.0).add();
    }

    private static void createLoadInBusBreakerModel(VoltageLevel voltageLevel) {
        voltageLevel.newLoad()
                .setId("Load")
                .setP0(10.0)
                .setQ0(3.0)
                .setBus("bus2")
                .setConnectableBus("bus2")
                .add();
    }

    private static void createLoadInNodeBreakerModel(VoltageLevel voltageLevel) {
        voltageLevel.newLoad()
                .setId("Load")
                .setP0(10.0)
                .setQ0(3.0)
                .setNode(20)
                .add();
    }

    private static Line createLineInBusBreakerModel(Network network, String lineId, String voltageLevelId1, String busId1, String voltageLevelId2, String busId2) {
        return network.newLine()
                .setId(lineId)
                .setR(0.001)
                .setX(0.001)
                .setVoltageLevel1(voltageLevelId1)
                .setVoltageLevel2(voltageLevelId2)
                .setBus1(busId1)
                .setConnectableBus1(busId1)
                .setBus2(busId2)
                .setConnectableBus2(busId2)
                .add();
    }

    private static Line createLineInNodeBreakerModel(Network network, String lineId, String voltageLevelId1, int node1, String voltageLevelId2, int node2) {
        return network.newLine()
                .setId(lineId)
                .setR(0.001)
                .setX(0.001)
                .setVoltageLevel1(voltageLevelId1)
                .setVoltageLevel2(voltageLevelId2)
                .setNode1(node1)
                .setNode2(node2)
                .add();
    }
}
