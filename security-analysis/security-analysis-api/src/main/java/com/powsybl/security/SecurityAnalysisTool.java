/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.google.auto.service.AutoService;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.Partition;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.ContingenciesProviderFactory;
import com.powsybl.contingency.ContingenciesProviders;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.tools.ConversionOption;
import com.powsybl.iidm.tools.DefaultConversionOption;
import com.powsybl.security.converter.SecurityAnalysisResultExporters;
import com.powsybl.security.detectors.DefaultLimitViolationDetector;
import com.powsybl.security.distributed.DistributedSecurityAnalysis;
import com.powsybl.security.distributed.ExternalSecurityAnalysis;
import com.powsybl.security.distributed.ExternalSecurityAnalysisConfig;
import com.powsybl.security.distributed.SubContingenciesProvider;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptors;
import com.powsybl.security.json.JsonSecurityAnalysisParameters;
import com.powsybl.tools.AbstractCommand;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.iidm.tools.ConversionToolConstants.CASE_FILE;
import static com.powsybl.security.SecurityAnalysisToolConstants.*;
import static com.powsybl.tools.ToolConstants.TASK;
import static com.powsybl.tools.ToolConstants.TASK_COUNT;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
@AutoService(Tool.class)
public class SecurityAnalysisTool implements Tool {

    private static final Supplier<ConversionOption> LOADER = Suppliers.memoize(() -> new DefaultConversionOption(CASE_FILE));

    private final ConversionOption conversionOption;

