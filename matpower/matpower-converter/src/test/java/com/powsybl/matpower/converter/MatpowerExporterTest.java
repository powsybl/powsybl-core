/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.matpower.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.matpower.model.MatpowerModel;
import com.powsybl.matpower.model.MatpowerModelFactory;
import com.powsybl.matpower.model.MatpowerReader;
import com.powsybl.matpower.model.MatpowerWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.util.Locale;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class MatpowerExporterTest extends AbstractSerDeTest {

    private PlatformConfig platformConfig;

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    private void exportToMatAndCompareTo(Network network, String refJsonFile) throws IOException {
        Properties parameters = new Properties();
        parameters.setProperty(MatpowerExporter.WITH_BUS_NAMES_PARAMETER_NAME, "true");
        exportToMatAndCompareTo(network, refJsonFile, parameters, null);
    }

    private void exportToMatAndCompareTo(Network network, String refJsonFile, Properties parameters) throws IOException {
        exportToMatAndCompareTo(network, refJsonFile, parameters, null);
    }

    private void exportToMatAndCompareTo(Network network, String refJsonFile, Properties parameters, DecimalFormat decimalFormat) throws IOException {
        MemDataSource dataSource = new MemDataSource();
        new MatpowerExporter(platformConfig).export(network, parameters, dataSource);
        byte[] mat = dataSource.getData(null, "mat");
        MatpowerModel model = MatpowerReader.read(new ByteArrayInputStream(mat), network.getId());

        // Map to JSON with a specific serializer for doubles if decimal format is received
        ObjectMapper jsonMapper = new ObjectMapper();
        if (decimalFormat != null) {
            jsonMapper.registerModule(new SimpleModule().addSerializer(double.class, new JsonSerializer<>() {
                @Override
                public void serialize(Double value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                    if (value == null) {
                        jsonGenerator.writeNull();
                    } else {
                        jsonGenerator.writeNumber(decimalFormat.format(value));
                    }
                }
            }));
        }
        String json = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(model);

        ComparisonUtils.assertTxtEquals(MatpowerExporterTest.class.getResourceAsStream(refJsonFile), json);
    }

    @Test
    void testEsgTuto1() throws IOException {
        var network = EurostagTutorialExample1Factory.create();
        exportToMatAndCompareTo(network, "/sim1.json");
    }

    @Test
    void testEsgTuto1WithoutActivePowerLimit() throws IOException {
        var network = EurostagTutorialExample1Factory.create();
        Generator gen = network.getGenerator("GEN");
        gen.newMinMaxReactiveLimits()
                .setMinQ(-Double.MAX_VALUE)
                .setMaxQ(Double.MAX_VALUE)
                .add();
        exportToMatAndCompareTo(network, "/sim1-without-active-power-limit.json");
    }

    @Test
    void testEsgTuto1WithoutBusNames() throws IOException {
        var network = EurostagTutorialExample1Factory.create();
        Properties parameters = new Properties();
        parameters.setProperty(MatpowerExporter.WITH_BUS_NAMES_PARAMETER_NAME, "false");
        exportToMatAndCompareTo(network, "/sim1-without-bus-names.json", parameters);
    }

    @Test
    void testMicroGridBe() throws IOException {
        Network network = Network.read(CgmesConformity1ModifiedCatalog.microGridBaseCaseBERatioPhaseTapChangerTabular().dataSource());
        exportToMatAndCompareTo(network, "/be.json");
    }

    @Test
    void testWithTieLines() throws IOException {
        var network = EurostagTutorialExample1Factory.createWithTieLine();
        exportToMatAndCompareTo(network, "/sim1-with-tie-lines.json");
    }

    @Test
    void testWithHvdcLines() throws IOException {
        var network = FourSubstationsNodeBreakerFactory.create();
        exportToMatAndCompareTo(network, "/fourSubstationFactory.json");
    }

    @Test
    void testCase30ConsideringBaseVoltage() throws IOException {
        MatpowerModel model = MatpowerModelFactory.create30();
        model.setCaseName("ieee30-considering-base-voltage");
        String caseId = model.getCaseName();
        Path matFile = tmpDir.resolve(caseId + ".mat");
        MatpowerWriter.write(model, matFile, true);

        Properties properties = new Properties();
        properties.put("matpower.import.ignore-base-voltage", false);
        Network network = new MatpowerImporter().importData(new FileDataSource(tmpDir, caseId), NetworkFactory.findDefault(), properties);

        exportToMatAndCompareTo(network, "/ieee30-considering-base-voltage.json");
    }

    @Test
    void testNonRegulatingGenOnPVBus() throws IOException {
        var network = EurostagTutorialExample1Factory.create();
        Bus slackBus = network.getBusView().getBus("VLHV1_0");
        SlackTerminal.attach(slackBus);
        network.getVoltageLevel("VLGEN").newGenerator()
                .setId("GEN2")
                .setBus("NGEN")
                .setTargetP(10)
                .setTargetQ(5)
                .setMinP(0)
                .setMaxP(1000)
                .setVoltageRegulatorOn(false)
                .add();
        exportToMatAndCompareTo(network, "/sim1-with-non-regulating-gen.json");
    }

    @Test
    void testWithCurrentLimits() throws IOException {
        var network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        exportToMatAndCompareTo(network, "/sim1-with-current-limits.json");
    }

    @Test
    void testWithCurrentLimits2() throws IOException {
        var network = EurostagTutorialExample1Factory.create();
        Line line = network.getLine("NHV1_NHV2_1");
        line.newCurrentLimits1()
                .setPermanentLimit(1000)
                .beginTemporaryLimit()
                    .setName("20'")
                    .setAcceptableDuration(20 * 60)
                    .setValue(1100)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("10'")
                    .setAcceptableDuration(10 * 60)
                    .setValue(1200)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("1'")
                    .setAcceptableDuration(60)
                    .setValue(1300)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                    .setName("N/A")
                    .setAcceptableDuration(0)
                    .setValue(Double.MAX_VALUE)
                .endTemporaryLimit()
                .add();
        exportToMatAndCompareTo(network, "/sim1-with-current-limits2.json");
    }

    @Test
    void testWithApparentPowerLimits() throws IOException {
        var network = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();
        var l = network.getLine("NHV1_NHV2_1");
        l.newApparentPowerLimits1()
                .setPermanentLimit(1000)
                .beginTemporaryLimit()
                    .setName("1'")
                    .setAcceptableDuration(60)
                    .setValue(1500)
                .endTemporaryLimit()
                .add();
        l.newCurrentLimits2()
                .setPermanentLimit(1000)
                .add();
        exportToMatAndCompareTo(network, "/sim1-with-apparent-power-limits.json");
    }

    @Test
    void testNanTargetQIssue() throws IOException {
        var network = EurostagTutorialExample1Factory.create();
        network.getGenerator("GEN").setTargetQ(Double.NaN);
        exportToMatAndCompareTo(network, "/sim1-with-nan-target-q.json");
    }

    @Test
    void testVscNpeIssue() throws IOException {
        var network = EurostagTutorialExample1Factory.create();
        VoltageLevel vlgen = network.getVoltageLevel("VLGEN");
        vlgen.newVscConverterStation()
                .setId("VSC")
                .setConnectableBus("NGEN")
                .setVoltageRegulatorOn(true)
                .setVoltageSetpoint(100)
                .setLossFactor(0)
                .add();
        exportToMatAndCompareTo(network, "/vsc-npe-issue.json");
    }

    @Test
    void testDanglingLineWithGeneration() throws IOException {
        var network = DanglingLineNetworkFactory.createWithGeneration();
        exportToMatAndCompareTo(network, "/dangling-line-generation.json");
    }

    @Test
    void testLineConnectedToSameBus() throws IOException {
        var network = EurostagTutorialExample1Factory.create();
        network.newLine()
                .setId("NL")
                .setBus1("NGEN")
                .setVoltageLevel1("VLGEN")
                .setBus2("NGEN")
                .setVoltageLevel2("VLGEN")
                .setR(0.1)
                .setX(0.1)
                .add();
        exportToMatAndCompareTo(network, "/line-connected-same-bus.json");
    }

    @Test
    void testSmallImpedanceLine() throws IOException {
        var network = EurostagTutorialExample1Factory.create();
        network.getLine("NHV1_NHV2_1")
                .setR(0.00000001)
                .setX(0.00000003);
        exportToMatAndCompareTo(network, "/small-impedance-line.json");
    }

    @Test
    void testExportCase9DcLine() throws IOException {
        MatpowerModel matpowerModel = MatpowerModelFactory.create9Dcline();
        String caseId = matpowerModel.getCaseName();
        Path matFile = tmpDir.resolve(caseId + ".mat");
        MatpowerWriter.write(matpowerModel, matFile, true);

        var network = new MatpowerImporter().importData(new FileDataSource(tmpDir, caseId), NetworkFactory.findDefault(), null);

        exportToMatAndCompareTo(network, "/t_case9_dcline_exported.json");
    }

    static class DecimalFormat14 extends DecimalFormat {
        private static final DecimalFormatSymbols SYMBOLS = DecimalFormatSymbols.getInstance(Locale.US);
        private static final DecimalFormat SCI = new DecimalFormat("0.0###############E0", SYMBOLS);

        DecimalFormat14() {
            super("0.0", SYMBOLS);
            super.setMaximumFractionDigits(14);
        }

        @Override
        public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
            if (number != 0.0 && Math.abs(number) < 1e-5 || Math.abs(number) > 1e10) {
                return SCI.format(number, result, fieldPosition);
            }
            return super.format(number, result, fieldPosition);
        }
    }

    @Test
    void testBusesToBeExported() throws IOException {
        Network network = createThreeComponentsConnectedByHvdcLinesNetwork();
        Properties parameters = new Properties();
        parameters.setProperty(MatpowerExporter.WITH_BUS_NAMES_PARAMETER_NAME, "true");
        // Write all doubles with a maximum precision of 15 fraction digits to avoid macOS 14 small diffs in output
        exportToMatAndCompareTo(network, "/threeComponentsConnectedByHvdcLines.json", parameters, new DecimalFormat14());
    }

    private static Network createThreeComponentsConnectedByHvdcLinesNetwork() {
        Network network = Network.create("threeComponentsConnectedByHvdcLines", "iidm");

        // Component 1
        VoltageLevel vl11 = network.newSubstation().setId("S11").add()
                .newVoltageLevel().setId("VL11").setNominalV(400.0).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        vl11.getBusBreakerView().newBus().setId("BUS-11").add();

        VoltageLevel vl12 = network.newSubstation().setId("S12").add()
                .newVoltageLevel().setId("VL12").setNominalV(400.0).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        vl12.getBusBreakerView().newBus().setId("BUS-12").add();

        // Component 2
        VoltageLevel vl21 = network.newSubstation().setId("S21").add()
                .newVoltageLevel().setId("VL21").setNominalV(400.0).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        vl21.getBusBreakerView().newBus().setId("BUS-21").add();

        VoltageLevel vl22 = network.newSubstation().setId("S22").add()
                .newVoltageLevel().setId("VL22").setNominalV(400.0).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        vl22.getBusBreakerView().newBus().setId("BUS-22").add();

        // Component 3
        VoltageLevel vl31 = network.newSubstation().setId("S31").add()
                .newVoltageLevel().setId("VL31").setNominalV(400.0).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        vl31.getBusBreakerView().newBus().setId("BUS-31").add();

        VoltageLevel vl32 = network.newSubstation().setId("S32").add()
                .newVoltageLevel().setId("VL32").setNominalV(400.0).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        vl32.getBusBreakerView().newBus().setId("BUS-32").add();

        vl11.newGenerator().setId("GENERATOR-11").setBus("BUS-11").setTargetP(10.0).setTargetQ(8.0).setTargetV(410.0).setMinP(0.0).setMaxP(15.0).setVoltageRegulatorOn(true).add();
        SlackTerminal.attach(network.getBusBreakerView().getBus("BUS-11"));
        vl22.newLoad().setId("LOAD-22").setBus("BUS-22").setP0(5.0).setQ0(4.0).add();
        vl32.newLoad().setId("LOAD-32").setBus("BUS-32").setP0(5.0).setQ0(4.0).add();

        network.newLine().setId("LINE-11-12").setBus1("BUS-11").setBus2("BUS-12").setR(0.0).setX(1.0).add();
        network.newLine().setId("LINE-21-22").setBus1("BUS-21").setBus2("BUS-22").setR(0.0).setX(1.0).add();
        network.newLine().setId("LINE-31-32").setBus1("BUS-31").setBus2("BUS-32").setR(0.0).setX(1.0).add();

        vl12.newLccConverterStation().setId("LCC-12").setBus("BUS-12").setPowerFactor(0.90f).setLossFactor(0.0f).add();
        vl21.newLccConverterStation().setId("LCC-21").setBus("BUS-21").setPowerFactor(0.90f).setLossFactor(0.0f).add();
        network.newHvdcLine().setId("HVDCLINE-12-21").setConverterStationId1("LCC-12").setConverterStationId2("LCC-21").setNominalV(400.0).setActivePowerSetpoint(5.0).setMaxP(5.0).setR(0.0).setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER).add();

        vl12.newVscConverterStation().setId("VSC-12").setBus("BUS-12").setLossFactor(0.0f).setReactivePowerSetpoint(4.0).setVoltageSetpoint(410.0).setVoltageRegulatorOn(true).add();
        vl31.newVscConverterStation().setId("VSC-31").setBus("BUS-31").setLossFactor(0.0f).setReactivePowerSetpoint(4.0).setVoltageSetpoint(410.0).setVoltageRegulatorOn(true).add();
        network.newHvdcLine().setId("HVDCLINE-12-31").setConverterStationId1("VSC-12").setConverterStationId2("VSC-31").setNominalV(400.0).setActivePowerSetpoint(5.0).setMaxP(5.0).setR(0.0).setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER).add();

        return network;
    }

    @Test
    void testStatus() throws IOException {
        Network network = EurostagTutorialExample1Factory.create();

        // add new disconnected equipment
        VoltageLevel vlGen = network.getVoltageLevel("VLGEN");
        assertNotNull(vlGen);

        vlGen.newGenerator()
                .setId("DisconnectedGen1")
                .setConnectableBus("NGEN")
                .setMaxP(250.0)
                .setMinP(0.0)
                .setTargetP(20.0)
                .setTargetQ(0.0)
                .setTargetV(vlGen.getNominalV())
                .setVoltageRegulatorOn(true)
                .add();
        vlGen.newGenerator()
                .setId("DisconnectedGen2")
                .setConnectableBus("NGEN")
                .setMaxP(200.0)
                .setMinP(0.0)
                .setTargetP(30.0)
                .setTargetQ(0.0)
                .setTargetV(vlGen.getNominalV())
                .setVoltageRegulatorOn(false)
                .add();

        network.newLine().setId("DisconnectedLine").setR(0.0).setX(1.0).setConnectableBus1("NHV1").setBus2("NHV2").add();

        VoltageLevel vlHv1 = network.getVoltageLevel("VLHV1");
        assertNotNull(vlHv1);

        vlHv1.getSubstation().orElseThrow().newTwoWindingsTransformer()
                .setId("DisconnectedTwoWindingsTransformer")
                .setR(0.0).setX(1.0)
                .setRatedU1(vlHv1.getNominalV())
                .setRatedU2(vlGen.getNominalV())
                .setConnectableBus1("NHV1")
                .setConnectableBus2("NGEN")
                .add();
        vlHv1.getSubstation().orElseThrow().newThreeWindingsTransformer()
                .setId("DisconnectedThreeWindingsTransformer")
                .setRatedU0(vlHv1.getNominalV())
                .newLeg1()
                .setR(0.0).setX(1.0)
                .setRatedU(vlHv1.getNominalV())
                .setBus("NHV1")
                .add()
                .newLeg2()
                .setR(0.0).setX(2.0)
                .setRatedU(vlGen.getNominalV())
                .setConnectableBus("NGEN")
                .add()
                .newLeg3()
                .setR(0.0).setX(2.0)
                .setRatedU(vlGen.getNominalV())
                .setConnectableBus("NGEN")
                .add()
                .add();

        Properties parameters = new Properties();
        parameters.setProperty(MatpowerExporter.WITH_BUS_NAMES_PARAMETER_NAME, "true");
        exportToMatAndCompareTo(network, "/sim1-with-disconnected-equipment.json");
    }
}
