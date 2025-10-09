/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.tools;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.table.*;
import com.powsybl.iidm.network.Exporter;
import com.powsybl.iidm.network.ImportConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tools.ConversionToolUtils;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.loadflow.json.JsonLoadFlowParameters;
import com.powsybl.loadflow.json.LoadFlowResultSerializer;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.powsybl.iidm.network.tools.ConversionToolUtils.*;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.it>}
 */
@AutoService(Tool.class)
public class RunLoadFlowTool implements Tool {

    private static final String CASE_FILE = "case-file";
    private static final String PARAMETERS_FILE = "parameters-file";
    private static final String OUTPUT_FILE = "output-file";
    private static final String OUTPUT_FORMAT = "output-format";
    private static final String OUTPUT_CASE_FORMAT = "output-case-format";
    private static final String OUTPUT_CASE_FILE = "output-case-file";

    private enum Format {
        CSV,
        JSON
    }

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "loadflow";
            }

            @Override
            public String getTheme() {
                return "Computation";
            }

            @Override
            public String getDescription() {
                return "Run loadflow";
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
                options.addOption(Option.builder().longOpt(PARAMETERS_FILE)
                        .desc("loadflow parameters as JSON file")
                        .hasArg()
                        .argName("FILE")
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FILE)
                        .desc("loadflow results output path")
                        .hasArg()
                        .argName("FILE")
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FORMAT)
                        .desc("loadflow results output format " + Arrays.toString(Format.values()))
                        .hasArg()
                        .argName("FORMAT")
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_CASE_FORMAT)
                        .desc("modified network output format " + Exporter.getFormats())
                        .hasArg()
                        .argName("CASEFORMAT")
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_CASE_FILE)
                        .desc("modified network base name")
                        .hasArg()
                        .argName("FILE")
                        .build());
                options.addOption(createImportParametersFileOption());
                options.addOption(createImportParameterOption());
                options.addOption(createExportParametersFileOption());
                options.addOption(createExportParameterOption());
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
        Path outputFile = null;
        Format format = null;
        Path outputCaseFile = null;

        // process a single network: output-file/output-format options available
        if (line.hasOption(OUTPUT_FILE)) {
            outputFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_FILE));
            if (!line.hasOption(OUTPUT_FORMAT)) {
                throw new ParseException("Missing required option: " + OUTPUT_FORMAT);
            }
            format = Format.valueOf(line.getOptionValue(OUTPUT_FORMAT));
        }

        if (line.hasOption(OUTPUT_CASE_FILE)) {
            outputCaseFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_CASE_FILE));
            if (!line.hasOption(OUTPUT_CASE_FORMAT)) {
                throw new ParseException("Missing required option: " + OUTPUT_CASE_FORMAT);
            }
        }

        context.getOutputStream().println("Loading network '" + caseFile + "'");
        Properties inputParams = readProperties(line, ConversionToolUtils.OptionType.IMPORT, context);
        Network network = Network.read(caseFile, context.getShortTimeExecutionComputationManager(), ImportConfig.load(), inputParams);
        if (network == null) {
            throw new PowsyblException("Case '" + caseFile + "' not found");
        }

        LoadFlowParameters params = LoadFlowParameters.load();
        if (line.hasOption(PARAMETERS_FILE)) {
            Path parametersFile = context.getFileSystem().getPath(line.getOptionValue(PARAMETERS_FILE));
            JsonLoadFlowParameters.update(params, parametersFile);
        }

        LoadFlowResult result = LoadFlow.run(network, context.getShortTimeExecutionComputationManager(), params);

        if (outputFile != null) {
            exportResult(result, context, outputFile, format);
        } else {
            printResult(result, context);
        }

        // exports the modified network to the filesystem, if requested
        if (outputCaseFile != null) {
            String outputCaseFormat = line.getOptionValue(OUTPUT_CASE_FORMAT);
            Properties outputParams = readProperties(line, ConversionToolUtils.OptionType.EXPORT, context);
            network.write(outputCaseFormat, outputParams, outputCaseFile);
        }
    }

    private void printLoadFlowResult(LoadFlowResult result, Path outputFile, TableFormatterFactory formatterFactory,
                                     TableFormatterConfig formatterConfig) {
        try (Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            printLoadFlowResult(result, writer, formatterFactory, formatterConfig);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void printLoadFlowResult(LoadFlowResult result, Writer writer, TableFormatterFactory formatterFactory,
                                    TableFormatterConfig formatterConfig) {
        try (TableFormatter formatter = formatterFactory.create(writer,
                "Loadflow results",
                formatterConfig,
                new Column("Ok"),
                new Column("Status"),
                new Column("Metrics"))) {
            formatter.writeCell(result.isOk());
            formatter.writeCell(result.getStatus().name());
            formatter.writeCell(result.getMetrics().toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        final String csvSeparator = String.valueOf(formatterConfig.getCsvSeparator());
        if (!result.getComponentResults().isEmpty()) {
            try (TableFormatter formatter = formatterFactory.create(writer,
                    "Components results",
                    formatterConfig,
                    new Column("Connected component"),
                    new Column("Synchronous component"),
                    new Column("Status"),
                    new Column("Status text"),
                    new Column("Metrics"),
                    new Column("Iteration count"),
                    new Column("Slack bus ID"),
                    new Column("Slack bus mismatch (MW)"),
                    new Column("Distributed Active Power (MW)"))) {
                for (LoadFlowResult.ComponentResult componentResult : result.getComponentResults()) {
                    formatter.writeCell(componentResult.getConnectedComponentNum());
                    formatter.writeCell(componentResult.getSynchronousComponentNum());
                    formatter.writeCell(componentResult.getStatus().name());
                    formatter.writeCell(componentResult.getStatusText());
                    formatter.writeCell(componentResult.getMetrics().toString());
                    formatter.writeCell(componentResult.getIterationCount());
                    formatter.writeCell(componentResult.getSlackBusResults().stream()
                            .map(LoadFlowResult.SlackBusResult::getId)
                            .collect(Collectors.joining(csvSeparator)));
                    formatter.writeCell(componentResult.getSlackBusResults().stream()
                            .map(sbr -> String.format(formatterConfig.getLocale(), "%.2f", sbr.getActivePowerMismatch()))
                            .collect(Collectors.joining(csvSeparator)));
                    formatter.writeCell(componentResult.getDistributedActivePower());
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void printResult(LoadFlowResult result, ToolRunningContext context) {
        Writer writer = new OutputStreamWriter(context.getOutputStream());

        AsciiTableFormatterFactory asciiTableFormatterFactory = new AsciiTableFormatterFactory();
        printLoadFlowResult(result, writer, asciiTableFormatterFactory, TableFormatterConfig.load());
    }

    private void exportResult(LoadFlowResult result, ToolRunningContext context, Path outputFile, Format format) {
        context.getOutputStream().println("Writing results to '" + outputFile + "'");
        switch (format) {
            case CSV:
                CsvTableFormatterFactory csvTableFormatterFactory = new CsvTableFormatterFactory();
                printLoadFlowResult(result, outputFile, csvTableFormatterFactory, TableFormatterConfig.load());
                break;

            case JSON:
                LoadFlowResultSerializer.write(result, outputFile);
                break;
        }
    }
}
