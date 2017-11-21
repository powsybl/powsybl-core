/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

import com.powsybl.commons.config.ComponentDefaultConfig;
import com.powsybl.commons.exceptions.UncheckedInterruptedException;
import com.powsybl.computation.ComputationManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class MpiToolUtil {

    private static final String TMP_DIR = "tmp-dir";
    private static final String STATISTICS_DB_DIR = "statistics-db-dir";
    private static final String STATISTICS_DB_NAME = "statistics-db-name";
    private static final String CORES = "cores";
    private static final String VERBOSE = "verbose";
    private static final String STDOUT_ARCHIVE = "stdout-archive";

    private MpiToolUtil() {
    }

    public static Options createMpiOptions() {
        Options options = new Options();
        options.addOption(Option.builder()
                .longOpt(TMP_DIR)
                .desc("local temporary directory")
                .hasArg()
                .argName("dir")
                .build());
        options.addOption(Option.builder()
                .longOpt(STATISTICS_DB_DIR)
                .desc("statistics db directory")
                .hasArg()
                .argName("dir")
                .build());
        options.addOption(Option.builder()
                .longOpt(STATISTICS_DB_NAME)
                .desc("statistics db name")
                .hasArg()
                .argName("name")
                .build());
        options.addOption(Option.builder()
                .longOpt(CORES)
                .desc("number of cores per rank")
                .hasArg()
                .required()
                .argName("n")
                .build());
        options.addOption(Option.builder()
                .longOpt(VERBOSE)
                .desc("verbose mode")
                .build());
        options.addOption(Option.builder()
                .longOpt(STDOUT_ARCHIVE)
                .desc("tasks standard output archive")
                .hasArg()
                .argName("file")
                .build());
        return options;
    }

    public static ComputationManager createMpiComputationManager(CommandLine line, FileSystem fileSystem) {
        Path tmpDir = fileSystem.getPath(line.hasOption(TMP_DIR) ? line.getOptionValue(TMP_DIR) : System.getProperty("java.io.tmpdir"));
        Path statisticsDbDir = line.hasOption(STATISTICS_DB_DIR) ? fileSystem.getPath(line.getOptionValue(STATISTICS_DB_DIR)) : null;
        String statisticsDbName = line.hasOption(STATISTICS_DB_NAME) ? line.getOptionValue(STATISTICS_DB_NAME) : null;
        int coresPerRank = Integer.parseInt(line.getOptionValue(CORES));
        boolean verbose = line.hasOption(VERBOSE);
        Path stdOutArchive = line.hasOption(STDOUT_ARCHIVE) ? fileSystem.getPath(line.getOptionValue(STDOUT_ARCHIVE)) : null;

        ComponentDefaultConfig config = ComponentDefaultConfig.load();

        MpiStatisticsFactory statisticsFactory = config.newFactoryImpl(MpiStatisticsFactory.class, NoMpiStatisticsFactory.class);
        try {
            return new MpiComputationManager(tmpDir, statisticsFactory, statisticsDbDir, statisticsDbName,
                                             new MpiExecutorContext(), coresPerRank, verbose, stdOutArchive);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UncheckedInterruptedException(e);
        }
    }
}
