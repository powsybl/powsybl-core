package com.powsybl.cgmes.conversion.test.update;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.cgmes.model.test.cim14.Cim14SmallCasesCatalog;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.iidm.network.Network;

public class ExportCgmesUpdateTester {

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
//        TestGridModel testGridModel14 = tinyCasesCatalog.TinyTest14();
        TestGridModel testGridModel16 = cgmesConformity1Catalog.smallBusBranch();
        iidmTestImportFromCgmes14 = new IidmTestImportFromCgmes(testGridModel14);
        iidmTestImportFromCgmes16 = new IidmTestImportFromCgmes(testGridModel16);
    }

    @Test
    public void exportIidmChangesToCgmesTest14() throws IOException {
        Network network = iidmTestImportFromCgmes14.importTestModelFromCgmes();
        if (modelNotEmpty(network)) {
            network.getGenerator("_GEN____3_SM").setRatedS(100);
            CgmesExport cgmesExport = new CgmesExport();
            Path exportFolder = Paths.get(".\\tmp\\");
            Files.createDirectories(exportFolder);
            DataSource exportDataSource = new FileDataSource(exportFolder, "");
            cgmesExport.export(network, new Properties(), exportDataSource);
        }
    }

    //@Test
    public void exportIidmChangesToCgmesTest16() throws IOException {
        Network network = iidmTestImportFromCgmes16.importTestModelFromCgmes();
        if (modelNotEmpty(network)) {
            ChangeTestIidmModel16 changeTestIidmModel = new ChangeTestIidmModel16(network);
            changeTestIidmModel.updateImportedTestModel();
            changeTestIidmModel.updateTester();
            CgmesExport e = new CgmesExport();
            Path exportFolder = Paths.get(".\\tmp\\");
            Files.createDirectories(exportFolder);
            DataSource exportDataSource = new FileDataSource(exportFolder, "");
            e.export(network, new Properties(), exportDataSource);
        }
    }

    private static IidmTestImportFromCgmes iidmTestImportFromCgmes14;
    private static IidmTestImportFromCgmes iidmTestImportFromCgmes16;
    private static Cim14SmallCasesCatalog smallCasesCatalog = new Cim14SmallCasesCatalog();
    private static CgmesConformity1Catalog cgmesConformity1Catalog = new CgmesConformity1Catalog();
    private static TinyCasesCatalog tinyCasesCatalog = new TinyCasesCatalog();
    private static final Logger LOG = LoggerFactory.getLogger(ExportCgmesUpdateTester.class);
}
