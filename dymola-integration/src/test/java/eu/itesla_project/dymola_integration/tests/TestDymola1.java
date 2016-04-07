/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.dymola_integration.tests;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import eu.itesla_project.computation.*;
import eu.itesla_project.computation.local.LocalComputationConfig;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.dymola.*;
import eu.itesla_project.iidm.datasource.ZipFileDataSource;
import eu.itesla_project.iidm.ddb.eurostag_imp_exp.IIDMDynamicDatabaseFactory;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.contingencies.ContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.contingencies.Contingency;
import eu.itesla_project.modules.ddb.DynamicDatabaseClient;
import eu.itesla_project.modules.ddb.DynamicDatabaseClientFactory;
import eu.itesla_project.modules.simulation.*;
import eu.itesla_project.modules.test.AutomaticContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.test.CsvFileContingenciesAndActionsDatabaseClient;
import eu.itesla_project.modules.test.CsvFileContingenciesAndActionsDatabaseClientFactory;
import eu.itesla_project.modules.test.CsvFileModelicaContingenciesAndActionsDatabaseClientFactory;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.impl.nio.file.ShrinkWrapFileSystem;
import org.jboss.shrinkwrap.impl.nio.file.ShrinkWrapFileSystemProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static eu.itesla_project.computation.FilePreProcessor.ARCHIVE_UNZIP;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class TestDymola1 {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestDymola1.class);

    Network network;

    public String getTestName() {
        final StackTraceElement[] ste = new Throwable().getStackTrace();
        return (ste[1].getMethodName());
    }

    @Before
    public void setUp() {
        System.setProperty("itesla.config.dir","/home/itesla/itesla_dymola/nordic44/.itesla");
        System.setProperty("itesla.cache.dir","/home/itesla/itesla_dymola/nordic44/.cache");
        System.setProperty("itesla.config.name","config");

        Properties importerProps=new Properties();
        importerProps.setProperty("usePsseNamingStrategy", "false");
        network = Importers.import_("CIM1", new ZipFileDataSource(Paths.get("/home/itesla/caserepo/CIM/SN/2015/04/01"), "20150401_0000_SN3_NO0"),importerProps);
    }

    @After
    public void tearDown() throws IOException {
    }

    private void run_test0() throws Exception {
        ContingenciesAndActionsDatabaseClient cadbClient = new CsvFileModelicaContingenciesAndActionsDatabaseClientFactory().create();
        List<Contingency> clist=cadbClient.getContingencies(network);
        LOGGER.info("contingencies set size: {}", clist.size());
        for (Contingency c: clist) {
            LOGGER.info(" contingency:  {} " , c.getElements());
        }
    }

    private void run_test1() throws Exception {
        try (ComputationManager computationManager = new LocalComputationManager(LocalComputationConfig.load())) {

            ContingenciesAndActionsDatabaseClient cadbClient = new CsvFileModelicaContingenciesAndActionsDatabaseClientFactory().create();
            List<Contingency> clist=cadbClient.getContingencies(network);
            LOGGER.info("contingencies set size: {}", clist.size());
            for (Contingency c: clist) {
                LOGGER.info(" contingency:  {} " , c);
            }

            DynamicDatabaseClientFactory ddbClientFactory=new IIDMDynamicDatabaseFactory();
            DymolaFactory dymolaFactory = new DymolaFactory();
            Stabilization stabilization = dymolaFactory.createStabilization(network, computationManager, 0, ddbClientFactory);
            ImpactAnalysis impactAnalysis = dymolaFactory.createImpactAnalysis(network, computationManager, 0, cadbClient);
            Map<String, Object> initContext = new HashMap<>();
            //does not matter, for the time being:  modelica events specifics, e.g. the fault time, are specified in the modelica contingencies-events csv file
            SimulationParameters simulationParameters = SimulationParameters.load();
            //stabilization is actually a mock; the entire simulation is performed inside the impactAnalysis
            stabilization.init(simulationParameters, initContext);
            //run the impact analysis (simulation and indexers computation)
            impactAnalysis.init(simulationParameters, initContext);
            StabilizationResult sr = stabilization.run();
            ImpactAnalysisResult ir = impactAnalysis.run(sr.getState());
            LOGGER.info("Simulation complete.");
            LOGGER.info(" metrics: " + ir.getMetrics());
            LOGGER.info(" indexes: " + ir.getSecurityIndexes());
        }
    }

    //dump contingencies, only
    //@Test
    public void test0() throws Exception {
        run_test0();
    }

    //run impact analysis
    //@Test
    public void test1() throws Exception {
        int RUNS = 1;
        for (int i = 0; i < RUNS; i++) {
            LOGGER.info("--------- TEST {} -----------------------", i);
            run_test1();
        }

    }


}