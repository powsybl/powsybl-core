/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.loadflow.validation;

import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.commons.tools.ToolRunningContext;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.StateManager;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowParameters;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
@AutoService(Tool.class)
public class ValidationTool implements Tool {

    private static Command COMMAND = new Command() {

        @Override
        public String getName() {
            return "loadflow-validation";
        }

        @Override
        public String getTheme() {
            return "Computation";
        }

        @Override
        public String getDescription() {
            return "Validate load-flow results of a network";
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
            options.addOption(Option.builder().longOpt("output-folder")
                    .desc("output folder path")
                    .hasArg()
                    .argName("FOLDER")
                    .required()
                    .build());
            options.addOption(Option.builder().longOpt("load-flow")
                    .desc("run loadflow")
                    .build());
            options.addOption(Option.builder().longOpt("verbose")
                    .desc("verbose output")
                    .build());
            options.addOption(Option.builder().longOpt("output-format")
                    .desc("output format (CSV/CSV_MULTILINE)")
                    .hasArg()
                    .argName("VALIDATION_WRITER")
                    .build());
            options.addOption(Option.builder().longOpt("types")
                    .desc("validation types (FLOWS/GENERATORS/...) to run, all of them if the option if not specified")
                    .hasArg()
                    .argName("VALIDATION_TYPE,VALIDATION_TYPE,...")
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
        Path outputFolder = Paths.get(line.getOptionValue("output-folder"));
        if (!Files.exists(outputFolder)) {
            Files.createDirectories(outputFolder);
        }
        ValidationConfig config = ValidationConfig.load();
        if (line.hasOption("verbose")) {
            config.setVerbose(true);
        }
        if (line.hasOption("output-format")) {
            config.setValidationOutputWriter(ValidationOutputWriter.valueOf(line.getOptionValue("output-format")));
        }
        context.getOutputStream().println("Loading case " + caseFile);
        Network network = Importers.loadNetwork(caseFile);
        if (network == null) {
            throw new RuntimeException("Case " + caseFile + " not found");
        }
        if (line.hasOption("load-flow")) {
            context.getOutputStream().println("Running loadflow on network " + network.getId());
            LoadFlowParameters parameters = LoadFlowParameters.load();
            LoadFlow loadFlow = config.getLoadFlowFactory().newInstance().create(network, context.getComputationManager(), 0);
            loadFlow.runAsync(StateManager.INITIAL_STATE_ID, parameters)
                    .thenAccept(loadFlowResult -> {
                        if (!loadFlowResult.isOk()) {
                            throw new RuntimeException("Loadflow on network " + network.getId() + " does not converge");
                        }
                    })
                    .join();
        }
        Set<ValidationType> validationTypes = Sets.newHashSet(ValidationType.values());
        if (line.hasOption("types")) {
            validationTypes = Arrays.stream(line.getOptionValue("types").split(","))
                                    .map(ValidationType::valueOf)
                                    .collect(Collectors.toSet());
        }
        validationTypes.forEach(validationType -> {
            try {
                context.getOutputStream().println("Validate load-flow results of network " + network.getId()
                                                  + " - validation type: " + validationType
                                                  + " - result: " + (validationType.check(network, config, outputFolder) ? "success" : "fail"));
            } catch (Exception e) {
                context.getErrorStream().println("Error validating load-flow results of network " + network.getId()
                                                 + " - validation type: " + validationType
                                                 + " - error: " + e.getMessage());
            }
        });
    }

}
