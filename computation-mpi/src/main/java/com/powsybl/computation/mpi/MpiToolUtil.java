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

    public static ComputationManager createMpiComputationManager(CommandLine line, FileSystem fileSystem) {
        Path tmpDir = fileSystem.getPath(line.hasOption("tmp-dir") ? line.getOptionValue("tmp-dir") : System.getProperty("java.io.tmpdir"));
        Path statisticsDbDir = line.hasOption("statistics-db-dir") ? fileSystem.getPath(line.getOptionValue("statistics-db-dir")) : null;
        String statisticsDbName = line.hasOption("statistics-db-name") ? line.getOptionValue("statistics-db-name") : null;
        int coresPerRank = Integer.parseInt(line.getOptionValue("cores"));
        boolean verbose = line.hasOption("verbose");
        Path stdOutArchive = line.hasOption("stdout-archive") ? fileSystem.getPath(line.getOptionValue("stdout-archive")) : null;

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
