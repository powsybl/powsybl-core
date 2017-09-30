/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExecutionEnvironment {

    public static ExecutionEnvironment createDefault() {
        return new ExecutionEnvironment(Collections.emptyMap(), "itools", false);
    }

    private Map<String, String> variables;

    private String workingDirPrefix;

    private boolean debug;

    public ExecutionEnvironment(Map<String, String> variables, String workingDirPrefix, boolean debug) {
        this.variables = Objects.requireNonNull(variables);
        this.workingDirPrefix = Objects.requireNonNull(workingDirPrefix);
        this.debug = debug;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public ExecutionEnvironment setVariables(Map<String, String> variables) {
        this.variables = variables;
        return this;
    }

    public String getWorkingDirPrefix() {
        return workingDirPrefix;
    }

    public ExecutionEnvironment setWorkingDirPrefix(String workingDirPrefix) {
        this.workingDirPrefix = workingDirPrefix;
        return this;
    }

    public boolean isDebug() {
        return debug;
    }

    public ExecutionEnvironment setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }
}
