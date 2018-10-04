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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.CgmesModel;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.test.network.compare.Comparison;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.cgmes.test.TestGridModel;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.XMLExporter;
import com.powsybl.triplestore.TripleStoreFactory;

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

    public void testConversion(Network expected, TestGridModel gm) {
        testConversion(expected, gm, this.networkComparison);
    }

    public void testConversion(Network expected, TestGridModel gm, ComparisonConfig config) {
        Path path = gm.path();
        if (!Files.exists(path)) {
            LOG.error("Input path does not exist {}", path.toString());
            return;
        }
        Properties params = new Properties();
        params.put("storeCgmesModelAsNetworkProperty", "true");
        if (onlyDiagnostics) {
            String impl = TripleStoreFactory.defaultImplementation();
            CgmesImport i = new CgmesImport();
            params.put("powsyblTripleStore", impl);
            ReadOnlyDataSource ds = DataSourceUtil.createDataSource(gm.path(), gm.basename(),
                    gm.getCompressionExtension(), null);
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
        } else {
            for (String impl : tripleStoreImplementations) {
                LOG.info("testConversion. TS implementation {}, grid model {}", impl, gm.id());
                CgmesImport i = new CgmesImport();
                ReadOnlyDataSource ds = DataSourceUtil.createDataSource(gm.path(), gm.basename(),
                        gm.getCompressionExtension(), null);
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
                    exportXiidm(expected, network, path, impl);
                    if (expected != null) {
                        new Comparison(expected, network, config).compare();
                    }
                } catch (Exception x) {
                    LOG.error(x.getMessage());
                    x.printStackTrace();
                    fail();
                }
            }
        }
    }

    private void exportXiidm(Network expected, Network actual, Path path, String impl) {
        XMLExporter xmlExporter = new XMLExporter();
        // Last component of the path is the name for the exported XML
        String name = path.getName(path.getNameCount() - 1).toString();
        String filename = "temp-conversion-" + name + "-" + impl;
        if (expected != null) {
            xmlExporter.export(expected, null, new FileDataSource(path, filename + "-expected"));
        }
        if (actual != null) {
            xmlExporter.export(actual, null, new FileDataSource(path, filename + "-actual"));
        }
    }

    private final List<String> tripleStoreImplementations;
    private final ComparisonConfig networkComparison;
    private final boolean onlyDiagnostics;
    private final boolean strictTopologyTest;

    private static final Logger LOG = LoggerFactory.getLogger(ConversionTester.class);
}
