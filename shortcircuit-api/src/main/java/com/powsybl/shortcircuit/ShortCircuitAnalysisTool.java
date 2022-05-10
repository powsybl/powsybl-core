/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.shortcircuit.converter.ShortCircuitAnalysisResultExporters;
import com.powsybl.shortcircuit.json.JsonFaultList;
import com.powsybl.shortcircuit.json.JsonShortCircuitParameters;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Boubakeur Brahimi
 */
@AutoService(Tool.class)
public class ShortCircuitAnalysisTool implements Tool {

    private static final String MISSING_REQUIRED_OPTION = "Missing required option: ";
    private static final String FILE_NOT_FOUND = "File %s does not exist or is not a regular file";

    private static final String INPUT_FILE_OPTION = "input-file";
    private static final String CASE_FILE_OPTION = "case-file";
    private static final String OUTPUT_FORMAT_OPTION = "output-format";
    private static final String OUTPUT_FILE_OPTION = "output-file";
    private static final String PARAMETERS_FILE = "parameters-file";

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

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        if (!line.hasOption(INPUT_FILE_OPTION)) {
            throw new ParseException(MISSING_REQUIRED_OPTION + INPUT_FILE_OPTION);
        }
        Path inputFile = context.getFileSystem().getPath(line.getOptionValue(INPUT_FILE_OPTION));
        if (!Files.exists(inputFile)) {
            throw new PowsyblException(String.format(FILE_NOT_FOUND, inputFile));
        }

        if (!line.hasOption(CASE_FILE_OPTION)) {
            throw new ParseException(MISSING_REQUIRED_OPTION + CASE_FILE_OPTION);
        }
        Path caseFile = context.getFileSystem().getPath(line.getOptionValue(CASE_FILE_OPTION));
        if (!Files.exists(caseFile)) {
            throw new PowsyblException(String.format(FILE_NOT_FOUND, caseFile));
        }

        Path outputFile = null;
        String format = null;
        if (line.hasOption(OUTPUT_FILE_OPTION)) {
            outputFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_FILE_OPTION));
            if (!Files.exists(outputFile)) {
                throw new PowsyblException(String.format(FILE_NOT_FOUND, outputFile));
            }
            if (!line.hasOption(OUTPUT_FORMAT_OPTION)) {
                throw new ParseException(MISSING_REQUIRED_OPTION + OUTPUT_FORMAT_OPTION);
            }
            format = line.getOptionValue(OUTPUT_FORMAT_OPTION);
        }

        context.getOutputStream().println("Loading fault list '" + inputFile + "'");
        List<Fault> faults = JsonFaultList.read(inputFile);
        if (faults.isEmpty()) {
            throw new PowsyblException("File '" + inputFile + "' is empty");
        }

        context.getOutputStream().println("Loading network '" + caseFile + "'");
        Network network = Importers.loadNetwork(caseFile);
        if (network == null) {
            throw new PowsyblException("Fail to load network from file '" + caseFile + "'");
        }
        ComputationManager computationManager = context.getShortTimeExecutionComputationManager();

        ShortCircuitParameters parameters = ShortCircuitParameters.load();
        if (line.hasOption(PARAMETERS_FILE)) {
            Path parametersFile = context.getFileSystem().getPath(line.getOptionValue(PARAMETERS_FILE));
            JsonShortCircuitParameters.update(parameters, parametersFile);
        }

        ShortCircuitAnalysisResult shortCircuitAnalysisResult = ShortCircuitAnalysis.runAsync(network, faults, parameters, computationManager).join();

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
