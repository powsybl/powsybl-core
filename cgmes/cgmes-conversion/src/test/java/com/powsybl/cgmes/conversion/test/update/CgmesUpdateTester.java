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
import com.powsybl.cgmes.conversion.CgmesModelExtension;
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
import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
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

    //@Test
    public void updateCgmes14Test() throws IOException {

        for (String impl : TripleStoreFactory.onlyDefaultImplementation()) {

            ReadOnlyDataSource ds = testGridModel14.dataSource();
            Network network0 = cgmesImport.importData(ds, importParameters(impl));

            if (!isEmpty(network0)) {
                UpdateNetworkFromCatalog14.updateNetwork(network0);

                runLoadFlowResultsCompletion(network0);

                DataSource tmp = tmpDataSource(impl);
                CgmesExport e = new CgmesExport();
                e.export(network0, new Properties(), tmp);

                // import new network to compare
                Network network1 = cgmesImport.importData(tmp, importParameters(impl));
                runLoadFlowResultsCompletion(network1);

                compare(network0, network1);
            } else {
                fail("Network is empty");
            }
        }
    }

    //@Test
    public void updateCgmes16Test() throws IOException {

        for (String impl : TripleStoreFactory.onlyDefaultImplementation()) {

            ReadOnlyDataSource ds = testGridModel16.dataSource();
            Network network0 = cgmesImport.importData(ds, importParameters(impl));

            if (!isEmpty(network0)) {
                UpdateNetworkFromCatalog16.updateNetwork(network0);
                int changesBeforeLoadFlow = UpdateNetworkFromCatalog16.changes.size();

                runLoadFlowResultsCompletion(network0);

                DataSource tmp = tmpDataSource(impl);
                CgmesExport e = new CgmesExport();
                e.export(network0, new Properties(), tmp);

                // import new network to compare
                Network network1 = cgmesImport.importData(tmp, importParameters(impl));
                runLoadFlowResultsCompletion(network1);

                compare(network0, network1);
            } else {
                fail("Network is empty");
            }
        }
    }

    @Test
    public void testPerformanceSmallGrid() throws IOException {
        boolean invalidateFlows = true;
        testPerformance(testGridModel16.dataSource(), invalidateFlows);
    }

    //@Test
    public void testPerformanceCges() throws IOException {
        boolean invalidateFlows = false;
        testPerformance(new ResourceDataSource("20190911_1130_fo3_me0",
            new ResourceSet("/20190911_1130_fo3_me0/", "20190911T0930Z_1D_CGES_EQ_000.xml",
                "20190911T0930Z_1D_CGES_SSH_000.xml",
                "20190911T0930Z_1D_CGES_SV_000.xml",
                "20190911T0930Z_1D_CGES_TP_000.xml"),
            new ResourceSet("/cgmes-boundaries/", "20190812T0000Z__ENTSOE_EQBD_001.xml",
                "20190812T0000Z__ENTSOE_TPBD_001.xml")),
            invalidateFlows);
    }

    //@Test
    public void testPerformanceRte() throws IOException {
        boolean invalidateFlows = false;
        testPerformance(new ResourceDataSource("20190911_1130_FO3_XX4",
            new ResourceSet("/20190911_1130_FO3_XX4/",
                "20190911T0930Z_1D_RTEFRANCE_EQ_004.xml",
                "20190911T0930Z_1D_RTEFRANCE_SSH_004.xml",
                "20190911T0930Z_1D_RTEFRANCE_SV_004.xml",
                "20190911T0930Z_1D_RTEFRANCE_TP_004.xml"),
            new ResourceSet("/cgmes-boundaries/",
                "20190812T0000Z__ENTSOE_EQBD_001.xml",
                "20190812T0000Z__ENTSOE_TPBD_001.xml")),
            invalidateFlows);
    }

    //@Test
    public void testPerformanceRen() throws IOException {
        boolean invalidateFlows = false;
        testPerformance(new ResourceDataSource("20190821_1130_FO3_PT1",
            new ResourceSet("/20190821_1130_FO3_PT1/",
                "20190821T0930Z_REN_EQ_001.xml",
                "20190821T0930Z_1D_REN_TP_001.xml",
                "20190821T0930Z_1D_REN_SV_001.xml",
                "20190821T0930Z_1D_REN_SSH_001.xml"),
            new ResourceSet("/cgmes-boundaries/",
                "20190812T0000Z__ENTSOE_EQBD_001.xml",
                "20190812T0000Z__ENTSOE_TPBD_001.xml")),
            invalidateFlows);
    }

    private void testPerformance(ReadOnlyDataSource ds, boolean invalidateFlows) throws IOException {
        testPerformance(ds, TripleStoreFactory.defaultImplementation(), invalidateFlows);
    }

    private void testPerformance(ReadOnlyDataSource ds, String impl, boolean invalidateFlows) throws IOException {
        Profiling profiling = new Profiling();

        profiling.start();
        Network network0 = cgmesImport.importData(ds, importParameters(impl));
        profiling.end(Operations.IMPORT_CGMES.name());
        if (isEmpty(network0)) {
            fail("Network is empty");
            return;
        }

        profiling.start();
        network0.getVariantManager().cloneVariant(network0.getVariantManager().getWorkingVariantId(), "1");
        network0.getVariantManager().setWorkingVariant("1");
        profiling.end(Operations.CLONE_VARIANT.name());

        profiling.start();
        UpdateLoadsGenerators.updateNetwork(network0);
        profiling.end(Operations.SCALING.name());
        int numChangesBeforeLoadFlow = network0.getExtension(CgmesModelExtension.class).getCgmesUpdate().changes().size();

        profiling.start();
        runLoadFlowResultsCompletion(network0, invalidateFlows);
        profiling.end(Operations.LOAD_FLOW.name());
        int numChangesAfterLoadFlow = network0.getExtension(CgmesModelExtension.class).getCgmesUpdate().changes().size();
//        System.err.println("changes");
//        for (IidmChange c : network0.getExtension(CgmesModelExtension.class).getCgmesUpdater().changes()) {
//            System.err.println("  " + c.getVariant() + " " + c.getAttribute() + " " + c.getIdentifiableName() + " " + c.getNewValue());
//        }

        DataSource tmp = tmpDataSource(impl);
        CgmesExport e = new CgmesExport(profiling);
        e.export(network0, new Properties(), tmp);

        // import new network to compare
        Network network1 = cgmesImport.importData(tmp, importParameters(impl));
        runLoadFlowResultsCompletion(network1, false);

        compare(network0, network1);
        reportProfiling(ds, network0, impl, numChangesBeforeLoadFlow, numChangesAfterLoadFlow, invalidateFlows, profiling);
    }

    private void compare(Network network0, Network network1) {
        ComparisonConfig config = new ComparisonConfig().checkNetworkId(false).tolerance(2.4e-4);
        Comparison comparison = new Comparison(network0, network1, config);
        comparison.compare();
    }

    private void reportProfiling(
        ReadOnlyDataSource ds,
        Network network,
        String impl,
        int numChangesBeforeLoadFlow,
        int numChangesAfterLoadFlow,
        boolean invalidateFlows,
        Profiling profiling) {
        LOG.info("Profiling summary");
        LOG.info("    test case                   : " + ds.getBaseName());
        LOG.info("    triplestore impl            : " + impl);
        LOG.info("    generators                  : " + network.getGeneratorCount());
        LOG.info("    loads                       : " + network.getLoadCount());
        LOG.info("    changes before LoadFlow run : " + numChangesBeforeLoadFlow);
        LOG.info("    invalidate flows before LF  : " + invalidateFlows);
        LOG.info("    changes after LoadFlow run  : " + numChangesAfterLoadFlow);
        profiling.report();
    }

    private void runLoadFlowResultsCompletion(Network network) {
        runLoadFlowResultsCompletion(network, false);
    }

    private void runLoadFlowResultsCompletion(Network network, boolean invalidateFlows) {
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        LoadFlowParameters lfParameters = new LoadFlowParameters();

        if (invalidateFlows) {
            invalidateFlows(network);
        }

        LoadFlow.Runner loadFlowMock = LoadFlow.find(null, ImmutableList.of(new LoadFlowProviderMock()), platformConfig);
        LoadFlowResult result = loadFlowMock.run(network, Mockito.mock(ComputationManager.class), lfParameters);
        assertNotNull(result);

        LoadFlowResultsCompletionParameters parameters = new LoadFlowResultsCompletionParameters();
        LoadFlowResultsCompletion lfResultsCompletion = new LoadFlowResultsCompletion(parameters, lfParameters);
        lfResultsCompletion.run(network, Mockito.mock(ComputationManager.class));
    }

    private void invalidateFlows(Network n) {
        n.getLineStream().forEach(line -> {
            invalidateFlow(line.getTerminal(Side.ONE));
            invalidateFlow(line.getTerminal(Side.TWO));
        });
        n.getTwoWindingsTransformerStream().forEach(twt -> {
            invalidateFlow(twt.getTerminal(Side.ONE));
            invalidateFlow(twt.getTerminal(Side.TWO));
        });
        n.getShuntCompensatorStream().forEach(sh -> {
            Terminal terminal = sh.getTerminal();
            terminal.setQ(Double.NaN);
        });
        n.getThreeWindingsTransformerStream().forEach(twt -> {
            invalidateFlow(twt.getLeg1().getTerminal());
            invalidateFlow(twt.getLeg2().getTerminal());
            invalidateFlow(twt.getLeg3().getTerminal());
        });
    }

    private void invalidateFlow(Terminal t) {
        t.setP(Double.NaN);
        t.setQ(Double.NaN);
    }

    private boolean isEmpty(Network network) {
        return network.getSubstationCount() == 0;
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

    private static final Logger LOG = LoggerFactory.getLogger(CgmesUpdateTester.class);
}
