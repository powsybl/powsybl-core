/*
 * Copyright (c) 2018-2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.contingency.*;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.tools.ConversionToolUtils;
import com.powsybl.sensitivity.converter.SensitivityAnalysisResultExporters;
import com.powsybl.sensitivity.json.JsonSensitivityAnalysisParameters;
import com.powsybl.sensitivity.converter.CsvSensitivityAnalysisResultExporter;
import com.powsybl.sensitivity.json.SensitivityFactorsJsonSerializer;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.*;
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
    private static final String OUTPUT_FORMAT_OPTION = "output-format";
    private static final String FACTORS_FILE_OPTION = "factors-file";
    private static final String CONTINGENCIES_FILE_OPTION = "contingencies-file";
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
                options.addOption(Option.builder().longOpt(OUTPUT_FILE_OPTION)
                        .desc("sensitivity analysis results output path")
                        .hasArg()
                        .argName("FILE")
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FORMAT_OPTION)
                        .desc("the output format " + SensitivityAnalysisResultExporters.getFormats())
                        .hasArg()
                        .argName("FORMAT")
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

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        Path caseFile = context.getFileSystem().getPath(line.getOptionValue(CASE_FILE_OPTION));
        Path outputFile = null;
        String format = null;

        // process a single network: output-file/output-format options available
        if (line.hasOption(OUTPUT_FILE_OPTION)) {
            outputFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_FILE_OPTION));
            if (!line.hasOption(OUTPUT_FORMAT_OPTION)) {
                throw new ParseException("Missing required option: " + OUTPUT_FORMAT_OPTION);
            }
            format = line.getOptionValue(OUTPUT_FORMAT_OPTION);
        }

        Path sensitivityFactorsFile = context.getFileSystem().getPath(line.getOptionValue(FACTORS_FILE_OPTION));

        context.getOutputStream().println("Loading network '" + caseFile + "'");
        Properties inputParams = readProperties(line, ConversionToolUtils.OptionType.IMPORT, context);
        Network network = Importers.loadNetwork(caseFile, context.getShortTimeExecutionComputationManager(), ImportConfig.load(), inputParams);
        if (network == null) {
            throw new PowsyblException("Case '" + caseFile + "' not found");
        }

        SensitivityAnalysisParameters params = SensitivityAnalysisParameters.load();

        if (line.hasOption(PARAMETERS_FILE)) {
            Path parametersFile = context.getFileSystem().getPath(line.getOptionValue(PARAMETERS_FILE));
            JsonSensitivityAnalysisParameters.update(params, parametersFile);
        }

        List<SensitivityFactor> sensitivityFactors;
        try (Reader reader = Files.newBufferedReader(sensitivityFactorsFile, StandardCharsets.UTF_8)) {
            sensitivityFactors = SensitivityFactorsJsonSerializer.read(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        List<Contingency> contingencies = Collections.emptyList();
        if (line.hasOption(CONTINGENCIES_FILE_OPTION)) {
            contingencies = ContingencyList.load(context.getFileSystem().getPath(line.getOptionValue(CONTINGENCIES_FILE_OPTION))).getContingencies(network);
        }
        List<SensitivityVariableSet> variableSets = Collections.emptyList();
        // FIXME : how to fill variableSets
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, network.getVariantManager().getWorkingVariantId(),
                sensitivityFactors, contingencies, variableSets, params,
                DefaultComputationManagerConfig.load().createLongTimeExecutionComputationManager());

        if (outputFile != null) {
            context.getOutputStream().println("Writing results to '" + outputFile + "'");
            SensitivityAnalysisResultExporters.export(result, outputFile, format);
        } else {
            // To avoid the closing of System.out
            Writer writer = new OutputStreamWriter(context.getOutputStream());
            new CsvSensitivityAnalysisResultExporter().export(result, writer);
        }
    }
}
