/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.cgmes.model.test.Cim14SmallCasesCatalog;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.triplestore.api.TripleStoreFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class ImportExportPerformanceTest {
    // TODO We should build tests that check that re-imported exported models
    // are equivalent to the original models

    @Test
    void smallcase1() throws IOException {
        importExport(TripleStoreFactory.onlyDefaultImplementation(), Cim14SmallCasesCatalog.small1());
    }

    private void importExport(List<String> tsImpls, GridModelReference gm) throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            ReadOnlyDataSource ds = gm.dataSource();

            int size = tsImpls.size();
            long[] startTimes = new long[size];
            long[] endTimes = new long[size];
            for (int k = 0; k < size; k++) {
                String impl = tsImpls.get(k);
                LOG.info("importExport performance, TS implementation {}, model {}", impl, gm.name());
                startTimes[k] = System.currentTimeMillis();

                importExport(impl, ds, fs);

                endTimes[k] = System.currentTimeMillis();
            }
            for (int k = 0; k < size; k++) {
                String impl = tsImpls.get(k);
                LOG.info("importExport {} took {} milliseconds", impl, endTimes[k] - startTimes[k]);
            }
        }
    }

    private void importExport(String ts, ReadOnlyDataSource ds, FileSystem fs) throws IOException {
        CgmesImport i = new CgmesImport();
        Properties importParameters = new Properties();
        importParameters.put("powsyblTripleStore", ts);
        importParameters.put("storeCgmesModelAsNetworkExtension", "true");
        importParameters.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network n = i.importData(ds, NetworkFactory.findDefault(), importParameters);
        CgmesModel cgmes = n.getExtension(CgmesModelExtension.class).getCgmesModel();
        cgmes.print(LOG::info);

        CgmesExport e = new CgmesExport();
        Path exportFolder = fs.getPath("impl-" + ts);
        Files.createDirectories(exportFolder);
        DataSource exportDataSource = DataSourceUtil.createDataSource(exportFolder, "", "");
        Properties exportParameters = new Properties();
        exportParameters.put(CgmesExport.CIM_VERSION, "16");
        e.export(n, exportParameters, exportDataSource);
    }

    private static final Logger LOG = LoggerFactory.getLogger(ImportExportPerformanceTest.class);
}
