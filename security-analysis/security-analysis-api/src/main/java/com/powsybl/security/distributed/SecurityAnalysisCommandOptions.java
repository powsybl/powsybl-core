/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.distributed;

import com.google.common.base.Preconditions;
import com.powsybl.computation.*;
import com.powsybl.security.LimitViolationType;
import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.powsybl.security.tools.SecurityAnalysisToolConstants.*;
import static com.powsybl.tools.ToolConstants.TASK;
import static com.powsybl.tools.ToolConstants.TASK_COUNT;
import static java.util.Objects.requireNonNull;

/**
 * Utility class to programmatically generate an {@literal itools security-analysis} command with its various options.
 *
 * Currently supported options are :
 *  - the {@link Path} to case file
 *  - an optional {@link Path} to contingencies file
 *  - an optional {@link Path} to parameters file
 *  - an optional {@link Path} to output file
 *  - an optional format for the output file
 *  - a list of requested result extensions
 *  - a list of violation types of interest
 *  - an optional task count
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class SecurityAnalysisCommandOptions {

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

    public SecurityAnalysisCommandOptions() {
        this.id = "security-analysis";
        this.resultExtensions = new ArrayList<>();
        this.violationTypes = new ArrayList<>();
        this.absolutePaths = false;
    }

    public SecurityAnalysisCommandOptions itoolsCommand(String itoolsCommand) {
        this.itoolsCommand = requireNonNull(itoolsCommand);
        return this;
    }

    public SecurityAnalysisCommandOptions id(String id) {
        this.id = requireNonNull(id);
        return this;
    }

    public SecurityAnalysisCommandOptions absolutePaths(boolean absolutePaths) {
        this.absolutePaths = absolutePaths;
        return this;
    }

    public SecurityAnalysisCommandOptions caseFile(Path caseFile) {
        this.caseFile = requireNonNull(caseFile);
        return this;
    }

    public SecurityAnalysisCommandOptions contingenciesFile(Path contingenciesFile) {
        this.contingenciesFile = requireNonNull(contingenciesFile);
        return this;
    }

    public SecurityAnalysisCommandOptions parametersFile(Path parametersFile) {
        this.parametersFile = requireNonNull(parametersFile);
        return this;
    }

    public SecurityAnalysisCommandOptions actionsFile(Path actionsFile) {
        this.actionsFile = actionsFile;
        return this;
    }

    public SecurityAnalysisCommandOptions strategiesFile(Path strategiesFile) {
        this.strategiesFile = strategiesFile;
        return this;
    }

    public SecurityAnalysisCommandOptions limitReductionsFile(Path limitReductionsFile) {
        this.limitReductionsFile = limitReductionsFile;
        return this;
    }

    public SecurityAnalysisCommandOptions taskCount(int taskCount) {
        this.taskCount = taskCount;
        return this;
    }

    public SecurityAnalysisCommandOptions outputFile(Function<Integer, Path> outputFile, String format) {
        this.outputFile = requireNonNull(outputFile);
        this.outputFileFormat = requireNonNull(format);
        return this;
    }

    public SecurityAnalysisCommandOptions logFile(Path logFile) {
        requireNonNull(logFile);
        this.logFile = i -> logFile;
        return this;
    }

    public SecurityAnalysisCommandOptions logFile(Function<Integer, Path> logFile) {
        this.logFile = requireNonNull(logFile);
        return this;
    }

    public SecurityAnalysisCommandOptions outputFile(Path outputFile, String format) {
        requireNonNull(outputFile);
        this.outputFile = i -> outputFile;
        this.outputFileFormat = requireNonNull(format);
        return this;
    }

    public SecurityAnalysisCommandOptions taskBasedOnIndex(int taskCount) {
        return task(i -> new Partition(i + 1, taskCount));
    }

    public SecurityAnalysisCommandOptions task(Function<Integer, Partition> task) {
        this.task = requireNonNull(task);
        return this;
    }

    public SecurityAnalysisCommandOptions task(Partition task) {
        requireNonNull(task);
        this.task = i -> task;
        return this;
    }

    public SecurityAnalysisCommandOptions resultExtension(String extensionName) {
        this.resultExtensions.add(requireNonNull(extensionName));
        return this;
    }

    public SecurityAnalysisCommandOptions resultExtensions(Collection<String> extensionNames) {
        this.resultExtensions.addAll(requireNonNull(extensionNames));
        return this;
    }

    public SecurityAnalysisCommandOptions violationType(LimitViolationType violationType) {
        this.violationTypes.add(requireNonNull(violationType));
        return this;
    }

    public SecurityAnalysisCommandOptions violationTypes(Collection<LimitViolationType> violationTypes) {
        this.violationTypes.addAll(requireNonNull(violationTypes));
        return this;
    }

    private String pathToString(Path path) {
        return (absolutePaths ? path.toAbsolutePath() : path).toString();
    }

    public SimpleCommand toCommand() {
        Objects.requireNonNull(caseFile, "Case file is not defined.");
        Preconditions.checkArgument(task == null || taskCount == null,
                "Options task and task-count may not be defined together.");

        SimpleCommandBuilder commandBuilder = new SimpleCommandBuilder()
                .id(id)
                .program(itoolsCommand != null ? itoolsCommand : getDefaultItoolsCommand())
                .arg("security-analysis")
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

        return commandBuilder.build();
    }

    private void setOptionIfPresent(SimpleCommandBuilder commandBuilder, String optionName, String optionValue) {
        if (optionValue != null) {
            commandBuilder.option(optionName, optionValue);
        }
    }

    private <T> void setOptionIfPresent(SimpleCommandBuilder commandBuilder, String optionName, T optionValue, Function<T, String> toString) {
        if (optionValue != null) {
            commandBuilder.option(optionName, toString.apply(optionValue));
        }
    }

    private <T> void setOptionIfPresent(SimpleCommandBuilder commandBuilder, String optionName, Function<Integer, T> optionValue, Function<T, String> toString) {
        if (optionValue != null) {
            commandBuilder.option(optionName, i -> toString.apply(optionValue.apply(i)));
        }
    }

    private static String getDefaultItoolsCommand() {
        return SystemUtils.IS_OS_WINDOWS ? "itools.bat" : "itools";
    }
}
