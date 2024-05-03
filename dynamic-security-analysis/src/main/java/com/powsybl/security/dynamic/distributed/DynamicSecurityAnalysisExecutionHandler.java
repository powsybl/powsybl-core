/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.distributed;

import com.google.common.io.ByteSource;
import com.powsybl.computation.CommandExecution;
import com.powsybl.computation.ComputationException;
import com.powsybl.computation.ExecutionHandler;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.distributed.AbstractSecurityAnalysisExecutionHandler;
import com.powsybl.security.distributed.SecurityAnalysisExecutionHandlers;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisParameters;
import com.powsybl.security.dynamic.execution.DynamicSecurityAnalysisExecutionInput;

import java.nio.file.Path;

/**
 * Base implementation for {@link ExecutionHandler}s which may execute one or multiple {@literal itools dynamic-security-analysis} command(s).
 * The exact behaviour is provided through the constructor argument.
 * Instances are provided by factory methods of {@link SecurityAnalysisExecutionHandlers}.
 *
 * <p>Specified {@link Network} variant is serialized as an XIIDM file.
 *
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSecurityAnalysisExecutionHandler<R> extends AbstractSecurityAnalysisExecutionHandler<R,
        DynamicSecurityAnalysisExecutionInput,
        DynamicSecurityAnalysisCommandOptions> {

    private static final String DYNAMIC_MODELS_FILE = "dynamicModels.groovy";
    private static final String EVENT_MODELS_FILE = "eventModels.groovy";

    /**
     * Creates a new security analysis execution handler.
     *
     * @param reader            Defines how results should be read from working directory.
     * @param optionsCustomizer If not {@code null}, defines additional command options.
     * @param exceptionHandler  Used to translate exceptions to a {@link ComputationException}.
     * @param executionCount    The number of executions of the command.
     * @param input             The execution input data.
     */
    public DynamicSecurityAnalysisExecutionHandler(ResultReader<R> reader,
                                                   OptionsCustomizer<DynamicSecurityAnalysisCommandOptions> optionsCustomizer,
                                                   ExceptionHandler exceptionHandler,
                                                   int executionCount,
                                                   DynamicSecurityAnalysisExecutionInput input) {
        super(reader, optionsCustomizer, exceptionHandler, executionCount, input);
    }

    @Override
    protected CommandExecution createSecurityAnalysisCommandExecution(Path workingDir) {
        DynamicSecurityAnalysisCommandOptions options = new DynamicSecurityAnalysisCommandOptions();
        addParametersFile(options, workingDir, input.getParameters());
        mapInputToCommand(workingDir, options);
        addDynamicModelsFile(options, workingDir, input.getDynamicModelsSource());
        input.getEventModelsSource().ifPresent(
                source -> addEventModelsFile(options, workingDir, source)
        );

        return new CommandExecution(options.toCommand(), executionCount);
    }

    /**
     * Add parameters file option, and write it as JSON to working directory.
     */
    private void addParametersFile(DynamicSecurityAnalysisCommandOptions options, Path workingDir, DynamicSecurityAnalysisParameters parameters) {
        Path parametersPath = getParametersPath(workingDir);
        options.parametersFile(parametersPath);
        LOGGER.debug("Writing parameters to file {}", parametersPath);
        parameters.write(parametersPath);
    }

    private static Path getDynamicModelsPath(Path workingDir) {
        return workingDir.resolve(DYNAMIC_MODELS_FILE);
    }

    private static Path getEventModelsPath(Path workingDir) {
        return workingDir.resolve(EVENT_MODELS_FILE);
    }

    private static void addDynamicModelsFile(DynamicSecurityAnalysisCommandOptions options, Path workingDir, ByteSource source) {
        Path dest = getDynamicModelsPath(workingDir);
        options.dynamicModelsFile(dest);
        LOGGER.debug("Writing dynamic models to file {}", dest);
        copySourceToPath(source, dest);
    }

    private static void addEventModelsFile(DynamicSecurityAnalysisCommandOptions options, Path workingDir, ByteSource source) {
        Path dest = getEventModelsPath(workingDir);
        options.eventModelsFile(dest);
        LOGGER.debug("Writing event models to file {}", dest);
        copySourceToPath(source, dest);
    }
}
