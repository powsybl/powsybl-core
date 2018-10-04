package com.powsybl.cgmes.conversion.test;

import java.io.IOException;
import java.nio.file.Files;

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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.CgmesModel;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.test.TestGridModel;
import com.powsybl.cgmes.test.cim14.Cim14SmallCasesCatalog;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.TripleStoreFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class MeasureImportExportTest {
    // TODO We should build tests that check that re-imported exported models
    // are equivalent to the original models

    @BeforeClass
    public static void setUp() {
        catalog = new Cim14SmallCasesCatalog();
    }

    @Test
    public void smallcase1() {
        Path output = Paths.get(System.getProperty("java.io.tmpdir")).resolve("cgmes");
        measureImportExportForTripleStoreImplementations(TripleStoreFactory.allImplementations(), catalog.small1(),
                output);
    }

    private void measureImportExportForTripleStoreImplementations(List<String> implementations, TestGridModel gm,
            Path output) {
        int size = implementations.size();
        long[] startTimes = new long[size];
        long[] endTimes = new long[size];
        Path input = gm.path();
        for (int k = 0; k < size; k++) {
            String impl = implementations.get(k);
            LOG.info("measureImportExport TS implementation {}, model {}", impl, gm.id());
            startTimes[k] = System.currentTimeMillis();

            Path output1 = output.resolve(impl);
            importExport(impl, gm, input, output1);

            endTimes[k] = System.currentTimeMillis();
        }
        for (int k = 0; k < size; k++) {
            String impl = implementations.get(k);
            LOG.info("testImportExport " + impl + " took " + (endTimes[k] - startTimes[k]) + " milliseconds");
        }
    }

    private void importExport(String ts, TestGridModel gm, Path input, Path output) {
        CgmesImport i = new CgmesImport();
        ReadOnlyDataSource importDataSource = DataSourceUtil.createDataSource(input, gm.basename(),
                gm.getCompressionExtension(), null);
        Properties importParameters = new Properties();
        importParameters.put("powsyblTripleStore", ts);
        importParameters.put("storeCgmesModelAsNetworkProperty", "true");
        Network n = i.importData(importDataSource, importParameters);
        Object c = n.getProperties().get("CGMESModel");
        assert c instanceof CgmesModel;
        CgmesModel cgmes = (CgmesModel) c;
        cgmes.dump(l -> LOG.info(l));
        CgmesExport e = new CgmesExport();
        ensureFolder(output);
        DataSource exportDataSource = new FileDataSource(output, "");
        e.export(n, new Properties(), exportDataSource);
    }

    private void ensureFolder(Path p) {
        try {
            Files.createDirectories(p);
        } catch (IOException x) {
            throw new PowsyblException(String.format("Creating directories %s", p), x);
        }
    }

    private static Cim14SmallCasesCatalog catalog;

    private static final Logger LOG = LoggerFactory.getLogger(MeasureImportExportTest.class);
}
