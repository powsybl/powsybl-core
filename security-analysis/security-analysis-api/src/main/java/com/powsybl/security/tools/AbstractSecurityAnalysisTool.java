/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.tools;

import com.powsybl.action.ActionList;
import com.powsybl.commons.io.FileUtil;
import com.powsybl.commons.io.table.AsciiTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.computation.ComputationException;
import com.powsybl.computation.Partition;
import com.powsybl.iidm.network.ImportConfig;
import com.powsybl.iidm.network.ImportersLoader;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.tools.ConversionToolUtils;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.*;
import com.powsybl.security.converter.SecurityAnalysisResultExporters;
import com.powsybl.security.execution.AbstractSecurityAnalysisExecutionBuilder;
import com.powsybl.security.execution.AbstractSecurityAnalysisExecutionInput;
import com.powsybl.security.json.limitreduction.LimitReductionListSerDeUtil;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.strategy.OperatorStrategyList;
import com.powsybl.tools.ToolOptions;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.powsybl.iidm.network.tools.ConversionToolUtils.readProperties;
import static com.powsybl.security.tools.SecurityAnalysisToolConstants.*;
import static com.powsybl.tools.ToolConstants.TASK;
import static com.powsybl.tools.ToolConstants.TASK_COUNT;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public abstract class AbstractSecurityAnalysisTool<T extends AbstractSecurityAnalysisExecutionInput<T, S>,
        S extends AbstractSecurityAnalysisParameters<S>,
        R extends AbstractSecurityAnalysisExecutionBuilder<R>> {

    public static Network readNetwork(CommandLine line, ToolRunningContext context, ImportersLoader importersLoader) throws IOException {
        ToolOptions options = new ToolOptions(line, context);
        Path caseFile = options.getPath(CASE_FILE_OPTION)
            .orElseThrow(IllegalStateException::new);
        Properties inputParams = readProperties(line, ConversionToolUtils.OptionType.IMPORT, context);
        context.getOutputStream().println("Loading network '" + caseFile + "'");
        Network network = Network.read(caseFile, context.getShortTimeExecutionComputationManager(), ImportConfig.load(), inputParams, importersLoader);
        network.getVariantManager().allowVariantMultiThreadAccess(true);
        return network;
    }

    protected static void uncheckedWriteBytes(byte[] bytes, Path path) {
        try {
            Files.write(path, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void run(CommandLine line, ToolRunningContext context,
             R executionBuilder,
             Supplier<S> parametersLoader,
             ImportersLoader importersLoader,
             Supplier<TableFormatterConfig> tableFormatterConfigLoader) throws ParseException, IOException {

        ToolOptions options = new ToolOptions(line, context);

        // Output file and output format
        Path outputFile = options.getPath(OUTPUT_FILE_OPTION).orElse(null);
        String format = getFormat(options, outputFile);
        Network network = readNetwork(line, context, importersLoader);

        T executionInput = getExecutionInput(network, parametersLoader);
        updateInput(options, executionInput);

        Supplier<SecurityAnalysisReport> supplier = getReportSupplier(context, options, executionBuilder, executionInput);

        SecurityAnalysisResult result = options.getPath(OUTPUT_LOG_OPTION)
                .map(logPath -> runSecurityAnalysisWithLog(supplier, logPath))
                .orElseGet(supplier).getResult();

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

    protected String getFormat(ToolOptions options, Path outputFile) throws ParseException {
        return outputFile != null ? options.getValue(OUTPUT_FORMAT_OPTION)
                    .orElseThrow(() -> new ParseException("Missing required option: " + OUTPUT_FORMAT_OPTION))
                : null;
    }

    protected abstract T getExecutionInput(Network network, Supplier<S> parametersLoader);

    public void updateInput(ToolOptions options, T inputs) {
        options.getPath(PARAMETERS_FILE_OPTION)
                .ifPresent(f -> inputs.getParameters().update(f));
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
        options.getPath(MONITORING_FILE)
                .ifPresent(monitorFilePath -> inputs.setMonitors(StateMonitor.read(monitorFilePath)));
        options.getPath(STRATEGIES_FILE)
                .ifPresent(operatorStrategyFilePath -> inputs.setOperatorStrategies(OperatorStrategyList.read(operatorStrategyFilePath).getOperatorStrategies()));
        options.getPath(ACTIONS_FILE)
                .ifPresent(actionFilePath -> inputs.setActions(ActionList.readJsonFile(actionFilePath).getActions()));
        options.getPath(LIMIT_REDUCTIONS_FILE)
                .ifPresent(limitReductionsFilePath -> inputs.setLimitReductions(LimitReductionListSerDeUtil.read(limitReductionsFilePath).getLimitReductions()));
    }

    protected SecurityAnalysisReport runSecurityAnalysisWithLog(Supplier<SecurityAnalysisReport> supplier, Path logPath) {
        try {
            SecurityAnalysisReport report = supplier.get();
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

    protected void setupExecutionBuilder(ToolOptions options, R builder) {
        builder.forward(options.hasOption(EXTERNAL));
        options.getInt(TASK_COUNT).ifPresent(builder::distributed);
        options.getValue(TASK, Partition::parse).ifPresent(builder::subTask);
    }

    protected abstract Supplier<SecurityAnalysisReport> getReportSupplier(ToolRunningContext context, ToolOptions options, R executionBuilder, T executionInput);
}
