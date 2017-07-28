/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation.mpi;

import eu.itesla_project.commons.config.ComponentDefaultConfig;
import eu.itesla_project.computation.ComputationManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystem;
import java.nio.file.Path;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MpiToolUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MpiToolUtil.class);

    private MpiToolUtil() {
    }

    public static Options createMpiOptions() {
        Options options = new Options();
        options.addOption(Option.builder()
                .longOpt("tmp-dir")
                .desc("local temporary directory")
                .hasArg()
                .argName("dir")
                .build());
        options.addOption(Option.builder()
                .longOpt("statistics-db-dir")
                .desc("statistics db directory")
                .hasArg()
                .argName("dir")
                .build());
        options.addOption(Option.builder()
                .longOpt("statistics-db-name")
                .desc("statistics db name")
                .hasArg()
                .argName("name")
                .build());
        options.addOption(Option.builder()
                .longOpt("cores")
                .desc("number of cores per rank")
                .hasArg()
                .required()
                .argName("n")
                .build());
        options.addOption(Option.builder()
                .longOpt("verbose")
                .desc("verbose mode")
                .build());
        options.addOption(Option.builder()
                .longOpt("stdout-archive")
                .desc("tasks standard output archive")
                .hasArg()
                .argName("file")
                .build());
        return options;
    }

    private static MpiStatisticsFactory createMpiStatisticsFactory(ComponentDefaultConfig config, Path statisticsDbDir, String statisticsDbName) {
        MpiStatisticsFactory statisticsFactory;
        if (statisticsDbDir != null && statisticsDbName != null) {
            statisticsFactory = config.newFactoryImpl(MpiStatisticsFactory.class, NoMpiStatisticsFactory.class);
        } else {
            statisticsFactory = new NoMpiStatisticsFactory();
        }
        return statisticsFactory;
    }

    public static ComputationManager createMpiComputationManager(CommandLine line, FileSystem fileSystem) {
        Path tmpDir = fileSystem.getPath(line.hasOption("tmp-dir") ? line.getOptionValue("tmp-dir") : System.getProperty("java.io.tmpdir"));
        Path statisticsDbDir = line.hasOption("statistics-db-dir") ? fileSystem.getPath(line.getOptionValue("statistics-db-dir")) : null;
        String statisticsDbName = line.hasOption("statistics-db-name") ? line.getOptionValue("statistics-db-name") : null;
        int coresPerRank = Integer.parseInt(line.getOptionValue("cores"));
        boolean verbose = line.hasOption("verbose");
        Path stdOutArchive = line.hasOption("stdout-archive") ? fileSystem.getPath(line.getOptionValue("stdout-archive")) : null;

        ComponentDefaultConfig config = ComponentDefaultConfig.load();

        MpiExecutorContext mpiExecutorContext = config.newFactoryImpl(MpiExecutorContextFactory.class, DefaultMpiExecutorContextFactory.class).create();
        MpiStatisticsFactory statisticsFactory = createMpiStatisticsFactory(config, statisticsDbDir, statisticsDbName);
        try {
            MpiStatistics statistics = statisticsFactory.create(statisticsDbDir, statisticsDbName);

            return new MpiComputationManager(tmpDir, statistics, mpiExecutorContext, coresPerRank, verbose, stdOutArchive) {
                @Override
                public void close() throws Exception {
                    try {
                        super.close();
                    } catch (Exception e) {
                        LOGGER.error(e.toString(), e);
                    }
                    try {
                        statistics.close();
                    } catch (Exception e) {
                        LOGGER.error(e.toString(), e);
                    }
                    try {
                        mpiExecutorContext.shutdown();
                    } catch (Exception e) {
                        LOGGER.error(e.toString(), e);
                    }
                }
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
