package com.powsybl.cgmes.update.test;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesOnDataSource;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.iidm.network.Network;

public class CgmesUpdaterTester16 {

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
        TestGridModel testGridModel = cgmesConformity1Catalog.smallBusBranch();
        iidmTestImportFromCgmes = new IidmTestImportFromCgmes(testGridModel);
//        cimNamespace = new CgmesOnDataSource(testGridModel.dataSource()).cimNamespace();
    }

    // @Test
    public void changeTestIidmModelTest() throws IOException {
        Network network = iidmTestImportFromCgmes.importTestModelFromCgmes();
        if (modelNotEmpty(network)) {
            ChangeTestIidmModel16 changeTestIidmModel = new ChangeTestIidmModel16(network);
            changeTestIidmModel.updateImportedTestModel();
        }
    }

    @Test
    public void mapIidmChangesToCgmesTest() throws IOException {
        Network network = iidmTestImportFromCgmes.importTestModelFromCgmes();
        if (modelNotEmpty(network)) {
            ChangeTestIidmModel16 changeTestIidmModel = new ChangeTestIidmModel16(network);
            changeTestIidmModel.updateImportedTestModel();
            CgmesModel cgmes = changeTestIidmModel.updateTester();
        }
    }

//    public static String cimNamespace;
    private static IidmTestImportFromCgmes iidmTestImportFromCgmes;
    private static CgmesConformity1Catalog cgmesConformity1Catalog = new CgmesConformity1Catalog();
}
