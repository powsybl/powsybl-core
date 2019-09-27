package com.powsybl.cgmes.conversion.test.update;

import java.io.IOException;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.test.network.compare.Comparison;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.cgmes.model.test.cim14.Cim14SmallCasesCatalog;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.TripleStoreFactory;

public class CgmesUpdaterTester {

    IidmImportFromCgmesTest im;

    @BeforeClass
    public static void setUp() throws IOException {
        smallCasesCatalog = new Cim14SmallCasesCatalog();
        testGridModel14 = smallCasesCatalog.ieee14();
        cgmesConformity1Catalog = new CgmesConformity1Catalog();
        testGridModel16 = cgmesConformity1Catalog.smallBusBranch();
//        TestGridModel testGridModel14 = new TestGridModelResources("case1_EQ", null,
//      new ResourceSet("/cim14/TinyRdfTest/", "case1_EQ.xml"));
    }

    @After
    public void tearDown() throws IOException {
        im.fileSystem.close();
    }

    @Test
    public void mapIidmChangesToCgmes14Test() throws IOException {

        for (String impl : TripleStoreFactory.onlyDefaultImplementation()) {

            im = new IidmImportFromCgmesTest(testGridModel14, impl);

            Network network0 = im.loadNetwork();
            if (im.modelNotEmpty(network0)) {
                network0 = new UpdateNetworkFromCatalog14(network0).updateNetwork();

                Network network1= im.exportAndLoadNetworkUpdated(network0);
                
                ComparisonConfig config = new ComparisonConfig()
                    .checkNetworkId(false)
                    // Expected cases are read using CIM1Importer, that uses floats to read numbers
                    // IIDM and CGMES now stores numbers as doubles
                    .tolerance(2.4e-4);
                Comparison comparison = new Comparison(network0, network1, config);
                comparison.compare();

            }
        }
    }

    // @Test
    public void mapIidmChangesToCgmes16Test() throws IOException {

        for (String impl : TripleStoreFactory.onlyDefaultImplementation()) {

            im = new IidmImportFromCgmesTest(testGridModel16, impl);

            Network network = im.loadNetwork();
            if (im.modelNotEmpty(network)) {
                UpdateNetworkFromCatalog16 up = new UpdateNetworkFromCatalog16(network);
                up.updateNetwork();

                Network networkUpdated = im.exportAndLoadNetworkUpdated(network);

            }
        }
    }

    private static TestGridModel testGridModel14;
    private static TestGridModel testGridModel16;
    private static Cim14SmallCasesCatalog smallCasesCatalog;
    private static CgmesConformity1Catalog cgmesConformity1Catalog;
}
