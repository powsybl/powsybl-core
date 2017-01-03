/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation.mpi.script;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.config.ComponentDefaultConfig;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.commons.tools.ToolRunningContext;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.mpi.*;
import eu.itesla_project.computation.script.GroovyScript;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class MpiMasterGroovyScriptTool implements Tool {

    private static final Command COMMAND = new Command() {
        @Override
        public String getName() {
            return "mpi-master-groovy-script";
        }

        @Override
        public String getTheme() {
            return "Computation";
        }

        @Override
        public String getDescription() {
            return "run groovy script in mpi computation mode";
        }

        @Override
        public Options getOptions() {
            Options options = new Options();
            options.addOption(Option.builder()
                    .longOpt("script")
                    .desc("the groovy script")
                    .hasArg()
                    .required()
                    .argName("FILE")
                    .build());
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

        @Override
        public boolean isHidden() {
            return true;
        }

        @Override
        public String getUsageFooter() {
            return null;
        }
    };
    @Override
    public Command getCommand() {
        return COMMAND;
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

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path file = context.getFileSystem().getPath(line.getOptionValue("script"));
        Path tmpDir = context.getFileSystem().getPath(line.hasOption("tmp-dir") ? line.getOptionValue("tmp-dir") : System.getProperty("java.io.tmpdir"));
        Path statisticsDbDir = line.hasOption("statistics-db-dir") ? context.getFileSystem().getPath(line.getOptionValue("statistics-db-dir")) : null;
        String statisticsDbName = line.hasOption("statistics-db-name") ? line.getOptionValue("statistics-db-name") : null;
        int coresPerRank = Integer.parseInt(line.getOptionValue("cores"));
        boolean verbose = line.hasOption("verbose");
        Path stdOutArchive = line.hasOption("stdout-archive") ? context.getFileSystem().getPath(line.getOptionValue("stdout-archive")) : null;

        ComponentDefaultConfig config = ComponentDefaultConfig.load();

        MpiExecutorContext mpiExecutorContext = config.newFactoryImpl(MpiExecutorContextFactory.class, DefaultMpiExecutorContextFactory.class).create();
        MpiStatisticsFactory statisticsFactory = createMpiStatisticsFactory(config, statisticsDbDir, statisticsDbName);
        try {
            try (MpiStatistics statistics = statisticsFactory.create(statisticsDbDir, statisticsDbName)) {
                try (ComputationManager computationManager = new MpiComputationManager(tmpDir, statistics, mpiExecutorContext, coresPerRank, verbose, stdOutArchive)) {
                    GroovyScript.run(file, computationManager);
                }
            }
        } finally {
            mpiExecutorContext.shutdown();
        }
    }
}
