package com.powsybl.cgmes.conversion.test.update;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.FileSystem;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.cgmes.model.test.cim14.Cim14SmallCasesCatalog;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.TripleStoreFactory;

public class ExportCgmesUpdateTester {

    IidmImportFromCgmesTest im;
    FileSystem fileSystem;

    @BeforeClass
    public static void setUp() throws IOException {
        smallCasesCatalog = new Cim14SmallCasesCatalog();
        testGridModel14 = smallCasesCatalog.ieee14();
        cgmesConformity1Catalog = new CgmesConformity1Catalog();
        testGridModel16 = cgmesConformity1Catalog.microGridBaseCaseBE();
    }

    @After
    public void tearDown() throws IOException {
        im.fileSystem.close();
    }

    @Test
    public void exportIidmChangesToCgmes14Test() throws IOException {
        for (String impl : TripleStoreFactory.allImplementations()) {

            im = new IidmImportFromCgmesTest(testGridModel14, impl);
            Network network = im.loadNetwork();

            if (im.modelNotEmpty(network)) {
                double ratedU1_before = network.getTwoWindingsTransformer("_BUS____4-BUS____7-1_PT").getRatedU1();
                network.getTwoWindingsTransformer("_BUS____4-BUS____7-1_PT").setRatedU1(70.0);

                Network networkUpdated = im.exportAndLoadNetworkUpdated(network);

                double ratedU1 = networkUpdated.getTwoWindingsTransformer("_BUS____4-BUS____7-1_PT").getRatedU1();
                assertTrue(ratedU1_before != ratedU1);
                assertTrue(ratedU1 == 70.0);
            }
        }
    }

    @Test
    public void exportIidmChangesToCgmes16Test() throws IOException {
        for (String impl : TripleStoreFactory.onlyDefaultImplementation()) {

            im = new IidmImportFromCgmesTest(testGridModel16, impl);
            Network network = im.loadNetwork();

            if (im.modelNotEmpty(network)) {
                double ratedU1_before = network.getTwoWindingsTransformer("_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0")
                    .getRatedU1();
                network.getTwoWindingsTransformer("_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0").setRatedU1(401.0);

                Network networkUpdated = im.exportAndLoadNetworkUpdated(network);

                double ratedU1 = networkUpdated.getTwoWindingsTransformer("_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0")
                    .getRatedU1();
                assertTrue(ratedU1_before != ratedU1);
                assertTrue(ratedU1 == 401.0);
            }
        }
    }

    private static TestGridModel testGridModel14;
    private static TestGridModel testGridModel16;
    private static Cim14SmallCasesCatalog smallCasesCatalog;
    private static CgmesConformity1Catalog cgmesConformity1Catalog;
}
