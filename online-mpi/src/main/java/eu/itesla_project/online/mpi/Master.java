/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.mpi;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.mpi.MpiComputationManager;
import eu.itesla_project.computation.mpi.MpiExecutorContext;
import eu.itesla_project.computation.mpi.MpiStatistics;
import eu.itesla_project.computation.mpi.MpiStatisticsFactory;
import eu.itesla_project.computation.mpi.util.MultiStateNetworkAwareMpiExecutorContext;
import eu.itesla_project.iidm.network.impl.util.MultiStateNetworkAwareExecutors;
import eu.itesla_project.modules.online.OnlineConfig;
import eu.itesla_project.online.LocalOnlineApplication;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class Master {

    private static final Logger LOGGER = LoggerFactory.getLogger(Master.class);

    public static final Options OPTIONS = new Options();

    static {
        Option modeOpt = new Option("m", "mode", true, "simulations or rules mode");
        modeOpt.setRequired(true);
        modeOpt.setArgName("simulations|rules");
        Option tmpDirOpt = new Option("t", "tmp-dir", true, "local temporary directory");
        tmpDirOpt.setRequired(true);
        tmpDirOpt.setArgName("dir");
        Option statisticsFactoryClassOpt = new Option("f", "statistics-factory-class", true, "statistics factory class name");
        statisticsFactoryClassOpt.setRequired(true);
        statisticsFactoryClassOpt.setArgName("class");
        Option statisticsDbDirOpt = new Option("s", "statistics-db-dir", true, "statistics db directory");
        statisticsDbDirOpt.setRequired(true);
        statisticsDbDirOpt.setArgName("dir");
        Option statisticsDbNameOpt = new Option("d", "statistics-db-name", true, "statistics db name");
        statisticsDbNameOpt.setRequired(true);
        statisticsDbNameOpt.setArgName("name");
        Option coresOpt = new Option("n", "cores", true, "number of cores per rank");
        coresOpt.setRequired(true);
        coresOpt.setArgName("n");
        Option stdOutArchiveOpt = new Option("o", "stdout-archive", true, "tasks standard output archive");
        stdOutArchiveOpt.setRequired(false);
        stdOutArchiveOpt.setArgName("file");
        OPTIONS.addOption(modeOpt);
        OPTIONS.addOption(tmpDirOpt);
        OPTIONS.addOption(statisticsFactoryClassOpt);
        OPTIONS.addOption(statisticsDbDirOpt);
        OPTIONS.addOption(statisticsDbNameOpt);
        OPTIONS.addOption(coresOpt);
        OPTIONS.addOption(stdOutArchiveOpt);
    }

    public Master() {
    }

    public static void main(String[] args) throws Exception {
        try {
            CommandLineParser parser = new GnuParser();
            CommandLine line = parser.parse(OPTIONS, args);

            String mode = line.getOptionValue("m");
            Path tmpDir = Paths.get(line.getOptionValue("t"));
            Class<?> statisticsFactoryClass = Class.forName(line.getOptionValue("f"));
            Path statisticsDbDir = Paths.get(line.getOptionValue("s"));
            String statisticsDbName = line.getOptionValue("d");
            int coresPerRank = Integer.parseInt(line.getOptionValue("n"));
            Path stdOutArchive = line.hasOption("o") ? Paths.get(line.getOptionValue("o")) : null;

            MpiExecutorContext mpiExecutorContext = new MultiStateNetworkAwareMpiExecutorContext();
            ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
            ExecutorService executorService = MultiStateNetworkAwareExecutors.newCachedThreadPool();
            try {
                MpiStatisticsFactory statisticsFactory = statisticsFactoryClass.asSubclass(MpiStatisticsFactory.class).newInstance();
                MpiStatistics statistics = statisticsFactory.create(statisticsDbDir, statisticsDbName);
                try (ComputationManager computationManager = new MpiComputationManager(tmpDir, statistics, mpiExecutorContext, coresPerRank, false, stdOutArchive)) {
                    OnlineConfig config = OnlineConfig.load();
                    try (LocalOnlineApplication application = new LocalOnlineApplication(config, computationManager, scheduledExecutorService, executorService, true)) {
                        switch (mode) {
                            case "ui":
                                System.out.println("LocalOnlineApplication created");
                                System.out.println("Waiting till shutdown");
                                // indefinitely wait for JMX commands
                                //TimeUnit.DAYS.sleep(Integer.MAX_VALUE);
                                synchronized(application)
                                {
                                    try{
                                        application.wait();
                                    }catch(InterruptedException ex){}

                                }
                                break;
                            default:
                                throw new IllegalArgumentException("Invalid mode " + mode);
                        }
                    }
                }
            } finally {
                mpiExecutorContext.shutdown();
                executorService.shutdown();
                scheduledExecutorService.shutdown();
                executorService.awaitTermination(15, TimeUnit.MINUTES);
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
