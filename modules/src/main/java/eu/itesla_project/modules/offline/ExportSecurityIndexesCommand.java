/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.offline;

import eu.itesla_project.commons.tools.Command;

import java.util.Arrays;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExportSecurityIndexesCommand implements Command {

    public static final ExportSecurityIndexesCommand INSTANCE = new ExportSecurityIndexesCommand();

    @Override
    public String getName() {
        return "export-security-indexes";
    }

    @Override
    public String getTheme() {
        return "Simulation DB";
    }

    @Override
    public String getDescription() {
        return "export security indexes from simulation db to csv file";
    }

    @Override
    @SuppressWarnings("static-access")
    public Options getOptions() {
        Options options = new Options();
        options.addOption(Option.builder().longOpt("simulation-db-name")
                        .desc("the simulation db name (default is " + OfflineConfig.DEFAULT_SIMULATION_DB_NAME + ")")
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
        options.addOption(Option.builder().longOpt("attributes-filter")
                                .desc("state attributes predefined filter")
                                .hasArg()
                                .argName("ATTRIBUTES_FILTER")
                                .build());
        options.addOption(Option.builder().longOpt("add-sample-column")
                                .desc("add sample number column")
                                .build());
        options.addOption(Option.builder().longOpt("keep-all-samples")
                                .desc("keep unsuccessful samples")
                                .build());
        return options;
    }

    @Override
    public String getUsageFooter() {
        return "Where ATTRIBUTES_FILTER is one of " + Arrays.toString(OfflineAttributesFilter.values());
    }

}
