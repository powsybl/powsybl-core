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
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
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

import static com.powsybl.commons.test.ComparisonUtils.assertTxtEquals;
import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class NetworkSerDeTest extends AbstractIidmSerDeTest {

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
                .addExtension("terminalMockNoSerDe").addExtension("loadZipModel")
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
        network.getGenerator("GEN").setProperty("test", "foo");
        Path xmlFile = tmpDir.resolve("n.xml");
        NetworkSerDe.write(network, xmlFile);
        Network readNetwork = NetworkSerDe.validateAndRead(xmlFile);
        assertEquals("foo", readNetwork.getGenerator("GEN").getProperty("test"));
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
}
