/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.offline;

import eu.itesla_project.commons.tools.Command;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExportMetricsCommand implements Command {

    public static final ExportMetricsCommand INSTANCE = new ExportMetricsCommand();

    @Override
    public String getName() {
        return "export-metrics";
    }

    @Override
    public String getTheme() {
        return "Simulation DB";
    }

    @Override
    public String getDescription() {
        return "export metrics from simulation db to csv file";
    }

    @Override
    @SuppressWarnings("static-access")
    public Options getOptions() {
        Options options = new Options();
        options.addOption(Option.builder().longOpt("metrics-db-name")
                        .desc("the metrics db name (default is " + OfflineConfig.DEFAULT_METRICS_DB_NAME + ")")
                        .hasArg()
                        .argName("NAME")
                        .build());
        options.addOption(Option.builder().longOpt("workflow")
                                .desc("the workflow id")
                                .hasArg()
                                .required()
                                .argName("ID")
                                .build());
        options.addOption(Option.builder().longOpt("output-file")
                                .desc("output file path")
                                .hasArg()
                                .argName("FILE")
                                .required()
                                .build());
        options.addOption(Option.builder().longOpt("delimiter")
                                .desc("CSV delimiter")
                                .hasArg()
                                .argName("CHAR")
                                .build());
        return options;
    }

    @Override
    public String getUsageFooter() {
        return null;
    }

}
