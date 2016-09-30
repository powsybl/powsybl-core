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
import eu.itesla_project.simulation.securityindexes.SecurityIndexId;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class SimulationsDbPrintSecurityIndexesDetails implements Tool {

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "simulations-db-print-security-indexes-details";
            }

            @Override
            public String getTheme() {
                return "Simulation DB";
            }

            @Override
            public String getDescription() {
                return "print simulations db security indexes details";
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
                options.addOption(Option.builder().longOpt("security-index")
                        .desc("the security index id")
                        .hasArg()
                        .required()
                        .argName("ID")
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
    public void run(CommandLine line) throws Exception {
        String simulationDbName = line.hasOption("simulation-db-name") ? line.getOptionValue("simulation-db-name") : OfflineConfig.DEFAULT_SIMULATION_DB_NAME;
        String workflowId = line.getOptionValue("workflow");
        SecurityIndexId securityIndexId = SecurityIndexId.fromString(line.getOptionValue("security-index"));
        OfflineConfig config = OfflineConfig.load();
        OfflineDb offlineDb = config.getOfflineDbFactoryClass().newInstance().create(simulationDbName);
        System.out.println("sample;secure;details");
        offlineDb.getSecurityIndexes(workflowId, securityIndexId).entrySet()
                .forEach(e -> System.out.println(e.getKey() + ";" + e.getValue().isOk() + ";" + e.getValue().toMap()));
    }

}
