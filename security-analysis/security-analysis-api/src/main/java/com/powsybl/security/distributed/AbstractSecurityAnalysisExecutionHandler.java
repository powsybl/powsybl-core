/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.distributed;

import com.google.common.io.ByteSource;
import com.powsybl.action.Action;
import com.powsybl.action.ActionList;
import com.powsybl.computation.*;
import com.powsybl.iidm.serde.NetworkSerDe;
import com.powsybl.security.execution.AbstractSecurityAnalysisExecutionInput;
import com.powsybl.security.execution.NetworkVariant;
import com.powsybl.security.json.limitreduction.LimitReductionListSerDeUtil;
import com.powsybl.security.limitreduction.LimitReduction;
import com.powsybl.security.limitreduction.LimitReductionList;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.strategy.OperatorStrategy;
import com.powsybl.security.strategy.OperatorStrategyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public abstract class AbstractSecurityAnalysisExecutionHandler<R,
        T extends AbstractSecurityAnalysisExecutionInput<T>,
        S extends AbstractSecurityAnalysisCommandOptions<S>> extends AbstractExecutionHandler<R> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractSecurityAnalysisExecutionHandler.class);

    private static final String NETWORK_FILE = "network.xiidm";
    private static final String CONTINGENCIES_FILE = "contingencies.groovy";
    private static final String PARAMETERS_FILE = "parameters.json";
    private static final String ACTIONS_FILE = "actions.json";
    private static final String STRATEGIES_FILE = "strategies.json";
    private static final String LIMIT_REDUCTIONS_FILE = "limit-reductions.json";

    protected final int executionCount;
    protected final T input;
    private final ResultReader<R> reader;
    private final OptionsCustomizer<S> optionsCustomizer;
    private final ExceptionHandler exceptionHandler;

    /**
     * Defines the result type, and how is should be read from the working directory after the command execution.
     * It is typically provided as a lambda.
     *
     * @param <R> the result type
     */
    @FunctionalInterface
    public interface ResultReader<R> {
        R read(Path workinDir);
    }

    /**
     * May define or override command options.
     * It is typically provided as a lambda.
     */
    @FunctionalInterface
    public interface OptionsCustomizer<S extends AbstractSecurityAnalysisCommandOptions<S>> {
        void customizeOptions(Path workinDir, S options);
    }

    /**
     * Defines the creation of computation exceptions, in particular log reading.
     */
    @FunctionalInterface
    public interface ExceptionHandler {
        ComputationException createComputationException(Path workingDir, Exception cause);
    }

    protected AbstractSecurityAnalysisExecutionHandler(ResultReader<R> reader,
                                                       OptionsCustomizer<S> optionsCustomizer,
                                                       ExceptionHandler exceptionHandler,
                                                       int executionCount,
                                                       T input) {
        this.reader = requireNonNull(reader);
        this.optionsCustomizer = optionsCustomizer;
        this.exceptionHandler = exceptionHandler;
        checkArgument(executionCount > 0, "Execution count must be positive.");
        this.executionCount = executionCount;
        this.input = requireNonNull(input);
    }


    /**
     * Copies case file, contingencies file, and parameters file to working directory,
     * and creates the {@literal itools security-analysis} command(s) to be executed,
     * based on configuration and the optional options' customizer.
     */
    @Override
    public List<CommandExecution> before(Path workingDir) throws IOException {
        CommandExecution execution = createSecurityAnalysisCommandExecution(workingDir);
        if (!input.getMonitors().isEmpty()) {
            StateMonitor.write(input.getMonitors(), workingDir.resolve("montoring_file.json"));
        }
        return Collections.singletonList(execution);
    }

    /**
     * Reads result from the working directory, as defined by the specified reader.
     */
    @Override
    public R after(Path workingDir, ExecutionReport report) throws IOException {
        try {
            super.after(workingDir, report);
            R result = reader.read(workingDir);
            LOGGER.debug("End of command execution in {}. ", workingDir);
            return result;
        } catch (Exception exception) {
            throw exceptionHandler.createComputationException(workingDir, exception);
        }
    }

    /**
     * Create the {@literal itools security-analysis} command and copies necessary files to working directory.
     * Options may be added through the specified {@link #optionsCustomizer}
     */
    protected abstract CommandExecution createSecurityAnalysisCommandExecution(Path workingDir);

    protected void mapInputToCommand(Path workingDir, S options) {
        options.resultExtensions(input.getResultExtensions())
            .violationTypes(input.getViolationTypes());

        addCaseFile(options, workingDir, input.getNetworkVariant());

        input.getContingenciesSource().ifPresent(
                source -> addContingenciesFile(options, workingDir, source)
        );

        if (executionCount > 1) {
            options.task(taskNumber -> new Partition(taskNumber + 1, executionCount));
        }

        if (optionsCustomizer != null) {
            optionsCustomizer.customizeOptions(workingDir, options);
        }
        addOperatorStrategyFile(options, workingDir, input.getOperatorStrategies());
        addActionFile(options, workingDir, input.getActions());
        addLimitReductionsFile(options, workingDir, input.getLimitReductions());
    }

    private static Path getCasePath(Path workingDir) {
        return workingDir.resolve(NETWORK_FILE);
    }

    protected static Path getParametersPath(Path workingDir) {
        return workingDir.resolve(PARAMETERS_FILE);
    }

    private static Path getActionsPath(Path workingDir) {
        return workingDir.resolve(ACTIONS_FILE);
    }

    private static Path getStrategiesPath(Path workingDir) {
        return workingDir.resolve(STRATEGIES_FILE);
    }

    private static Path getLimitReductionsPath(Path workingDir) {
        return workingDir.resolve(LIMIT_REDUCTIONS_FILE);
    }

    private static Path getContingenciesPath(Path workingDir) {
        return workingDir.resolve(CONTINGENCIES_FILE);
    }

    /**
     * Add case file option, and write network to working directory.
     */
    private void addCaseFile(S options, Path workingDir, NetworkVariant variant) {
        Path dest = getCasePath(workingDir);
        options.caseFile(dest);
        LOGGER.debug("Copying network to file {}", dest);
        NetworkSerDe.write(variant.getVariant(), dest);
    }

    /**
     * Add contingencies file option, and write it to working directory.
     */
    private void addContingenciesFile(S options, Path workingDir, ByteSource source) {
        Path dest = getContingenciesPath(workingDir);
        options.contingenciesFile(dest);
        LOGGER.debug("Writing contingencies to file {}", dest);
        copySourceToPath(source, dest);
    }

    /**
     * Add operator strategies file option, and write it as JSON to working directory.
     */
    private void addOperatorStrategyFile(S options, Path workingDir, List<OperatorStrategy> operatorStrategies) {
        if (operatorStrategies.isEmpty()) {
            return;
        }
        Path path = getStrategiesPath(workingDir);
        options.strategiesFile(path);
        LOGGER.debug("Writing operator strategies to file {}", path);
        new OperatorStrategyList(operatorStrategies)
                .write(path);
    }

    /**
     * Add action file option, and write it as JSON to working directory.
     */
    private void addActionFile(S options, Path workingDir, List<Action> actions) {
        if (actions.isEmpty()) {
            return;
        }
        Path path = getActionsPath(workingDir);
        options.actionsFile(path);
        LOGGER.debug("Writing actions to file {}", path);
        new ActionList(actions)
                .writeJsonFile(path);
    }

    /**
     * Add limit reductions file option, and write it as JSON to working directory.
     */
    private void addLimitReductionsFile(S options, Path workingDir, List<LimitReduction> limitReductions) {
        if (limitReductions.isEmpty()) {
            return;
        }
        Path path = getLimitReductionsPath(workingDir);
        options.limitReductionsFile(path);
        LOGGER.debug("Writing limit reductions to file {}", path);
        LimitReductionListSerDeUtil.write(new LimitReductionList(limitReductions), path);
    }

    /**
     * Copies bytes from the source to target path.
     */
    protected static void copySourceToPath(ByteSource source, Path dest) {
        try (InputStream is = source.openBufferedStream()) {
            Files.copy(is, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
