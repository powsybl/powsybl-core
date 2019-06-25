package com.powsybl.cgmes.update.test;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.cgmes.model.test.cim14.Cim14SmallCasesCatalog;
import com.powsybl.iidm.network.Network;

public class MapIidmChangesTest {

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
        TestGridModel gm = smallCasesCatalog.small1();
        importToIidm = new IidmImportFromCgmes(gm);
    }

    // @Test
    public void changeIidmModelTest() throws IOException {
        Network network = importToIidm.importTestModelFromCgmes();
        if (modelNotEmpty(network)) {
            ChangeIidmModel changedModel = new ChangeIidmModel(network);
            changedModel.updateImportedModel();
        }
    }

    @Test
    public void mapIidmChangesToCgmesTest() throws IOException {
        Network network = importToIidm.importTestModelFromCgmes();
        if (modelNotEmpty(network)) {
            ChangeIidmModel changedModel = new ChangeIidmModel(network);
            changedModel.updateImportedModel();
            CgmesModel cgmes = changedModel.mapIidmChangesToCgmesTester();
        }
    }

    private static IidmImportFromCgmes importToIidm;
    private static Cim14SmallCasesCatalog smallCasesCatalog = new Cim14SmallCasesCatalog();
}
