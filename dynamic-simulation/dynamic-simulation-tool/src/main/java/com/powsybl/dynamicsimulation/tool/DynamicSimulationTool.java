/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.tool;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.table.*;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.dynamicsimulation.*;
import com.powsybl.dynamicsimulation.groovy.DynamicSimulationSupplierFactory;
import com.powsybl.dynamicsimulation.json.DynamicSimulationResultSerializer;
import com.powsybl.dynamicsimulation.json.JsonDynamicSimulationParameters;
import com.powsybl.iidm.network.ImportConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.tools.ConversionToolUtils;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
@AutoService(Tool.class)
public class DynamicSimulationTool implements Tool {

    private static final String CASE_FILE = "case-file";
    private static final String DYNAMIC_MODELS_FILE = "dynamic-models-file";
    private static final String EVENT_MODELS_FILE = "event-models-file";
    private static final String OUTPUT_VARIABLES_FILE = "output-variables-file";
    private static final String PARAMETERS_FILE = "parameters-file";
    private static final String OUTPUT_FILE = "output-file";
    private static final String OUTPUT_LOG_FILE = "output-log-file";

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "dynamic-simulation";
            }

            @Override
            public String getTheme() {
                return "Computation";
            }

            @Override
            public String getDescription() {
                return "Run dynamic simulation";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt(CASE_FILE)
                    .desc("the case path")
                    .hasArg()
                    .argName("FILE")
                    .required()
                    .build());
                options.addOption(Option.builder().longOpt(DYNAMIC_MODELS_FILE)
                    .desc("dynamic models description as a Groovy file: defines the dynamic models to be associated to chosen equipments of the network")
                    .hasArg()
                    .argName("FILE")
                    .required()
                    .build());
                options.addOption(Option.builder().longOpt(EVENT_MODELS_FILE)
                    .desc("dynamic event models description as a Groovy file: defines the dynamic event models to be associated to chosen equipments of the network")
                    .hasArg()
                    .argName("FILE")
                    .build());
                options.addOption(Option.builder().longOpt(OUTPUT_VARIABLES_FILE)
                    .desc("output variables description as Groovy file: defines a list of variables to plot or get the final value")
                    .hasArg()
                    .argName("FILE")
                    .build());
                options.addOption(Option.builder().longOpt(PARAMETERS_FILE)
                    .desc("dynamic simulation parameters as JSON file")
                    .hasArg()
                    .argName("FILE")
                    .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FILE)
                    .desc("dynamic simulation results output path")
                    .hasArg()
                    .argName("FILE")
                    .build());
                options.addOption(Option.builder().longOpt(OUTPUT_LOG_FILE)
                    .desc("dynamic simulation logs output path")
                    .hasArg()
                    .argName("FILE")
                    .build());
                options.addOption(ConversionToolUtils.createImportParametersFileOption());
                options.addOption(ConversionToolUtils.createImportParameterOption());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }

        };
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path caseFile = context.getFileSystem().getPath(line.getOptionValue(CASE_FILE));
        // process a single network: output-file/output-format options available

        context.getOutputStream().println("Loading network '" + caseFile + "'");
        Properties inputParams = ConversionToolUtils.readProperties(line, ConversionToolUtils.OptionType.IMPORT, context);
        Network network = Network.read(caseFile, context.getShortTimeExecutionComputationManager(), ImportConfig.load(), inputParams);
        if (network == null) {
            throw new PowsyblException("Case '" + caseFile + "' not found");
        }

        DynamicSimulation.Runner runner = DynamicSimulation.find();

        Path dydFile = context.getFileSystem().getPath(line.getOptionValue(DYNAMIC_MODELS_FILE));
        DynamicModelsSupplier dynamicModelsSupplier = DynamicSimulationSupplierFactory.createDynamicModelsSupplier(dydFile, runner.getName());

        EventModelsSupplier eventSupplier = EventModelsSupplier.empty();
        if (line.hasOption(EVENT_MODELS_FILE)) {
            Path eventFile = context.getFileSystem().getPath(line.getOptionValue(EVENT_MODELS_FILE));
            eventSupplier = DynamicSimulationSupplierFactory.createEventModelsSupplier(eventFile, runner.getName());
        }

        OutputVariablesSupplier outputVariablesSupplier = OutputVariablesSupplier.empty();
        if (line.hasOption(OUTPUT_VARIABLES_FILE)) {
            Path outputVariablesFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_VARIABLES_FILE));
            outputVariablesSupplier = DynamicSimulationSupplierFactory.createOutputVariablesSupplier(outputVariablesFile, runner.getName());
        }

        DynamicSimulationParameters params = DynamicSimulationParameters.load();
        if (line.hasOption(PARAMETERS_FILE)) {
            Path parametersFile = context.getFileSystem().getPath(line.getOptionValue(PARAMETERS_FILE));
            JsonDynamicSimulationParameters.update(params, parametersFile);
        }

        ReportNode reportNode = ReportNode.newRootReportNode().withMessageTemplate("dynamicSimulationTool", "Dynamic Simulation Tool").build();
        DynamicSimulationResult result = runner.run(network, dynamicModelsSupplier, eventSupplier, outputVariablesSupplier, VariantManagerConstants.INITIAL_VARIANT_ID, context.getShortTimeExecutionComputationManager(), params, reportNode);

        Path outputLogFile = line.hasOption(OUTPUT_LOG_FILE) ? context.getFileSystem().getPath(line.getOptionValue(OUTPUT_LOG_FILE)) : null;
        if (outputLogFile != null) {
            exportLog(reportNode, context, outputLogFile);
        } else {
            printLog(reportNode, context);
        }

        Path outputFile = line.hasOption(OUTPUT_FILE) ? context.getFileSystem().getPath(line.getOptionValue(OUTPUT_FILE)) : null;
        if (outputFile != null) {
            exportResult(result, context, outputFile);
        } else {
            printResult(result, context);
        }
    }

    private void printResult(DynamicSimulationResult result, ToolRunningContext context) {
        Writer writer = new OutputStreamWriter(context.getOutputStream());
        AsciiTableFormatterFactory asciiTableFormatterFactory = new AsciiTableFormatterFactory();
        printDynamicSimulationResult(result, writer, asciiTableFormatterFactory, TableFormatterConfig.load());
    }

    private void printLog(ReportNode reportNode, ToolRunningContext context) throws IOException {
        Writer writer = new OutputStreamWriter(context.getOutputStream());
        reportNode.print(writer);
        writer.flush();
    }

    private void printDynamicSimulationResult(DynamicSimulationResult result, Writer writer,
        TableFormatterFactory formatterFactory,
        TableFormatterConfig formatterConfig) {
        try (TableFormatter formatter = formatterFactory.create(writer,
            "dynamic simulation results",
            formatterConfig,
            new Column("Result"))) {
            formatter.writeCell(result.getStatus().name());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void exportResult(DynamicSimulationResult result, ToolRunningContext context, Path outputFile) {
        context.getOutputStream().println("Writing results to '" + outputFile + "'");
        DynamicSimulationResultSerializer.write(result, outputFile);
    }

    private void exportLog(ReportNode reportNode, ToolRunningContext context, Path outputLogFile) throws IOException {
        context.getOutputStream().println("Writing logs to '" + outputLogFile + "'");
        reportNode.print(outputLogFile);
    }
}