    public SecurityAnalysisTool() {
        this(LOADER.get());
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

    protected TableFormatterConfig createTableFormatterConfig() {
        return TableFormatterConfig.load();
    }

    private static Optional<String> getOptionValue(CommandLine line, String option) {
        return line.hasOption(option) ? Optional.of(line.getOptionValue(option)) : Optional.empty();
    }

    private static SecurityAnalysisResult runSecurityAnalysis(CommandLine line, ToolRunningContext context, ContingenciesProvider contingenciesProvider, SecurityAnalysisParameters parameters, SecurityAnalysis securityAnalysis, String currentState) {
        SecurityAnalysisResult result;
        if (!line.hasOption(OUTPUT_LOG_OPTION)) {
            result = securityAnalysis.run(currentState, parameters, contingenciesProvider).join();
        } else {
            SecurityAnalysisResultWithLog resultWithLog = securityAnalysis.runWithLog(currentState, parameters, contingenciesProvider).join();
            result = resultWithLog.getResult();
            // copy log bytes to file
            resultWithLog.getLogBytes().ifPresent(logBytes -> {
                Path outlogDest = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_LOG_OPTION));
                try (ByteArrayInputStream bis = new ByteArrayInputStream(logBytes);
                     OutputStream fos = Files.newOutputStream(outlogDest)) {
                    IOUtils.copy(bis, fos);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
        return result;
    }

    private static SecurityAnalysis createSecurityAnalysis(CommandLine line, List<String> extensions, Set<SecurityAnalysisInterceptor> interceptors, Network network,
                                                           LimitViolationFilter limitViolationFilter, ComputationManager computationManager, SecurityAnalysisFactory factory) {
        SecurityAnalysis securityAnalysis;
        if (line.hasOption(EXTERNAL)) {
            Integer taskCount = getOptionValue(line, TASK_COUNT).map(Integer::parseInt).orElse(null);
            ExternalSecurityAnalysisConfig config = ExternalSecurityAnalysisConfig.load();
            securityAnalysis = new ExternalSecurityAnalysis(config, network, computationManager, extensions, taskCount);
        } else if (line.hasOption(TASK_COUNT)) {
            int taskCount = Integer.parseInt(line.getOptionValue(TASK_COUNT));
            ExternalSecurityAnalysisConfig config = ExternalSecurityAnalysisConfig.load();
            securityAnalysis = new DistributedSecurityAnalysis(config, network, computationManager, extensions, taskCount);
        } else {
            securityAnalysis = factory.create(network, new DefaultLimitViolationDetector(), limitViolationFilter, computationManager, 0);
            interceptors.forEach(securityAnalysis::addInterceptor);
        }
        return securityAnalysis;
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        run(line, context, ContingenciesProviders.newDefaultFactory(), SecurityAnalysisFactories.newDefaultFactory());

    }

    void run(CommandLine line, ToolRunningContext context, ContingenciesProviderFactory cf, SecurityAnalysisFactory factory) throws Exception {
        Set<LimitViolationType> limitViolationTypes = line.hasOption(LIMIT_TYPES_OPTION)
                ? Arrays.stream(line.getOptionValue(LIMIT_TYPES_OPTION).split(",")).map(LimitViolationType::valueOf).collect(Collectors.toSet())
                : EnumSet.allOf(LimitViolationType.class);

        //Required extensions names
        List<String> extensions = getOptionValue(line, WITH_EXTENSIONS_OPTION)
                .map(s -> Arrays.stream(s.split(","))
                        .filter(ext -> !ext.isEmpty())
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        Set<SecurityAnalysisInterceptor> interceptors = extensions.stream().map(SecurityAnalysisInterceptors::createInterceptor).collect(Collectors.toSet());

        // Output file and output format
        Path outputFile = null;
        String format = null;
        if (line.hasOption(OUTPUT_FILE_OPTION)) {
            outputFile = context.getFileSystem().getPath(line.getOptionValue(OUTPUT_FILE_OPTION));
            if (!line.hasOption(OUTPUT_FORMAT_OPTION)) {
                throw new ParseException("Missing required option: " + OUTPUT_FORMAT_OPTION);
            }
            format = line.getOptionValue(OUTPUT_FORMAT_OPTION);
        }

        // Contingencies file
        Path contingenciesFile = getOptionValue(line, CONTINGENCIES_FILE_OPTION).map(context.getFileSystem()::getPath).orElse(null);

        Network network = conversionOption.read(line, context);
        network.getVariantManager().allowVariantMultiThreadAccess(true);

        LimitViolationFilter limitViolationFilter = LimitViolationFilter.load();
        limitViolationFilter.setViolationTypes(limitViolationTypes);

        ContingenciesProvider contingenciesProvider = contingenciesFile != null ?
                cf.create(contingenciesFile) : ContingenciesProviders.emptyProvider();

        ComputationManager computationManager = context.getLongTimeExecutionComputationManager();

        if (line.hasOption(TASK)) {
            Partition partition = Partition.parse(line.getOptionValue(TASK));
            contingenciesProvider = new SubContingenciesProvider(contingenciesProvider, partition);
            computationManager = context.getShortTimeExecutionComputationManager();
        }

        SecurityAnalysisParameters parameters = SecurityAnalysisParameters.load();
        if (line.hasOption(PARAMETERS_FILE_OPTION)) {
            Path parametersFile = context.getFileSystem().getPath(line.getOptionValue(PARAMETERS_FILE_OPTION));
            JsonSecurityAnalysisParameters.update(parameters, parametersFile);
        }

        SecurityAnalysis securityAnalysis = createSecurityAnalysis(line, extensions, interceptors, network, limitViolationFilter, computationManager, factory);

        String currentState = network.getVariantManager().getWorkingVariantId();

        SecurityAnalysisResult result = runSecurityAnalysis(line, context, contingenciesProvider, parameters, securityAnalysis, currentState);

        if (!result.getPreContingencyResult().isComputationOk()) {
            context.getErrorStream().println("Pre-contingency state divergence");
        }
        if (outputFile != null) {
            context.getOutputStream().println("Writing results to '" + outputFile + "'");
            SecurityAnalysisResultExporters.export(result, outputFile, format);
        } else {
            // To avoid the closing of System.out
            Writer writer = new OutputStreamWriter(context.getOutputStream());
            Security.print(result, network, writer, new AsciiTableFormatterFactory(), createTableFormatterConfig());
        }
    }
}
