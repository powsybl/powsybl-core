/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation;

import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CommandExecution {

    private final Command command;

    private final int executionCount;

    private final int priority;

    private final Map<String, String> tags;

    public CommandExecution(Command command, int executionCount) {
        this(command, executionCount, Integer.MAX_VALUE);
    }

    public CommandExecution(Command command, int executionCount, int priority) {
        this(command, executionCount, priority, null);
    }

    public CommandExecution(Command command, int executionCount, int priority, Map<String, String> tags) {
        this.command = Objects.requireNonNull(command, "command is null");
        if (executionCount < 1) {
            throw new IllegalArgumentException("execution count must be > 0");
        }
        this.executionCount = executionCount;
        this.priority = priority;
        this.tags = tags;
    }

    public Command getCommand() {
        return command;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public int getPriority() {
        return priority;
    }

    public Map<String, String> getTags() {
        return tags;
    }

}
