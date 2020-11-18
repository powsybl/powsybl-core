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
import org.junit.Test;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class StateVariablesExportTest extends AbstractConverterTest {

    @Test
    public void microGridBE() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), 2);
    }

    @Test
    public void smallGrid() throws IOException, XMLStreamException {
        test(CgmesConformity1Catalog.smallBusBranch().dataSource(), 4);
    }

    private void test(ReadOnlyDataSource dataSource, int svVersion) throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put("iidm.import.cgmes.profile-used-for-initial-state-values", "SV");

        // Import original
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);

        // Export SV
        Path test = tmpDir.resolve("test.xml");
        try (OutputStream os = Files.newOutputStream(test)) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            StateVariablesExport.write(expected, writer, new CgmesExportContext(expected).setSvVersion(svVersion));
        }

        // Zip with new SV
        try (OutputStream fos = Files.newOutputStream(tmpDir.resolve("repackaged.zip"));
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            try (InputStream eqIs = newInputStream(dataSource, this::eq)) {
                zipFile("test_EQ.xml", eqIs, zipOut);
            }
            try (InputStream tpIs = newInputStream(dataSource, this::tp)) {
                zipFile("test_TP.xml", tpIs, zipOut);
            }
            try (InputStream sshIs = newInputStream(dataSource, this::ssh)) {
                zipFile("test_SSH.xml", sshIs, zipOut);
            }
            try (InputStream svIs = Files.newInputStream(test)) {
                zipFile("test_SV.xml", svIs, zipOut);
            }
            try (InputStream eqBdIs = newInputStream(dataSource, this::eqBd)) {
                zipFile("test_EQ_BD.xml", eqBdIs, zipOut);
            }
            try (InputStream tpBdIs = newInputStream(dataSource, this::tpBd)) {
                zipFile("test_TP_BD.xml", tpBdIs, zipOut);
            }
        }

        // Import with new SV
        Network actual = Importers.loadNetwork(tmpDir.resolve("repackaged.zip"),
                DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager(), ImportConfig.load(), properties);

        // Export original and with new SV
        NetworkXml.writeAndValidate(expected, tmpDir.resolve("expected"));
        NetworkXml.writeAndValidate(actual, tmpDir.resolve("actual"));

        // Compare
        try (InputStream expIs = Files.newInputStream(tmpDir.resolve("expected"));
             InputStream actIs = Files.newInputStream(tmpDir.resolve("actual"))) {
            compareXmlWithDelta(expIs, actIs);
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

    private static void compareXmlWithDelta(InputStream expected, InputStream actual) {
        Source control = Input.fromStream(expected).build();
        Source test = Input.fromStream(actual).build();
        Diff myDiff = DiffBuilder.compare(control).withTest(test).ignoreWhitespace().ignoreComments().build();
        for (Difference diff : myDiff.getDifferences()) {
            if (diff.getComparison().getControlDetails().getXPath().endsWith("forecastDistance")) {
                continue;
            }
            double exp = Double.parseDouble((String) diff.getComparison().getControlDetails().getValue());
            double act = Double.parseDouble((String) diff.getComparison().getTestDetails().getValue());
            assertEquals(exp, act, getDelta(diff.getComparison().getControlDetails().getXPath()));
        }
    }

    private static double getDelta(String xPath) {
        if (xPath.contains("danglingLine")) {
            return 1e-1;
        }
        return 0.0;
    }
}
