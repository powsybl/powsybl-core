/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExecutionError {

    private final Command command;

    private final int index;

    private final int exitCode;

    public ExecutionError(Command command, int index, int exitCode) {
        Objects.requireNonNull(command);
        if (index < 0) {
            throw new IllegalArgumentException("index < 1");
        }
        this.command = command;
        this.index = index;
        this.exitCode = exitCode;
    }

    public Command getCommand() {
        return command;
    }

    public int getIndex() {
        return index;
    }

    public int getExitCode() {
        return exitCode;
    }

    @Override
    public String toString() {
        return command.getId() + "[" + index + "]=" + exitCode;
    }

}
