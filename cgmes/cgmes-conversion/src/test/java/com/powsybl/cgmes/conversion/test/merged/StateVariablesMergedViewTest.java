/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.merged;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.test.network.compare.Comparison;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.cgmes.conversion.test.update.NetworkChanges;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.iidm.mergingview.MergingView;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 */
public class StateVariablesMergedViewTest {

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        cgmesImport = new CgmesImport(new InMemoryPlatformConfig(fileSystem));
        config = new ComparisonConfig().checkNetworkId(false).tolerance(1e-4);
        network1 = cgmesImport.importData(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(),
            NetworkFactory.findDefault(), new Properties());
        network2 = cgmesImport.importData(CgmesConformity1Catalog.microGridBaseCaseNL().dataSource(),
            NetworkFactory.findDefault(), new Properties());
        networkAssembled = cgmesImport.importData(
            CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(), NetworkFactory.findDefault(),
            new Properties());
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void conformityMicroGridBaseCaseMergeCompare() throws IOException {
        MergingView networkMerged = MergingView.create("mergingView", "iidm");
        networkMerged.merge(network1, network2);

        // Run compare test on ENTSOE assembled vs IIDM merged networks
        new Comparison(networkAssembled, networkMerged, config).compare();
    }

    @Test
    public void conformityMicroGridBaseCaseMergeExportSV() throws IOException {
        MergingView networkMerged = MergingView.create("mergingView", "iidm");
        networkMerged.merge(network1, network2);

        // Run changes State Variables changes on the merged Network
        NetworkChanges.modifyStateVariables(networkMerged);

        // Export SV files from original IGMs
        DataSource tmp = tmpDataSource("mergedNetworkTest");
        CgmesExport e = new CgmesExport();
        e.export(network1, new Properties(), tmp, CgmesSubset.STATE_VARIABLES);
        e.export(network2, new Properties(), tmp, CgmesSubset.STATE_VARIABLES);

        // Compare values in SV files vs mergedNetwork
    }

    private DataSource tmpDataSource(String tmpName) throws IOException {
        Path exportFolder = fileSystem.getPath(tmpName);
        if (Files.exists(exportFolder)) {
            FileUtils.cleanDirectory(exportFolder.toFile());
        }
        Files.createDirectories(exportFolder);
        DataSource tmpDataSource = new FileDataSource(exportFolder, "");
        return tmpDataSource;
    }

    private FileSystem fileSystem;
    private CgmesImport cgmesImport;
    private ComparisonConfig config;
    private Network network1;
    private Network network2;
    private Network networkAssembled;
}

