/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import java.util.List;

/**
 * A single command to be executed, defined by its program name and a list of arguments.
 * The actual values of arguments may depend on the execution number, when several executions are submitted
 * to a {@link ComputationManager}.
 *
 * <p>A timeout for the execution of this command may be specified.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface SimpleCommand extends Command {

    /**
     * Define the name of the program to be executed.
     *
     * @return the name of the program to be executed.
     */
    String getProgram();

    /**
     * The list of arguments to be passed to the program, for the specified execution number.
     *
     * @param executionNumber execution number for which arguments are requested.
     * @return                the list of arguments to be passed to the program, for the specified execution number.
     */
    List<String> getArgs(int executionNumber);

    /**
     * @deprecated {@link ComputationParameters#getTimeout} should be used instead.
     *
     * A timeout in milliseconds for this command execution.
     * If less than zero, the execution time should be considered as unlimited.
     *
     * @return the timeout in milliseconds for this command execution.
     */
    @Deprecated(since = "2.5.0")
    int getTimeout();

}
