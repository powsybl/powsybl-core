/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.tools.ConversionOption;
import com.powsybl.iidm.tools.DefaultConversionOption;
import com.powsybl.security.*;
import com.powsybl.security.converter.SecurityAnalysisResultExporters;
import com.powsybl.security.distributed.ExternalSecurityAnalysisConfig;
import com.powsybl.security.execution.SecurityAnalysisExecution;
import com.powsybl.security.execution.SecurityAnalysisExecutionBuilder;
import com.powsybl.security.execution.SecurityAnalysisExecutionInput;
import com.powsybl.security.execution.SecurityAnalysisInputBuildStrategy;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptors;
import com.powsybl.security.json.JsonSecurityAnalysisParameters;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessorFactory;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessors;
import com.powsybl.tools.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.powsybl.iidm.tools.ConversionToolConstants.CASE_FILE;
import static com.powsybl.security.tools.SecurityAnalysisToolConstants.*;
import static com.powsybl.tools.ToolConstants.TASK;
import static com.powsybl.tools.ToolConstants.TASK_COUNT;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
@AutoService(Tool.class)
public class SecurityAnalysisTool implements Tool {

    private final ConversionOption conversionOption;

    public SecurityAnalysisTool() {
        this(new DefaultConversionOption(CASE_FILE));
    }

    public SecurityAnalysisTool(ConversionOption conversionOption) {
        this.conversionOption = conversionOption;
    }

    @Override
    public Command getCommand() {
        return new AbstractCommand("security-analysis",
                "Computation",
                "Run security analysis") {

            @Override
            public Options getOptions() {
                Options options = new Options();
                conversionOption.addImportOptions(options);
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

    static void updateInput(ToolOptions options, SecurityAnalysisExecutionInput inputs) {
        options.getPath(PARAMETERS_FILE_OPTION)
                .ifPresent(f -> JsonSecurityAnalysisParameters.update(inputs.getParameters(), f));

        options.getPath(CONTINGENCIES_FILE_OPTION)
                .map(p -> FileUtil.asByteSource(p))
                .ifPresent(inputs::setContingenciesSource);

        options.getValues(LIMIT_TYPES_OPTION)
                .map(types -> types.stream().map(LimitViolationType::valueOf).collect(Collectors.toList()))
                .ifPresent(inputs::addViolationTypes);

        options.getValues(WITH_EXTENSIONS_OPTION)
                .ifPresent(inputs::addResultExtensions);
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
        return new SecurityAnalysisExecutionBuilder(() -> ExternalSecurityAnalysisConfig.load(platformConfig),
            () -> SecurityAnalysisFactories.newDefaultFactory(platformConfig),
            configBasedInputBuildStrategy(platformConfig));
    }

    private static SecurityAnalysisExecution buildExecution(ToolOptions options, SecurityAnalysisExecutionBuilder builder) {
        builder.forward(options.hasOption(EXTERNAL));
        options.getInt(TASK_COUNT).ifPresent(builder::distributed);
        options.getValue(TASK, Partition::parse).ifPresent(builder::subTask);
        return builder.build();
    }

    private static SecurityAnalysisResult runSecurityAnalysisWithLog(ComputationManager computationManager,
                                                             SecurityAnalysisExecution execution,
                                                             SecurityAnalysisExecutionInput input,
                                                             Path logPath) {
        try {
            SecurityAnalysisResultWithLog resultWithLog = execution.executeWithLog(computationManager, input).join();
            // copy log bytes to file
            resultWithLog.getLogBytes()
                    .ifPresent(logBytes -> uncheckedWriteBytes(logBytes, logPath));
            return resultWithLog.getResult();
        } catch (CompletionException e) {
            if (e.getCause() instanceof ComputationException) {
                ComputationException computationException = (ComputationException) e.getCause();
                byte[] bytes = computationException.toZipBytes();
                uncheckedWriteBytes(bytes, logPath);
            }
            throw e;
        }
    }

    Network readNetwork(CommandLine line, ToolRunningContext context) throws IOException {
        Network network = conversionOption.read(line, context);
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
                TableFormatterConfig::load);
    }

    void run(CommandLine line, ToolRunningContext context,
             SecurityAnalysisExecutionBuilder executionBuilder,
             Supplier<SecurityAnalysisParameters> parametersLoader,
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

        Network network = readNetwork(line, context);

        SecurityAnalysisExecutionInput executionInput = new SecurityAnalysisExecutionInput()
                .setNetworkVariant(network, VariantManagerConstants.INITIAL_VARIANT_ID)
                .setParameters(parametersLoader.get());

        updateInput(options, executionInput);

        SecurityAnalysisExecution execution = buildExecution(options, executionBuilder);

        ComputationManager computationManager = options.hasOption(TASK) ? context.getLongTimeExecutionComputationManager() :
                context.getShortTimeExecutionComputationManager();

        SecurityAnalysisResult result = options.getPath(OUTPUT_LOG_OPTION)
                .map(logPath -> runSecurityAnalysisWithLog(computationManager, execution, executionInput, logPath))
                .orElseGet(() -> execution.execute(computationManager, executionInput).join());

        if (!result.getPreContingencyResult().isComputationOk()) {
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
