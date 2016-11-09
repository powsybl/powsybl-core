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
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;

import java.util.Arrays;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CheckSecurityCommand implements Command {

    static final double DEFAULT_PURITY_THRESHOLD = 0.95;

    public static final CheckSecurityCommand INSTANCE = new CheckSecurityCommand();

    @Override
    public String getName() {
        return "check-security";
    }

    @Override
    public String getTheme() {
        return "Security rules DB";
    }

    @Override
    public String getDescription() {
        return "check case security";
    }

    @Override
    public Options getOptions() {
        Options options = new Options();
        options.addOption(Option.builder().longOpt("case-file")
                .desc("the case path")
                .hasArg()
                .argName("FILE")
                .required()
                .build());
        options.addOption(Option.builder().longOpt("workflow")
                .desc("the workflow id (to find rules)")
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
        options.addOption(Option.builder().longOpt("purity-threshold")
                .desc("the purity threshold (related to decision tree), default is " + DEFAULT_PURITY_THRESHOLD)
                .hasArg()
                .argName("THRESHOLD")
                .build());
        options.addOption(Option.builder().longOpt("rules-db-name")
                .desc("the rules db name (default is " + OfflineConfig.DEFAULT_RULES_DB_NAME + ")")
                .hasArg()
                .argName("NAME")
                .build());
        options.addOption(Option.builder().longOpt("output-csv-file")
                .desc("output CSV file path (pretty print on standard output if not specified)")
                .hasArg()
                .argName("FILE")
                .build());
        options.addOption(Option.builder().longOpt("security-index-types")
                .desc("security index type to check, all if no specified")
                .hasArg()
                .argName("INDEX_TYPE1,INDEX_TYPE2,...")
                .build());
        options.addOption(Option.builder().longOpt("contingencies")
                .desc("contingencies to check, all if no specified")
                .hasArg()
                .argName("CONTINGENCY1,CONTINGENCY2,...")
                .build());
        return options;
    }

    @Override
    public String getUsageFooter() {
        return "Where ATTRIBUTE_SET is one of " + Arrays.toString(RuleAttributeSet.values())
                + "\n      INDEX_TYPE is one of " + Arrays.toString(SecurityIndexType.values());
    }

}
