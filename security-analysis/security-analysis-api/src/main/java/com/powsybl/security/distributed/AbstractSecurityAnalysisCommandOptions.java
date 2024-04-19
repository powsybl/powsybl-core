/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.distributed;

import com.google.common.base.Preconditions;
import com.powsybl.computation.Partition;
import com.powsybl.computation.SimpleCommand;
import com.powsybl.computation.SimpleCommandBuilder;
import com.powsybl.security.LimitViolationType;
import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.powsybl.security.tools.SecurityAnalysisToolConstants.*;
import static com.powsybl.tools.ToolConstants.TASK;
import static com.powsybl.tools.ToolConstants.TASK_COUNT;
import static java.util.Objects.requireNonNull;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public abstract class AbstractSecurityAnalysisCommandOptions<T extends AbstractSecurityAnalysisCommandOptions<T>> {

    private String itoolsCommand;
    private String id;
    private Path caseFile;
    private Path contingenciesFile;
    private Path parametersFile;
    private Path actionsFile;
    private Path strategiesFile;
    private Path limitReductionsFile;
    private Integer taskCount;
    private Function<Integer, Path> outputFile;
    private Function<Integer, Path> logFile;
    private Function<Integer, Partition> task;
    private String outputFileFormat;
    private List<String> resultExtensions;
    private List<LimitViolationType> violationTypes;

    private boolean absolutePaths;

    protected AbstractSecurityAnalysisCommandOptions(String id) {
        this.id = id;
        this.resultExtensions = new ArrayList<>();
        this.violationTypes = new ArrayList<>();
        this.absolutePaths = false;
    }

    public T itoolsCommand(String itoolsCommand) {
        this.itoolsCommand = requireNonNull(itoolsCommand);
        return self();
    }

    public T id(String id) {
        this.id = requireNonNull(id);
        return self();
    }

    public T absolutePaths(boolean absolutePaths) {
        this.absolutePaths = absolutePaths;
        return self();
    }

    public T caseFile(Path caseFile) {
        this.caseFile = requireNonNull(caseFile);
        return self();
    }

    public T contingenciesFile(Path contingenciesFile) {
        this.contingenciesFile = requireNonNull(contingenciesFile);
        return self();
    }

    public T parametersFile(Path parametersFile) {
        this.parametersFile = requireNonNull(parametersFile);
        return self();
    }

    public T actionsFile(Path actionsFile) {
        this.actionsFile = actionsFile;
        return self();
    }

    public T strategiesFile(Path strategiesFile) {
        this.strategiesFile = strategiesFile;
        return self();
    }

    public T limitReductionsFile(Path limitReductionsFile) {
        this.limitReductionsFile = limitReductionsFile;
        return self();
    }

    public T taskCount(int taskCount) {
        this.taskCount = taskCount;
        return self();
    }

    public T outputFile(Function<Integer, Path> outputFile, String format) {
        this.outputFile = requireNonNull(outputFile);
        this.outputFileFormat = requireNonNull(format);
        return self();
    }

    public T logFile(Path logFile) {
        requireNonNull(logFile);
        this.logFile = i -> logFile;
        return self();
    }

    public T logFile(Function<Integer, Path> logFile) {
        this.logFile = requireNonNull(logFile);
        return self();
    }

    public T outputFile(Path outputFile, String format) {
        requireNonNull(outputFile);
        this.outputFile = i -> outputFile;
        this.outputFileFormat = requireNonNull(format);
        return self();
    }

    public T taskBasedOnIndex(int taskCount) {
        return task(i -> new Partition(i + 1, taskCount));
    }

    public T task(Function<Integer, Partition> task) {
        this.task = requireNonNull(task);
        return self();
    }

    public T task(Partition task) {
        requireNonNull(task);
        this.task = i -> task;
        return self();
    }

    public T resultExtension(String extensionName) {
        this.resultExtensions.add(requireNonNull(extensionName));
        return self();
    }

    public T resultExtensions(Collection<String> extensionNames) {
        this.resultExtensions.addAll(requireNonNull(extensionNames));
        return self();
    }

    public T violationType(LimitViolationType violationType) {
        this.violationTypes.add(requireNonNull(violationType));
        return self();
    }

    public T violationTypes(Collection<LimitViolationType> violationTypes) {
        this.violationTypes.addAll(requireNonNull(violationTypes));
        return self();
    }

    protected String pathToString(Path path) {
        return (absolutePaths ? path.toAbsolutePath() : path).toString();
    }

    public SimpleCommand toCommand() {
        return toCommandBuilder().build();
    }

    protected SimpleCommandBuilder toCommandBuilder() {
        Objects.requireNonNull(caseFile, "Case file is not defined.");
        Preconditions.checkArgument(task == null || taskCount == null,
                "Options task and task-count may not be defined together.");

        SimpleCommandBuilder commandBuilder = new SimpleCommandBuilder()
                .id(id)
                .program(itoolsCommand != null ? itoolsCommand : getDefaultItoolsCommand())
                .arg(getCommandName())
                .option(CASE_FILE_OPTION, pathToString(caseFile));

        setOptionIfPresent(commandBuilder, PARAMETERS_FILE_OPTION, parametersFile, this::pathToString);
        setOptionIfPresent(commandBuilder, ACTIONS_FILE, actionsFile, this::pathToString);
        setOptionIfPresent(commandBuilder, STRATEGIES_FILE, strategiesFile, this::pathToString);
        setOptionIfPresent(commandBuilder, CONTINGENCIES_FILE_OPTION, contingenciesFile, this::pathToString);
        setOptionIfPresent(commandBuilder, LIMIT_REDUCTIONS_FILE, limitReductionsFile, this::pathToString);
        setOptionIfPresent(commandBuilder, OUTPUT_FILE_OPTION, outputFile, this::pathToString);
        setOptionIfPresent(commandBuilder, OUTPUT_FORMAT_OPTION, outputFileFormat);
        setOptionIfPresent(commandBuilder, OUTPUT_LOG_OPTION, logFile, this::pathToString);
        if (!resultExtensions.isEmpty()) {
            commandBuilder.option(WITH_EXTENSIONS_OPTION, String.join(",", resultExtensions));
        }
        if (!violationTypes.isEmpty()) {
            commandBuilder.option(LIMIT_TYPES_OPTION, violationTypes.stream().map(LimitViolationType::name).collect(Collectors.joining(",")));
        }
        setOptionIfPresent(commandBuilder, TASK_COUNT, taskCount, i -> Integer.toString(i));
        setOptionIfPresent(commandBuilder, TASK, task, Partition::toString);
        return commandBuilder;
    }

    protected void setOptionIfPresent(SimpleCommandBuilder commandBuilder, String optionName, String optionValue) {
        if (optionValue != null) {
            commandBuilder.option(optionName, optionValue);
        }
    }

    protected <R> void setOptionIfPresent(SimpleCommandBuilder commandBuilder, String optionName, R optionValue, Function<R, String> toString) {
        if (optionValue != null) {
            commandBuilder.option(optionName, toString.apply(optionValue));
        }
    }

    protected <R> void setOptionIfPresent(SimpleCommandBuilder commandBuilder, String optionName, Function<Integer, R> optionValue, Function<R, String> toString) {
        if (optionValue != null) {
            commandBuilder.option(optionName, i -> toString.apply(optionValue.apply(i)));
        }
    }

    protected abstract String getCommandName();

    protected abstract T self();

    private static String getDefaultItoolsCommand() {
        return SystemUtils.IS_OS_WINDOWS ? "itools.bat" : "itools";
    }
}
