/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.test.network.compare.Comparison;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.XMLExporter;
import com.powsybl.triplestore.api.TripleStoreFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class ConversionTester {

    public ConversionTester(Properties importParams, List<String> tripleStoreImplementations, ComparisonConfig networkComparison) {
        this.importParams = importParams;
        this.tripleStoreImplementations = tripleStoreImplementations;
        this.networkComparison = networkComparison;
        this.onlyReport = false;
        this.strictTopologyTest = false;
        this.exportXiidm = false;
        this.exportCgmes = false;
        this.testExportImportCgmes = false;
    }

    public ConversionTester(List<String> tripleStoreImplementations, ComparisonConfig networkComparison) {
        this.importParams = null;
        this.tripleStoreImplementations = tripleStoreImplementations;
        this.networkComparison = networkComparison;
        this.onlyReport = false;
        this.strictTopologyTest = false;
        this.exportXiidm = false;
        this.exportCgmes = false;
        this.testExportImportCgmes = false;
    }

    public void setOnlyReport(boolean onlyReport) {
        this.onlyReport = onlyReport;
    }

    public void setReportConsumer(Consumer<String> reportConsumer) {
        this.reportConsumer = reportConsumer;
    }

    public void setStrictTopologyTest(boolean strictTopologyTest) {
        this.strictTopologyTest = strictTopologyTest;
    }

    public void setExportXiidm(boolean exportXiidm) {
        this.exportXiidm = exportXiidm;
    }

    public void setExportCgmes(boolean exportCgmes) {
        this.exportCgmes = exportCgmes;
    }

    public void setTestExportImportCgmes(boolean testExportImportCgmes) {
        this.testExportImportCgmes = testExportImportCgmes;
    }

    public void testConversion(Network expected, TestGridModel gm) throws IOException {
        testConversion(expected, gm, this.networkComparison);
    }

    public void testConversion(Network expected, TestGridModel gm, ComparisonConfig config)
            throws IOException {
        if (!gm.exists()) {
            LOG.error("Test grid model does not exist {}", gm.name());
            return;
        }
        if (onlyReport) {
            testConversionOnlyReport(gm);
        } else {
            for (String impl : tripleStoreImplementations) {
                LOG.info("testConversion. TS implementation {}, grid model {}", impl, gm.name());
                testConversion(expected, gm, config, impl);
            }
        }
    }

    private void testConversion(Network expected, TestGridModel gm, ComparisonConfig cconfig, String impl)
            throws IOException {
        Properties iparams = importParams == null ? new Properties() : importParams;
        iparams.put("storeCgmesModelAsNetworkProperty", "true");
        iparams.put("powsyblTripleStore", impl);
        CgmesImport i = new CgmesImport();
        try (FileSystem fs = Jimfs.newFileSystem()) {
            ReadOnlyDataSource ds = gm.dataSourceBasedOn(fs);
            Network network = i.importData(ds, iparams);
            if (network.getSubstationCount() == 0) {
                fail("Model is empty");
            }
            CgmesModel cgmes = (CgmesModel) network.getProperties().get(CgmesImport.NETWORK_PS_CGMES_MODEL);
            if (!new TopologyTester(cgmes, network).test(strictTopologyTest)) {
                fail("Topology test failed");
            }
            if (expected != null) {
                new Comparison(expected, network, cconfig).compare();
            }
            if (exportXiidm) {
                exportXiidm(gm.name(), impl, expected, network);
            }
            if (exportCgmes) {
                exportCgmes(gm.name(), impl, network);
            }
            if (testExportImportCgmes) {
                testExportImportCgmes(network, fs, i, iparams, cconfig);
            }
        }
    }

    private void testConversionOnlyReport(TestGridModel gm) throws IOException {
        String impl = TripleStoreFactory.defaultImplementation();
        CgmesImport i = new CgmesImport();
        Properties params = new Properties();
        params.put("storeCgmesModelAsNetworkProperty", "true");
        params.put("powsyblTripleStore", impl);
        try (FileSystem fs = Jimfs.newFileSystem()) {
            ReadOnlyDataSource ds = gm.dataSourceBasedOn(fs);
            LOG.info("Importer.exists() == {}", i.exists(ds));
            Network n = i.importData(ds, params);
            CgmesModel m = (CgmesModel) n.getProperties().get(CgmesImport.NETWORK_PS_CGMES_MODEL);
            new Conversion(m).report(reportConsumer);
        }
    }

    private void exportXiidm(String name, String impl, Network expected, Network actual) throws IOException {
        String name1 = name.replace('/', '-');
        Path path = Files.createTempDirectory("temp-conversion-" + name1 + "-" + impl + "-");
        XMLExporter xmlExporter = new XMLExporter();
        // Last component of the path is the name for the exported XML
        if (expected != null) {
            xmlExporter.export(expected, null, new FileDataSource(path, "expected"));
        }
        if (actual != null) {
            xmlExporter.export(actual, null, new FileDataSource(path, "actual"));
        }
    }

    private void exportCgmes(String name, String impl, Network network) throws IOException {
        String name1 = name.replace('/', '-');
        Path path = Files.createTempDirectory("temp-export-cgmes-" + name1 + "-" + impl + "-");
        new CgmesExport().export(network, null, new FileDataSource(path, "foo"));
    }

    private void testExportImportCgmes(Network network, FileSystem fs, CgmesImport i, Properties iparams,
            ComparisonConfig config) throws IOException {

        Path path = fs.getPath("temp-export-cgmes");
        Files.createDirectories(path);
        new CgmesExport().export(network, null, new FileDataSource(path, "bar"));

        ReadOnlyDataSource ds = new FileDataSource(path, "bar");
        Network actual = i.importData(ds, iparams);
        Network expected = network;
        new Comparison(expected, actual, config).compare();
    }

    private final Properties importParams;
    private final List<String> tripleStoreImplementations;
    private final ComparisonConfig networkComparison;
    private boolean onlyReport;
    private boolean exportXiidm;
    private boolean exportCgmes;
    private boolean testExportImportCgmes;
    private Consumer<String> reportConsumer;
    private boolean strictTopologyTest;

    private static final Logger LOG = LoggerFactory.getLogger(ConversionTester.class);
}
