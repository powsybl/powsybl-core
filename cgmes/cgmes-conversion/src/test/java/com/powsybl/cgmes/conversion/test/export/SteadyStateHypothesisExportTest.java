/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.*;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.SteadyStateHypothesisExport;
import com.powsybl.cgmes.extensions.CgmesControlArea;
import com.powsybl.cgmes.extensions.CgmesControlAreas;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.iidm.network.ImportConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;

import javax.xml.stream.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
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
        assertTrue(test(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), 2, knownDiffsSsh, knownDiffsXiidm));
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
        assertTrue(test(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEHiddenTapChangers().dataSource(), 2, knownDiffsSsh, knownDiffsXiidm));
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
        assertTrue(test(CgmesConformity1ModifiedCatalog.microGridBaseCaseBESharedRegulatingControl().dataSource(), 2, knownDiffsSsh, knownDiffsXiidm));
    }

    @Test
    void smallGrid() throws IOException, XMLStreamException {
        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
            ExportXmlCompare::sameScenarioTime,
            ExportXmlCompare::ensuringIncreasedModelVersion,
            ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        assertTrue(test(CgmesConformity1Catalog.smallBusBranch().dataSource(), 4, knownDiffs, DifferenceEvaluators.chain(
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
        assertTrue(test(CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource(), 4, knownDiffs, DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                ExportXmlCompare::numericDifferenceEvaluator,
                ExportXmlCompare::ignoringControlAreaNetInterchange,
                ExportXmlCompare::ignoringCgmesMetadataModels,
                ExportXmlCompare::ignoringHvdcLinePmax)));
    }

    private boolean test(ReadOnlyDataSource dataSource, int version, DifferenceEvaluator knownDiffsSsh, DifferenceEvaluator knownDiffsIidm) throws IOException, XMLStreamException {
        // Import original
        importParams.put("iidm.import.cgmes.create-cgmes-export-mapping", "true");
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), importParams);

        // Export SSH
        Path exportedSsh = tmpDir.resolve("exportedSsh.xml");
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(exportedSsh))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(expected);
            context.getExportedSSHModel().setVersion(version);
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

        // Remove ControlAreas extension
        expected.removeExtension(CgmesControlAreas.class);
        actual.removeExtension(CgmesControlAreas.class);

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

        String ssh = exportSshAsString(network, 2);

        // Equivalent shunts should not have entries in SSH
        String equivalentShuntId = "d771118f-36e9-4115-a128-cc3d9ce3e3da";
        assertNotNull(network.getShuntCompensator(equivalentShuntId));
        SshLinearShuntCompensators sshLinearShuntCompensators = readSshLinearShuntCompensator(ssh);
        assertFalse(sshLinearShuntCompensators.map.isEmpty());
        assertFalse(sshLinearShuntCompensators.map.containsKey(equivalentShuntId));
    }

    private static String exportSshAsString(Network network, int sshVersion) throws XMLStreamException {
        CgmesExportContext context = new CgmesExportContext(network);
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", stringWriter);
        context.getExportedSSHModel().setVersion(sshVersion);
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
        CgmesControlAreas controlAreas = be.getExtension(CgmesControlAreas.class);
        assertNotNull(controlAreas);
        assertFalse(controlAreas.getCgmesControlAreas().isEmpty());
        CgmesControlArea controlArea = controlAreas.getCgmesControlAreas().iterator().next();
        assertEquals(236.9798, controlArea.getNetInterchange(), 1e-10);
        assertEquals(10, controlArea.getPTolerance(), 1e-10);

        // Update control area data
        controlArea.setNetInterchange(controlArea.getNetInterchange() * 2);
        controlArea.setPTolerance(controlArea.getPTolerance() / 2);

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

        // Check that SSH full model contains a reference to the original SSH that is superseding
        String modelSupersedes = readSshModelSupersedes(outputPath.resolve("BE_SSH.xml"));
        assertEquals("urn:uuid:1b092ff0-f8a0-49da-82d3-75eff5f1e820", modelSupersedes);
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

    private static final String ATTR_RESOURCE = "resource";
    private static final String MODEL_SUPERSEDES = "Model.Supersedes";

    private static String readSshModelSupersedes(Path ssh) {
        String modelSupersedes;
        try (InputStream is = Files.newInputStream(ssh)) {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
            while (reader.hasNext()) {
                int next = reader.next();
                if (next == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getLocalName().equals(MODEL_SUPERSEDES)) {
                        modelSupersedes = reader.getAttributeValue(CgmesNamespace.RDF_NAMESPACE, ATTR_RESOURCE);
                        reader.close();
                        return modelSupersedes;
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Test
    void microGridCgmesExportPreservingOriginalClassesOfLoads() throws IOException, XMLStreamException {
        ReadOnlyDataSource ds = Cgmes3ModifiedCatalog.microGridBaseCaseAllTypesOfLoads().dataSource();
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), importParams);

        // Export as cgmes
        Path outputPath = tmpDir.resolve("temp.cgmesExport");
        Files.createDirectories(outputPath);
        String baseName = "microGridCgmesExportPreservingOriginalClassesOfLoads";
        new CgmesExport().export(network, new Properties(), DataSourceUtil.createDataSource(outputPath, "", baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(DataSourceUtil.createDataSource(outputPath, "", baseName), NetworkFactory.findDefault(), importParams);

        InputStream expectedSsh = Repackager.newInputStream(ds, Repackager::ssh);
        String actualSsh = exportSshAsString(actual, 5);

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
        new CgmesExport().export(network, new Properties(), DataSourceUtil.createDataSource(outputPath, "", baseName));

        // re-import after adding the original boundary files
        copyBoundary(outputPath, baseName, ds);
        Network actual = new CgmesImport().importData(DataSourceUtil.createDataSource(outputPath, "", baseName), NetworkFactory.findDefault(), new Properties());

        InputStream expectedSsh = Repackager.newInputStream(ds, Repackager::ssh);
        String actualSsh = exportSshAsString(actual, 5);

        DifferenceEvaluator knownDiffsSsh = DifferenceEvaluators.chain(
                ExportXmlCompare::ignoringFullModelDependentOn,
                ExportXmlCompare::ignoringFullModelModelingAuthoritySet,
                ExportXmlCompare::ignoringRdfChildNodeListLength,
                ExportXmlCompare::ignoringConformLoad,
                ExportXmlCompare::ignoringChildLookupNull);
        assertTrue(ExportXmlCompare.compareSSH(expectedSsh, new ByteArrayInputStream(actualSsh.getBytes(StandardCharsets.UTF_8)), knownDiffsSsh));
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
