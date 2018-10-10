package com.powsybl.cgmes.conversion.test;

/*
 * #%L
 * CGMES conversion
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public ConversionTester(List<String> tripleStoreImplementations, ComparisonConfig networkComparison) {
        this.tripleStoreImplementations = tripleStoreImplementations;
        this.networkComparison = networkComparison;
        this.onlyDiagnostics = false;
        this.strictTopologyTest = false;
    }

    public void setOnlyDiagnostics(boolean onlyDiagnostics) {
        this.onlyDiagnostics = onlyDiagnostics;
    }

    public void setStrictTopologyTest(boolean strictTopologyTest) {
        this.strictTopologyTest = strictTopologyTest;
    }

    public void testConversion(Network expected, TestGridModel gm) {
        testConversion(expected, gm, this.networkComparison);
    }

    public void testConversion(Network expected, TestGridModel gm, ComparisonConfig config) {
        if (!gm.exists()) {
            LOG.error("Test grid model does not exist {}", gm.name());
            return;
        }
        Properties params = new Properties();
        params.put("storeCgmesModelAsNetworkProperty", "true");
        if (onlyDiagnostics) {
            testConversionOnlyDiagnostics(gm);
        } else {
            for (String impl : tripleStoreImplementations) {
                LOG.info("testConversion. TS implementation {}, grid model {}", impl, gm.name());
                testConversion(expected, gm, config, impl);
            }
        }
    }

    private void testConversion(Network expected, TestGridModel gm, ComparisonConfig config, String impl) {
        Properties params = new Properties();
        params.put("storeCgmesModelAsNetworkProperty", "true");
        CgmesImport i = new CgmesImport();
        ReadOnlyDataSource ds = gm.dataSource();
        params.put("powsyblTripleStore", impl);
        try {
            Network network = i.importData(ds, params);
            if (network.getSubstationCount() == 0) {
                fail("Model is empty");
            }
            CgmesModel cgmes = (CgmesModel) network.getProperties().get(CgmesImport.NETWORK_PS_CGMES_MODEL);
            if (!new TopologyTester(cgmes, network).test(strictTopologyTest)) {
                fail("Topology test failed");
            }
            exportXiidm(gm.name(), impl, expected, network);
            if (expected != null) {
                new Comparison(expected, network, config).compare();
            }
        } catch (Exception x) {
            LOG.error(x.getMessage());
            x.printStackTrace();
            fail();
        }
    }

    private void testConversionOnlyDiagnostics(TestGridModel gm) {
        Properties params = new Properties();
        params.put("storeCgmesModelAsNetworkProperty", "true");
        String impl = TripleStoreFactory.defaultImplementation();
        CgmesImport i = new CgmesImport();
        params.put("powsyblTripleStore", impl);
        ReadOnlyDataSource ds = gm.dataSource();
        try {
            LOG.info("Importer.exists() == {}", i.exists(ds));
            Network n = i.importData(ds, params);
            CgmesModel m = (CgmesModel) n.getProperties().get(CgmesImport.NETWORK_PS_CGMES_MODEL);
            new Conversion(m).diagnose();
        } catch (Exception x) {
            LOG.error(x.getMessage());
            x.printStackTrace();
            fail();
        }
    }

    private void exportXiidm(String name, String impl, Network expected, Network actual) throws IOException {
        String name1 = name.replace('/', '-');
        Path path = Files.createTempDirectory("temp-conversion-" + name1 + "-" + impl);
        XMLExporter xmlExporter = new XMLExporter();
        // Last component of the path is the name for the exported XML
        if (expected != null) {
            xmlExporter.export(expected, null, new FileDataSource(path, "expected"));
        }
        if (actual != null) {
            xmlExporter.export(actual, null, new FileDataSource(path, "actual"));
        }
    }

    private final List<String> tripleStoreImplementations;
    private final ComparisonConfig networkComparison;
    private boolean onlyDiagnostics;
    private boolean strictTopologyTest;

    private static final Logger LOG = LoggerFactory.getLogger(ConversionTester.class);
}
