/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.mpi;

import com.google.common.base.Joiner;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.mpi.MpiComputationManager;
import eu.itesla_project.computation.mpi.MpiExecutorContext;
import eu.itesla_project.computation.mpi.MpiStatistics;
import eu.itesla_project.computation.mpi.MpiStatisticsFactory;
import eu.itesla_project.computation.mpi.util.MultiStateNetworkAwareMpiExecutorContext;
import eu.itesla_project.iidm.network.impl.util.MultiStateNetworkAwareExecutors;
import eu.itesla_project.modules.offline.OfflineConfig;
import eu.itesla_project.modules.offline.OfflineWorkflowCreationParameters;
import eu.itesla_project.offline.LocalOfflineApplication;
import eu.itesla_project.offline.OfflineWorkflowStartParameters;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Master {

    private static final Logger LOGGER = LoggerFactory.getLogger(Master.class);

    public static final Options OPTIONS = new Options();

    private enum Mode {
        ui,
        simulations,
        rules
    }

    static {
        OPTIONS.addOption(Option.builder().longOpt("mode")
                        .desc("simulations, rules or ui, mode")
                        .hasArg()
                        .required()
                        .argName(Joiner.on('|').join(Mode.values()))
                        .build());
        OPTIONS.addOption(Option.builder().longOpt("simulation-db-name")
                        .desc("simulation db name (default is " + OfflineConfig.DEFAULT_SIMULATION_DB_NAME + ")")
                        .hasArg()
                        .argName("name")
                        .build());
        OPTIONS.addOption(Option.builder().longOpt("rules-db-name")
                        .desc("rules db name (default is " + OfflineConfig.DEFAULT_RULES_DB_NAME + ")")
                        .hasArg()
                        .argName("name")
                        .build());
        OPTIONS.addOption(Option.builder().longOpt("metrics-db-name")
                        .desc("metrics db name (default is " + OfflineConfig.DEFAULT_METRICS_DB_NAME + ")")
                        .hasArg()
                        .argName("name")
                        .build());
        OPTIONS.addOption(Option.builder().longOpt("tmp-dir")
                        .desc("local temporary directory")
                        .hasArg()
                        .required()
                        .argName("dir")
                        .build());
        OPTIONS.addOption(Option.builder().longOpt("statistics-factory-class")
                        .desc("statistics factory class name")
                        .hasArg()
                        .required()
                        .argName("class")
                        .build());
        OPTIONS.addOption(Option.builder().longOpt("statistics-db-dir")
                        .desc("statistics db directory")
                        .hasArg()
                        .required()
                        .argName("dir")
                        .build());
        OPTIONS.addOption(Option.builder().longOpt("statistics-db-name")
                        .desc("statistics db name")
                        .hasArg()
                        .required()
                        .argName("name")
                        .build());
        OPTIONS.addOption(Option.builder().longOpt("cores")
                        .desc("number of cores per rank")
                        .hasArg()
                        .required()
                        .argName("n")
                        .build());
        OPTIONS.addOption(Option.builder().longOpt("stdout-archive")
                        .desc("tasks standard output archive")
                        .hasArg()
                        .argName("file")
                        .build());
        OPTIONS.addOption(Option.builder().longOpt("workflow")
                        .desc("workflow id to work on, create a new one if not specified")
                        .hasArg()
                        .argName("ID")
                        .build());
    }
    
    public Master() {
    }

    public static void main(String[] args) throws Exception {
        try {
            CommandLineParser parser = new GnuParser();
            CommandLine line = parser.parse(OPTIONS, args);

            Mode mode = Mode.valueOf(line.getOptionValue("mode"));
            String simulationDbName = line.hasOption("simulation-db-name") ? line.getOptionValue("simulation-db-name") : OfflineConfig.DEFAULT_SIMULATION_DB_NAME;
            String rulesDbName = line.hasOption("rules-db-name") ? line.getOptionValue("rules-db-name") : OfflineConfig.DEFAULT_RULES_DB_NAME;
            String metricsDbName = line.hasOption("metrics-db-name") ? line.getOptionValue("metrics-db-name") : OfflineConfig.DEFAULT_METRICS_DB_NAME;
            Path tmpDir = Paths.get(line.getOptionValue("tmp-dir"));
            Class<?> statisticsFactoryClass = Class.forName(line.getOptionValue("statistics-factory-class"));
            Path statisticsDbDir = Paths.get(line.getOptionValue("statistics-db-dir"));
            String statisticsDbName = line.getOptionValue("statistics-db-name");
            int coresPerRank = Integer.parseInt(line.getOptionValue("cores"));
            Path stdOutArchive = line.hasOption("stdout-archive") ? Paths.get(line.getOptionValue("stdout-archive")) : null;
            String workflowId = line.hasOption("workflow") ? line.getOptionValue("workflow") : null;

            MpiExecutorContext mpiExecutorContext = new MultiStateNetworkAwareMpiExecutorContext();
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
            ExecutorService offlineExecutorService = MultiStateNetworkAwareExecutors.newSizeLimitedThreadPool("OFFLINE_POOL", 100);
            try {
                MpiStatisticsFactory statisticsFactory = statisticsFactoryClass.asSubclass(MpiStatisticsFactory.class).newInstance();
                try (MpiStatistics statistics = statisticsFactory.create(statisticsDbDir, statisticsDbName)) {
                    try (ComputationManager computationManager = new MpiComputationManager(tmpDir, statistics, mpiExecutorContext, coresPerRank, false, stdOutArchive)) {
                        OfflineConfig config = OfflineConfig.load();
                        try (LocalOfflineApplication application = new LocalOfflineApplication(config, computationManager, simulationDbName,
                                rulesDbName, metricsDbName, scheduledExecutorService,
                                offlineExecutorService)) {
                            switch (mode) {
                                case ui:
                                    application.await();
                                    break;

                                case simulations: {
                                    if (workflowId == null) {
                                        workflowId = application.createWorkflow(null, OfflineWorkflowCreationParameters.load());
                                    }
                                    application.startWorkflowAndWait(workflowId, OfflineWorkflowStartParameters.load());
                                }
                                break;

                                case rules: {
                                    if (workflowId == null) {
                                        throw new RuntimeException("Workflow '" + workflowId + "' not found");
                                    }
                                    application.computeSecurityRulesAndWait(workflowId);
                                }
                                break;

                                default:
                                    throw new IllegalArgumentException("Invalid mode " + mode);
                            }
                        }
                    }
                }
            } finally {
                mpiExecutorContext.shutdown();
                offlineExecutorService.shutdown();
                scheduledExecutorService.shutdown();
                offlineExecutorService.awaitTermination(15, TimeUnit.MINUTES);
                scheduledExecutorService.awaitTermination(15, TimeUnit.MINUTES);
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("master", OPTIONS, true);
            System.exit(-1);
        } catch (Throwable t) {
            LOGGER.error(t.toString(), t);
            System.exit(-1);
        }
    }

}
