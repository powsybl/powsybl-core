package com.powsybl.cgmes.conversion.test.update;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletion;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletionParameters;
import com.powsybl.triplestore.api.TripleStoreFactory;

public class CgmesUpdateTest {

    @Before
    public void setUp() throws IOException {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        cgmesImport = new CgmesImport(new InMemoryPlatformConfig(fileSystem));
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void testPerformanceSmallGrid() throws IOException {
        boolean invalidateFlows = true;
        testPerformance(CgmesConformity1Catalog.smallBusBranch().dataSource(), invalidateFlows);
    }

    void testPerformance(ReadOnlyDataSource ds, boolean invalidateFlows) throws IOException {
        testPerformance(ds, TripleStoreFactory.defaultImplementation(), invalidateFlows);
    }

    private void testPerformance(ReadOnlyDataSource ds, String impl, boolean invalidateFlows) throws IOException {
        Profiling profiling = new Profiling();

        cgmesImport.setProfiling(profiling);
        Network network0 = cgmesImport.importData(ds, NetworkFactory.findDefault(), importParameters(impl));
        if (isEmpty(network0)) {
            fail("Network is empty");
            return;
        }
        // For very big networks we will limit the number of changes made in the test
        boolean isBigNetwork = network0.getBusView().getBusStream().count() > 5000;

        profiling.start();
        network0.getVariantManager().cloneVariant(network0.getVariantManager().getWorkingVariantId(), "1");
        network0.getVariantManager().setWorkingVariant("1");
        profiling.end(Operations.CLONE_VARIANT.name());

        profiling.start();
        int numChangesInLoadsAndGenerators = isBigNetwork ? 1000 : Integer.MAX_VALUE;
        NetworkChanges.scaleLoadGenerator(network0, numChangesInLoadsAndGenerators);
        profiling.end(Operations.SCALING.name());
        int numChangesBeforeLoadFlow = network0.getExtension(CgmesModelExtension.class).getCgmesUpdate().changelog().getChangesForVariant(network0.getVariantManager().getWorkingVariantId()).size();

        profiling.start();
        if (!isBigNetwork) {
            // Compute all flows only for small networks
            // as this will introduce a lot of changes
            runLoadFlowResultsCompletion(network0, invalidateFlows);
        }
        profiling.end(Operations.LOAD_FLOW.name());
        List<IidmChange> changes = network0.getExtension(CgmesModelExtension.class).getCgmesUpdate().changelog().getChangesForVariant(network0.getVariantManager().getWorkingVariantId());
        int numChangesAfterLoadFlow = changes.size();

        DataSource tmp = tmpDataSource(impl);
        CgmesExport e = new CgmesExport(profiling);
        e.export(network0, new Properties(), tmp);

        // import saved data and compare with original Network
        cgmesImport.setProfiling(null);
        Network network1 = cgmesImport.importData(tmp, NetworkFactory.findDefault(), importParameters(impl));
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
        profiling.report(true);
    }

    private void runLoadFlowResultsCompletion(Network network, boolean invalidateFlows) {
        if (invalidateFlows) {
            invalidateFlows(network);
        }
        LoadFlowParameters lfParameters = new LoadFlowParameters();
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
        importParameters.put(CgmesImport.POWSYBL_TRIPLESTORE, impl);
        importParameters.put(CgmesImport.STORE_CGMES_MODEL_AS_NETWORK_EXTENSION, "true");
        // SvInjections are converted to IIDM loads
        // Changes in these loads are not mapped back to CGMES,
        // because there is no corresponding object
        // For these tests, we simply ignore SvInjections when importing
        importParameters.put(CgmesImport.CONVERT_SV_INJECTIONS, "false");
        return importParameters;
    }

    private DataSource tmpDataSource(String impl) throws IOException {
        Path exportFolder = fileSystem.getPath("impl-" + impl);
        if (Files.exists(exportFolder)) {
            FileUtils.cleanDirectory(exportFolder.toFile());
        }
        Files.createDirectories(exportFolder);
        DataSource tmpDataSource = new FileDataSource(exportFolder, "");
        return tmpDataSource;
    }

    private FileSystem fileSystem;
    private CgmesImport cgmesImport;

    private static final Logger LOG = LoggerFactory.getLogger(CgmesUpdateTest.class);
}
