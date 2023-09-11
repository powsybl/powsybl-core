/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.SteadyStateHypothesisExport;
import com.powsybl.cgmes.extensions.CgmesControlAreas;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.iidm.network.ImportConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.xml.NetworkXml;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;

import javax.xml.stream.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
class SteadyStateHypothesisExportTest extends AbstractConverterTest {

    @Test
    void microGridBE() throws IOException, XMLStreamException {
        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
            ExportXmlCompare::sameScenarioTime,
            ExportXmlCompare::ensuringIncreasedModelVersion,
            ExportXmlCompare::ignoringSynchronousMachinesSVCsWithTargetDeadband,
            ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        test(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), 2, knownDiffs);
    }

    @Test
    void microGridBEWithHiddenTapChangers() throws IOException, XMLStreamException {
        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
            ExportXmlCompare::sameScenarioTime,
            ExportXmlCompare::ensuringIncreasedModelVersion,
            ExportXmlCompare::ignoringSynchronousMachinesSVCsWithTargetDeadband,
            ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        test(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEHiddenTapChangers().dataSource(), 2, knownDiffs);
    }

    @Test
    void microGridBEWithSharedRegulatingControl() throws IOException, XMLStreamException {
        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
            ExportXmlCompare::sameScenarioTime,
            ExportXmlCompare::ensuringIncreasedModelVersion,
            ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        test(CgmesConformity1ModifiedCatalog.microGridBaseCaseBESharedRegulatingControl().dataSource(), 2, knownDiffs);
    }

    @Test
    void smallGrid() throws IOException, XMLStreamException {
        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
            ExportXmlCompare::sameScenarioTime,
            ExportXmlCompare::ensuringIncreasedModelVersion,
            ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        test(CgmesConformity1Catalog.smallBusBranch().dataSource(), 4, knownDiffs, DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                ExportXmlCompare::numericDifferenceEvaluator,
                ExportXmlCompare::ignoringControlAreaNetInterchange));
    }

    @Test
    void smallGridHVDC() throws IOException, XMLStreamException {
        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
                ExportXmlCompare::sameScenarioTime,
                ExportXmlCompare::ensuringIncreasedModelVersion,
                ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        test(CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource(), 4, knownDiffs, DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                ExportXmlCompare::numericDifferenceEvaluator,
                ExportXmlCompare::ignoringControlAreaNetInterchange,
                ExportXmlCompare::ignoringHvdcLinePmax));
    }

    private void test(ReadOnlyDataSource dataSource, int version, DifferenceEvaluator knownDiffs) throws IOException, XMLStreamException {
        test(dataSource, version, knownDiffs, DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                ExportXmlCompare::numericDifferenceEvaluator));
    }

    private void test(ReadOnlyDataSource dataSource, int version, DifferenceEvaluator knownDiffsSsh, DifferenceEvaluator knownDiffsIidm) throws IOException, XMLStreamException {
        // Import original
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.create-cgmes-export-mapping", "true");
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);

        // Export SSH
        Path exportedSsh = tmpDir.resolve("exportedSsh.xml");
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(exportedSsh))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(expected);
            context.getSshModelDescription().setVersion(version);
            SteadyStateHypothesisExport.write(expected, writer, context);
        }

        // Compare the exported SSH with the original one
        try (InputStream expectedssh = Repackager.newInputStream(dataSource, Repackager::ssh);
             InputStream actualssh = Files.newInputStream(exportedSsh)) {
            ExportXmlCompare.compareSSH(expectedssh, actualssh, knownDiffsSsh);
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
                DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager(), ImportConfig.load(), properties);

        // Remove ControlAreas extension
        expected.removeExtension(CgmesControlAreas.class);
        actual.removeExtension(CgmesControlAreas.class);

        // Export original and with new SSH
        NetworkXml.writeAndValidate(expected, tmpDir.resolve("expected.xml"));
        NetworkXml.writeAndValidate(actual, tmpDir.resolve("actual.xml"));

        // Compare
        ExportXmlCompare.compareNetworks(tmpDir.resolve("expected.xml"), tmpDir.resolve("actual.xml"), knownDiffsIidm);
    }

    @Test
    void equivalentShuntTest() throws XMLStreamException {
        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.microGridBaseCaseBEEquivalentShunt().dataSource();
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), null);

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
        context.getSshModelDescription().setVersion(sshVersion);
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
}
