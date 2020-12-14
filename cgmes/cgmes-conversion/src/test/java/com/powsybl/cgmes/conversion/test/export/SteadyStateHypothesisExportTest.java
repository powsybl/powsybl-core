/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.test.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.SteadyStateHypothesisExport;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class SteadyStateHypothesisExportTest extends AbstractConverterTest {

    @Test
    public void microGridBE() throws IOException, XMLStreamException {
        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
            ExportXmlCompare::sameScenarioTime,
            ExportXmlCompare::ensuringIncreasedModelVersion,
            ExportXmlCompare::ignoringSynchronousMachinesSVCsWithTargetDeadband,
            ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        test(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), 2, knownDiffs);
    }

    @Test
    public void microGridBEWithHiddenTapChangers() throws IOException, XMLStreamException {
        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
            ExportXmlCompare::sameScenarioTime,
            ExportXmlCompare::ensuringIncreasedModelVersion,
            ExportXmlCompare::ignoringSynchronousMachinesSVCsWithTargetDeadband,
            ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        test(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEHiddenTapChangers().dataSource(), 2, knownDiffs);
    }

    @Test
    public void microGridBEWithSharedRegulatingControl() throws IOException, XMLStreamException {
        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
            ExportXmlCompare::sameScenarioTime,
            ExportXmlCompare::ensuringIncreasedModelVersion,
            ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        test(CgmesConformity1ModifiedCatalog.microGridBaseCaseBESharedRegulatingControl().dataSource(), 2, knownDiffs);
    }

    @Test
    public void smallGrid() throws IOException, XMLStreamException {
        DifferenceEvaluator knownDiffs = DifferenceEvaluators.chain(
            ExportXmlCompare::sameScenarioTime,
            ExportXmlCompare::ensuringIncreasedModelVersion,
            ExportXmlCompare::ignoringJunctionOrBusbarTerminals);
        test(CgmesConformity1Catalog.smallBusBranch().dataSource(), 4, knownDiffs);
    }

    private void test(ReadOnlyDataSource dataSource, int version, DifferenceEvaluator knownDiffs) throws IOException, XMLStreamException {
        // Import original
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.profile-used-for-initial-state-values", "SSH");
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);

        // Export SSH
        Path exportedSsh = tmpDir.resolve("exportedSsh.xml");
        try (OutputStream os = Files.newOutputStream(exportedSsh)) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(expected);
            context.getSshModelDescription().setVersion(version);
            SteadyStateHypothesisExport.write(expected, writer, context);
        }

        // Compare the exported SSH with the original one
        try (InputStream expectedssh = Repackager.newInputStream(dataSource, Repackager::ssh);
                InputStream actualssh = Files.newInputStream(exportedSsh)) {
            ExportXmlCompare.compareSSH(expectedssh, actualssh, knownDiffs);
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
        Network actual = Importers.loadNetwork(repackaged,
                DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager(), ImportConfig.load(), properties);

        // Export original and with new SSH
        NetworkXml.writeAndValidate(expected, tmpDir.resolve("expected.xml"));
        NetworkXml.writeAndValidate(actual, tmpDir.resolve("actual.xml"));

        // Compare
        ExportXmlCompare.compareNetworks(tmpDir.resolve("expected.xml"), tmpDir.resolve("actual.xml"));
    }
}
