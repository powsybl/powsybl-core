/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.*;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.SteadyStateHypothesisExport;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.commons.datasource.DirectoryDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.ImportConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.PhaseShifterTestCaseFactory;
import com.powsybl.iidm.network.test.ShuntTestCaseFactory;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;

import javax.xml.stream.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class SteadyStateHypothesisExportTest extends AbstractSerDeTest {

    private Properties importParams;

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
    }

    @Test
    void microGridBE() throws IOException, XMLStreamException {
        DifferenceEvaluator knownDiffsSsh = DifferenceEvaluators.chain(
            ExportXmlCompare::sameScenarioTime,
            ExportXmlCompare::ensuringIncreasedModelVersion,
            ExportXmlCompare::ignoringSynchronousMachinesSVCsWithTargetDeadband,
            ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        DifferenceEvaluator knownDiffsXiidm = DifferenceEvaluators.chain(
            DifferenceEvaluators.Default,
            ExportXmlCompare::ignoringCgmesMetadataModels);
        assertTrue(test(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), knownDiffsSsh, knownDiffsXiidm));
    }

    @Test
    void microGridBEWithHiddenTapChangers() throws IOException, XMLStreamException {
        DifferenceEvaluator knownDiffsSsh = DifferenceEvaluators.chain(
            ExportXmlCompare::sameScenarioTime,
            ExportXmlCompare::ensuringIncreasedModelVersion,
            ExportXmlCompare::ignoringSynchronousMachinesSVCsWithTargetDeadband,
            ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        DifferenceEvaluator knownDiffsXiidm = DifferenceEvaluators.chain(
            DifferenceEvaluators.Default,
            ExportXmlCompare::ignoringCgmesMetadataModels);
        assertTrue(test(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEHiddenTapChangers().dataSource(), knownDiffsSsh, knownDiffsXiidm));
    }

    @Test
    void microGridBEWithSharedRegulatingControl() throws IOException, XMLStreamException {
        DifferenceEvaluator knownDiffsSsh = DifferenceEvaluators.chain(
            ExportXmlCompare::sameScenarioTime,
            ExportXmlCompare::ensuringIncreasedModelVersion,
            ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        DifferenceEvaluator knownDiffsXiidm = DifferenceEvaluators.chain(
            DifferenceEvaluators.Default,
            ExportXmlCompare::ignoringCgmesMetadataModels);
        assertTrue(test(CgmesConformity1ModifiedCatalog.microGridBaseCaseBESharedRegulatingControl().dataSource(), knownDiffsSsh, knownDiffsXiidm));
    }

    @Test
    void smallGrid() throws IOException, XMLStreamException {
        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
            ExportXmlCompare::sameScenarioTime,
            ExportXmlCompare::ensuringIncreasedModelVersion,
            ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        assertTrue(test(CgmesConformity1Catalog.smallBusBranch().dataSource(), knownDiffs, DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                ExportXmlCompare::numericDifferenceEvaluator,
                ExportXmlCompare::ignoringCgmesMetadataModels,
                ExportXmlCompare::ignoringControlAreaNetInterchange)));
    }

    @Test
    void smallGridHVDC() throws IOException, XMLStreamException {
        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
                ExportXmlCompare::sameScenarioTime,
                ExportXmlCompare::ensuringIncreasedModelVersion,
                ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        assertTrue(test(CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource(), knownDiffs, DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                ExportXmlCompare::numericDifferenceEvaluator,
                ExportXmlCompare::ignoringControlAreaNetInterchange,
                ExportXmlCompare::ignoringCgmesMetadataModels,
                ExportXmlCompare::ignoringHvdcLinePmax)));
    }

    private boolean test(ReadOnlyDataSource dataSource, DifferenceEvaluator knownDiffsSsh, DifferenceEvaluator knownDiffsIidm) throws IOException, XMLStreamException {
        // Import original
        importParams.put("iidm.import.cgmes.create-cgmes-export-mapping", "true");
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), importParams);

        // Export SSH
        Path exportedSsh = tmpDir.resolve("exportedSsh.xml");
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(exportedSsh))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(expected);
            SteadyStateHypothesisExport.write(expected, writer, context);
        }

        // Compare the exported SSH with the original one
        try (InputStream expectedssh = Repackager.newInputStream(dataSource, Repackager::ssh);
             InputStream actualssh = Files.newInputStream(exportedSsh)) {
            if (!ExportXmlCompare.compareSSH(expectedssh, actualssh, knownDiffsSsh)) {
                return false;
            }
        }

        // Zip with new SSH
        Path repackaged = tmpDir.resolve("repackaged.zip");
        Repackager r = new Repackager(dataSource)
                .with("test_EQ.xml", Repackager::eq)
                .with("test_TP.xml", Repackager::tp)
                .with("test_SV.xml", Repackager::sv)
                .with("test_SSH.xml", exportedSsh)
                .with("test_EQ_BD.xml", Repackager::eqBd)
                .with("test_TP_BD.xml", Repackager::tpBd);
        r.zip(repackaged);

        // Import with new SSH
        Network actual = Network.read(repackaged,
                DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager(), ImportConfig.load(), importParams);

        // Remove Areas
        expected.getAreaStream().map(Area::getId).toList().forEach(a -> expected.getArea(a).remove());
        actual.getAreaStream().map(Area::getId).toList().forEach(a -> actual.getArea(a).remove());

        // Export original and with new SSH
        Path expectedPath = tmpDir.resolve("expected.xml");
        Path actualPath = tmpDir.resolve("actual.xml");
        NetworkSerDe.write(expected, expectedPath);
        NetworkSerDe.write(actual, actualPath);
        NetworkSerDe.validate(actualPath);

        // Compare
        return ExportXmlCompare.compareNetworks(expectedPath, actualPath, knownDiffsIidm);
    }

    @Test
    void equivalentShuntTest() throws XMLStreamException {
        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.microGridBaseCaseBEEquivalentShunt().dataSource();
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), importParams);

        String ssh = exportSshAsString(network);

        // Equivalent shunts should not have entries in SSH
        String equivalentShuntId = "d771118f-36e9-4115-a128-cc3d9ce3e3da";
        assertNotNull(network.getShuntCompensator(equivalentShuntId));
        SshLinearShuntCompensators sshLinearShuntCompensators = readSshLinearShuntCompensator(ssh);
        assertFalse(sshLinearShuntCompensators.map.isEmpty());
        assertFalse(sshLinearShuntCompensators.map.containsKey(equivalentShuntId));
    }

    private static String exportSshAsString(Network network) throws XMLStreamException {
        CgmesExportContext context = new CgmesExportContext(network);
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", stringWriter);
        SteadyStateHypothesisExport.write(network, writer, context);

        return stringWriter.toString();
    }

    private static SshLinearShuntCompensators readSshLinearShuntCompensator(String ssh) {
        final String sshLinearShuntCompensator = "LinearShuntCompensator";
        final String sshLinearShuntCompensatorSections = "ShuntCompensator.sections";
        final String sshLinearShuntCompensatorControlEnabled = "RegulatingCondEq.controlEnabled";
        final String attrAbout = "about";

        SshLinearShuntCompensators sshLinearShuntCompensators = new SshLinearShuntCompensators();
        try (InputStream is = new ByteArrayInputStream(ssh.getBytes(StandardCharsets.UTF_8))) {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
            Integer sections = null;
            Boolean controlEnabled = null;
            String shuntCompensatorId = null;
            while (reader.hasNext()) {
                int next = reader.next();
                if (next == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getLocalName().equals(sshLinearShuntCompensator)) {
                        sections = null;
                        controlEnabled = null;
                        shuntCompensatorId = reader.getAttributeValue(CgmesNamespace.RDF_NAMESPACE, attrAbout).substring(2);
                    } else if (reader.getLocalName().equals(sshLinearShuntCompensatorSections)) {
                        String text = reader.getElementText();
                        sections = Integer.parseInt(text);
                    } else if (reader.getLocalName().equals(sshLinearShuntCompensatorControlEnabled)) {
                        String text = reader.getElementText();
                        controlEnabled = Boolean.valueOf(text);
                    }
                } else if (next == XMLStreamConstants.END_ELEMENT) {
                    if (reader.getLocalName().equals(sshLinearShuntCompensator) && sections != null && controlEnabled != null) {
                        sshLinearShuntCompensators.add(shuntCompensatorId, sections, controlEnabled);
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
        return sshLinearShuntCompensators;
    }

    private static final class SshLinearShuntCompensators {
        private final Map<String, Pair<Integer, Boolean>> map = new HashMap<>();

        void add(String shuntCompensatorId, int sections, boolean controlEnabled) {
            map.put(shuntCompensatorId, Pair.of(sections, controlEnabled));
        }
    }

    @Test
    void testUpdateControlArea() throws IOException {
        Path outputPath = tmpDir.resolve("update-control-areas");
        Files.createDirectories(outputPath);

        // Read network and check control area data
        Network be = Network.read(CgmesConformity3Catalog.microGridBaseCaseBE().dataSource(), importParams);
        long numControlAreas = be.getAreaStream().filter(a -> a.getAreaType().equals("ControlAreaTypeKind.Interchange")).count();
        assertEquals(1, numControlAreas);
        Area controlArea = be.getAreas().iterator().next();
        assertEquals(236.9798, controlArea.getInterchangeTarget().getAsDouble(), 1e-10);
        double pTolerance = Double.parseDouble(controlArea.getProperty("pTolerance"));
        assertEquals(10, pTolerance, 1e-10);

        // Update control area data
        controlArea.setInterchangeTarget(controlArea.getInterchangeTarget().getAsDouble() * 2);
        controlArea.setProperty("pTolerance", Double.toString(pTolerance / 2));

        // Write and read the network to check serialization of the extension
        Path updatedXiidm = outputPath.resolve("BE-updated.xiidm");
        be.write("XIIDM", null, updatedXiidm);
        Network beUpdated = Network.read(updatedXiidm);

        // Export only SSH instance file
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.PROFILES, "SSH");
        beUpdated.write("CGMES", exportParams, outputPath.resolve("BE"));

        // Check that exported SSH contains updated values for net interchange and p tolerance
        Collection<SshExportedControlArea> sshExportedControlAreas = readSshControlAreas(outputPath.resolve("BE_SSH.xml"));
        assertFalse(sshExportedControlAreas.isEmpty());
        SshExportedControlArea sshExportedControlArea = sshExportedControlAreas.iterator().next();
        assertEquals(473.9596, sshExportedControlArea.netInterchange, 1e-10);
        assertEquals(5, sshExportedControlArea.pTolerance, 1e-10);
    }

    static class SshExportedControlArea {
        String id;
        double netInterchange;
        double pTolerance;
    }

    private static final String ATTR_ABOUT = "about";
    private static final String CONTROL_AREA = "ControlArea";
    private static final String CONTROL_AREA_NET_INTERCHANGE = "ControlArea.netInterchange";
    private static final String CONTROL_AREA_P_TOLERANCE = "ControlArea.pTolerance";

    private static Collection<SshExportedControlArea> readSshControlAreas(Path ssh) {
        List<SshExportedControlArea> sshExportedControlAreas = new ArrayList<>();
        SshExportedControlArea sshExportedControlArea = null;
        try (InputStream is = Files.newInputStream(ssh)) {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
            while (reader.hasNext()) {
                int next = reader.next();
                if (next == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getLocalName().equals(CONTROL_AREA)) {
                        sshExportedControlArea = new SshExportedControlArea();
                        sshExportedControlArea.id = reader.getAttributeValue(CgmesNamespace.RDF_NAMESPACE, ATTR_ABOUT).substring(2);
                    } else if (reader.getLocalName().equals(CONTROL_AREA_NET_INTERCHANGE) && sshExportedControlArea != null) {
                        sshExportedControlArea.netInterchange = Double.parseDouble(reader.getElementText());
                    } else if (reader.getLocalName().equals(CONTROL_AREA_P_TOLERANCE) && sshExportedControlArea != null) {
                        sshExportedControlArea.pTolerance = Double.parseDouble(reader.getElementText());
                    }
                } else if (next == XMLStreamConstants.END_ELEMENT) {
                    if (reader.getLocalName().equals(CONTROL_AREA) && sshExportedControlArea != null) {
                        sshExportedControlAreas.add(sshExportedControlArea);
                        sshExportedControlArea = null;
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
        return sshExportedControlAreas;
    }

    @Test
    void microGridCgmesExportPreservingOriginalClassesOfLoads() throws IOException, XMLStreamException {
        ReadOnlyDataSource ds = Cgmes3ModifiedCatalog.microGridBaseCaseAllTypesOfLoads().dataSource();
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), importParams);

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "microGridCgmesExportPreservingOriginalClassesOfLoads";
        Properties exportParams = new Properties();
        new CgmesExport().export(network, exportParams, new DirectoryDataSource(outputPath, baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(new DirectoryDataSource(outputPath, baseName), NetworkFactory.findDefault(), importParams);

        InputStream expectedSsh = Repackager.newInputStream(ds, Repackager::ssh);
        String actualSsh = exportSshAsString(actual);

        DifferenceEvaluator knownDiffsSsh = DifferenceEvaluators.chain(
                ExportXmlCompare::ignoringFullModelDependentOn,
                ExportXmlCompare::ignoringFullModelModelingAuthoritySet,
                ExportXmlCompare::ignoringRdfChildNodeListLength,
                ExportXmlCompare::ignoringChildLookupNull,
                ExportXmlCompare::ignoringTextValueShuntCompensatorControlEnabled,
                ExportXmlCompare::ignoringTextValueTapChangerControlEnabled,
                ExportXmlCompare::ignoringRdfChildLookupTerminal,
                ExportXmlCompare::ignoringRdfChildLookupEquivalentInjection,
                ExportXmlCompare::ignoringRdfChildLookupStaticVarCompensator,
                ExportXmlCompare::ignoringRdfChildLookupRegulatingControl,
                ExportXmlCompare::ignoringTextValueEquivalentInjection);
        assertTrue(ExportXmlCompare.compareSSH(expectedSsh, new ByteArrayInputStream(actualSsh.getBytes(StandardCharsets.UTF_8)), knownDiffsSsh));
    }

    @Test
    void miniGridCgmesExportPreservingOriginalClasses() throws IOException, XMLStreamException {
        ReadOnlyDataSource ds = Cgmes3Catalog.miniGrid().dataSource();
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.convert-boundary", "true");
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), properties);

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "miniGridCgmesExportPreservingOriginalClasses";
        Properties exportParams = new Properties();
        new CgmesExport().export(network, exportParams, new DirectoryDataSource(outputPath, baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(new DirectoryDataSource(outputPath, baseName), NetworkFactory.findDefault(), new Properties());

        InputStream expectedSsh = Repackager.newInputStream(ds, Repackager::ssh);
        String actualSsh = exportSshAsString(actual);

        DifferenceEvaluator knownDiffsSsh = DifferenceEvaluators.chain(
                ExportXmlCompare::ignoringFullModelDependentOn,
                ExportXmlCompare::ignoringFullModelModelingAuthoritySet,
                ExportXmlCompare::ignoringRdfChildNodeListLength,
                ExportXmlCompare::ignoringConformLoad,
                ExportXmlCompare::ignoringChildLookupNull);
        assertTrue(ExportXmlCompare.compareSSH(expectedSsh, new ByteArrayInputStream(actualSsh.getBytes(StandardCharsets.UTF_8)), knownDiffsSsh));
    }

    @Test
    void phaseTapChangerTapChangerControlSSHTest() throws IOException {
        String exportFolder = "/test-pst-tcc";
        String baseName = "testPstTcc";
        Network network;
        String ssh;
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));
            Properties exportParams = new Properties();
            exportParams.put(CgmesExport.PROFILES, "SSH");

            // PST with FIXED_TAP
            network = PhaseShifterTestCaseFactory.createWithTargetDeadband();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithoutAttribute(ssh, "_PS1_PTC_RC", "true", "false", "10", "200", "M");

            // PST local with ACTIVE_POWER_CONTROL
            network = PhaseShifterTestCaseFactory.createLocalActivePowerWithTargetDeadband();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_PS1_PTC_RC", "true", "false", "10", "200", "M");
            network.getTwoWindingsTransformer("PS1").getPhaseTapChanger().setRegulating(true);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_PS1_PTC_RC", "true", "true", "10", "200", "M");

            // PST local with CURRENT_LIMITER
            network = PhaseShifterTestCaseFactory.createLocalCurrentLimiterWithTargetDeadband();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_PS1_PTC_RC", "true", "false", "10", "200", "none");
            network.getTwoWindingsTransformer("PS1").getPhaseTapChanger().setRegulating(true);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_PS1_PTC_RC", "true", "true", "10", "200", "none");

            // PST remote with CURRENT_LIMITER
            network = PhaseShifterTestCaseFactory.createRemoteCurrentLimiterWithTargetDeadband();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_PS1_PTC_RC", "true", "false", "10", "200", "none");
            network.getTwoWindingsTransformer("PS1").getPhaseTapChanger().setRegulating(true);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_PS1_PTC_RC", "true", "true", "10", "200", "none");

            // PST remote with ACTIVE_POWER_CONTROL
            network = PhaseShifterTestCaseFactory.createRemoteActivePowerWithTargetDeadband();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_PS1_PTC_RC", "true", "false", "10", "200", "M");
            network.getTwoWindingsTransformer("PS1").getPhaseTapChanger().setRegulating(true);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_PS1_PTC_RC", "true", "true", "10", "200", "M");
        }
    }

    @Test
    void ratioTapChangerTapChangerControlSSHTest() throws IOException {
        String exportFolder = "/test-rtc-tcc";
        String baseName = "testRtcTcc";
        Network network;
        String ssh;
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));
            Properties exportParams = new Properties();
            exportParams.put(CgmesExport.PROFILES, "SSH");

            // RTC without control
            network = EurostagTutorialExample1Factory.createWithoutRtcControl();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithoutAttribute(ssh, "_NHV2_NLOAD_RTC_RC", "", "", "", "", "");

            // RTC local with VOLTAGE
            network = EurostagTutorialExample1Factory.create();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_NHV2_NLOAD_RTC_RC", "true", "true", "0", "158", "k");
            network.getTwoWindingsTransformer("NHV2_NLOAD").getRatioTapChanger().setRegulating(false);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_NHV2_NLOAD_RTC_RC", "true", "false", "0", "158", "k");

            // RTC local with REACTIVE_POWER
            network = EurostagTutorialExample1Factory.createWithReactiveTcc();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_NHV2_NLOAD_RTC_RC", "true", "true", "0", "100", "M");
            network.getTwoWindingsTransformer("NHV2_NLOAD").getRatioTapChanger().setRegulating(false);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_NHV2_NLOAD_RTC_RC", "true", "false", "0", "100", "M");

            // RTC remote with VOLTAGE
            network = EurostagTutorialExample1Factory.createRemoteVoltageTcc();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_NHV2_NLOAD_RTC_RC", "true", "true", "0", "158", "k");
            network.getTwoWindingsTransformer("NHV2_NLOAD").getRatioTapChanger().setRegulating(false);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_NHV2_NLOAD_RTC_RC", "true", "false", "0", "158", "k");

            // RTC remote with REACTIVE_POWER
            network = EurostagTutorialExample1Factory.createRemoteReactiveTcc();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_NHV2_NLOAD_RTC_RC", "true", "true", "0", "100", "M");
            network.getTwoWindingsTransformer("NHV2_NLOAD").getRatioTapChanger().setRegulating(false);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_NHV2_NLOAD_RTC_RC", "true", "false", "0", "100", "M");

            // 3w without control
            network = EurostagTutorialExample1Factory.createWith3wWithoutControl();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithoutAttribute(ssh, "_NGEN_V2_NHV1_RTC_RC", "", "", "", "", "");

            // 3w with local voltage control
            network = EurostagTutorialExample1Factory.createWith3wWithVoltageControl();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_NGEN_V2_NHV1_RTC_RC", "true", "true", "0", "158", "k");

            // 3w with local reactive control
            network = EurostagTutorialExample1Factory.create3wWithReactiveTcc();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_NGEN_V2_NHV1_RTC_RC", "true", "true", "0", "100", "M");
            network.getThreeWindingsTransformer("NGEN_V2_NHV1").getLeg1().getRatioTapChanger().setRegulating(false);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_NGEN_V2_NHV1_RTC_RC", "true", "false", "0", "100", "M");

            // 3w with remote voltage
            network = EurostagTutorialExample1Factory.create3wRemoteVoltageTcc();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_NGEN_V2_NHV1_RTC_RC", "true", "true", "0", "158", "k");
            network.getThreeWindingsTransformer("NGEN_V2_NHV1").getLeg1().getRatioTapChanger().setRegulating(false);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_NGEN_V2_NHV1_RTC_RC", "true", "false", "0", "158", "k");

            // 3w with remote reactive
            network = EurostagTutorialExample1Factory.create3wRemoteReactiveTcc();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_NGEN_V2_NHV1_RTC_RC", "true", "true", "0", "100", "M");
            network.getThreeWindingsTransformer("NGEN_V2_NHV1").getLeg1().getRatioTapChanger().setRegulating(false);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithAttribute(ssh, "_NGEN_V2_NHV1_RTC_RC", "true", "false", "0", "100", "M");
        }
    }

    @Test
    void staticVarCompensatorRegulatingControlSSHTest() throws IOException {
        String exportFolder = "/test-svc-rc";
        String baseName = "testSvcRc";
        Network network;
        String ssh;
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));
            Properties exportParams = new Properties();
            exportParams.put(CgmesExport.PROFILES, "SSH");

            // SVC VOLTAGE
            // Local
            network = SvcTestCaseFactory.createLocalVoltageControl();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SVC2_RC", "false", "true", "0", "390", "k");

            // Remote
            network = SvcTestCaseFactory.createRemoteVoltageControl();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SVC2_RC", "false", "true", "0", "390", "k");

            // SVC REACTIVE_POWER
            // Local
            network = SvcTestCaseFactory.createLocalReactiveControl();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SVC2_RC", "false", "true", "0", "350", "M");

            // Remote
            network = SvcTestCaseFactory.createRemoteReactiveControl();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SVC2_RC", "false", "true", "0", "350", "M");

            // SVC OFF
            // Local
            network = SvcTestCaseFactory.createLocalOffNoTarget();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRCWithoutAttribute(ssh, "_SVC2_RC");
            network = SvcTestCaseFactory.createLocalOffReactiveTarget();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SVC2_RC", "false", "false", "0", "350", "M");
            network = SvcTestCaseFactory.createLocalOffVoltageTarget();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SVC2_RC", "false", "false", "0", "390", "k");
            network = SvcTestCaseFactory.createLocalOffBothTarget();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SVC2_RC", "false", "false", "0", "390", "k");

            // Remote
            network = SvcTestCaseFactory.createRemoteOffNoTarget();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SVC2_RC", "false", "false", "0", "0", "k");
            network = SvcTestCaseFactory.createRemoteOffReactiveTarget();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SVC2_RC", "false", "false", "0", "350", "M");
            network = SvcTestCaseFactory.createRemoteOffVoltageTarget();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SVC2_RC", "false", "false", "0", "390", "k");
            network = SvcTestCaseFactory.createRemoteOffBothTarget();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SVC2_RC", "false", "false", "0", "390", "k");
        }
    }

    @Test
    void shuntCompensatorRegulatingControlSSHTest() throws IOException {
        String exportFolder = "/test-sc-rc";
        String baseName = "testScRc";
        Network network;
        String ssh;
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));
            Properties exportParams = new Properties();
            exportParams.put(CgmesExport.PROFILES, "SSH");

            // SC linear
            network = ShuntTestCaseFactory.create();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SHUNT_RC", "true", "true", "5", "200", "k");
            testTcTccWithoutAttribute(ssh, "", "", "", "", "", "M");

            network = ShuntTestCaseFactory.createLocalLinear();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SHUNT_RC", "true", "true", "5", "200", "k");
            testTcTccWithoutAttribute(ssh, "", "", "", "", "", "M");

            network = ShuntTestCaseFactory.createDisabledRemoteLinear();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SHUNT_RC", "true", "false", "5", "200", "k");
            testTcTccWithoutAttribute(ssh, "", "", "", "", "", "M");

            network = ShuntTestCaseFactory.createDisabledLocalLinear();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SHUNT_RC", "true", "false", "5", "200", "k");
            testTcTccWithoutAttribute(ssh, "", "", "", "", "", "M");

            network = ShuntTestCaseFactory.createLocalLinearNoTarget();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithoutAttribute(ssh, "_SHUNT_RC", "", "", "", "", "");

            network = ShuntTestCaseFactory.createRemoteLinearNoTarget();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SHUNT_RC", "true", "false", "5", "0", "k");

            // SC nonlinear
            network = ShuntTestCaseFactory.createNonLinear();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SHUNT_RC", "true", "true", "5", "200", "k");
            testTcTccWithoutAttribute(ssh, "", "", "", "", "", "M");

            network = ShuntTestCaseFactory.createLocalNonLinear();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SHUNT_RC", "true", "true", "5", "200", "k");
            testTcTccWithoutAttribute(ssh, "", "", "", "", "", "M");

            network = ShuntTestCaseFactory.createDisabledRemoteNonLinear();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SHUNT_RC", "true", "false", "5", "200", "k");
            testTcTccWithoutAttribute(ssh, "", "", "", "", "", "M");

            network = ShuntTestCaseFactory.createDisabledLocalNonLinear();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SHUNT_RC", "true", "false", "5", "200", "k");
            testTcTccWithoutAttribute(ssh, "", "", "", "", "", "M");

            network = ShuntTestCaseFactory.createLocalNonLinearNoTarget();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testTcTccWithoutAttribute(ssh, "_SHUNT_RC", "", "", "", "", "");

            network = ShuntTestCaseFactory.createRemoteNonLinearNoTarget();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_SHUNT_RC", "true", "false", "5", "0", "k");
        }
    }

    @Test
    void generatorRegulatingControlSSHTest() throws IOException {
        String exportFolder = "/test-gen-rc";
        String baseName = "testGenRc";
        Network network;
        String ssh;
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath(exportFolder));
            Properties exportParams = new Properties();
            exportParams.put(CgmesExport.PROFILES, "SSH");

            // Generator local voltage
            network = EurostagTutorialExample1Factory.create();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "true", "0", "24.5", "k");
            network.getGenerator("GEN").setVoltageRegulatorOn(false);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "false", "0", "24.5", "k");

            // Generator remote voltage
            network = EurostagTutorialExample1Factory.createWithRemoteVoltageGenerator();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "true", "0", "399", "k");
            network.getGenerator("GEN").setVoltageRegulatorOn(false);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "false", "0", "399", "k");

            // Generator with remote voltage regulation exported in local regulation mode
            Properties exportInLocalRegulationModeParams = new Properties();
            exportInLocalRegulationModeParams.put(CgmesExport.PROFILES, "SSH");
            exportInLocalRegulationModeParams.put(CgmesExport.EXPORT_GENERATORS_IN_LOCAL_REGULATION_MODE, true);
            network = EurostagTutorialExample1Factory.createWithRemoteVoltageGenerator();
            ssh = getSSH(network, baseName, tmpDir, exportInLocalRegulationModeParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "true", "0", "25.2", "k");

            // Generator with local reactive
            network = EurostagTutorialExample1Factory.createWithLocalReactiveGenerator();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "true", "0", "200", "M");
            network.getGenerator("GEN").getExtension(RemoteReactivePowerControl.class).setEnabled(false);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "false", "0", "200", "M");

            // Generator with remote reactive
            network = EurostagTutorialExample1Factory.createWithRemoteReactiveGenerator();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "true", "0", "200", "M");
            network.getGenerator("GEN").getExtension(RemoteReactivePowerControl.class).setEnabled(false);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "false", "0", "200", "M");

            // Generator with local reactive and voltage
            network = EurostagTutorialExample1Factory.createWithLocalReactiveAndVoltageGenerator();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "true", "0", "24.5", "k");
            network.getGenerator("GEN").setVoltageRegulatorOn(false);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "true", "0", "200", "M");
            network.getGenerator("GEN").getExtension(RemoteReactivePowerControl.class).setEnabled(false);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "false", "0", "24.5", "k");
            network.getGenerator("GEN").setVoltageRegulatorOn(true);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "true", "0", "24.5", "k");

            // Generator with remote reactive and voltage
            network = EurostagTutorialExample1Factory.createWithRemoteReactiveAndVoltageGenerators();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "true", "0", "399", "k");
            network.getGenerator("GEN").setVoltageRegulatorOn(false);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "true", "0", "200", "M");
            network.getGenerator("GEN").getExtension(RemoteReactivePowerControl.class).setEnabled(false);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "false", "0", "399", "k");
            network.getGenerator("GEN").setVoltageRegulatorOn(true);
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "true", "0", "399", "k");

            // Generator without control
            network = EurostagTutorialExample1Factory.createWithoutControl();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "false", "0", "0", "k");

            // Generator with remote terminal without control
            network = EurostagTutorialExample1Factory.createRemoteWithoutControl();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRcWithAttribute(ssh, "_GEN_RC", "false", "false", "0", "0", "k");

            // Generator without control capability
            network = EurostagTutorialExample1Factory.create();
            network.getGenerator("GEN").newMinMaxReactiveLimits().setMaxQ(0).setMinQ(0).add();
            ssh = getSSH(network, baseName, tmpDir, exportParams);
            testRcEqRCWithoutAttribute(ssh, "_GEN_RC");
        }
    }

    private void testTcTccWithoutAttribute(String ssh, String rcID, String discrete, String enabled, String deadband, String target, String multiplier) {
        assertFalse(ssh.contains("cim:TapChangerControl rdf:about=\"#" + rcID + "\""));
        assertFalse(ssh.contains("<cim:RegulatingControl.discrete>" + discrete + "</cim:RegulatingControl.discrete>"));
        assertFalse(ssh.contains("<cim:RegulatingControl.enabled>" + enabled + "</cim:RegulatingControl.enabled>"));
        assertFalse(ssh.contains("<cim:RegulatingControl.targetDeadband>" + deadband + "</cim:RegulatingControl.targetDeadband>"));
        assertFalse(ssh.contains("<cim:RegulatingControl.targetValue>" + target + "</cim:RegulatingControl.targetValue>"));
        assertFalse(ssh.contains("UnitMultiplier." + multiplier + "\""));
    }

    private void testTcTccWithAttribute(String ssh, String rcID, String discrete, String enabled, String deadband, String target, String multiplier) {
        assertTrue(ssh.contains("cim:TapChangerControl rdf:about=\"#" + rcID + "\""));
        assertTrue(ssh.contains("<cim:RegulatingControl.discrete>" + discrete + "</cim:RegulatingControl.discrete>"));
        assertTrue(ssh.contains("<cim:RegulatingControl.enabled>" + enabled + "</cim:RegulatingControl.enabled>"));
        assertTrue(ssh.contains("<cim:RegulatingControl.targetDeadband>" + deadband + "</cim:RegulatingControl.targetDeadband>"));
        assertTrue(ssh.contains("<cim:RegulatingControl.targetValue>" + target + "</cim:RegulatingControl.targetValue>"));
        assertTrue(ssh.contains("UnitMultiplier." + multiplier + "\""));
    }

    private void testRcEqRCWithoutAttribute(String ssh, String rcID) {
        assertFalse(ssh.contains("cim:RegulatingControl rdf:about=\"#" + rcID + "\""));
    }

    private void testRcEqRcWithAttribute(String ssh, String rcID, String discrete, String enabled, String deadband, String target, String multiplier) {
        assertTrue(ssh.contains("cim:RegulatingControl rdf:about=\"#" + rcID + "\""));
        assertTrue(ssh.contains("<cim:RegulatingControl.discrete>" + discrete + "</cim:RegulatingControl.discrete>"));
        assertTrue(ssh.contains("<cim:RegulatingControl.enabled>" + enabled + "</cim:RegulatingControl.enabled>"));
        assertTrue(ssh.contains("<cim:RegulatingControl.targetDeadband>" + deadband + "</cim:RegulatingControl.targetDeadband>"));
        assertTrue(ssh.contains("<cim:RegulatingControl.targetValue>" + target + "</cim:RegulatingControl.targetValue>"));
        assertTrue(ssh.contains("UnitMultiplier." + multiplier + "\""));
    }

    private String getSSH(Network network, String baseName, Path tmpDir, Properties exportParams) throws IOException {
        new CgmesExport().export(network, exportParams, new DirectoryDataSource(tmpDir, baseName));
        return Files.readString(tmpDir.resolve(baseName + "_SSH.xml"));
    }

    private static void copyBoundary(Path outputFolder, String baseName, ReadOnlyDataSource originalDataSource) throws IOException {
        String eqbd = originalDataSource.listNames(".*EQ_BD.*").stream().findFirst().orElse(null);
        if (eqbd != null) {
            try (InputStream is = originalDataSource.newInputStream(eqbd)) {
                Files.copy(is, outputFolder.resolve(baseName + "_EQ_BD.xml"));
            }
        }
    }
}
