/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online.tools;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.google.auto.service.AutoService;

import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.iidm.datasource.DataSource;
import eu.itesla_project.iidm.datasource.FileDataSource;
import eu.itesla_project.iidm.export.Exporters;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.online.OnlineConfig;
import eu.itesla_project.modules.online.OnlineDb;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
@AutoService(Tool.class)
public class AmplExportOnlineWorkflowStatesTool implements Tool {

    private static Command COMMAND = new Command() {

        @Override
        public String getName() {
            return "ampl-export-online-workflow-states";
        }

        @Override
        public String getTheme() {
            return Themes.ONLINE_WORKFLOW;
        }

        @Override
        public String getDescription() {
            return "Export network data of stored states of an online workflow, in AMPL format";
        }

        @Override
        public Options getOptions() {
            Options options = new Options();
            options.addOption(Option.builder().longOpt("workflow")
                    .desc("the workflow id")
                    .hasArg()
                    .required()
                    .argName("ID")
                    .build());
            options.addOption(Option.builder().longOpt("state")
                    .desc("the state id")
                    .hasArg()
                    .argName("STATE")
                    .build());
            options.addOption(Option.builder().longOpt("folder")
                    .desc("the folder where to export the network data")
                    .hasArg()
                    .required()
                    .argName("FOLDER")
                    .build());
            return options;
        }

        @Override
        public String getUsageFooter() {
            return null;
        }

    };

    @Override
    public Command getCommand() {
        return COMMAND;
    }

    @Override
    public void run(CommandLine line) throws Exception {
        OnlineConfig config = OnlineConfig.load();
        OnlineDb onlinedb = config.getOnlineDbFactoryClass().newInstance().create();
        String workflowId = line.getOptionValue("workflow");
        List<Integer> states = line.hasOption("state") ? Arrays.asList(Integer.valueOf(line.getOptionValue("state"))) : onlinedb.listStoredStates(workflowId);
        Path folder =  Paths.get(line.getOptionValue("folder"));
        System.out.println("Exporting in AMPL format network data of workflow " + workflowId + ", " + states.size() + " state(s), to folder " + folder);
        states.forEach(state -> exportState(onlinedb, workflowId, state, folder));
        onlinedb.close();
    }

    private void exportState(OnlineDb onlinedb, String workflowId, Integer stateId, Path folder) {
        System.out.println("Exporting network data of workflow " + workflowId +", state " + stateId);
        Network network = onlinedb.getState(workflowId, stateId);
        if (network == null) {
            System.out.println("Cannot export network data: no stored state " + stateId + " for workflow " + workflowId);
            return;
        }
        Path stateFolder = Paths.get(folder.toString(), "wf_" + workflowId + "_state_" + stateId);
        System.out.println("Exporting network data of workflow " + workflowId + ", state " + stateId + " to folder " + stateFolder);
        if (stateFolder.toFile().exists()) {
            System.out.println("Cannot export network data of workflow " + workflowId + ", state " + stateId + ": folder " + stateFolder + " already exists");
            return;
        }
        if (! stateFolder.toFile().mkdirs()) {
            System.out.println("Cannot export network data of workflow " + workflowId + ", state " + stateId + ": unable to create " + stateFolder + " folder");
            return;
        }
        DataSource dataSource = new FileDataSource(stateFolder, "wf_" + workflowId + "_state_" + stateId);
        Exporters.export("AMPL", network, new Properties(), dataSource);
    }

}
