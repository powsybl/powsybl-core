/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import com.google.auto.service.AutoService;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StateManagerConstants;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.validation.io.ValidationWriters;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
@AutoService(Tool.class)
public class ValidationTool implements Tool {

    private static final String CASE_FILE = "case-file";
    private static final String OUTPUT_FOLDER = "output-folder";
    private static final String LOAD_FLOW = "load-flow";
    private static final String VERBOSE = "verbose";
    private static final String OUTPUT_FORMAT = "output-format";
    private static final String TYPES = "types";
    private static final String COMPARE_RESULTS = "compare-results";
    private static final String GROOVY_SCRIPT = "groovy-script";
    private static final String RUN_COMPUTATION = "run-computation";
    private static final String COMPARE_CASE_FILE = "compare-case-file";

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
            options.addOption(Option.builder().longOpt(RUN_COMPUTATION)
                    .desc("run a computation on the network before validation, available computations are "
                            + Arrays.toString(CandidateComputations.getComputationsNames().toArray()))
                    .hasArg()
                    .argName("COMPUTATION")
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
            options.addOption(Option.builder().longOpt(COMPARE_RESULTS)
                    .desc("compare results of two validations, printing output files with results of both ones. Available comparisons are " + Arrays.toString(ComparisonType.values()))
                    .hasArg()
                    .argName("COMPARISON_TYPE")
                    .build());
            options.addOption(Option.builder().longOpt(COMPARE_CASE_FILE)
                    .desc("path to the case file to compare")
                    .hasArg()
                    .argName("FILE")
                    .build());
            options.addOption(Option.builder().longOpt(GROOVY_SCRIPT)
                    .desc("groovy script to run before validation")
                    .hasArg()
                    .argName("FILE")
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
        ComparisonType comparisonType = null;
        if (line.hasOption(COMPARE_RESULTS)) {
            config.setCompareResults(true);
            comparisonType = ComparisonType.valueOf(line.getOptionValue(COMPARE_RESULTS));
        }
        Set<ValidationType> validationTypes = Sets.newHashSet(ValidationType.values());
        if (line.hasOption(TYPES)) {
            validationTypes = Arrays.stream(line.getOptionValue(TYPES).split(","))
                                    .map(ValidationType::valueOf)
                                    .collect(Collectors.toSet());
        }
        Network network = loadNetwork(caseFile, context);
        if (line.hasOption(GROOVY_SCRIPT)) {
            runGroovyScript(Paths.get(line.getOptionValue(GROOVY_SCRIPT)), network, context);
        }
        try (ValidationWriters validationWriters = new ValidationWriters(network.getId(), validationTypes, outputFolder, config)) {
            if (config.isCompareResults() && ComparisonType.COMPUTATION.equals(comparisonType)) {
                Preconditions.checkArgument(line.hasOption(LOAD_FLOW) || line.hasOption(RUN_COMPUTATION),
                        "Computation results comparison requires to run a computation (options --" + LOAD_FLOW + " or --" + RUN_COMPUTATION + ").");

                context.getOutputStream().println("Running pre-loadflow validation on network " + network.getId());
                runValidation(network, config, validationTypes, validationWriters, context);
            }

            if (line.hasOption(LOAD_FLOW)) {
                runLoadflow(network, config, context);
                context.getOutputStream().println("Running post-loadflow validation on network " + network.getId());
            } else if (line.hasOption(RUN_COMPUTATION)) {
                runComputation(line.getOptionValue(RUN_COMPUTATION), network, context);
                context.getOutputStream().println("Running post-computation validation on network " + network.getId());
            }

            runValidation(network, config, validationTypes, validationWriters, context);

            if (config.isCompareResults() && ComparisonType.BASECASE.equals(comparisonType)) {
                Preconditions.checkArgument(line.hasOption(COMPARE_CASE_FILE),
                        "Basecases comparison requires to provide a second basecase (option --" + COMPARE_CASE_FILE + ").");
                Path compareCaseFile = Paths.get(line.getOptionValue(COMPARE_CASE_FILE));
                Network compareNetwork = loadNetwork(compareCaseFile, context);
                context.getOutputStream().println("Running validation on network " + compareNetwork.getId() + " to compare");
                runValidation(compareNetwork, config, validationTypes, validationWriters, context);
            }
        }
    }

    private Network loadNetwork(Path caseFile, ToolRunningContext context) {
        context.getOutputStream().println("Loading case " + caseFile);
        Network network = Importers.loadNetwork(caseFile);
        if (network == null) {
            throw new PowsyblException("Case " + caseFile + " not found");
        }
        return network;
    }

    private void runGroovyScript(Path script, Network network, ToolRunningContext context) {
        if (Files.exists(script)) {
            context.getOutputStream().println("Running Groovy script " + script + " on network " + network.getId());
            CompilerConfiguration conf = new CompilerConfiguration();
            Binding binding = new Binding();
            binding.setVariable("network", network);
            binding.setVariable("computationManager", context.getShortTimeExecutionComputationManager());
            GroovyShell shell = new GroovyShell(binding, conf);
            try {
                shell.evaluate(script.toFile());
            } catch (CompilationFailedException | IOException e) {
                throw new PowsyblException("Error running Groovy script " + script + " on network " + network.getId() + ": " + e.getMessage());
            }
        } else {
            throw new PowsyblException("Groovy script " + script + " does not exist");
        }
    }

    private void runValidation(Network network, ValidationConfig config, Set<ValidationType> validationTypes, ValidationWriters validationWriter, ToolRunningContext context) {
        validationTypes.forEach(validationType -> {
            context.getOutputStream().println("Validate load-flow results of network " + network.getId()
                                              + " - validation type: " + validationType
                                              + " - result: " + (validationType.check(network, config, validationWriter.getWriter(validationType)) ? "success" : "fail"));
            validationWriter.getWriter(validationType).setValidationCompleted();
        });
    }

    private void runLoadflow(Network network, ValidationConfig config, ToolRunningContext context) throws InstantiationException, IllegalAccessException {
        context.getOutputStream().println("Running loadflow on network " + network.getId());
        LoadFlowParameters parameters = LoadFlowParameters.load();
        LoadFlow loadFlow = config.getLoadFlowFactory().newInstance().create(network, context.getShortTimeExecutionComputationManager(), 0);
        loadFlow.run(StateManagerConstants.INITIAL_STATE_ID, parameters)
                .thenAccept(loadFlowResult -> {
                    if (!loadFlowResult.isOk()) {
                        throw new PowsyblException("Loadflow on network " + network.getId() + " does not converge");
                    }
                })
                .join();
    }

    private void runComputation(String computationName, Network network, ToolRunningContext context) {
        CandidateComputation computation = CandidateComputations.getComputation(computationName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown computation type : " + computationName));
        context.getOutputStream().format("Running computation '%s' on network '%s'", computationName, network.getId()).println();
        computation.run(network, context.getShortTimeExecutionComputationManager());
    }

    enum ComparisonType {
        COMPUTATION("compare the validation of a basecase before and after the computation"),
        BASECASE("compare the validation of two basecases");

        private final String description;

        ComparisonType(String description) {
            this.description = Objects.requireNonNull(description);
        }

        @Override
        public String toString() {
            return this.name() + " (" + this.description + ")";
        }

    }

}
