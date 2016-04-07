/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.tools;

import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.modules.offline.OfflineWorkflowCreationParameters;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CreateOfflineWorkflowCommand implements Command {

    public static final CreateOfflineWorkflowCommand INSTANCE = new CreateOfflineWorkflowCommand();

    @Override
    public String getName() {
        return "create-offline-workflow";
    }

    @Override
    public String getTheme() {
        return Themes.OFFLINE_APPLICATION_REMOTE_CONTROL;
    }

    @Override
    public String getDescription() {
        return "create a new offline workflow";
    }

    @Override
    @SuppressWarnings("static-access")
    public Options getOptions() {
        Options options = new Options();
        options.addOption(Option.builder().longOpt("workflow")
                        .desc("the workflow id")
                        .hasArg()
                        .argName("ID")
                        .build());
        options.addOption(Option.builder().longOpt("base-case-countries")
                        .desc("base case country list (ISO code)")
                        .hasArg()
                        .argName("COUNTRY1,COUNTRY2")
                        .build());
        options.addOption(Option.builder().longOpt("base-case-date")
                        .desc("base case date (example 2013-01-15T18:45:00+01:00)")
                        .hasArg()
                        .argName("DATE")
                        .build());
        options.addOption(Option.builder().longOpt("history-interval")
                        .desc("history time interval (example 2013-01-01T00:00:00+01:00/2013-01-31T23:59:00+01:00)")
                        .hasArg()
                        .argName("DATE1/DATE2")
                        .build());
        options.addOption(Option.builder().longOpt("generation-sampled")
                        .desc("sample renewable generation")
                        .build());
        options.addOption(Option.builder().longOpt("boundaries-sampled")
                        .desc("sample boundaries flow")
                        .build());
        options.addOption(Option.builder().longOpt("topo-init")
                        .desc("topology initialisation (default is " + OfflineWorkflowCreationParameters.DEFAULT_INIT_TOPO + ")")
                        .build());
        options.addOption(Option.builder().longOpt("correlation-threshold")
                        .desc("correlation threshold (default is " + OfflineWorkflowCreationParameters.DEFAULT_CORRELATION_THRESHOLD + ")")
                        .hasArg()
                        .argName("THRESHOLD")
                        .build());
        options.addOption(Option.builder().longOpt("probability-threshold")
                        .desc("probability threshold (default is " + OfflineWorkflowCreationParameters.DEFAULT_PROBABILITY_THRESHOLD + ")")
                        .hasArg()
                        .argName("THRESHOLD")
                        .build());
        options.addOption(Option.builder().longOpt("simplified-workflow")
                        .desc("simplified workflow")
                        .build());
        options.addOption(Option.builder().longOpt("merge-optimized")
                        .desc("run optimizer after merging")
                        .build());
        options.addOption(Option.builder().longOpt("attributes-country-filter")
                        .desc("list of countries to filter attributes used in security rules")
                        .hasArg()
                        .argName("COUNTRY1,COUNTRY2")
                        .build());
        options.addOption(Option.builder().longOpt("attributes-min-base-voltage-filter")
                        .desc("minimum base voltage to filter attributes used in security rules")
                        .hasArg()
                        .argName("BASE_VOLTAGE")
                        .build());
        return options;
    }

    @Override
    public String getUsageFooter() {
        return null;
    }

}
