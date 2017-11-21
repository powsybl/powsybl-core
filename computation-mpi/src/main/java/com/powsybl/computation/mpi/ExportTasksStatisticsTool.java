/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

import com.google.auto.service.AutoService;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class ExportTasksStatisticsTool implements Tool {

    private static final String STATISTICS_DB_DIR = "statistics-db-dir";
    private static final String STATISTICS_DB_NAME = "statistics-db-name";
    private static final String OUTPUT_FILE = "output-file";

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "export-tasks-statistics";
            }

            @Override
            public String getTheme() {
                return "MPI statistics";
            }

            @Override
            public String getDescription() {
                return "export tasks statistics to CSV";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt(STATISTICS_DB_DIR)
                        .desc("statistics db directory")
                        .hasArg()
                        .argName("DIR")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(STATISTICS_DB_NAME)
                        .desc("statistics db name")
                        .hasArg()
                        .argName("NAME")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FILE)
                        .desc("CSV output file")
                        .hasArg()
                        .argName("FILE")
                        .required()
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path statisticsDbDir = context.getFileSystem().getPath(line.getOptionValue(STATISTICS_DB_DIR));
        String statisticsDbName = line.getOptionValue(STATISTICS_DB_NAME);
        Path outputFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_FILE));
        try (MpiStatistics statistics = new CsvMpiStatistics(statisticsDbDir, statisticsDbName)) {
            try (Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
                statistics.exportTasksToCsv(writer);
            }
        }
    }

}
