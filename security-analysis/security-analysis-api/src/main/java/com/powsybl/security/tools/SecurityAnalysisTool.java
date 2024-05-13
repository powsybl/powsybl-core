/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.tools;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.io.FileUtil;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.computation.ComputationException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.Partition;
import com.powsybl.contingency.ContingenciesProviders;
import com.powsybl.iidm.network.ImportConfig;
import com.powsybl.iidm.network.ImportersLoader;
import com.powsybl.iidm.network.ImportersServiceLoader;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.tools.ConversionToolUtils;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.*;
import com.powsybl.action.ActionList;
import com.powsybl.security.converter.SecurityAnalysisResultExporters;
import com.powsybl.security.distributed.ExternalSecurityAnalysisConfig;
import com.powsybl.security.execution.SecurityAnalysisExecution;
import com.powsybl.security.execution.SecurityAnalysisExecutionBuilder;
import com.powsybl.security.execution.SecurityAnalysisExecutionInput;
import com.powsybl.security.execution.SecurityAnalysisInputBuildStrategy;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptors;
import com.powsybl.security.json.JsonSecurityAnalysisParameters;
import com.powsybl.security.json.limitreduction.LimitReductionListSerDeUtil;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.strategy.OperatorStrategyList;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessorFactory;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessors;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolOptions;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.powsybl.iidm.network.tools.ConversionToolUtils.*;
import static com.powsybl.security.tools.SecurityAnalysisToolConstants.*;
import static com.powsybl.tools.ToolConstants.TASK;
import static com.powsybl.tools.ToolConstants.TASK_COUNT;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
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
                    .desc("log output path (.zip)")
                    .hasArg()
                    .argName("FILE")
                    .build());
                options.addOption(Option.builder().longOpt(MONITORING_FILE)
                    .desc("monitoring file (.json) to get network's infos after computation")
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

    static void updateInput(ToolOptions options, SecurityAnalysisExecutionInput inputs) {
        options.getPath(PARAMETERS_FILE_OPTION)
            .ifPresent(f -> JsonSecurityAnalysisParameters.update(inputs.getParameters(), f));

        options.getPath(CONTINGENCIES_FILE_OPTION)
            .map(FileUtil::asByteSource)
            .ifPresent(inputs::setContingenciesSource);

        options.getValues(LIMIT_TYPES_OPTION)
            .map(types -> types.stream().map(LimitViolationType::valueOf).collect(Collectors.toList()))
            .ifPresent(inputs::addViolationTypes);

        options.getValues(WITH_EXTENSIONS_OPTION)
            .ifPresent(inputs::addResultExtensions);

        options.getValues(OUTPUT_LOG_OPTION)
            .ifPresent(f -> inputs.setWithLogs(true));
    }

    private static SecurityAnalysisInputBuildStrategy configBasedInputBuildStrategy(PlatformConfig config) {
        return preprocessedInputBuildStrategy(() -> LimitViolationFilter.load(config),
            SecurityAnalysisPreprocessors.configuredFactory(config)
                .orElseGet(() -> SecurityAnalysisPreprocessors.wrap(ContingenciesProviders.newDefaultFactory(config))));
    }

    private static SecurityAnalysisInputBuildStrategy preprocessedInputBuildStrategy(Supplier<LimitViolationFilter> filterInitializer,
                                                                                     SecurityAnalysisPreprocessorFactory preprocessorFactory) {
        return executionInput -> buildPreprocessedInput(executionInput, filterInitializer, preprocessorFactory);
    }

    static SecurityAnalysisInput buildPreprocessedInput(SecurityAnalysisExecutionInput executionInput,
                                                        Supplier<LimitViolationFilter> filterInitializer,
                                                        SecurityAnalysisPreprocessorFactory preprocessorFactory) {

        SecurityAnalysisInput input = new SecurityAnalysisInput(executionInput.getNetworkVariant())
            .setParameters(executionInput.getParameters())
            .setFilter(filterInitializer.get());

        executionInput.getResultExtensions().stream()
            .map(SecurityAnalysisInterceptors::createInterceptor)
            .forEach(input::addInterceptor);

        if (!executionInput.getViolationTypes().isEmpty()) {
            input.getFilter().setViolationTypes(ImmutableSet.copyOf(executionInput.getViolationTypes()));
        }

        executionInput.getContingenciesSource()
            .map(preprocessorFactory::newPreprocessor)
            .ifPresent(p -> p.preprocess(input));

        return input;
    }

    private static SecurityAnalysisExecutionBuilder createBuilder(PlatformConfig platformConfig) {
        String providerName = platformConfig.getOptionalModuleConfig(MODULE_CONFIG_NAME_PROPERTY)
                .flatMap(c -> c.getOptionalStringProperty(DEFAULT_SERVICE_IMPL_NAME_PROPERTY))
                .orElse(null);
        return new SecurityAnalysisExecutionBuilder(() -> ExternalSecurityAnalysisConfig.load(platformConfig),
                providerName, configBasedInputBuildStrategy(platformConfig));
    }

    private static SecurityAnalysisExecution buildExecution(ToolOptions options, SecurityAnalysisExecutionBuilder builder) {
        builder.forward(options.hasOption(EXTERNAL));
        options.getInt(TASK_COUNT).ifPresent(builder::distributed);
        options.getValue(TASK, Partition::parse).ifPresent(builder::subTask);
        return builder.build();
    }

    private static SecurityAnalysisReport runSecurityAnalysisWithLog(ComputationManager computationManager,
                                                                     SecurityAnalysisExecution execution,
                                                                     SecurityAnalysisExecutionInput input,
                                                                     Path logPath) {
        try {
            SecurityAnalysisReport report = execution.execute(computationManager, input).join();
            // copy log bytes to file
            report.getLogBytes()
                .ifPresent(logBytes -> uncheckedWriteBytes(logBytes, logPath));
            return report;
        } catch (CompletionException e) {
            if (e.getCause() instanceof ComputationException computationException) {
                byte[] bytes = computationException.toZipBytes();
                uncheckedWriteBytes(bytes, logPath);
            }
            throw e;
        }
    }

    static Network readNetwork(CommandLine line, ToolRunningContext context, ImportersLoader importersLoader) throws IOException {
        ToolOptions options = new ToolOptions(line, context);
        Path caseFile = options.getPath(CASE_FILE_OPTION)
            .orElseThrow(IllegalStateException::new);
        Properties inputParams = readProperties(line, ConversionToolUtils.OptionType.IMPORT, context);
        context.getOutputStream().println("Loading network '" + caseFile + "'");
        Network network = Network.read(caseFile, context.getShortTimeExecutionComputationManager(), ImportConfig.load(), inputParams, importersLoader);
        network.getVariantManager().allowVariantMultiThreadAccess(true);
        return network;
    }

    private static void uncheckedWriteBytes(byte[] bytes, Path path) {
        try {
            Files.write(path, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        run(line, context,
            createBuilder(PlatformConfig.defaultConfig()),
            SecurityAnalysisParameters::load,
            new ImportersServiceLoader(),
            TableFormatterConfig::load);
    }

    void run(CommandLine line, ToolRunningContext context,
             SecurityAnalysisExecutionBuilder executionBuilder,
             Supplier<SecurityAnalysisParameters> parametersLoader,
             ImportersLoader importersLoader,
             Supplier<TableFormatterConfig> tableFormatterConfigLoader) throws Exception {
        ToolOptions options = new ToolOptions(line, context);

        // Output file and output format
        Path outputFile = options.getPath(OUTPUT_FILE_OPTION)
            .orElse(null);
        String format = null;
        if (outputFile != null) {
            format = options.getValue(OUTPUT_FORMAT_OPTION)
                .orElseThrow(() -> new ParseException("Missing required option: " + OUTPUT_FORMAT_OPTION));
        }

        Network network = readNetwork(line, context, importersLoader);

        SecurityAnalysisExecutionInput executionInput = new SecurityAnalysisExecutionInput()
            .setNetworkVariant(network, VariantManagerConstants.INITIAL_VARIANT_ID)
            .setParameters(parametersLoader.get());

        options.getPath(MONITORING_FILE).ifPresent(monitorFilePath -> executionInput.setMonitors(StateMonitor.read(monitorFilePath)));
        options.getPath(STRATEGIES_FILE).ifPresent(operatorStrategyFilePath -> executionInput.setOperatorStrategies(OperatorStrategyList.read(operatorStrategyFilePath).getOperatorStrategies()));
        options.getPath(ACTIONS_FILE).ifPresent(actionFilePath -> executionInput.setActions(ActionList.readJsonFile(actionFilePath).getActions()));
        options.getPath(LIMIT_REDUCTIONS_FILE).ifPresent(limitReductionsFilePath -> executionInput.setLimitReductions(LimitReductionListSerDeUtil.read(limitReductionsFilePath).getLimitReductions()));

        updateInput(options, executionInput);

        SecurityAnalysisExecution execution = buildExecution(options, executionBuilder);

        ComputationManager computationManager = options.hasOption(TASK) ? context.getShortTimeExecutionComputationManager() :
            context.getLongTimeExecutionComputationManager();

        SecurityAnalysisReport report = options.getPath(OUTPUT_LOG_OPTION)
            .map(logPath -> runSecurityAnalysisWithLog(computationManager, execution, executionInput, logPath))
            .orElseGet(() -> execution.execute(computationManager, executionInput).join());

        SecurityAnalysisResult result = report.getResult();

        if (result.getPreContingencyResult().getStatus() != LoadFlowResult.ComponentResult.Status.CONVERGED) {
            context.getErrorStream().println("Pre-contingency state divergence");
        }

        if (outputFile != null) {
            context.getOutputStream().println("Writing results to '" + outputFile + "'");
            SecurityAnalysisResultExporters.export(result, outputFile, format);
        } else {
            // To avoid the closing of System.out
            Writer writer = new OutputStreamWriter(context.getOutputStream());
            Security.print(result, network, writer, new AsciiTableFormatterFactory(), tableFormatterConfigLoader.get());
        }
    }
}
