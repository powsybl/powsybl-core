/*
 * Copyright (c) 2018-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyList;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.tools.ConversionToolUtils;
import com.powsybl.sensitivity.json.SensitivityJsonModule;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static com.powsybl.iidm.tools.ConversionToolUtils.*;

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
                        .desc("sensitivity values output path")
                        .hasArg()
                        .argName("FILE")
                        .required()
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

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path caseFile = context.getFileSystem().getPath(line.getOptionValue(CASE_FILE_OPTION));
        Path outputFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_FILE_OPTION));
        boolean csv = isCsv(outputFile);
        Path factorsFile = context.getFileSystem().getPath(line.getOptionValue(FACTORS_FILE_OPTION));

        context.getOutputStream().println("Loading network '" + caseFile + "'");
        Properties inputParams = readProperties(line, ConversionToolUtils.OptionType.IMPORT, context);
        Network network = Importers.loadNetwork(caseFile, context.getShortTimeExecutionComputationManager(), ImportConfig.load(), inputParams);
        if (network == null) {
            throw new PowsyblException("Case '" + caseFile + "' not found");
        }

        ObjectMapper objectMapper = JsonUtil.createObjectMapper()
                .registerModule(new SensitivityJsonModule());

        SensitivityAnalysisParameters params = SensitivityAnalysisParameters.load();

        if (line.hasOption(PARAMETERS_FILE)) {
            Path parametersFile = context.getFileSystem().getPath(line.getOptionValue(PARAMETERS_FILE));
            JsonUtil.readJsonAndUpdate(parametersFile, params, objectMapper);
        }

        List<Contingency> contingencies = line.hasOption(CONTINGENCIES_FILE_OPTION)
                ? ContingencyList.load(context.getFileSystem().getPath(line.getOptionValue(CONTINGENCIES_FILE_OPTION))).getContingencies(network)
                : Collections.emptyList();

        List<SensitivityVariableSet> variableSets;
        if (line.hasOption(VARIABLE_SETS_FILE_OPTION)) {
            try (Reader reader = Files.newBufferedReader(context.getFileSystem().getPath(line.getOptionValue(CONTINGENCIES_FILE_OPTION)), StandardCharsets.UTF_8)) {
                variableSets = objectMapper.readValue(reader, new TypeReference<>() {
                });
            }
        } else {
            variableSets = Collections.emptyList();
        }

        SensitivityFactorJsonReader factorsReader = new SensitivityFactorJsonReader(factorsFile);

        context.getOutputStream().println("Running analysis...");
        try (ComputationManager computationManager = DefaultComputationManagerConfig.load().createLongTimeExecutionComputationManager()) {
            if (csv) {
                try (Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8);
                     TableFormatter formatter = SensitivityValueCsvWriter.createTableFormatter(writer)) {
                    SensitivityValueWriter valuesWriter = new SensitivityValueCsvWriter(formatter, contingencies);
                    SensitivityAnalysis.run(network, network.getVariantManager().getWorkingVariantId(),
                            factorsReader, valuesWriter, contingencies, variableSets, params,
                            computationManager);
                }
            } else {
                JsonUtil.writeJson(outputFile, jsonGenerator -> {
                    try (SensitivityValueJsonWriter valuesWriter = new SensitivityValueJsonWriter(jsonGenerator)) {
                        SensitivityAnalysis.run(network, network.getVariantManager().getWorkingVariantId(),
                                factorsReader, valuesWriter, contingencies, variableSets, params,
                                computationManager);
                    }
                });
            }
        }
    }
}
