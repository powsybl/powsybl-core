package com.powsybl.cgmes.update.test;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesOnDataSource;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.cgmes.model.test.TestGridModelResources;
import com.powsybl.cgmes.model.test.cim14.Cim14SmallCasesCatalog;
import com.powsybl.commons.datasource.ResourceSet;
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
//        TestGridModel testGridModel = new TestGridModelResources(
//            "not_all_tap_changers_have_control",
//            null,
//            new ResourceSet("/sample_not_all_tap_changers_have_control", "MicroGridTestConfiguration_BC_BE_EQ_V2.xml",
//                                                                         "MicroGridTestConfiguration_BC_BE_SSH_V2.xml"));
        // TestGridModel testGridModel = smallCasesCatalog.small1();
        TestGridModel testGridModel = smallCasesCatalog.ieee14();
        iidmTestImportFromCgmes = new IidmTestImportFromCgmes(testGridModel);
        cimNamespace = new CgmesOnDataSource(testGridModel.dataSource()).cimNamespace();
    }

    //@Test
    public void changeTestIidmModelTest() throws IOException {
        Network network = iidmTestImportFromCgmes.importTestModelFromCgmes();
        if (modelNotEmpty(network)) {
            ChangeTestIidmModel changeTestIidmModel = new ChangeTestIidmModel(network);
            changeTestIidmModel.updateImportedTestModel();
        }
    }

    @Test
    public void mapIidmChangesToCgmesTest() throws IOException {
        Network network = iidmTestImportFromCgmes.importTestModelFromCgmes();
        if (modelNotEmpty(network)) {
            ChangeTestIidmModel changeTestIidmModel = new ChangeTestIidmModel(network);
            changeTestIidmModel.updateImportedTestModel();
            CgmesModel cgmes = changeTestIidmModel.updateTester();
        }
    }

    public static String cimNamespace;
    private static IidmTestImportFromCgmes iidmTestImportFromCgmes;
    private static Cim14SmallCasesCatalog smallCasesCatalog = new Cim14SmallCasesCatalog();
}
