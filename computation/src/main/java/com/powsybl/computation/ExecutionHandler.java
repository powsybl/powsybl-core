/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * Base interface for processing to be executed through a {@link ComputationManager}.
 *
 * <p>The {@link #before(Path)} method will be called before the commands are actually executed:
 * it is in charge of any preprocessing of providing the list of commands to be executed.
 * Typically, it will copy input data to the specified working directory,
 * and create the command to be executed with those inputs.
 *
 * <p>The {@link #after(Path, ExecutionReport)} method will be called after the commands have been executed:
 * it is in charge of any postprocessing and of providing the actual computation result.
 * Typically, it will check that the execution was correctly peformed, then read command results
 * from the specified working directory and translate it to a business object.
 *
 * <p>The generic parameter {@link R} is the type of the result object provided by the command execution.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ExecutionHandler<R> {

    /**
     * Method called by the {@link ComputationManager} with the working directory as parameter,
     * before the commands are actually executed:
     * it is in charge of any preprocessing of providing the list of commands to be executed.
     * Typically, it will copy input data to the specified working directory,
     * and create the command to be executed with those inputs.
     *
     * <p>If the call throws an exception, no command will be executed.
     *
     * @param workingDir   the working directory used for this computation. Input data may be written to it.
     * @return             the list of {@link CommandExecution}s to be executed.
     * @throws IOException when an error occurs while wirting to working directory.
     */
    List<CommandExecution> before(Path workingDir) throws IOException;

    void onExecutionStart(CommandExecution execution, int executionIndex);

    void onExecutionCompletion(CommandExecution execution, int executionIndex);

    /**
     * Method called by the {@link ComputationManager} with the working directory and an execution report as parameters,
     * after the commands defined by the {@link #before} method have been executed:
     * it is in charge of any postprocessing and of providing the actual computation result.
     * Typically, it will check that the execution was correctly performed, then read command results
     * from the specified working directory and translate it to a business object.
     *
     * <p>That method will not be called in case the call to {@link #before} throws an exception,
     * or if the execution is cancelled.
     *
     * @param workingDir   the working directory used for this computation. Results may be read from it.
     * @param report       the execution report, in particular reporting command execution errors.
     * @return             the actual result of the processing.
     * @throws IOException if and error occurs while reading results from the working directory.
     */
    R after(Path workingDir, ExecutionReport report) throws IOException;

}
