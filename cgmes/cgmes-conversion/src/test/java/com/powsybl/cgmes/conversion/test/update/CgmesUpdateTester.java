package com.powsybl.cgmes.conversion.test.update;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesExport.Operations;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Profiling;
import com.powsybl.cgmes.conversion.test.network.compare.Comparison;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.cgmes.model.test.cim14.Cim14SmallCasesCatalog;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletion;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletionParameters;
import com.powsybl.triplestore.api.TripleStoreFactory;

public class CgmesUpdateTester {

    @Before
    public void setUp() throws IOException {
        testGridModel14 = Cim14SmallCasesCatalog.ieee14();
        testGridModel16 = CgmesConformity1Catalog.smallBusBranch();
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        cgmesImport = new CgmesImport(new InMemoryPlatformConfig(fileSystem));

//        TestGridModel testGridModel14 = new TestGridModelResources("case1_EQ", null,
//      new ResourceSet("/cim14/TinyRdfTest/", "case1_EQ.xml"));
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void updateCgmes14Test() throws IOException {

        for (String impl : TripleStoreFactory.onlyDefaultImplementation()) {

            ReadOnlyDataSource ds = testGridModel14.dataSource();
            Network network0 = cgmesImport.importData(ds, importParameters(impl));

            if (modelNotEmpty(network0)) {
                UpdateNetworkFromCatalog14.updateNetwork(network0);

                runLoadFlow(network0);

                DataSource tmp = tmpDataSource(impl);
                CgmesExport e = new CgmesExport();
                e.export(network0, new Properties(), tmp);

                // import new network to compare
                Network network1 = cgmesImport.importData(tmp, importParameters(impl));
                runLoadFlow(network1);

                compare(network0, network1);
            }
        }
    }

    @Test
    public void updateCgmes16Test() throws IOException {

        for (String impl : TripleStoreFactory.onlyDefaultImplementation()) {

            ReadOnlyDataSource ds = testGridModel16.dataSource();
            Network network0 = cgmesImport.importData(ds, importParameters(impl));

            if (modelNotEmpty(network0)) {
                UpdateNetworkFromCatalog16.updateNetwork(network0);
                int changesBeforeLoadFlow = UpdateNetworkFromCatalog16.changes.size();

                runLoadFlow(network0);

                DataSource tmp = tmpDataSource(impl);
                CgmesExport e = new CgmesExport();
                e.export(network0, new Properties(), tmp);

                // import new network to compare
                Network network1 = cgmesImport.importData(tmp, importParameters(impl));
                runLoadFlow(network1);

                compare(network0, network1);
            }
        }
    }

    @Test
    public void updateLoadsGeneratorsTest() throws IOException {

        for (String impl : TripleStoreFactory.onlyDefaultImplementation()) {
            profiling = new Profiling();
            profiling.start();

//            ReadOnlyDataSource ds = testGridModel16.dataSource();
            ReadOnlyDataSource ds = new ResourceDataSource("20190911_1130_fo3_me0",
                new ResourceSet("/20190911_1130_fo3_me0/", "20190911T0930Z_1D_CGES_EQ_000.xml",
                    "20190911T0930Z_1D_CGES_SSH_000.xml",
                    "20190911T0930Z_1D_CGES_SV_000.xml",
                    "20190911T0930Z_1D_CGES_TP_000.xml"),
                new ResourceSet("/cgmes-boundaries/", "20190812T0000Z__ENTSOE_EQBD_001.xml",
                    "20190812T0000Z__ENTSOE_TPBD_001.xml"));
            Network network0 = cgmesImport.importData(ds, importParameters(impl));
            profiling.end(String.valueOf(Operations.LOAD_CGMES_TO_IIDM));

            if (modelNotEmpty(network0)) {
                UpdateLoadsGenerators.updateNetwork(network0);
                int changesBeforeLoadFlow = UpdateLoadsGenerators.changes.size();

                profiling.start();
                runLoadFlow(network0);
                profiling.end(String.valueOf(Operations.RUN_LOAD_FLOW));

                DataSource tmp = tmpDataSource(impl);
                CgmesExport e = new CgmesExport();
                e.export(network0, new Properties(), tmp);

                // import new network to compare
                Network network1 = cgmesImport.importData(tmp, importParameters(impl));
                runLoadFlow(network1);

                compare(network0, network1);
                getProfilingForAllSteps(impl, changesBeforeLoadFlow, e);
            }
        }
    }

    private void compare(Network network0, Network network1) {
        ComparisonConfig config = new ComparisonConfig().checkNetworkId(false).tolerance(2.4e-4);
        Comparison comparison = new Comparison(network0, network1, config);
        comparison.compare();
    }

    private void getProfilingForAllSteps(String impl, int changesBeforeLoadFlow, CgmesExport e) {
        LOG.info("Profiling for {} ", impl);
        LOG.info("# of changes before LoadFlow run: " + changesBeforeLoadFlow);
        profiling.report();
        e.profilingReport();
    }

    private void runLoadFlow(Network network) {

        LoadFlowResultsCompletionParameters parameters = new LoadFlowResultsCompletionParameters();
        LoadFlowParameters lfParameters = new LoadFlowParameters();
        LoadFlowResultsCompletion lfResultCompletion = new LoadFlowResultsCompletion(parameters, lfParameters);
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);

        LoadFlow.Runner loadFlow = LoadFlow.find(null, ImmutableList.of(new LoadFlowProviderMock()), platformConfig);
        LoadFlowResult result = loadFlow.run(network, Mockito.mock(ComputationManager.class), new LoadFlowParameters());
        assertNotNull(result);
        lfResultCompletion.run(network, Mockito.mock(ComputationManager.class));
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
//        Path exportFolder = fileSystem.getPath("impl-" + impl);
        Path exportFolder = Paths.get(".\\tmp\\", impl);
        if (Files.exists(exportFolder)) {
            FileUtils.cleanDirectory(exportFolder.toFile());
        }
        Files.createDirectories(exportFolder);
        DataSource tmpDataSource = new FileDataSource(exportFolder, "");
        return tmpDataSource;
    }

    private FileSystem fileSystem;

    private static TestGridModel testGridModel14;
    private static TestGridModel testGridModel16;
    private static CgmesImport cgmesImport;

    private Profiling profiling;
    private static final Logger LOG = LoggerFactory.getLogger(CgmesUpdateTester.class);
}
