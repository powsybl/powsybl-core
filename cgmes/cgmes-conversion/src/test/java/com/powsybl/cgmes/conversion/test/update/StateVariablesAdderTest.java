/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.update;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelException;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStoreFactory;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 */
public class StateVariablesAdderTest {

    @Before
    public void setUp() throws IOException {
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

    void importExportTest(ReadOnlyDataSource ds) throws IOException {
        importExportTest(ds, TripleStoreFactory.defaultImplementation());
    }

    private void importExportTest(ReadOnlyDataSource ds, String impl) throws IOException {
        Network network0 = cgmesImport.importData(ds, NetworkFactory.findDefault(), importParameters("false"));
        CgmesModelExtension ext0 = network0.getExtension(CgmesModelExtension.class);
        if (ext0 == null) {
            throw new CgmesModelException("No extension for CGMES model found in Network");
        }
        CgmesModel cgmes0 = ext0.getCgmesModel();

        PropertyBags topologicalIslands0 = cgmes0.topologicalIslands();
        PropertyBags fullModel0 = cgmes0.fullModel(CgmesSubset.STATE_VARIABLES.getProfile());
        NetworkChanges.modifyStateVariables(network0);

        // Export modified network to new cgmes
        DataSource tmp = tmpDataSource(impl);
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
        assertTrue(topologicalIslands0.equals(topologicalIslands1));
        assertTrue(fullModel0.equals(fullModel1));
        assertTrue(networkVoltagesChangesInCgmes(network0, cgmes1));
    }

    private Properties importParameters(String convertBoundary) {
        Properties importParameters = new Properties();
        importParameters.put(CgmesImport.CONVERT_BOUNDARY, convertBoundary);
        return importParameters;
    }

    private boolean networkVoltagesChangesInCgmes(Network network0, CgmesModel cgmes1) {
        for (PropertyBag tn : cgmes1.topologicalNodes()) {
            Bus b = network0.getBusBreakerView().getBus(tn.getId(CgmesNames.TOPOLOGICAL_NODE));
            if (b != null) {
                String.valueOf(b.getV()).equals(tn.getId(CgmesNames.VOLTAGE));
                String.valueOf(b.getAngle()).equals(tn.getId(CgmesNames.ANGLE));
                return true;
            }
        }
        return false;
    }

    private DataSource tmpDataSource(String impl) throws IOException {
        Path exportFolder = fileSystem.getPath("impl-" + impl);
        if (Files.exists(exportFolder)) {
            FileUtils.cleanDirectory(exportFolder.toFile());
        }
        Files.createDirectories(exportFolder);
        DataSource tmpDataSource = new FileDataSource(exportFolder, "");
        return tmpDataSource;
    }

    private FileSystem fileSystem;
    private CgmesImport cgmesImport;
}
