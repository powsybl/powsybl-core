package com.powsybl.cgmes.update.test;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.cgmes.model.test.cim14.Cim14SmallCasesCatalog;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.iidm.network.Network;

public class CgmesUpdaterTester {

    private static boolean modelNotEmpty(Network network) {
        if (network.getSubstationCount() == 0) {
            fail("Model is empty");
            return false;
        } else {
            return true;
        }
    }

    @BeforeClass
    public static void setUp() throws IOException {
        // TestGridModel testGridModel14 = smallCasesCatalog.small1();
        TestGridModel testGridModel14 = smallCasesCatalog.ieee14();
        TestGridModel testGridModel16 = cgmesConformity1Catalog.smallBusBranch();
        iidmTestImportFromCgmes14 = new IidmTestImportFromCgmes(testGridModel14);
        iidmTestImportFromCgmes16 = new IidmTestImportFromCgmes(testGridModel16);
//        cimNamespace = new CgmesOnDataSource(testGridModel.dataSource()).cimNamespace();
    }

    // @Test
    public void changeTestIidmModelTest() throws IOException {
        Network network = iidmTestImportFromCgmes14.importTestModelFromCgmes();
        if (modelNotEmpty(network)) {
            ChangeTestIidmModel14 changeTestIidmModel = new ChangeTestIidmModel14(network);
            changeTestIidmModel.updateImportedTestModel();
        }
    }

    //@Test
    public void mapIidmChangesToCgmesTest14() throws IOException {
        Network network = iidmTestImportFromCgmes14.importTestModelFromCgmes();
        if (modelNotEmpty(network)) {
            ChangeTestIidmModel14 changeTestIidmModel = new ChangeTestIidmModel14(network);
            changeTestIidmModel.updateImportedTestModel();
            CgmesModel cgmes = changeTestIidmModel.updateTester();
        }
    }

    @Test
    public void mapIidmChangesToCgmesTest16() throws IOException {
        Network network = iidmTestImportFromCgmes16.importTestModelFromCgmes();
        if (modelNotEmpty(network)) {
            ChangeTestIidmModel16 changeTestIidmModel = new ChangeTestIidmModel16(network);
            changeTestIidmModel.updateImportedTestModel();
            CgmesModel cgmes = changeTestIidmModel.updateTester();
        }
    }

    private static IidmTestImportFromCgmes iidmTestImportFromCgmes14;
    private static IidmTestImportFromCgmes iidmTestImportFromCgmes16;
    private static Cim14SmallCasesCatalog smallCasesCatalog = new Cim14SmallCasesCatalog();
    private static CgmesConformity1Catalog cgmesConformity1Catalog = new CgmesConformity1Catalog();
}
