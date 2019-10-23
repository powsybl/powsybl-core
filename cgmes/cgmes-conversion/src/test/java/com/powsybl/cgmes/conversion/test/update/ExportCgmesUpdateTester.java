package com.powsybl.cgmes.conversion.test.update;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.cgmes.model.test.cim14.Cim14SmallCasesCatalog;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.TripleStoreFactory;

public class ExportCgmesUpdateTester {

    @Before
    public void setUp() throws IOException {
        testGridModel14 = Cim14SmallCasesCatalog.ieee14();
        testGridModel16 = CgmesConformity1Catalog.microGridBaseCaseBE();
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        cgmesImport = new CgmesImport(new InMemoryPlatformConfig(fileSystem));
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void exportIidmChangesToCgmes14Test() throws IOException {
        for (String impl : TripleStoreFactory.allImplementations()) {

            ReadOnlyDataSource ds = testGridModel14.dataSource();
            Network network = cgmesImport.importData(ds, importParameters(impl));

            if (modelNotEmpty(network)) {
                double ratedU1Before = network.getTwoWindingsTransformer("_BUS____4-BUS____7-1_PT").getRatedU1();
                network.getTwoWindingsTransformer("_BUS____4-BUS____7-1_PT").setRatedU1(70.0);

                CgmesExport e = new CgmesExport();
                DataSource tmp = tmpDataSource(impl);
                e.export(network, new Properties(), tmp);

                Network network1 = cgmesImport.importData(tmp, importParameters(impl));

                double ratedU1 = network1.getTwoWindingsTransformer("_BUS____4-BUS____7-1_PT").getRatedU1();
                assertTrue(ratedU1Before != ratedU1);
                assertTrue(ratedU1 == 70.0);
            }
        }
    }

    @Test
    public void exportIidmChangesToCgmes16Test() throws IOException {
        for (String impl : TripleStoreFactory.allImplementations()) {

            ReadOnlyDataSource ds = testGridModel16.dataSource();
            Network network = cgmesImport.importData(ds, importParameters(impl));

            if (modelNotEmpty(network)) {
                double ratedU1Before = network.getTwoWindingsTransformer("_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0")
                    .getRatedU1();
                network.getTwoWindingsTransformer("_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").setRatedU1(401.0);

                CgmesExport e = new CgmesExport();
                DataSource tmp = tmpDataSource(impl);
                e.export(network, new Properties(), tmp);

                Network network1 = cgmesImport.importData(tmp, importParameters(impl));

                double ratedU1 = network1.getTwoWindingsTransformer("_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0")
                    .getRatedU1();
                assertTrue(ratedU1Before != ratedU1);
                assertTrue(ratedU1 == 401.0);
            }
        }
    }

    private boolean modelNotEmpty(Network network) {
        if (network.getSubstationCount() == 0) {
            fail("Model is empty");
            return false;
        } else {
            return true;
        }
    }

    private Properties importParameters(String impl) {
        Properties importParameters = new Properties();
        importParameters.put("powsyblTripleStore", impl);
        importParameters.put("storeCgmesModelAsNetworkExtension", "true");
        return importParameters;
    }

    private DataSource tmpDataSource(String impl) throws IOException {
        Path exportFolder = fileSystem.getPath("impl-" + impl);
        Files.createDirectories(exportFolder);
        DataSource tmpDataSource = new FileDataSource(exportFolder, "");
        return tmpDataSource;
    }

    private static FileSystem fileSystem;
    private static TestGridModel testGridModel14;
    private static TestGridModel testGridModel16;
    private static CgmesImport cgmesImport;
}
