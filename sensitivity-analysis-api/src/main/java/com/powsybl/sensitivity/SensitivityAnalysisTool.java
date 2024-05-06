/*
 * Copyright (c) 2018-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.google.common.base.Stopwatch;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.contingency.json.ContingencyJsonModule;
import com.powsybl.iidm.network.ImportConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tools.ConversionToolUtils;
import com.powsybl.sensitivity.json.JsonSensitivityAnalysisParameters;
import com.powsybl.sensitivity.json.SensitivityJsonModule;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.powsybl.iidm.network.tools.ConversionToolUtils.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@AutoService(Tool.class)
public class SensitivityAnalysisTool implements Tool {

    private static final String CASE_FILE_OPTION = "case-file";
    private static final String OUTPUT_FILE_OPTION = "output-file";
    private static final String FACTORS_FILE_OPTION = "factors-file";
    private static final String CONTINGENCIES_FILE_OPTION = "contingencies-file";
    private static final String VARIABLE_SETS_FILE_OPTION = "variable-sets-file";
    private static final String PARAMETERS_FILE = "parameters-file";
    private static final String OUTPUT_CONTINGENCY_STATUS_FILE_OPTION = "output-contingency-file";
    private static final String SINGLE_OUTPUT = "single-output";

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "sensitivity-analysis";
            }

            @Override
            public String getTheme() {
                return "Computation";
            }

            @Override
            public String getDescription() {
                return "Run sensitivity analysis";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt(CASE_FILE_OPTION)
                    .desc("the case path")
                    .hasArg()
                    .argName("FILE")
                    .required()
                    .build());
                options.addOption(Option.builder().longOpt(FACTORS_FILE_OPTION)
                    .desc("sensitivity factors input file path")
                    .hasArg()
                    .argName("FILE")
                    .required()
                    .build());
                options.addOption(Option.builder().longOpt(CONTINGENCIES_FILE_OPTION)
                    .desc("contingencies input file path")
                    .hasArg()
                    .argName("FILE")
                    .build());
                options.addOption(Option.builder().longOpt(VARIABLE_SETS_FILE_OPTION)
                    .desc("variable sets input file path")
                    .hasArg()
                    .argName("FILE")
                    .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FILE_OPTION)
                    .desc("Sensitivity results output path")
                    .hasArg()
                    .argName("FILE")
                    .required()
                    .build());
                options.addOption(Option.builder().longOpt(OUTPUT_CONTINGENCY_STATUS_FILE_OPTION)
                    .desc("contingency status output path (csv only)")
                    .hasArg()
                    .argName("FILE")
                    .build());
                options.addOption(Option.builder().longOpt(SINGLE_OUTPUT)
                    .desc("Output sensitivity analysis results in a single json file using output file option (values, factors and contingency status).")
                    .build());
                options.addOption(Option.builder().longOpt(PARAMETERS_FILE)
                    .desc("sensitivity analysis parameters as JSON file")
                    .hasArg()
                    .argName("FILE")
                    .build());
                options.addOption(createImportParametersFileOption());
                options.addOption(createImportParameterOption());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    private static boolean isCsv(Path outputFile) {
        String fileName = outputFile.getFileName().toString();
        if (fileName.endsWith(".json")) {
            return false;
        } else if (fileName.endsWith(".csv")) {
            return true;
        } else {
            throw new PowsyblException("Unsupported output format: " + fileName);
        }
    }

    private static String buildContingencyStatusPath(String outputFile) {
        return outputFile.replace(".csv", "_contingency_status.csv");
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path caseFile = context.getFileSystem().getPath(line.getOptionValue(CASE_FILE_OPTION));
        Path outputFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_FILE_OPTION));
        boolean csv = isCsv(outputFile);
        Path outputFileStatus = null;

        if (csv) {
            if (line.hasOption(OUTPUT_CONTINGENCY_STATUS_FILE_OPTION)) {
                outputFileStatus = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_CONTINGENCY_STATUS_FILE_OPTION));
            } else {
                outputFileStatus = context.getFileSystem().getPath(buildContingencyStatusPath(line.getOptionValue(OUTPUT_FILE_OPTION)));
            }
            boolean contingencyCsv = isCsv(outputFileStatus);
            if (!contingencyCsv) {
                throw new PowsyblException(OUTPUT_FILE_OPTION + " and " + OUTPUT_CONTINGENCY_STATUS_FILE_OPTION + " files must have the same format (csv).");
            }

            if (line.hasOption(SINGLE_OUTPUT)) {
                throw new PowsyblException("Unsupported " + SINGLE_OUTPUT + " option does not support csv file as argument of " + OUTPUT_FILE_OPTION + ". Must be json.");
            }
        }

        Path factorsFile = context.getFileSystem().getPath(line.getOptionValue(FACTORS_FILE_OPTION));

        context.getOutputStream().println("Loading network '" + caseFile + "'");
        Properties inputParams = readProperties(line, ConversionToolUtils.OptionType.IMPORT, context);
        Network network = Network.read(caseFile, context.getShortTimeExecutionComputationManager(), ImportConfig.load(), inputParams);
        if (network == null) {
            throw new PowsyblException("Case '" + caseFile + "' not found");
        }

        ObjectMapper objectMapper = JsonSensitivityAnalysisParameters.createObjectMapper()
            .registerModule(new ContingencyJsonModule());

        SensitivityAnalysisParameters params = SensitivityAnalysisParameters.load();

        if (line.hasOption(PARAMETERS_FILE)) {
            Path parametersFile = context.getFileSystem().getPath(line.getOptionValue(PARAMETERS_FILE));
            JsonUtil.readJsonAndUpdate(parametersFile, params, objectMapper);
        }

        List<Contingency> contingencies = line.hasOption(CONTINGENCIES_FILE_OPTION)
            ? ContingencyList.load(context.getFileSystem().getPath(line.getOptionValue(CONTINGENCIES_FILE_OPTION))).getContingencies(network)
            : Collections.emptyList();

        List<SensitivityVariableSet> variableSets = Collections.emptyList();
        if (line.hasOption(VARIABLE_SETS_FILE_OPTION)) {
            try (Reader reader = Files.newBufferedReader(context.getFileSystem().getPath(line.getOptionValue(VARIABLE_SETS_FILE_OPTION)), StandardCharsets.UTF_8)) {
                variableSets = objectMapper.readValue(reader, new TypeReference<>() {
                });
            }
        }

        SensitivityFactorJsonReader factorsReader = new SensitivityFactorJsonReader(factorsFile);

        context.getOutputStream().println("Running analysis...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        try (ComputationManager computationManager = DefaultComputationManagerConfig.load().createLongTimeExecutionComputationManager()) {
            SensitivityAnalysisParametersRecord parametersRecord = new SensitivityAnalysisParametersRecord(factorsReader, params, network, contingencies,
                variableSets, computationManager, outputFile, outputFileStatus, csv);
            run(line, parametersRecord);
        }
        context.getOutputStream().println("Analysis done in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
    }

    private record SensitivityAnalysisParametersRecord(SensitivityFactorJsonReader factorsReader,
                                                       SensitivityAnalysisParameters params,
                                                       Network network,
                                                       List<Contingency> contingencies,
                                                       List<SensitivityVariableSet> variableSets,
                                                       ComputationManager computationManager,
                                                       Path outputFile,
                                                       Path outputFileStatus,
                                                       boolean csv) {
    }

    private void run(CommandLine line, SensitivityAnalysisParametersRecord parametersRecord) {
        if (line.hasOption(SINGLE_OUTPUT)) {
            if (parametersRecord.csv) {
                throw new PowsyblException("Unsupported " + SINGLE_OUTPUT + " option does not support csv file as argument of " + OUTPUT_FILE_OPTION + ". Must be json.");
            }
            List<SensitivityFactor> factors = new ArrayList<>();
            parametersRecord.factorsReader.read((functionType, functionId, variableType, variableId, variableSet, contingencyContext) ->
                factors.add(new SensitivityFactor(functionType, functionId, variableType, variableId, variableSet, contingencyContext)));
            SensitivityAnalysisResult result = SensitivityAnalysis.run(parametersRecord.network, parametersRecord.network.getVariantManager().getWorkingVariantId(),
                factors, parametersRecord.contingencies, parametersRecord.variableSets, parametersRecord.params,
                parametersRecord.computationManager, ReportNode.NO_OP);
            ObjectMapper sensiObjectMapper = JsonUtil.createObjectMapper().registerModule(new SensitivityJsonModule());
            JsonUtil.writeJson(parametersRecord.outputFile, result, sensiObjectMapper);
        } else {
            if (parametersRecord.csv) {
                try (Writer writer = Files.newBufferedWriter(parametersRecord.outputFile, StandardCharsets.UTF_8);
                     Writer writerStatuses = Files.newBufferedWriter(parametersRecord.outputFileStatus, StandardCharsets.UTF_8);
                     TableFormatter formatter = SensitivityResultCsvWriter.createTableFormatter(writer);
                     TableFormatter formatterStatus = SensitivityResultCsvWriter.createContingencyStatusTableFormatter(writerStatuses)) {
                    SensitivityResultWriter valuesWriter = new SensitivityResultCsvWriter(formatter, formatterStatus, parametersRecord.contingencies);
                    SensitivityAnalysis.run(parametersRecord.network, parametersRecord.network.getVariantManager().getWorkingVariantId(),
                        parametersRecord.factorsReader, valuesWriter, parametersRecord.contingencies, parametersRecord.variableSets, parametersRecord.params,
                        parametersRecord.computationManager, ReportNode.NO_OP);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            } else {
                JsonFactory factory = JsonUtil.createJsonFactory();
                try (BufferedWriter writer = Files.newBufferedWriter(parametersRecord.outputFile, StandardCharsets.UTF_8);
                     JsonGenerator generator = factory.createGenerator(writer);
                     SensitivityResultJsonWriter valuesWriter = new SensitivityResultJsonWriter(generator, parametersRecord.contingencies)) {
                    generator.useDefaultPrettyPrinter();
                    SensitivityAnalysis.run(parametersRecord.network, parametersRecord.network.getVariantManager().getWorkingVariantId(),
                        parametersRecord.factorsReader, valuesWriter, parametersRecord.contingencies, parametersRecord.variableSets, parametersRecord.params,
                        parametersRecord.computationManager, ReportNode.NO_OP);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }
}
