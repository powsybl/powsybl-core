/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;

import org.junit.Test;
import org.w3c.dom.Attr;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluators;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.SteadyStateHypothesisExport;
import com.powsybl.cgmes.model.CgmesOnDataSource;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.xml.NetworkXml;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class SteadyStateHypothesisExportTest extends AbstractConverterTest {

    @Test
    public void microGridBE() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), 2);
    }

    @Test
    public void smallGrid() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.smallBusBranch().dataSource(), 4);
    }

    private void test(ReadOnlyDataSource dataSource, int version) throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.profile-used-for-initial-state-values", "SSH");

        // Import original
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);

        // Export SSH
        Path test = tmpDir.resolve("test.xml");
        try (OutputStream os = Files.newOutputStream(test)) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(expected);
            context.getSshModelDescription().setVersion(version);
            SteadyStateHypothesisExport.write(expected, writer, context);
        }

        try (InputStream expectedssh = newInputStream(dataSource, this::ssh); InputStream actualssh = Files.newInputStream(test)) {
            ExportXmlCompare.compareSSH(expectedssh, actualssh);
        }

        // Zip with new SV
        try (OutputStream fos = Files.newOutputStream(tmpDir.resolve("repackaged.zip"));
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            try (InputStream eqIs = newInputStream(dataSource, this::eq)) {
                zipFile("EQ.xml", eqIs, zipOut);
            }
            try (InputStream tpIs = newInputStream(dataSource, this::tp)) {
                zipFile("TP.xml", tpIs, zipOut);
            }
            try (InputStream sshIs = Files.newInputStream(test)) {
                zipFile("SSH.xml", sshIs, zipOut);
            }
            try (InputStream svIs = newInputStream(dataSource, this::sv)) {
                zipFile("SV.xml", svIs, zipOut);
            }
            try (InputStream eqBdIs = newInputStream(dataSource, this::eqBd)) {
                zipFile("EQ_BD.xml", eqBdIs, zipOut);
            }
            try (InputStream tpBdIs = newInputStream(dataSource, this::tpBd)) {
                zipFile("TP_BD.xml", tpBdIs, zipOut);
            }
        }

        // Import with new SSH
        Network actual = Importers.loadNetwork(tmpDir.resolve("repackaged.zip"),
                DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager(), ImportConfig.load(), properties);

        // Export original and with new SSH
        NetworkXml.writeAndValidate(expected, tmpDir.resolve("expected.xml"));
        NetworkXml.writeAndValidate(actual, tmpDir.resolve("actual.xml"));

        // Compare
        try (InputStream expIs = Files.newInputStream(tmpDir.resolve("expected.xml"));
             InputStream actIs = Files.newInputStream(tmpDir.resolve("actual.xml"))) {
            compareNetworkXml(expIs, actIs);
        }
    }

    private InputStream newInputStream(ReadOnlyDataSource ds, Predicate<String> file) throws IOException {
        return ds.newInputStream(getName(ds, file));
    }

    private String getName(ReadOnlyDataSource ds, Predicate<String> file) {
        CgmesOnDataSource ns = new CgmesOnDataSource(ds);
        return ns.names().stream().filter(n -> file.test(n)).findFirst().get();
    }

    private boolean eq(String name) {
        return !name.contains("_BD") && name.contains("_EQ");
    }

    private boolean tp(String name) {
        return !name.contains("_BD") && name.contains("_TP");
    }

    private boolean ssh(String name) {
        return name.contains("_SSH");
    }

    private boolean sv(String name) {
        return name.contains("_SV");
    }

    private boolean eqBd(String name) {
        return name.contains("_EQ") && name.contains("_BD");
    }

    private boolean tpBd(String name) {
        return name.contains("_TP") && name.contains("_BD");
    }

    private static void zipFile(String entryName, InputStream toZip, ZipOutputStream zipOut) throws IOException {
        ZipEntry zipEntry = new ZipEntry(entryName);
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = toZip.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
    }

    private static void compareNetworkXml(InputStream expected, InputStream actual) {
        Source control = Input.fromStream(expected).build();
        Source test = Input.fromStream(actual).build();
        Diff diff = DiffBuilder
            .compare(control)
            .withTest(test)
            .ignoreWhitespace()
            .ignoreComments()
            .withAttributeFilter(SteadyStateHypothesisExportTest::isConsidered)
            .withDifferenceEvaluator(DifferenceEvaluators.chain(
                    DifferenceEvaluators.Default,
                    ExportXmlCompare::numericDifferenceEvaluator))
            .withComparisonListeners(ExportXmlCompare::debugComparison)
            .build();
        assertTrue(!diff.hasDifferences());
    }

    private static boolean isConsidered(Attr attr) {
        return !(attr.getLocalName().equals("forecastDistance"));
    }
}
