/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.offline;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import org.apache.commons.cli.CommandLine;

import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class ExportMetricsTool implements Tool {

    @Override
    public Command getCommand() {
        return ExportMetricsCommand.INSTANCE;
    }

    @Override
    public void run(CommandLine line) throws Exception {
        String metricsDbName = line.hasOption("metrics-db-name") ? line.getOptionValue("metrics-db-name") : OfflineConfig.DEFAULT_METRICS_DB_NAME;
        OfflineConfig config = OfflineConfig.load();
        MetricsDb metricsDb = config.getMetricsDbFactoryClass().newInstance().create(metricsDbName);
        String workflowId = line.getOptionValue("workflow");
        Path outputFile = Paths.get(line.getOptionValue("output-file"));
        char delimiter = ';';
        if (line.hasOption("delimiter")) {
            String value = line.getOptionValue("delimiter");
            if (value.length() != 1) {
                throw new RuntimeException("A character is expected");
            }
            delimiter = value.charAt(0);
        }
        try (Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            metricsDb.exportCsv(workflowId, writer, delimiter);
        }
    }

}
