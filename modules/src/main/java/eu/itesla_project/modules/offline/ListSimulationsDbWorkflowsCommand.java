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
public class ListSimulationsDbWorkflowsCommand implements Command {

    public static final ListSimulationsDbWorkflowsCommand INSTANCE = new ListSimulationsDbWorkflowsCommand();

    @Override
    public String getName() {
        return "list-simulations-db-workflows";
    }

    @Override
    public String getTheme() {
        return "Simulation DB";
    }

    @Override
    public String getDescription() {
        return "list simulations db offline workflows";
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
        return options;
    }

    @Override
    public String getUsageFooter() {
        return null;
    }

}
