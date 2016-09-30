/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules;

import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.modules.offline.OfflineConfig;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;
import java.util.Arrays;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ComputeSecurityRuleCommand implements Command {

    public static final ComputeSecurityRuleCommand INSTANCE = new ComputeSecurityRuleCommand();

    @Override
    public String getName() {
        return "compute-security-rule";
    }

    @Override
    public String getTheme() {
        return "Security rules DB";
    }

    @Override
    public String getDescription() {
        return "compute a single security rule";
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
        options.addOption(Option.builder().longOpt("attribute-set")
                                .desc("the attribute set")
                                .hasArg()
                                .required()
                                .argName("ATTRIBUTE_SET")
                                .build());
        options.addOption(Option.builder().longOpt("contingency")
                                .desc("the contingency id")
                                .hasArg()
                                .required()
                                .argName("ID")
                                .build());
        options.addOption(Option.builder().longOpt("index-type")
                                .desc("the index type")
                                .hasArg()
                                .required()
                                .argName("INDEX_TYPE")
                                .build());
        options.addOption(Option.builder().longOpt("rules-db-name")
                .desc("the rules db name (default is " + OfflineConfig.DEFAULT_RULES_DB_NAME + ")")
                .hasArg()
                .argName("NAME")
                .build());
        options.addOption(Option.builder().longOpt("metrics-db-name")
                .desc("the metrics db name (default is " + OfflineConfig.DEFAULT_METRICS_DB_NAME + ")")
                .hasArg()
                .argName("NAME")
                .build());
        return options;
    }

    @Override
    public String getUsageFooter() {
        return "Where ATTRIBUTE_SET is one of " + Arrays.toString(RuleAttributeSet.values())
                + "\n      INDEX_TYPE is one of " + Arrays.toString(SecurityIndexType.values());
    }

}
