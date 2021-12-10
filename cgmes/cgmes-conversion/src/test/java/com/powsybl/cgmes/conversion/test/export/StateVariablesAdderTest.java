/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.conversion.test.network.compare.Comparison;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.cgmes.conversion.test.update.NetworkChanges;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.triplestore.api.PropertyBags;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 */
public class StateVariablesAdderTest {

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        cgmesImport = new CgmesImport(new InMemoryPlatformConfig(fileSystem));
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void conformityMicroGridBaseCase() throws IOException {
        importExportTest(CgmesConformity1Catalog.microGridBaseCaseNL().dataSource());
    }

    private void importExportTest(ReadOnlyDataSource ds) throws IOException {
        Network network0 = cgmesImport.importData(ds, NetworkFactory.findDefault(), importParameters("false"));
        CgmesModelExtension ext0 = network0.getExtension(CgmesModelExtension.class);
        if (ext0 == null) {
            throw new CgmesModelException("No extension for CGMES model found in Network");
        }
        CgmesModel cgmes0 = ext0.getCgmesModel();

        PropertyBags topologicalIslands0 = cgmes0.topologicalIslands();
        PropertyBags fullModel0 = cgmes0.fullModel(CgmesSubset.STATE_VARIABLES.getProfile());
        NetworkChanges.modifyStateVariables(network0);

        // Export modified network to new CGMES
        DataSource tmp = ExportAlternativesTest.tmpDataSource(fileSystem, "export-modified");
        CgmesExport e = new CgmesExport();
        e.export(network0, new Properties(), tmp);

        // Recreate new network with modified State Variables
        Network network1 = cgmesImport.importData(tmp, NetworkFactory.findDefault(), importParameters("false"));
        CgmesModelExtension ext1 = network1.getExtension(CgmesModelExtension.class);
        if (ext1 == null) {
            throw new CgmesModelException("No extension for CGMES model found in Network");
        }
        CgmesModel cgmes1 = ext1.getCgmesModel();
        PropertyBags topologicalIslands1 = cgmes1.topologicalIslands();
        PropertyBags fullModel1 = cgmes1.fullModel(CgmesSubset.STATE_VARIABLES.getProfile());

        // Compare
        assertEquals(topologicalIslands0, topologicalIslands1);
        assertEquals(fullModel0, fullModel1);
        compareVoltages(network0, network1);
    }

    private Properties importParameters(String convertBoundary) {
        Properties importParameters = new Properties();
        importParameters.put(CgmesImport.CONVERT_BOUNDARY, convertBoundary);
        return importParameters;
    }

    private void compareVoltages(Network network0, Network network1) {
        new Comparison(network0, network1, new ComparisonConfig()).compareBuses();
    }

    private FileSystem fileSystem;
    private CgmesImport cgmesImport;
}
