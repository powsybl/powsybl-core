/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionSerDe;
import com.powsybl.commons.extensions.ExtensionSerDe;
import com.powsybl.commons.io.DeserializerContext;
import com.powsybl.commons.io.SerializerContext;
import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.commons.test.TestUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.*;
import com.powsybl.iidm.serde.extensions.util.NetworkSourceExtension;
import com.powsybl.iidm.serde.extensions.util.NetworkSourceExtensionImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.function.Supplier;

import static com.powsybl.commons.test.ComparisonUtils.assertTxtEquals;
import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class NetworkSerDeTest extends AbstractIidmSerDeTest {

    public static final String TEST_PROPERTY = "test";

    static Network createEurostagTutorialExample1() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00+01:00"));
        return network;
    }

    @Test
    void roundTripTest() throws IOException {
        allFormatsRoundTripTest(createEurostagTutorialExample1(), "eurostag-tutorial-example1.xml", CURRENT_IIDM_VERSION);

        // backward compatibility
        allFormatsRoundTripAllPreviousVersionedXmlTest("eurostag-tutorial-example1.xml");
    }

    @ParameterizedTest
    @EnumSource(value = TreeDataFormat.class, names = {"XML", "JSON"})
    void testSkippedExtension(TreeDataFormat format) throws IOException {
        Network network = NetworkSerDe.read(getNetworkAsStream("/skippedExtensions.xml"));
        Path file = tmpDir.resolve("data");
        NetworkSerDe.write(network, new ExportOptions().setFormat(format), file);

        // Read file with all extensions included (default ImportOptions)
        ReportNode reportNode1 = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("root")
                .build();
        Network networkReadExtensions = NetworkSerDe.read(file,
                new ImportOptions().setFormat(format), null, NetworkFactory.findDefault(), reportNode1);
        Load load1 = networkReadExtensions.getLoad("LOAD1");
        assertNotNull(load1.getExtension(LoadBarExt.class));
        assertNotNull(load1.getExtension(LoadZipModel.class));

        StringWriter sw1 = new StringWriter();
        reportNode1.print(sw1);
        assertEquals("""
                + Root reportNode
                   Validation warnings
                   + Imported extensions
                      Extension loadBar imported.
                      Extension loadZipModel imported.
                """, TestUtil.normalizeLineSeparator(sw1.toString()));

        // Read file with only terminalMockNoSerDe and loadZipModel extensions included
        ReportNode reportNode2 = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("root")
                .build();
        ImportOptions notAllExtensions = new ImportOptions()
                .addIncludedExtension("terminalMockNoSerDe").addIncludedExtension("loadZipModel")
                .setFormat(format);
        Network networkSkippedExtensions = NetworkSerDe.read(file,
                notAllExtensions, null, NetworkFactory.findDefault(), reportNode2);
        Load load2 = networkSkippedExtensions.getLoad("LOAD1");
        assertNull(load2.getExtension(LoadBarExt.class));
        LoadZipModel loadZipModelExt = load2.getExtension(LoadZipModel.class);
        assertNotNull(loadZipModelExt);
        assertEquals(3.0, loadZipModelExt.getA3(), 0.001);

        StringWriter sw2 = new StringWriter();
        reportNode2.print(sw2);
        assertEquals("""
                + Root reportNode
                   Validation warnings
                   + Imported extensions
                      Extension loadZipModel imported.
                """, TestUtil.normalizeLineSeparator(sw2.toString()));
    }

    @Test
    void testNotFoundExtension() throws IOException {
        // Read file with all extensions included (default ImportOptions)
        ReportNode reportNode1 = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("root")
                .build();
        Network networkReadExtensions = NetworkSerDe.read(getNetworkAsStream("/notFoundExtension.xml"),
                new ImportOptions(), null, NetworkFactory.findDefault(), reportNode1);
        Load load1 = networkReadExtensions.getLoad("LOAD");
        assertNotNull(load1.getExtension(LoadBarExt.class));
        assertNotNull(load1.getExtension(LoadZipModel.class));

        StringWriter sw1 = new StringWriter();
        reportNode1.print(sw1);
        assertEquals("""
                + Root reportNode
                   Validation warnings
                   + Imported extensions
                      Extension loadBar imported.
                      Extension loadZipModel imported.
                   + Not found extensions
                      Extension terminalMockNoSerDe not found.
                """, TestUtil.normalizeLineSeparator(sw1.toString()));
    }

    @Test
    void testValidationIssueWithProperties() {
        Network network = createEurostagTutorialExample1();
        network.getGenerator("GEN").setProperty(TEST_PROPERTY, "foo");
        Path xmlFile = tmpDir.resolve("n.xml");
        NetworkSerDe.write(network, xmlFile);
        Network readNetwork = NetworkSerDe.validateAndRead(xmlFile);
        assertEquals("foo", readNetwork.getGenerator("GEN").getProperty(TEST_PROPERTY));
    }

    @Test
    void testGzipGunzip() throws IOException {
        Network network = createEurostagTutorialExample1();
        Path file1 = tmpDir.resolve("n.xml");
        NetworkSerDe.write(network, file1);
        Network network2 = NetworkSerDe.copy(network);
        Path file2 = tmpDir.resolve("n2.xml");
        NetworkSerDe.write(network2, file2);
        assertArrayEquals(Files.readAllBytes(file1), Files.readAllBytes(file2));
    }

    @Test
    void testCopyFormat() {
        Network network = createEurostagTutorialExample1();
        Path file1 = tmpDir.resolve("n.xml");
        NetworkSerDe.write(network, file1);
        Network network2 = NetworkSerDe.copy(network);
        Path file2 = tmpDir.resolve("n2.xml");
        NetworkSerDe.write(network2, file2);
        assertTxtEquals(file1, file2);
        Network network3 = NetworkSerDe.copy(network, TreeDataFormat.BIN);
        Path file3 = tmpDir.resolve("n3.xml");
        NetworkSerDe.write(network3, file3);
        assertTxtEquals(file1, file3);
    }

    @AutoService(ExtensionSerDe.class)
    public static class BusbarSectionExtSerDe extends AbstractExtensionSerDe<BusbarSection, BusbarSectionExt> {

        public BusbarSectionExtSerDe() {
            super("busbarSectionExt", "network", BusbarSectionExt.class, "busbarSectionExt.xsd",
                    "http://www.itesla_project.eu/schema/iidm/ext/busbarSectionExt/1_0", "bbse");
        }

        @Override
        public void write(BusbarSectionExt busbarSectionExt, SerializerContext context) {
            // this method is abstract
        }

        @Override
        public BusbarSectionExt read(BusbarSection busbarSection, DeserializerContext context) {
            context.getReader().readEndNode();
            var bbsExt = new BusbarSectionExt(busbarSection);
            busbarSection.addExtension(BusbarSectionExt.class, bbsExt);
            return bbsExt;
        }
    }

    private static Network writeAndRead(Network network, ExportOptions options) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            NetworkSerDe.write(network, options, os);
            try (InputStream is = new ByteArrayInputStream(os.toByteArray())) {
                return NetworkSerDe.read(is);
            }
        }
    }

    @Test
    void busBreakerExtensions() throws IOException {
        Network network = NetworkTest1Factory.create();
        BusbarSection bb = network.getBusbarSection("voltageLevel1BusbarSection1");
        bb.addExtension(BusbarSectionExt.class, new BusbarSectionExt(bb));

        //Re-import in node breaker
        Network nodeBreakerNetwork = writeAndRead(network, new ExportOptions());

        assertNotSame(network, nodeBreakerNetwork);

        //Check that busbar and its extension is still here
        BusbarSection bb2 = nodeBreakerNetwork.getBusbarSection("voltageLevel1BusbarSection1");
        assertEquals(1, bb2.getExtensions().size());
        assertNotNull(bb2.getExtension(BusbarSectionExt.class));

        //Re-import in bus breaker
        //Check that network is correctly imported, and busbar and its extension are not here any more
        Network busBreakerNetwork = writeAndRead(network, new ExportOptions().setTopologyLevel(TopologyLevel.BUS_BREAKER));
        assertNull(busBreakerNetwork.getBusbarSection("voltageLevel1BusbarSection1"));
    }

    @Test
    void testScada() throws IOException {
        Network network = ScadaNetworkFactory.create();
        assertEquals(ValidationLevel.EQUIPMENT, network.runValidationChecks(false));
        allFormatsRoundTripTest(network, "scadaNetwork.xml", CURRENT_IIDM_VERSION);

        // backward compatibility
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("scadaNetwork.xml", IidmVersion.V_1_7);
    }

    @Test
    void checkWithSpecificEncoding() throws IOException {
        Network network = NetworkTest1Factory.create();
        BusbarSection bb = network.getBusbarSection("voltageLevel1BusbarSection1");
        bb.addExtension(BusbarSectionExt.class, new BusbarSectionExt(bb));
        ExportOptions export = new ExportOptions();
        export.setCharset(StandardCharsets.ISO_8859_1);
        //Re-import in node breaker
        Network nodeBreakerNetwork = writeAndRead(network, export);

        //Check that busbar and its extension is still here
        BusbarSection bb2 = nodeBreakerNetwork.getBusbarSection("voltageLevel1BusbarSection1");
        assertEquals(1, bb2.getExtensions().size());
        assertNotNull(bb2.getExtension(BusbarSectionExt.class));
    }

    @Test
    void failImportWithSeveralSubnetworkLevels() throws URISyntaxException {
        Path path = Path.of(getClass().getResource(getVersionedNetworkPath("multiple-subnetwork-levels.xml",
                CURRENT_IIDM_VERSION)).toURI());
        PowsyblException e = assertThrows(PowsyblException.class, () -> NetworkSerDe.validateAndRead(path));
        assertTrue(e.getMessage().contains("Only one level of subnetworks is currently supported."));
    }

    @Test
    void roundTripWithSubnetworksTest() throws IOException {
        Network n1 = createNetwork(1);
        Network n2 = createNetwork(2);
        n1.setCaseDate(ZonedDateTime.parse("2013-01-15T18:41:00+01:00"));
        n2.setCaseDate(ZonedDateTime.parse("2013-01-15T18:42:00+01:00"));

        Network merged = Network.merge("Merged", n1, n2);
        merged.setCaseDate(ZonedDateTime.parse("2013-01-15T18:40:00+01:00"));
        // add an extension at root network level
        NetworkSourceExtension source = new NetworkSourceExtensionImpl("Source_0");
        merged.addExtension(NetworkSourceExtension.class, source);

        allFormatsRoundTripTest(merged, "subnetworks.xml", IidmSerDeConstants.CURRENT_IIDM_VERSION);

        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("subnetworks.xml", IidmVersion.V_1_5);
    }

    private Network createNetwork(int num) {
        String dlId = "dl" + num;
        String voltageLevelId = "vl" + num;
        String busId = "b" + num;

        Network network = Network.create("Network-" + num, "format");
        Substation s1 = network.newSubstation()
                .setId("s" + num)
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId(voltageLevelId)
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId(busId)
                .add();
        network.getVoltageLevel(voltageLevelId).newDanglingLine()
                .setId(dlId)
                .setName(dlId + "_name")
                .setConnectableBus(busId)
                .setBus(busId)
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.0)
                .setX(2.0)
                .setG(4.0)
                .setB(5.0)
                .setPairingKey("code")
                .add();

        // Add an extension on the network and on an inner element
        NetworkSourceExtension source = new NetworkSourceExtensionImpl("Source_" + num);
        network.addExtension(NetworkSourceExtension.class, source);

        if (num == 1) {
            Generator generator = vl1.newGenerator()
                    .setId("GEN")
                    .setBus(busId)
                    .setConnectableBus(busId)
                    .setMinP(-9999.99)
                    .setMaxP(9999.99)
                    .setVoltageRegulatorOn(true)
                    .setTargetV(24.5)
                    .setTargetP(607.0)
                    .setTargetQ(301.0)
                    .add();
            generator.newMinMaxReactiveLimits()
                    .setMinQ(-9999.99)
                    .setMaxQ(9999.99)
                    .add();
        } else if (num == 2) {
            vl1.newLoad()
                    .setId("LOAD")
                    .setBus(busId)
                    .setConnectableBus(busId)
                    .setP0(600.0)
                    .setQ0(200.0)
                    .add();

            // Add an extension on an inner element
            Load load = network.getLoad("LOAD");
            TerminalMockExt terminalMockExt = new TerminalMockExt(load);
            load.addExtension(TerminalMockExt.class, terminalMockExt);
        }
        return network;
    }

    @Test
    void emptySourceFormatTest() {
        Network network = Network.create("id", "");
        Path xmlFile = tmpDir.resolve("emptySourceFormat.xml");
        testForAllVersionsSince(IidmVersion.V_1_0, iidmVersion -> {
            ExportOptions options = new ExportOptions().setVersion(iidmVersion.toString("."));
            NetworkSerDe.write(network, options, xmlFile);
            Network readNetwork = NetworkSerDe.validateAndRead(xmlFile);
            assertEquals("", readNetwork.getSourceFormat());
        });
    }

    /*
     *   [ ]  Extension / AbstractExtension:
     *   [X]  VoltageLevel
     *   [ ]  TopologyModel / AbstractTopologyModel:
     *   [X]  ReactiveLimits / ReactiveCapabilityCurveImpl & MinMaxReactiveLimitsImpl:
     *   [X]  ReactiveCapabilityCurve.Point / ReactiveCapabilityCurveImpl.PointImpl:
     *   [X]  LoadModel / AbstractLoadModel:
     *   [X]  ShuntCompensatorModel / ShuntCompensatorLinearModelImpl & ShuntCompensatorNonLinearModelImpl:
     *   [X]  ShuntCompensatorNonLinearModel.Section / ShuntCompensatorNonLinearModelImpl.SectionImpl:
     *   [X]  OperationalLimits / AbstractLoadingLimits & AbstractReducedLoadingLimits:  (pas trouvé de serialisation iidm pour AbstractReducedLoadingLimits)
     *   [X]  LoadingLimits / AbstractLoadingLimits.TemporaryLimitImpl & AbstractReducedLoadingLimits.ReducedTemporaryLimit:
     *   [X]  OverloadManagementSystem.Tripping / OverloadManagementSystemImpl.AbstractTrippingImpl:
     *   [X]  TapChangerStep / TapChangerStepImpl:
     *   [X]  TapChanger / AbstractTapChanger:
     *   [X]  AreaBoundary / AreaBoundaryImpl:
     */

    private TwoWindingsTransformer createTwoWindingsTransformer(Substation substation) {
        return substation.newTwoWindingsTransformer()
                .setId("twt2")
                .setName("twt2_name")
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setRatedU1(5.0)
                .setRatedU2(6.0)
                .setVoltageLevel1("vl1")
                .setVoltageLevel2("vl2")
                .setConnectableBus1("busA")
                .setConnectableBus2("busB")
                .add();
    }

    private void createPhaseTapChanger(PhaseTapChangerHolder ptch) {
        PhaseTapChanger phaseTapChanger = ptch.newPhaseTapChanger()
                .setTapPosition(1)
                .setLowTapPosition(0)
                .setRegulating(false)
                .setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                .beginStep()
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setAlpha(5.0)
                .setRho(6.0)
                .endStep()
                .beginStep()
                .setR(1.0)
                .setX(2.0)
                .setG(3.0)
                .setB(4.0)
                .setAlpha(5.0)
                .setRho(6.0)
                .endStep()
                .add();
        phaseTapChanger.setProperty(TEST_PROPERTY, "valuePhaseTapChanger");
        phaseTapChanger.getCurrentStep().setProperty(TEST_PROPERTY, "value");
    }

    private void createRatioTapChanger(RatioTapChangerHolder rtch) {
        RatioTapChanger ratioTapChanger = rtch.newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(1)
                .setLoadTapChangingCapabilities(false)
                .beginStep()
                .setR(39.78473)
                .setX(39.784725)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .endStep()
                .beginStep()
                .setR(39.78474)
                .setX(39.784726)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .endStep()
                .beginStep()
                .setR(39.78475)
                .setX(39.784727)
                .setG(0.0)
                .setB(0.0)
                .setRho(1.0)
                .endStep()
                .add();
        ratioTapChanger.setProperty(TEST_PROPERTY, "valueRatioTapChanger");
        ratioTapChanger.getCurrentStep().setProperty(TEST_PROPERTY, "value");
    }

    @Test
    void propertiesHolderSerDeTest() throws IOException {
        Network network = NetworkTest1Factory.create();

        ReactiveCapabilityCurve reactiveCapabilityCurve = network.getGenerator("generator1").getReactiveLimits(ReactiveCapabilityCurve.class);
        reactiveCapabilityCurve.setProperty(TEST_PROPERTY, "valueReactiveCapabilityCurve");
        reactiveCapabilityCurve.getPoints().iterator().next().setProperty(TEST_PROPERTY, "valueReactiveCapabilityCurvePoint");
        VoltageLevel voltageLevel = network.getVoltageLevel("voltageLevel1");
        voltageLevel.setProperty(TEST_PROPERTY, "valueVoltageLevel");

        Load zipLoad = voltageLevel.newLoad()
                .setId("zipLoad")
                .setNode(3)
                .setP0(10)
                .setQ0(3)
                .newZipModel().setC0p(0.5).setC0q(0.25).setC1p(0.25).setC1q(0.25).setC2p(0.25).setC2q(0.5).add()
                .add();

        zipLoad.setProperty(TEST_PROPERTY, "valueZipLoad");
        zipLoad.getModel().orElseThrow().setProperty(TEST_PROPERTY, "valueZipLoadModel");

        Load expLoad = voltageLevel.newLoad()
                .setId("expLoad")
                .setNode(4)
                .setP0(10)
                .setQ0(3)
                .newExponentialModel().add()
                .add();

        expLoad.setProperty(TEST_PROPERTY, "valueExpLoad");
        expLoad.getModel().orElseThrow().setProperty(TEST_PROPERTY, "valueExpLoadModel");

        ShuntCompensator shuntCompensator = voltageLevel.newShuntCompensator()
                .setId("shunt")
                .setNode(6)
                .setSectionCount(1)
                .setVoltageRegulatorOn(true)
                .setRegulatingTerminal(zipLoad.getTerminal())
                .setTargetV(200)
                .setTargetDeadband(5.0)
                .newLinearModel()
                .setMaximumSectionCount(1)
                .setBPerSection(3)
                .add()
                .add();
        shuntCompensator.setProperty(TEST_PROPERTY, "valueLinearShuntCompensator");
        ShuntCompensatorLinearModel linearModel = (ShuntCompensatorLinearModel) shuntCompensator.getModel();
        linearModel.setProperty(TEST_PROPERTY, "valueLinearShuntCompensatorModel");

        ShuntCompensator nonLinearShuntCompensator = voltageLevel.newShuntCompensator()
                .setId("shuntNonLinear")
                .setNode(8)
                .setSectionCount(1)
                .setVoltageRegulatorOn(true)
                .setRegulatingTerminal(zipLoad.getTerminal())
                .setTargetV(200)
                .setTargetDeadband(5.0)
                .newNonLinearModel()
                .beginSection()
                .setB(1.0)
                .setG(2.0).endSection()
                .add()
                .add();
        nonLinearShuntCompensator.setProperty(TEST_PROPERTY, "valueNonLinearShuntCompensator");
        ShuntCompensatorNonLinearModel nonLinearModel = (ShuntCompensatorNonLinearModel) nonLinearShuntCompensator.getModel();
        nonLinearModel.setProperty(TEST_PROPERTY, "valueNonLinearShuntCompensatorModel");
        for (ShuntCompensatorNonLinearModel.Section s : nonLinearModel.getAllSections()) {
            s.setProperty(TEST_PROPERTY, "valueNonLinearShuntCompensatorModelSection");
        }

        Area defaultControlArea = network.newArea().setId("defaultControlArea").setAreaType(TEST_PROPERTY).add();
        defaultControlArea.setProperty(TEST_PROPERTY, "testValue");

        BusbarSection bb = network.getBusbarSection("voltageLevel1BusbarSection1");
        bb.setProperty(TEST_PROPERTY, "testBusbarSectionValue");

        ExportOptions options = new ExportOptions().setVersion(IidmVersion.V_1_15.toString("."));
        Network nodeBreakerNetwork = writeAndRead(network, options);

        //Check that busbar and its extension is still here
        BusbarSection bb2 = nodeBreakerNetwork.getBusbarSection("voltageLevel1BusbarSection1");
        assertEquals("testBusbarSectionValue", bb2.getProperty(TEST_PROPERTY));
        assertEquals("testValue", nodeBreakerNetwork.getArea("defaultControlArea").getProperty(TEST_PROPERTY));

        Load load1 = nodeBreakerNetwork.getLoad("zipLoad");
        assertEquals("valueZipLoad", load1.getProperty(TEST_PROPERTY));
        assertEquals("valueZipLoadModel", load1.getModel().orElseThrow().getProperty(TEST_PROPERTY));

        Load load2 = nodeBreakerNetwork.getLoad("expLoad");
        assertEquals("valueExpLoad", load2.getProperty(TEST_PROPERTY));
        assertEquals("valueExpLoadModel", load2.getModel().orElseThrow().getProperty(TEST_PROPERTY));

        assertEquals("valueVoltageLevel", nodeBreakerNetwork.getVoltageLevel("voltageLevel1").getProperty(TEST_PROPERTY));
        for (ShuntCompensator shuntCompensator1 : voltageLevel.getShuntCompensators()) {
            ShuntCompensatorModel model1 = shuntCompensator1.getModel();
            if (model1 instanceof ShuntCompensatorNonLinearModel) {
                assertEquals("valueNonLinearShuntCompensator", shuntCompensator1.getProperty(TEST_PROPERTY));
                assertEquals("valueNonLinearShuntCompensatorModel", model1.getProperty(TEST_PROPERTY));
                for (ShuntCompensatorNonLinearModel.Section s : ((ShuntCompensatorNonLinearModel) model1).getAllSections()) {
                    assertEquals("valueNonLinearShuntCompensatorModelSection", s.getProperty(TEST_PROPERTY));
                }
            } else if (model1 instanceof ShuntCompensatorLinearModel) {
                assertEquals("valueLinearShuntCompensator", shuntCompensator1.getProperty(TEST_PROPERTY));
                assertEquals("valueLinearShuntCompensatorModel", model1.getProperty(TEST_PROPERTY));
            }
        }

        ReactiveCapabilityCurve reactiveCapabilityCurve1 = nodeBreakerNetwork.getGenerator("generator1").getReactiveLimits(ReactiveCapabilityCurve.class);
        assertEquals("valueReactiveCapabilityCurve", reactiveCapabilityCurve1.getProperty(TEST_PROPERTY));
        assertEquals("valueReactiveCapabilityCurvePoint", reactiveCapabilityCurve1.getPoints().iterator().next().getProperty(TEST_PROPERTY));

    }

    @Test
    void testTrippings() throws IOException {
        Network network = OverloadManagementSystemSerDeTest.createNetwork();
        for (OverloadManagementSystem.Tripping tripping : network.getOverloadManagementSystem("OMS1").getTrippings()) {
            tripping.setProperty(TEST_PROPERTY, "valueTripping");
        }
        ExportOptions options = new ExportOptions().setVersion(IidmVersion.V_1_15.toString("."));
        Network network2 = writeAndRead(network, options);
        for (OverloadManagementSystem.Tripping tripping : network2.getOverloadManagementSystem("OMS1").getTrippings()) {
            assertEquals("valueTripping", tripping.getProperty(TEST_PROPERTY));
        }
    }

    @Test
    void propertiesHolderSerDeTestTapChangers() throws IOException {
        Network network = NoEquipmentNetworkFactory.create();
        Substation substation = network.getSubstation("sub");
        substation.setProperty(TEST_PROPERTY, "value");
        substation.setProperty("test2", "value2");

        // Check name for two winding transformers
        TwoWindingsTransformer twt2 = createTwoWindingsTransformer(substation);
        twt2.setProperty(TEST_PROPERTY, "twt2Value");
        createPhaseTapChanger(twt2);
        createRatioTapChanger(twt2);

        ExportOptions options = new ExportOptions().setVersion(IidmVersion.V_1_15.toString("."));
        Network network2 = writeAndRead(network, options);
        assertEquals("value", network2.getSubstation("sub").getProperty(TEST_PROPERTY));
        assertEquals("value2", network2.getSubstation("sub").getProperty("test2"));
        TwoWindingsTransformer transformer = network2.getSubstation("sub").getTwoWindingsTransformers().iterator().next();
        assertEquals("twt2Value", transformer.getProperty(TEST_PROPERTY));
        PhaseTapChanger phaseTapChanger = transformer.getPhaseTapChanger();
        RatioTapChanger ratioTapChanger = transformer.getRatioTapChanger();
        assertEquals("valuePhaseTapChanger", phaseTapChanger.getProperty(TEST_PROPERTY));
        assertEquals("valueRatioTapChanger", ratioTapChanger.getProperty(TEST_PROPERTY));
        assertEquals("value", phaseTapChanger.getStep(1).getProperty(TEST_PROPERTY));

    }

    @Test
    void testPowerLimits() throws IOException {
        Network network = DanglingLineNetworkFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));
        DanglingLine dl = network.getDanglingLine("DL");
        OperationalLimitsGroup operationalLimitsGroup = dl.getOrCreateSelectedOperationalLimitsGroup();
        ActivePowerLimits activePowerLimit = createLoadingLimits(operationalLimitsGroup::newActivePowerLimits);
        ApparentPowerLimits apparentPowerLimit = createLoadingLimits(operationalLimitsGroup::newApparentPowerLimits);
        CurrentLimits currentLimits = createLoadingLimits(operationalLimitsGroup::newCurrentLimits);
        activePowerLimit.setProperty(TEST_PROPERTY, "valueActivePowerLimits");
        apparentPowerLimit.setProperty(TEST_PROPERTY, "valueApparentPowerLimits");
        currentLimits.setProperty(TEST_PROPERTY, "valueCurrentLimits");
        ExportOptions options = new ExportOptions().setVersion(IidmVersion.V_1_15.toString("."));
        Network network2 = writeAndRead(network, options);
        DanglingLine dl2 = network2.getDanglingLine("DL");
        assertEquals("valueActivePowerLimits", dl2.getActivePowerLimits().orElseThrow().getProperty(TEST_PROPERTY));
        assertEquals("valueApparentPowerLimits", dl2.getApparentPowerLimits().orElseThrow().getProperty(TEST_PROPERTY));
        assertEquals("valueCurrentLimits", dl2.getCurrentLimits().orElseThrow().getProperty(TEST_PROPERTY));
    }

    private static <L extends LoadingLimits, A extends LoadingLimitsAdder<L, A>> L createLoadingLimits(Supplier<A> limitsAdderSupplier) {
        A adder = limitsAdderSupplier.get()
                .setPermanentLimit(350)
                .beginTemporaryLimit()
                .setValue(370)
                .setAcceptableDuration(20 * 60)
                .setName("20'")
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setValue(380)
                .setAcceptableDuration(10 * 60)
                .setName("10'")
                .endTemporaryLimit();
        adder.setProperty(TEST_PROPERTY, "testLoadingLimit");
        return adder.add();
    }

    private static <L extends LoadingLimits, A extends LoadingLimitsAdder<L, A>> L createReducedLoadingLimits(Supplier<A> limitsAdderSupplier) {
        A adder = limitsAdderSupplier.get()
                .setPermanentLimit(350)
                .beginTemporaryLimit()
                .setValue(370)
                .setAcceptableDuration(20 * 60)
                .setName("20'")
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setValue(380)
                .setAcceptableDuration(10 * 60)
                .setName("10'")
                .endTemporaryLimit();
        adder.setProperty(TEST_PROPERTY, "testLoadingLimit");
        return adder.add();
    }
}
