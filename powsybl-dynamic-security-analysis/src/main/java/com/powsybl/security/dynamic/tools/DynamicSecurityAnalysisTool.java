/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.tools;

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
import com.powsybl.dynamicsimulation.DynamicModelsSupplier;
import com.powsybl.dynamicsimulation.groovy.DynamicSimulationSupplierFactory;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.tools.ConversionToolUtils;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.*;
import com.powsybl.security.action.ActionList;
import com.powsybl.security.converter.SecurityAnalysisResultExporters;
import com.powsybl.security.distributed.ExternalSecurityAnalysisConfig;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisInput;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisParameters;
import com.powsybl.security.dynamic.execution.DynamicSecurityAnalysisExecution;
import com.powsybl.security.dynamic.execution.DynamicSecurityAnalysisExecutionBuilder;
import com.powsybl.security.dynamic.execution.DynamicSecurityAnalysisExecutionInput;
import com.powsybl.security.dynamic.execution.DynamicSecurityAnalysisInputBuildStrategy;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptors;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessorFactory;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessors;
import com.powsybl.security.strategy.OperatorStrategyList;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolOptions;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.powsybl.iidm.network.tools.ConversionToolUtils.readProperties;
import static com.powsybl.security.dynamic.tools.DynamicSecurityAnalysisToolConstants.DYNAMIC_MODELS_FILE_OPTION;
import static com.powsybl.security.dynamic.tools.DynamicSecurityAnalysisToolConstants.EVENT_MODELS_FILE_OPTION;
import static com.powsybl.security.tools.SecurityAnalysisToolConstants.*;
import static com.powsybl.tools.ToolConstants.TASK;
import static com.powsybl.tools.ToolConstants.TASK_COUNT;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
@AutoService(Tool.class)
public class DynamicSecurityAnalysisTool implements Tool {

    @Override
    public Command getCommand() {
        return new DynamicSecurityAnalysisCommand();
    }

    static void updateInput(ToolOptions options, DynamicSecurityAnalysisExecutionInput inputs) {
        options.getPath(PARAMETERS_FILE_OPTION)
                .ifPresent(f -> inputs.getParameters().update(f));

        options.getPath(EVENT_MODELS_FILE_OPTION)
                .map(FileUtil::asByteSource)
                .ifPresent(inputs::setEventModelsSource);

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

    private static DynamicSecurityAnalysisInputBuildStrategy configBasedInputBuildStrategy(PlatformConfig config) {
        return preprocessedInputBuildStrategy(() -> LimitViolationFilter.load(config),
            SecurityAnalysisPreprocessors.configuredFactory(config)
                .orElseGet(() -> SecurityAnalysisPreprocessors.wrap(ContingenciesProviders.newDefaultFactory(config))));
    }

    private static DynamicSecurityAnalysisInputBuildStrategy preprocessedInputBuildStrategy(Supplier<LimitViolationFilter> filterInitializer,
                                                                                     SecurityAnalysisPreprocessorFactory preprocessorFactory) {
        return (executionInput, providerName) -> buildPreprocessedInput(executionInput, providerName, filterInitializer, preprocessorFactory);
    }

    static DynamicSecurityAnalysisInput buildPreprocessedInput(DynamicSecurityAnalysisExecutionInput executionInput,
                                                               String providerName,
                                                               Supplier<LimitViolationFilter> filterInitializer,
                                                               SecurityAnalysisPreprocessorFactory preprocessorFactory) {

        DynamicModelsSupplier dynamicModelsSupplier;
        try (InputStream is = executionInput.getDynamicModelsSource().openBufferedStream()) {
            dynamicModelsSupplier = DynamicSimulationSupplierFactory.createDynamicModelsSupplier(is, providerName);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        DynamicSecurityAnalysisInput input = new DynamicSecurityAnalysisInput(executionInput.getNetworkVariant(), dynamicModelsSupplier)
            .setParameters(executionInput.getParameters())
            .setFilter(filterInitializer.get());

        executionInput.getResultExtensions().stream()
            .map(SecurityAnalysisInterceptors::createInterceptor)
            .forEach(input::addInterceptor);

        if (!executionInput.getViolationTypes().isEmpty()) {
            input.getFilter().setViolationTypes(ImmutableSet.copyOf(executionInput.getViolationTypes()));
        }

        executionInput.getEventModelsSource().ifPresent(
            es -> {
                try (InputStream is = es.openBufferedStream()) {
                    input.setEventModelsSupplier(DynamicSimulationSupplierFactory.createEventModelsSupplier(is, providerName));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        );

        executionInput.getContingenciesSource()
            .map(preprocessorFactory::newPreprocessor)
            .ifPresent(p -> p.preprocess(input));

        return input;
    }

    private static DynamicSecurityAnalysisExecutionBuilder createBuilder(PlatformConfig platformConfig) {
        String providerName = platformConfig.getOptionalModuleConfig(DynamicSecurityAnalysisToolConstants.MODULE_CONFIG_NAME_PROPERTY)
                .flatMap(c -> c.getOptionalStringProperty(DEFAULT_SERVICE_IMPL_NAME_PROPERTY))
                .orElse(null);
        return new DynamicSecurityAnalysisExecutionBuilder(() -> ExternalSecurityAnalysisConfig.load(platformConfig),
                providerName, configBasedInputBuildStrategy(platformConfig));
    }

    private static DynamicSecurityAnalysisExecution buildExecution(ToolOptions options, DynamicSecurityAnalysisExecutionBuilder builder) {
        builder.forward(options.hasOption(EXTERNAL));
        options.getInt(TASK_COUNT).ifPresent(builder::distributed);
        options.getValue(TASK, Partition::parse).ifPresent(builder::subTask);
        return builder.build();
    }

    private static SecurityAnalysisReport runSecurityAnalysisWithLog(ComputationManager computationManager,
                                                                     DynamicSecurityAnalysisExecution execution,
                                                                     DynamicSecurityAnalysisExecutionInput input,
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
            DynamicSecurityAnalysisParameters::load,
            new ImportersServiceLoader(),
            TableFormatterConfig::load);
    }

    void run(CommandLine line, ToolRunningContext context,
             DynamicSecurityAnalysisExecutionBuilder executionBuilder,
             Supplier<DynamicSecurityAnalysisParameters> parametersLoader,
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

        DynamicSecurityAnalysisExecutionInput executionInput = new DynamicSecurityAnalysisExecutionInput()
            .setNetworkVariant(network, VariantManagerConstants.INITIAL_VARIANT_ID)
            .setParameters(parametersLoader.get());

        executionInput.setDynamicModelsSource(options.getPath(DYNAMIC_MODELS_FILE_OPTION)
                .map(FileUtil::asByteSource)
                .orElseThrow(() -> new ParseException("Dynamic models file not found")));

        options.getPath(MONITORING_FILE).ifPresent(monitorFilePath -> executionInput.setMonitors(StateMonitor.read(monitorFilePath)));
        options.getPath(STRATEGIES_FILE).ifPresent(operatorStrategyFilePath -> executionInput.setOperatorStrategies(OperatorStrategyList.read(operatorStrategyFilePath).getOperatorStrategies()));
        options.getPath(ACTIONS_FILE).ifPresent(actionFilePath -> executionInput.setActions(ActionList.readJsonFile(actionFilePath).getActions()));

        updateInput(options, executionInput);

        DynamicSecurityAnalysisExecution execution = buildExecution(options, executionBuilder);

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
