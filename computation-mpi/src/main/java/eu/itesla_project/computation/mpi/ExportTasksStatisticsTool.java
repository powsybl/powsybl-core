/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation.mpi;

import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.commons.tools.Command;
import com.google.auto.service.AutoService;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.cli.CommandLine;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class ExportTasksStatisticsTool implements Tool {

    @Override
    public Command getCommand() {
        return ExportTasksStatisticsCommand.INSTANCE;
    }

    @Override
    public void run(CommandLine line) throws Exception {
        Path statisticsDbDir = Paths.get(line.getOptionValue("statistics-db-dir"));
        String statisticsDbName = line.getOptionValue("statistics-db-name");
        Path outputFile = Paths.get(line.getOptionValue("output-file"));
        try (MpiStatistics statistics = new CsvMpiStatistics(statisticsDbDir, statisticsDbName)) {
            try (Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
                statistics.exportTasksToCsv(writer);
            }
        }
    }

}
