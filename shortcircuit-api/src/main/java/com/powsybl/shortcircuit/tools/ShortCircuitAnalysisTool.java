/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.tools;

import com.google.auto.service.AutoService;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.shortcircuit.*;
import com.powsybl.shortcircuit.converter.ShortCircuitAnalysisResultExporters;
import com.powsybl.shortcircuit.json.JsonShortCircuitParameters;
import com.powsybl.shortcircuit.FaultParameters;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolOptions;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;

import static com.powsybl.shortcircuit.tools.ShortCircuitAnalysisToolConstants.*;

/**
 * @author Boubakeur Brahimi
 */
@AutoService(Tool.class)
public class ShortCircuitAnalysisTool implements Tool {

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getDescription() {
                return "Run short circuit analysis";
            }

            @Override
            public String getName() {
                return "shortcircuit";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt(INPUT_FILE_OPTION).desc("fault list as JSON file").hasArg()
                        .argName("FILE").required().build());
                options.addOption(Option.builder().longOpt(CASE_FILE_OPTION).desc("the case path").hasArg()
                        .argName("FILE").required().build());
                options.addOption(Option.builder().longOpt(PARAMETERS_FILE).desc("short circuit parameters as JSON file").hasArg()
                        .argName("FILE").build());
                options.addOption(Option.builder().longOpt(OUTPUT_FILE_OPTION).desc("the output path").hasArg()
                        .argName("FILE").build());
                options.addOption(Option.builder().longOpt(OUTPUT_FORMAT_OPTION)
                        .desc("the output format " + ShortCircuitAnalysisResultExporters.getFormats()).hasArg()
                        .argName("FORMAT").build());
                options.addOption(Option.builder().longOpt(FAULT_PARAMETERS_FILE)
                        .desc("fault parameters file (.json) to get network's info after computation").hasArg()
                        .argName("FILE").build());
                return options;
            }

            @Override
            public String getTheme() {
                return "Computation";
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    static Network readNetwork(CommandLine line, ToolRunningContext context) {
        ToolOptions options = new ToolOptions(line, context);
        Path caseFile = options.getPath(CASE_FILE_OPTION)
                .orElseThrow(IllegalStateException::new);
        context.getOutputStream().println("Loading network '" + caseFile + "'");
        return Network.read(caseFile);
    }

    static ShortCircuitInput readInput(CommandLine line, ToolRunningContext context) throws ParseException {
        ToolOptions options = new ToolOptions(line, context);
        ShortCircuitInput input = new ShortCircuitInput();
        // Faults is required
        Path inputFile = options.getPath(INPUT_FILE_OPTION)
                .orElseThrow(() -> new ParseException("Missing required option: " + INPUT_FILE_OPTION));
        context.getOutputStream().println("Loading input '" + inputFile + "'");
        input.setFaults(Fault.read(inputFile));
        // ShortCircuit parameters loading
        input.setParameters(ShortCircuitParameters.load());
        options.getPath(PARAMETERS_FILE).ifPresent(parametersFile -> {
            context.getOutputStream().println("Loading parameters '" + parametersFile + "'");
            JsonShortCircuitParameters.update(input.getParameters(), parametersFile);
        });
        // FaultParameters list
        options.getPath(FAULT_PARAMETERS_FILE).ifPresent(faultParametersFilePath -> {
            context.getOutputStream().println("Loading fault parameters '" + faultParametersFilePath + "'");
            input.setFaultParameters(FaultParameters.read(faultParametersFilePath));
        });
        return input;
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        ToolOptions options = new ToolOptions(line, context);

        // Output file and output format
        Path outputFile = options.getPath(OUTPUT_FILE_OPTION)
                .orElse(null);
        String format = null;
        if (outputFile != null) {
            format = options.getValue(OUTPUT_FORMAT_OPTION)
                    .orElseThrow(() -> new ParseException("Missing required option: " + OUTPUT_FORMAT_OPTION));
        }
        // Network loading
        Network network = readNetwork(line, context);
        // ComputationManager
        ComputationManager computationManager = context.getShortTimeExecutionComputationManager();
        // ShortCircuit inputs (faults, parameters & faultParameters) loading
        ShortCircuitInput executionInput = readInput(line, context);
        // Execution
        ShortCircuitAnalysisResult shortCircuitAnalysisResult = ShortCircuitAnalysis.runAsync(network, executionInput.getFaults(), executionInput.getParameters(), computationManager, executionInput.getFaultParameters()).join();
        // Results
        if (shortCircuitAnalysisResult != null) {
            if (outputFile != null) {
                context.getOutputStream().println("Writing results to '" + outputFile + "'");
                ShortCircuitAnalysisResultExporters.export(shortCircuitAnalysisResult, outputFile, format, network);
            } else {
                Writer writer = new OutputStreamWriter(context.getOutputStream());
                ShortCircuitAnalysisResultExporters.export(shortCircuitAnalysisResult, writer, "ASCII", network);
            }
        } else {
            context.getErrorStream().println("Error. No results to be displayed!");
        }
    }
}
