package com.powsybl.cgmes.update.test;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.cgmes.model.test.cim14.Cim14SmallCasesCatalog;
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
        // TestGridModel testGridModel = smallCasesCatalog.small1();
        TestGridModel testGridModel = smallCasesCatalog.ieee14();
        iidmTestImportFromCgmes = new IidmTestImportFromCgmes(testGridModel);
    }

    // @Test
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

    private static IidmTestImportFromCgmes iidmTestImportFromCgmes;
    private static Cim14SmallCasesCatalog smallCasesCatalog = new Cim14SmallCasesCatalog();
}
