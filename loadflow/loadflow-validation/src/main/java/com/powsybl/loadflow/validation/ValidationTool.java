/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StateManager;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowFactory;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import java.util.Objects;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
@AutoService(Tool.class)
public class ValidationTool implements Tool {

    private static final String CASE_FILE = "case-file";
    private static final String OUTPUT_FOLDER = "output-folder";
    private static final String LOAD_FLOW = "load-flow";
    private static final String VERBOSE = "verbose";
    private static final String OUTPUT_FORMAT = "output-format";
    private static final String TYPES = "types";
    private static final String CONFIG_FILE = "config-file";

    private static final Command COMMAND = new Command() {

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
            options.addOption(Option.builder().longOpt(CASE_FILE)
                    .desc("case file path")
                    .hasArg()
                    .argName("FILE")
                    .required()
                    .build());
            options.addOption(Option.builder().longOpt(OUTPUT_FOLDER)
                    .desc("output folder path")
                    .hasArg()
                    .argName("FOLDER")
                    .required()
                    .build());
            options.addOption(Option.builder().longOpt(LOAD_FLOW)
                    .desc("run loadflow")
                    .build());
            options.addOption(Option.builder().longOpt(VERBOSE)
                    .desc("verbose output")
                    .build());
            options.addOption(Option.builder().longOpt(OUTPUT_FORMAT)
                    .desc("output format " + Arrays.toString(ValidationOutputWriter.values()))
                    .hasArg()
                    .argName("VALIDATION_WRITER")
                    .build());
            options.addOption(Option.builder().longOpt(TYPES)
                    .desc("validation types " + Arrays.toString(ValidationType.values()) + " to run, all of them if the option if not specified")
                    .hasArg()
                    .argName("VALIDATION_TYPE,VALIDATION_TYPE,...")
                    .build());
            options.addOption(Option.builder().longOpt(CONFIG_FILE)
                    .desc("configuration file")
                    .hasArg()
                    .argName("CONFIG-FILE")
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
        Path caseFile = Paths.get(line.getOptionValue(CASE_FILE));
        Path outputFolder = Paths.get(line.getOptionValue(OUTPUT_FOLDER));
        if (!Files.exists(outputFolder)) {
            Files.createDirectories(outputFolder);
        }
        ValidationConfig config = ValidationConfig.load();
        if (line.hasOption(VERBOSE)) {
            config.setVerbose(true);
        }
        if (line.hasOption(OUTPUT_FORMAT)) {
            config.setValidationOutputWriter(ValidationOutputWriter.valueOf(line.getOptionValue(OUTPUT_FORMAT)));
        }
        context.getOutputStream().println("Loading case " + caseFile);
        Network network = Importers.loadNetwork(caseFile);
        if (network == null) {
            throw new PowsyblException("Case " + caseFile + " not found");
        }

        // Configuration file
        Path configFile = line.hasOption(CONFIG_FILE) ? context.getFileSystem().getPath(line.getOptionValue(CONFIG_FILE)) : null;

        if (line.hasOption(LOAD_FLOW)) {
            context.getOutputStream().println("Running loadflow on network " + network.getId());
            LoadFlowFactory loadFlowFactory = config.getLoadFlowFactory().newInstance();
            LoadFlowParameters parameters;
            if (Objects.isNull(configFile)) {
                parameters = LoadFlowParameters.load();
            } else {
                parameters = LoadFlowParameters.load(configFile);
            }
            LoadFlow loadFlow = loadFlowFactory.create(network, context.getComputationManager(), 0);
            loadFlow.runAsync(StateManager.INITIAL_STATE_ID, parameters)
                    .thenAccept(loadFlowResult -> {
                        if (!loadFlowResult.isOk()) {
                            throw new PowsyblException("Loadflow on network " + network.getId() + " does not converge");
                        }
                    })
                    .join();
        }
        Set<ValidationType> validationTypes = Sets.newHashSet(ValidationType.values());
        if (line.hasOption(TYPES)) {
            validationTypes = Arrays.stream(line.getOptionValue(TYPES).split(","))
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
