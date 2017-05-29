/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.loadflow.validation;

import java.nio.file.Path;
import java.nio.file.Paths;

import eu.itesla_project.commons.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.google.auto.service.AutoService;

import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.StateManager;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowParameters;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
@AutoService(Tool.class)
public class CheckFlowsTool implements Tool {
    
    private static Command COMMAND = new Command() {

        @Override
        public String getName() {
            return "check-flows";
        }

        @Override
        public String getTheme() {
            return "Computation";
        }

        @Override
        public String getDescription() {
            return "Check flows of a network";
        }

        @Override
        public Options getOptions() {
            Options options = new Options();
            options.addOption(Option.builder().longOpt("case-file")
                    .desc("case file path")
                    .hasArg()
                    .argName("FILE")
                    .required()
                    .build());
            options.addOption(Option.builder().longOpt("output-file")
                    .desc("output file path")
                    .hasArg()
                    .argName("FILE")
                    .required()
                    .build());
            options.addOption(Option.builder().longOpt("load-flow")
                    .desc("run loadflow")
                    .build());
            options.addOption(Option.builder().longOpt("verbose")
                    .desc("verbose output")
                    .build());
            options.addOption(Option.builder().longOpt("output-format")
                    .desc("output format")
                    .hasArg()
                    .argName("FLOWS_WRITER")
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
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path caseFile = Paths.get(line.getOptionValue("case-file"));
        Path outputFile = Paths.get(line.getOptionValue("output-file"));
        CheckFlowsConfig config = CheckFlowsConfig.load();
        if (line.hasOption("verbose")) {
            config.setVerbose(true);
        }
        if (line.hasOption("output-format")) {
            config.setFlowOutputWriter(FlowOutputWriter.valueOf(line.getOptionValue("output-format")));
        }
        System.out.println("Loading case " + caseFile);
        Network network = Importers.loadNetwork(caseFile);
        if (network == null) {
            throw new RuntimeException("Case " + caseFile + " not found");
        }
        if (line.hasOption("load-flow")) {
            System.out.println("Running loadflow on network " + network.getId());
            try (ComputationManager computationManager = new LocalComputationManager()) {
                LoadFlowParameters parameters = LoadFlowParameters.load(); 
                LoadFlow loadFlow = config.getLoadFlowFactory().newInstance().create(network, computationManager, 0);
                loadFlow.runAsync(StateManager.INITIAL_STATE_ID, parameters)
                        .thenAccept(loadFlowResult -> {
                            if (!loadFlowResult.isOk()) {
                                throw new RuntimeException("Loadflow on network " + network.getId() + " does not converge");
                            }
                        })
                        .join();
            }
        }
        System.out.println("Check flows on network " + network.getId() + " result: " + (Validation.checkFlows(network, config, outputFile) ? "success" : "fail"));
    }
    
}
