package com.powsybl.cgmes.conversion.test.update;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;
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

    @BeforeClass
    public static void setUp() throws IOException {
        smallCasesCatalog = new Cim14SmallCasesCatalog();
        testGridModel14 = smallCasesCatalog.ieee14();
        cgmesConformity1Catalog = new CgmesConformity1Catalog();
        testGridModel16 = cgmesConformity1Catalog.microGridBaseCaseBE();
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void exportIidmChangesToCgmes14Test() throws IOException {
        for (String impl : TripleStoreFactory.allImplementations()) {

            CgmesImport i = new CgmesImport(new InMemoryPlatformConfig(fileSystem));
            ReadOnlyDataSource ds = testGridModel14.dataSource();
            Network network = i.importData(ds, importParameters(impl));

            if (modelNotEmpty(network)) {
                double ratedU1_before = network.getTwoWindingsTransformer("_BUS____4-BUS____7-1_PT").getRatedU1();
                network.getTwoWindingsTransformer("_BUS____4-BUS____7-1_PT").setRatedU1(70.0);

                CgmesExport e = new CgmesExport();
                DataSource tmp = tmpDataSource(impl);
                e.export(network, new Properties(), tmp);

                Network network1 = i.importData(tmp, importParameters(impl));

                double ratedU1 = network1.getTwoWindingsTransformer("_BUS____4-BUS____7-1_PT").getRatedU1();
                assertTrue(ratedU1_before != ratedU1);
                assertTrue(ratedU1 == 70.0);
            }
        }
    }

    @Test
    public void exportIidmChangesToCgmes16Test() throws IOException {
        for (String impl : TripleStoreFactory.allImplementations()) {

            CgmesImport i = new CgmesImport(new InMemoryPlatformConfig(fileSystem));
            ReadOnlyDataSource ds = testGridModel16.dataSource();
            Network network = i.importData(ds, importParameters(impl));

            if (modelNotEmpty(network)) {
                double ratedU1_before = network.getTwoWindingsTransformer("_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0")
                    .getRatedU1();
                network.getTwoWindingsTransformer("_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").setRatedU1(401.0);

                CgmesExport e = new CgmesExport();
                DataSource tmp = tmpDataSource(impl);
                e.export(network, new Properties(), tmp);

                Network network1 = i.importData(tmp, importParameters(impl));

                double ratedU1 = network1.getTwoWindingsTransformer("_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0")
                    .getRatedU1();
                assertTrue(ratedU1_before != ratedU1);
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
//        Path exportFolder = Paths.get(".\\tmp\\", impl);
//        FileUtils.cleanDirectory(exportFolder.toFile());
        Files.createDirectories(exportFolder);
        DataSource tmpDataSource = new FileDataSource(exportFolder, "");
        return tmpDataSource;
    }

    private FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());
    private static TestGridModel testGridModel14;
    private static TestGridModel testGridModel16;
    private static Cim14SmallCasesCatalog smallCasesCatalog;
    private static CgmesConformity1Catalog cgmesConformity1Catalog;
}
