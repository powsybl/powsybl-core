/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.distributed;

import com.powsybl.computation.CommandExecution;
import com.powsybl.computation.ComputationException;
import com.powsybl.computation.ExecutionHandler;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.execution.SecurityAnalysisExecutionInput;

import java.nio.file.Path;

/**
 * Base implementation for {@link ExecutionHandler}s which may execute one or multiple {@literal itools security-analysis} command(s).
 * The exact behaviour is provided through the constructor argument.
 * Instances are provided by factory methods of {@link SecurityAnalysisExecutionHandlers}.
 *
 * <p>Specified {@link Network} variant is serialized as an XIIDM file.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class SecurityAnalysisExecutionHandler<R> extends AbstractSecurityAnalysisExecutionHandler<R,
        SecurityAnalysisExecutionInput,
        SecurityAnalysisCommandOptions,
        SecurityAnalysisParameters> {

    /**
     * Creates a new security analysis execution handler.
     *
     * @param reader            Defines how results should be read from working directory.
     * @param optionsCustomizer If not {@code null}, defines additional command options.
     * @param exceptionHandler  Used to translate exceptions to a {@link ComputationException}.
     * @param executionCount    The number of executions of the command.
     * @param input             The execution input data.
     */
    public SecurityAnalysisExecutionHandler(ResultReader<R> reader,
                                            OptionsCustomizer<SecurityAnalysisCommandOptions> optionsCustomizer,
                                            ExceptionHandler exceptionHandler,
                                            int executionCount,
                                            SecurityAnalysisExecutionInput input) {
        super(reader, optionsCustomizer, exceptionHandler, executionCount, input);
    }

    @Override
    protected CommandExecution createSecurityAnalysisCommandExecution(Path workingDir) {
        SecurityAnalysisCommandOptions options = new SecurityAnalysisCommandOptions();
        mapInputToCommand(workingDir, options);
        return new CommandExecution(options.toCommand(), executionCount);
    }
}
