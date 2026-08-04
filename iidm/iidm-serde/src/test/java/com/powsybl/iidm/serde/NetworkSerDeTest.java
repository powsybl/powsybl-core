/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedSaxException;
import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.commons.test.TestUtil;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.*;
import com.powsybl.iidm.serde.extensions.util.DefaultExtensionsSupplier;
import com.powsybl.iidm.serde.extensions.util.ExtensionsSupplier;
import com.powsybl.iidm.serde.extensions.util.NetworkSourceExtension;
import com.powsybl.iidm.serde.extensions.util.NetworkSourceExtensionImpl;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.powsybl.commons.test.ComparisonUtils.assertTxtEquals;
import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    void roundTripTestMultipleSelectedOperationalLimitsGroup() throws IOException {
        allFormatsRoundTripTest(EurostagTutorialExample1Factory.createWithMultipleSelectedFixedCurrentLimits(), "eurostag-tutorial-multiple-selected-op-lim-group.xml", CURRENT_IIDM_VERSION);

        // backward compatibility : in versions older than IIDM 1.16 we only export the last selected limits group
        // no need to test before 1.12 as OperationalLimitsGroup did not exist before
        allFormatsRoundTripFromVersionedXmlFromMinToMaxVersionTest("eurostag-tutorial-multiple-selected-op-lim-group.xml", IidmVersion.V_1_12, IidmVersion.V_1_16);
    }

    @Test
    void roundTripTestOperationalLimitsGroupSpecialCharacterName() throws IOException {
        Network n = EurostagTutorialExample1Factory.createWithMultipleSelectedFixedCurrentLimits();
        Line line = n.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1);
        String name = "notANiceName\"";
        String name2 = "anotherName,,,";
        line.newOperationalLimitsGroup1(name);
        line.newOperationalLimitsGroup1(name2);
        line.addSelectedOperationalLimitsGroups(TwoSides.ONE, name, name2);
        Network networkRead = allFormatsRoundTripTest(n, "eurostag-tutorial-multiple-selected-op-lim-group_special_character_name.xml", CURRENT_IIDM_VERSION);
        assertEquals(6, networkRead.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).getOperationalLimitsGroups1().size());
        assertEquals(line.getAllSelectedOperationalLimitsGroupIdsOrdered(TwoSides.ONE),
            networkRead.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).getAllSelectedOperationalLimitsGroupIdsOrdered(TwoSides.ONE));
    }

    @Test
    void writeMultipleSelectedOperationalLimitsGroupToOlderFormat() throws IOException {
        testWriteVersionedXmlBetweenVersions(
            EurostagTutorialExample1Factory.createWithMultipleSelectedFixedCurrentLimits(),
            new ExportOptions(),
            "eurostag-tutorial-multiple-selected-op-lim-group.xml",
            IidmVersion.V_1_12,
            IidmVersion.V_1_15
        );
    }

    @Test
    void roundTripTestExportOnlyActiveGroups() throws IOException {
        Network n = EurostagTutorialExample1Factory.createWithMultipleSelectedFixedCurrentLimits();
        TwoWindingsTransformer twt = n.getTwoWindingsTransformer(EurostagTutorialExample1Factory.NGEN_NHV1);
        twt.newOperationalLimitsGroup2(EurostagTutorialExample1Factory.ACTIVATED_TWO_ONE).newApparentPowerLimits().setPermanentLimit(150).add();
        twt.newOperationalLimitsGroup2("not activated");
        twt.addSelectedOperationalLimitsGroups(TwoSides.TWO, EurostagTutorialExample1Factory.ACTIVATED_TWO_ONE);
        n.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).deselectOperationalLimitsGroups(TwoSides.ONE, EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE);
        //network with some inactive groups, export only active
        Network networkRead = allFormatsRoundTripTest(n, "eurostag-tutorial-only-active-groups.xml", CURRENT_IIDM_VERSION,
            new ExportOptions().setOnlySelectedOperationalLimitsGroups(true));
        assertEquals(Set.of("DEFAULT", EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO),
            networkRead.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).getAllSelectedOperationalLimitsGroupIds(TwoSides.ONE));
    }

    @Test
    void roundTripTestExportOnlyActiveGroupsThreeWindingTransformer() throws IOException {
        Network n = EurostagTutorialExample1Factory.createWith3wTransformer();
        n.setCaseDate(ZonedDateTime.parse("2013-01-15T18:45:00.000+01:00"));
        ThreeWindingsTransformer twt = n.getThreeWindingsTransformer(EurostagTutorialExample1Factory.NGEN_V2_NHV1);
        twt.getLeg1().newOperationalLimitsGroup(EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE).newCurrentLimits()
            .setPermanentLimit(100)
            .beginTemporaryLimit()
            .setName("40'")
            .setAcceptableDuration(40 * 60)
            .setValue(500)
            .endTemporaryLimit()
            .add();
        twt.getLeg1().newOperationalLimitsGroup("not activated").newActivePowerLimits().setPermanentLimit(10).add();
        twt.getLeg1().addSelectedOperationalLimitsGroups(EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE);

        twt.getLeg2().newOperationalLimitsGroup(EurostagTutorialExample1Factory.ACTIVATED_TWO_ONE).newApparentPowerLimits().setPermanentLimit(75).add();
        twt.getLeg2().newOperationalLimitsGroup(EurostagTutorialExample1Factory.ACTIVATED_TWO_TWO).newApparentPowerLimits().setPermanentLimit(80).add();
        twt.getLeg2().newOperationalLimitsGroup("not activated").newApparentPowerLimits().setPermanentLimit(100).add();
        twt.getLeg2().newOperationalLimitsGroup("not activated 2").newApparentPowerLimits().setPermanentLimit(110).add();
        twt.getLeg2().addSelectedOperationalLimitsGroups(EurostagTutorialExample1Factory.ACTIVATED_TWO_ONE, EurostagTutorialExample1Factory.ACTIVATED_TWO_TWO);

        twt.getLeg3().newOperationalLimitsGroup(EurostagTutorialExample1Factory.ACTIVATED_THREE_ONE).newActivePowerLimits()
            .setPermanentLimit(150)
            .beginTemporaryLimit()
            .setName("30'")
            .setAcceptableDuration(30 * 60)
            .setValue(400)
            .endTemporaryLimit()
            .add();
        twt.getLeg3().newOperationalLimitsGroup("not activated");
        twt.getLeg3().newOperationalLimitsGroup("not activated 2");
        twt.getLeg3().newOperationalLimitsGroup("not activated 3").newCurrentLimits().setPermanentLimit(500).add();
        twt.getLeg3().addSelectedOperationalLimitsGroups(EurostagTutorialExample1Factory.ACTIVATED_THREE_ONE);

        allFormatsRoundTripTest(n, "eurostag-tutorial-only-active-groups-three-winding.xml", CURRENT_IIDM_VERSION, new ExportOptions().setOnlySelectedOperationalLimitsGroups(true));
    }

    @Test
    void roundTripTestExportOnlyActiveGroupsBoundaryLine() throws IOException {
        Network n = EurostagTutorialExample1Factory.createWithTieLine();
        BoundaryLine bl1 = n.getBoundaryLine(EurostagTutorialExample1Factory.BOUNDARY_LINE_XNODE1_1);
        bl1.newOperationalLimitsGroup(EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE).newCurrentLimits().setPermanentLimit(175).add();
        bl1.newOperationalLimitsGroup("not activated").newActivePowerLimits().setPermanentLimit(145).add();
        bl1.addSelectedOperationalLimitsGroups(EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE);

        BoundaryLine bl2 = n.getBoundaryLine(EurostagTutorialExample1Factory.BOUNDARY_LINE_XNODE1_2);
        bl2.newOperationalLimitsGroup(EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE).newApparentPowerLimits().setPermanentLimit(135).add();
        bl2.newOperationalLimitsGroup(EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO).newCurrentLimits().setPermanentLimit(500).add();
        bl2.newOperationalLimitsGroup("not activated");
        bl2.newOperationalLimitsGroup("not activated 2");
        bl2.addSelectedOperationalLimitsGroups(EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE, EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO);

        allFormatsRoundTripTest(n, "eurostag-tutorial-only-active-groups-boundary-line.xml", CURRENT_IIDM_VERSION, new ExportOptions().setOnlySelectedOperationalLimitsGroups(true));
    }

    @Test
    void testImportOnlyActiveGroups() throws URISyntaxException {
        ImportOptions importOptions = new ImportOptions().setOnlySelectedOperationalLimitsGroups(true);
        //read only active groups
        Network onlyActiveGroupsNetwork = NetworkSerDe.read(Paths.get(getClass().getResource(getVersionedNetworkPath("eurostag-tutorial-only-active-groups.xml",
            CURRENT_IIDM_VERSION)).toURI()), importOptions);
        Line line1 = onlyActiveGroupsNetwork.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1);
        assertAllGroupsOneLineSelected(line1, List.of("DEFAULT", EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO), TwoSides.ONE);
        assertAllGroupsOneLineSelected(line1, List.of("DEFAULT", EurostagTutorialExample1Factory.ACTIVATED_TWO_ONE), TwoSides.TWO);
        Line line2 = onlyActiveGroupsNetwork.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_2);
        assertAllGroupsOneLineSelected(line2, List.of("DEFAULT"), TwoSides.ONE);
        assertAllGroupsOneLineSelected(line2, List.of("DEFAULT", EurostagTutorialExample1Factory.ACTIVATED_TWO_ONE, EurostagTutorialExample1Factory.ACTIVATED_TWO_TWO), TwoSides.TWO);
    }

    private void assertAllGroupsOneLineSelected(Line line, List<String> expectedIds, TwoSides side) {
        assertEquals(expectedIds, line.getAllSelectedOperationalLimitsGroupIdsOrdered(side));
        int allGroupsSize = switch (side) {
            case ONE -> line.getOperationalLimitsGroups1().size();
            case TWO -> line.getOperationalLimitsGroups2().size();
        };
        assertEquals(0, allGroupsSize - line.getAllSelectedOperationalLimitsGroups(side).size());
    }

    @Test
    void checkNoExportOfLowLimits() throws IOException {
        Network network = EurostagTutorialExample1Factory.createWithMultipleSelectedFixedCurrentLimits();
        network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1)
            .newOperationalLimitsGroup1("low limits")
            .newApparentPowerLimits()
            .setDetectionKind(DetectionKind.LOW)
            .beginTemporaryLimit()
            .setValue(1000)
            .setAcceptableDuration(60)
            .setName("1'")
            .endTemporaryLimit()
            .add();
        String referenceFilename = getVersionedNetworkPath("eurostag-tutorial-multiple-selected-op-lim-group-force_low_limit.xml", IidmVersion.V_1_17);
        assertThrows(NotImplementedException.class, () -> writeXmlTest(network,
            (n, p) -> NetworkSerDe.write(n, new ExportOptions().setVersion(IidmVersion.V_1_17.toString(".")), p),
            referenceFilename
            ));
        allFormatsRoundTripTest(network, "eurostag-tutorial-multiple-selected-op-lim-group-force_low_limit.xml", IidmVersion.V_1_17, new ExportOptions().setForceExportNetworkWithBetaFeatures(true));
    }

    @ParameterizedTest
    @EnumSource(TreeDataFormat.class)
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

    static Network writeAndRead(Network network, ExportOptions options) throws IOException {
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
        network.getVoltageLevel(voltageLevelId).newBoundaryLine()
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

    @Test
    void testExportWithFlatten() {
        Network n1 = createNetwork(1);
        Network n2 = createNetwork(2);
        Path xmlFile = tmpDir.resolve("flattenedNetwork.xml");

        Network merged = Network.merge("Merged", n1, n2);

        ExportOptions options = new ExportOptions().setFlatten(true);
        NetworkSerDe.write(merged, options, xmlFile);
        Network readNetwork = NetworkSerDe.validateAndRead(xmlFile);
        assertEquals(2, merged.getSubnetworks().size());
        assertEquals(0, readNetwork.getSubnetworks().size());
    }

    @Test
    void testExportWithoutFlatten() {
        Network n1 = createNetwork(1);
        Network n2 = createNetwork(2);
        Path xmlFile = tmpDir.resolve("networkWithSubnetworks.xml");

        Network merged = Network.merge("Merged", n1, n2);

        ExportOptions options = new ExportOptions();
        NetworkSerDe.write(merged, options, xmlFile);
        Network readNetwork = NetworkSerDe.validateAndRead(xmlFile);
        assertEquals(2, merged.getSubnetworks().size());
        assertEquals(2, readNetwork.getSubnetworks().size());
    }

    @Test
    void testValidateByVersionOnIidm102() throws IOException {
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/V1_2/shuntRoundTripRef.xml"))) {
            assertDoesNotThrow(() -> NetworkSerDe.validate(is, IidmVersion.V_1_2));
        }
    }

    @Test
    void testValidateByVersionOnIidm116() throws IOException {
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/V1_16/shuntRoundTripRef.xml"))) {
            assertDoesNotThrow(() -> NetworkSerDe.validate(is, IidmVersion.V_1_16));
        }
    }

    @Test
    void testValidateByVersionWhenMismatchedNetworkVersion() throws IOException {
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/V1_16/shuntRoundTripRef.xml"))) {
            assertThatThrownBy(() -> NetworkSerDe.validate(is, IidmVersion.V_1_15))
                    .isInstanceOf(PowsyblException.class)
                    .hasMessageContaining("Namespace mismatch: expected validation version 1.15, found namespace http://www.powsybl.org/schema/iidm/1_16");
        }
    }

    @Test
    void testValidateByVersionWhenInvalidNetwork() throws IOException {
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/V1_16/shuntOldTagName.xml"))) {
            assertThatThrownBy(() -> NetworkSerDe.validate(is, IidmVersion.V_1_16))
                    .isInstanceOf(com.powsybl.commons.exceptions.UncheckedSaxException.class)
                    .hasMessageContaining("Invalid content was found starting with element '{\"http://www.powsybl.org/schema/iidm/1_16\":shunt}'");
        }
    }

    @Test
    void testValidateByVersionWhenNetworkContainSlackTerminalExtension() throws IOException {
        // test extension loading including slackTerminal which is in version 1.5 and require iidm version 1.8, when validate should succeed
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/V1_16/europeanLvTestFeederRef.xml"))) {
            assertDoesNotThrow(() -> NetworkSerDe.validate(is, IidmVersion.V_1_16));
        }
    }

    @Test
    void testValidateByVersionWhenNetworkContainTerminalMockExtension() throws IOException {
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/V1_16/eurostag-tutorial-example1-with-terminalMock-ext.xml"))) {
            assertDoesNotThrow(() -> NetworkSerDe.validate(is, IidmVersion.V_1_16));
        }
    }

    @Test
    void testValidateByVersionWhenInSupportedEnumValue() throws IOException {
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/gen_enum_not_supported.xml"))) {
            assertThatThrownBy(() -> NetworkSerDe.validate(is, IidmVersion.V_1_17))
                    .isInstanceOf(com.powsybl.commons.exceptions.UncheckedSaxException.class)
                    .hasMessageContaining("Value 'TEST' is not facet-valid with respect to enumeration " +
                            "'[HYDRO, NUCLEAR, WIND, THERMAL, SOLAR, OTHER]'. It must be a value from the enumeration.");
        }
    }

    @Test
    void testValidateWithCustomExtensionSupplier() throws IOException {
        ExtensionsSupplier customExtensionsSupplier = () -> DefaultExtensionsSupplier.getInstance().get();
        assertNotSame(DefaultExtensionsSupplier.getInstance(), customExtensionsSupplier);

        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/V1_16/shuntRoundTripRef.xml"))) {
            assertDoesNotThrow(() -> NetworkSerDe.validate(is, IidmVersion.V_1_16, customExtensionsSupplier));
        }
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/V1_16/shuntRoundTripRef.xml"))) {
            assertDoesNotThrow(() -> NetworkSerDe.validate(is, customExtensionsSupplier));
        }
    }

    @Test
    void testValidateByVersionWhenMissingNamespace() throws IOException {
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/network-without-namespace.xml"))) {
            assertThatThrownBy(() -> NetworkSerDe.validate(is, IidmVersion.V_1_16))
                    .isInstanceOf(PowsyblException.class)
                    .hasMessageContaining("Namespace mismatch: expected validation version 1.16, found namespace  ");
        }
    }

    @Test
    void testValidateWhenParseMalformedXml() {
        String xml = "<iidm:network";
        byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
        InputStream is = new ByteArrayInputStream(bytes);
        assertThatThrownBy(() -> NetworkSerDe.validate(is, IidmVersion.V_1_16))
                .isInstanceOf(UncheckedSaxException.class)
                .hasMessageContaining("XML document structures must start and end within the same entity");
    }
}
