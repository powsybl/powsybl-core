/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import com.powsybl.commons.config.PlatformConfig;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.test.network.compare.Comparison;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.XMLExporter;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.mock.LoadFlowFactoryMock;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletion;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletionParameters;
import com.powsybl.loadflow.validation.ValidationConfig;
import com.powsybl.loadflow.validation.ValidationType;
import com.powsybl.triplestore.api.TripleStoreFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class ConversionTester {

    public ConversionTester(Properties importParams, List<String> tripleStoreImplementations,
            ComparisonConfig networkComparison) {
        this.importParams = importParams;
        this.tripleStoreImplementations = tripleStoreImplementations;
        this.networkComparison = networkComparison;
        this.onlyReport = false;
        this.strictTopologyTest = true;
        this.exportXiidm = false;
        this.exportCgmes = false;
        this.testExportImportCgmes = false;
    }

    public ConversionTester(List<String> tripleStoreImplementations, ComparisonConfig networkComparison) {
        this.importParams = null;
        this.tripleStoreImplementations = tripleStoreImplementations;
        this.networkComparison = networkComparison;
        this.onlyReport = false;
        this.strictTopologyTest = true;
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

    public void setValidateBusBalances(boolean b) {
        this.validateBusBalances = b;
    }

    public void testConversion(Network expected, TestGridModel gm) throws IOException {
        testConversion(expected, gm, this.networkComparison);
    }

    public void testConversion(Network expected, TestGridModel gm, ComparisonConfig config)
            throws IOException {
        if (onlyReport) {
            testConversionOnlyReport(gm);
        } else {
            for (String impl : tripleStoreImplementations) {
                LOG.info("testConversion. TS implementation {}, grid model {}", impl, gm.name());
                testConversion(expected, gm, config, impl);
            }
        }
    }

    public Network lastConvertedNetwork() {
        return lastConvertedNetwork;
    }

    private void testConversion(Network expected, TestGridModel gm, ComparisonConfig cconfig, String impl)
            throws IOException {
        Properties iparams = importParams == null ? new Properties() : importParams;
        iparams.put("storeCgmesModelAsNetworkExtension", "true");
        iparams.put("powsyblTripleStore", impl);
        // This is to be able to easily compare the topology computed
        // by powsybl against the topology present in the CGMES model
        iparams.put("createBusbarSectionForEveryConnectivityNode", "true");
        try (FileSystem fs = Jimfs.newFileSystem()) {
            PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
            CgmesImport i = new CgmesImport(platformConfig);
            ReadOnlyDataSource ds = gm.dataSource();
            Network network = i.importData(ds, iparams);
            if (network.getSubstationCount() == 0) {
                fail("Model is empty");
            }
            CgmesModel cgmes = network.getExtension(CgmesModelExtension.class).getCgmesModel();
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
            if (validateBusBalances) {
                validateBusBalances(network);
            }
            lastConvertedNetwork = network;
        }
    }

    private void testConversionOnlyReport(TestGridModel gm) throws IOException {
        String impl = TripleStoreFactory.defaultImplementation();
        try (FileSystem fs = Jimfs.newFileSystem()) {
            PlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
            CgmesImport i = new CgmesImport(platformConfig);
            Properties params = new Properties();
            params.put("storeCgmesModelAsNetworkExtension", "true");
            params.put("powsyblTripleStore", impl);
            ReadOnlyDataSource ds = gm.dataSource();
            LOG.info("Importer.exists() == {}", i.exists(ds));
            Network n = i.importData(ds, params);
            CgmesModel m = n.getExtension(CgmesModelExtension.class).getCgmesModel();
            new Conversion(m).report(reportConsumer);
        }
    }

    private void exportXiidm(String name, String impl, Network expected, Network actual) throws IOException {
        String name1 = name.replace('/', '-');
        Path path = Files.createTempDirectory("temp-conversion-" + name1 + "-" + impl + "-");
        XMLExporter xmlExporter = new XMLExporter(Mockito.mock(PlatformConfig.class));
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

    private void validateBusBalances(Network network) throws IOException {
        // Precision required on bus balances (MVA)
        double threshold = 0.01;
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            ValidationConfig config = loadFlowValidationConfig(fs, threshold);
            Path working = Files.createDirectories(fs.getPath("lf-validation"));

            computeMissingFlows(network, config.getLoadFlowParameters());
            assertTrue(ValidationType.BUSES.check(network, config, working));
        }
    }

    private ValidationConfig loadFlowValidationConfig(FileSystem fs, double threshold) {
        InMemoryPlatformConfig pconfig = new InMemoryPlatformConfig(fs);
        pconfig
                .createModuleConfig("componentDefaultConfig")
                .setStringProperty("LoadFlowFactory", LoadFlowFactoryMock.class.getCanonicalName());
        ValidationConfig config = ValidationConfig.load(pconfig);
        config.setVerbose(true);
        config.setThreshold(threshold);
        config.setOkMissingValues(false);
        config.setLoadFlowParameters(new LoadFlowParameters());
        LOG.info("specificCompatibility is {}", config.getLoadFlowParameters().isSpecificCompatibility());
        return config;
    }

    private void computeMissingFlows(Network network, LoadFlowParameters lfparams) {
        LoadFlowResultsCompletionParameters p = new LoadFlowResultsCompletionParameters(
                LoadFlowResultsCompletionParameters.EPSILON_X_DEFAULT,
                LoadFlowResultsCompletionParameters.APPLY_REACTANCE_CORRECTION_DEFAULT,
                LoadFlowResultsCompletionParameters.Z0_THRESHOLD_DIFF_VOLTAGE_ANGLE);
        LoadFlowResultsCompletion lf = new LoadFlowResultsCompletion(p, lfparams);
        try {
            lf.run(network, null);
        } catch (Exception e) {
            LOG.error("computeFlows, error {}", e.getMessage());
        }
    }

    private final Properties importParams;
    private final List<String> tripleStoreImplementations;
    private final ComparisonConfig networkComparison;
    private boolean onlyReport;
    private boolean exportXiidm;
    private boolean exportCgmes;
    private boolean testExportImportCgmes;
    private boolean validateBusBalances;
    private Consumer<String> reportConsumer;
    private boolean strictTopologyTest;
    private Network lastConvertedNetwork;

    private static final Logger LOG = LoggerFactory.getLogger(ConversionTester.class);
}
