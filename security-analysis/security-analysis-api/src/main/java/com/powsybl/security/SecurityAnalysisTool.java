/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.google.auto.service.AutoService;
import com.google.common.io.Files;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.Partition;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.tools.ConversionToolUtils;
import com.powsybl.security.converter.SecurityAnalysisResultExporters;
import com.powsybl.security.execution.SecurityAnalysisExecution;
import com.powsybl.security.execution.SecurityAnalysisExecutionBuilder;
import com.powsybl.security.execution.SecurityAnalysisExecutionInput;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptors;
import com.powsybl.security.json.JsonSecurityAnalysisParameters;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolOptions;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.powsybl.iidm.tools.ConversionToolUtils.*;
import static com.powsybl.security.SecurityAnalysisToolConstants.*;
import static com.powsybl.tools.ToolConstants.TASK;
import static com.powsybl.tools.ToolConstants.TASK_COUNT;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
@AutoService(Tool.class)
public class SecurityAnalysisTool implements Tool {

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "security-analysis";
            }

            @Override
            public String getTheme() {
                return "Computation";
            }

            @Override
            public String getDescription() {
                return "Run security analysis";
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
                options.addOption(Option.builder().longOpt(PARAMETERS_FILE_OPTION)
                        .desc("loadflow parameters as JSON file")
                        .hasArg()
                        .argName("FILE")
                        .build());
                options.addOption(Option.builder().longOpt(LIMIT_TYPES_OPTION)
                        .desc("limit type filter (all if not set)")
                        .hasArg()
                        .argName("LIMIT-TYPES")
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FILE_OPTION)
                        .desc("the output path")
                        .hasArg()
                        .argName("FILE")
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FORMAT_OPTION)
                        .desc("the output format " + SecurityAnalysisResultExporters.getFormats())
                        .hasArg()
                        .argName("FORMAT")
                        .build());
                options.addOption(Option.builder().longOpt(CONTINGENCIES_FILE_OPTION)
                    .desc("the contingencies path")
                    .hasArg()
                    .argName("FILE")
                    .build());
                options.addOption(Option.builder().longOpt(WITH_EXTENSIONS_OPTION)
                        .desc("the extension list to enable")
                        .hasArg()
                        .argName("EXTENSIONS")
                        .build());
                options.addOption(Option.builder().longOpt(TASK_COUNT)
                        .desc("number of tasks used for parallelization")
                        .hasArg()
                        .argName("NTASKS")
                        .build());
                options.addOption(Option.builder().longOpt(TASK)
                        .desc("task identifier (task-index/task-count)")
                        .hasArg()
                        .argName("TASKID")
                        .build());
                options.addOption(Option.builder().longOpt(EXTERNAL)
                        .desc("external execution")
                        .build());
                options.addOption(createImportParametersFileOption());
                options.addOption(createImportParameterOption());
                options.addOption(Option.builder().longOpt(OUTPUT_LOG_OPTION)
                        .desc("log output path (.zip")
                        .hasArg()
                        .argName("FILE")
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return String.join(System.lineSeparator(),
                        "Allowed LIMIT-TYPES values are " + Arrays.toString(LimitViolationType.values()),
                        "Allowed EXTENSIONS values are " + SecurityAnalysisInterceptors.getExtensionNames()
                );
            }
        };
    }

    private SecurityAnalysisExecutionInput parseInputsFromOptions(ToolOptions options) {
        SecurityAnalysisExecutionInput inputs = new SecurityAnalysisExecutionInput();

        SecurityAnalysisParameters parameters = SecurityAnalysisParameters.load();
        options.getPath(PARAMETERS_FILE_OPTION)
                .ifPresent(f -> JsonSecurityAnalysisParameters.update(parameters, f));
        inputs.setParameters(parameters);

        options.getPath(CONTINGENCIES_FILE_OPTION)
                .map(p -> Files.asByteSource(p.toFile()))
                .ifPresent(inputs::setContingenciesSource);

        options.getValues(LIMIT_TYPES_OPTION)
                .map(types -> types.stream().map(LimitViolationType::valueOf).collect(Collectors.toList()))
                .ifPresent(inputs::addViolationTypes);

        options.getValues(WITH_EXTENSIONS_OPTION)
                .ifPresent(inputs::addResultExtensions);

        return inputs;
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        run(line, context, SecurityAnalysisExecutionBuilder.usingDefaultConfig());
    }

    public static void uncheckedWriteBytes(byte[] bytes, Path path) {
        try {
            java.nio.file.Files.write(path, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void run(CommandLine line, ToolRunningContext context,
             SecurityAnalysisExecutionBuilder executionBuilder) throws Exception {

        ToolOptions options = new ToolOptions(line, context);
        Path caseFile = options.getPath(CASE_FILE_OPTION)
                .orElseThrow(AssertionError::new);

        // Output file and output format
        Path outputFile = options.getPath(OUTPUT_FILE_OPTION)
                .orElse(null);
        String format = null;
        if (outputFile != null) {
            format = options.getValue(OUTPUT_FORMAT_OPTION)
                            .orElseThrow(() -> new ParseException("Missing required option: " + OUTPUT_FORMAT_OPTION));
        }

        context.getOutputStream().println("Loading network '" + caseFile + "'");
        Properties inputParams = readProperties(line, ConversionToolUtils.OptionType.IMPORT, context);
        Network network = Importers.loadNetwork(caseFile, context.getShortTimeExecutionComputationManager(), ImportConfig.load(), inputParams);
        network.getVariantManager().allowVariantMultiThreadAccess(true);

        SecurityAnalysisExecutionInput executionInput = parseInputsFromOptions(options);
        executionInput.setNetworkVariant(network, VariantManagerConstants.INITIAL_VARIANT_ID);

        // Computation execution options
        boolean forward = options.hasOption(EXTERNAL);
        Integer taskCount = options.getInt(TASK_COUNT).orElse(null);
        Partition subPart = options.getValue(TASK, Partition::parse).orElse(null);

        SecurityAnalysisExecution execution = executionBuilder.forward(forward)
                .distributed(taskCount)
                .subTask(subPart)
                .build();

        ComputationManager computationManager = subPart != null ?
                context.getLongTimeExecutionComputationManager() : context.getShortTimeExecutionComputationManager();

        Path logPath = options.getPath(OUTPUT_LOG_OPTION).orElse(null);
        SecurityAnalysisResult result;
        if (logPath != null) {
            SecurityAnalysisResultWithLog resultWithLog = execution.executeWithLog(computationManager, executionInput).join();
            result = resultWithLog.getResult();
            // copy log bytes to file
            resultWithLog.getLogBytes()
                    .ifPresent(logBytes -> uncheckedWriteBytes(logBytes, logPath));
        } else {
            result = execution.execute(computationManager, executionInput).join();
        }

        if (!result.getPreContingencyResult().isComputationOk()) {
            context.getErrorStream().println("Pre-contingency state divergence");
        }

        if (outputFile != null) {
            context.getOutputStream().println("Writing results to '" + outputFile + "'");
            SecurityAnalysisResultExporters.export(result, outputFile, format);
        } else {
            // To avoid the closing of System.out
            Writer writer = new OutputStreamWriter(context.getOutputStream());
            Security.print(result, network, writer, new AsciiTableFormatterFactory(), TableFormatterConfig.load());
        }
    }
}
