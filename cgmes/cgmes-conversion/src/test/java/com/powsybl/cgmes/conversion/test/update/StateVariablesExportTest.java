/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.update;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.update.CgmesExportContext;
import com.powsybl.cgmes.conversion.update.StateVariablesExport;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXml;
import javanet.staxutils.IndentingXMLStreamWriter;
import org.junit.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class StateVariablesExportTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.profile-used-for-initial-state-values", "SV");

        // Import original micro grid BE
        Network expected = new CgmesImport().importData(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), properties);

        // Export SV
        Path test = tmpDir.resolve("test");
        try (OutputStream os = Files.newOutputStream(test)) {
            XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(os, StandardCharsets.UTF_8.toString());
            IndentingXMLStreamWriter indentingWriter = new IndentingXMLStreamWriter(writer);
            indentingWriter.setIndent("    ");
            writer = indentingWriter;
            StateVariablesExport.write(expected, writer, new CgmesExportContext(expected).setSvVersion(2));
        }

        // Zip micro grid BE with new SV
        try (OutputStream fos = Files.newOutputStream(tmpDir.resolve("repackaged-microGrid-BE.zip"));
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            try (InputStream eqIs = CgmesConformity1Catalog.class.getResourceAsStream("/conformity/cas-1.1.3-data-4.0.3/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/MicroGridTestConfiguration_BC_BE_EQ_V2.xml")) {
                zipFile("EQ.xml", eqIs, zipOut);
            }
            try (InputStream tpIs = CgmesConformity1Catalog.class.getResourceAsStream("/conformity/cas-1.1.3-data-4.0.3/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/MicroGridTestConfiguration_BC_BE_TP_V2.xml")) {
                zipFile("TP.xml", tpIs, zipOut);
            }
            try (InputStream sshIs = CgmesConformity1Catalog.class.getResourceAsStream("/conformity/cas-1.1.3-data-4.0.3/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BC_BE_v2/MicroGridTestConfiguration_BC_BE_SSH_V2.xml")) {
                zipFile("SSH.xml", sshIs, zipOut);
            }
            try (InputStream svIs = Files.newInputStream(test)) {
                zipFile("SV.xml", svIs, zipOut);
            }
            try (InputStream eqBdIs = CgmesConformity1Catalog.class.getResourceAsStream("/conformity/cas-1.1.3-data-4.0.3/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/MicroGridTestConfiguration_EQ_BD.xml")) {
                zipFile("EQ_BD.xml", eqBdIs, zipOut);
            }
            try (InputStream tpBdIs = CgmesConformity1Catalog.class.getResourceAsStream("/conformity/cas-1.1.3-data-4.0.3/MicroGrid/BaseCase/CGMES_v2.4.15_MicroGridTestConfiguration_BD_v2/MicroGridTestConfiguration_TP_BD.xml")) {
                zipFile("TP_BD.xml", tpBdIs, zipOut);
            }
        }

        // Import micro grid BE with new SV
        Network actual = Importers.loadNetwork(tmpDir.resolve("repackaged-microGrid-BE.zip"),
                DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager(), ImportConfig.load(), properties);

        // Export original micro grid BE and micro grid BE with new SV
        NetworkXml.writeAndValidate(expected, tmpDir.resolve("expected"));
        NetworkXml.writeAndValidate(actual, tmpDir.resolve("actual"));

        // Compare
        try (InputStream expIs = Files.newInputStream(tmpDir.resolve("expected"));
             InputStream actIs = Files.newInputStream(tmpDir.resolve("actual"))) {
            compareXmlWithDelta(expIs, actIs);
        }
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

    private static void compareXmlWithDelta(InputStream expected, InputStream actual) {
        Source control = Input.fromStream(expected).build();
        Source test = Input.fromStream(actual).build();
        Diff myDiff = DiffBuilder.compare(control).withTest(test).ignoreWhitespace().ignoreComments().build();
        for (Difference diff : myDiff.getDifferences()) {
            double exp = Double.parseDouble((String) diff.getComparison().getControlDetails().getValue());
            double act = Double.parseDouble((String) diff.getComparison().getTestDetails().getValue());
            assertEquals(exp, act, getDelta(diff.getComparison().getControlDetails().getXPath()));
        }
    }

    private static double getDelta(String xPath) {
        if (xPath.contains("danglingLine")) {
            if (xPath.endsWith("p") || xPath.endsWith("q")) {
                return 1e-1;
            }
            return 1e-5;
        }
        return 0.0;
    }
}
