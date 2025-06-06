/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 *
 * Defines the execution environment for commands to be executed:
 * <ul>
 *   <li>a map of environment variables</li>
 *   <li>a prefix for the execution working directory</li>
 *   <li>a debug indicator</li>
     <li>the directory where execution files will be dumped</li>
 * </ul>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ExecutionEnvironment {

    public static ExecutionEnvironment createDefault() {
        return new ExecutionEnvironment(Collections.emptyMap(), "itools", false);
    }

    private Map<String, String> variables;

    private String workingDirPrefix;

    /**
     * If debug=true, execution files generated for and by the binary model will not be removed after the computation. There are available in the workingDir.
     * If debug=false, execution files are removed from the workingDir after computation.
     */
    private boolean debug;

    /**
     * If dumpDir is filled, in/out execution files are dumped in dumpDir.
     * If dumpDir=null, nothing is dumped.
     * debug and dumpDir have fully independent behaviors.
     */
    private String dumpDir;

    public ExecutionEnvironment(Map<String, String> variables, String workingDirPrefix, boolean debug, String dumpDir) {
        this.variables = Objects.requireNonNull(variables);
        this.workingDirPrefix = Objects.requireNonNull(workingDirPrefix);
        this.debug = debug;
        this.dumpDir = dumpDir;
    }

    public ExecutionEnvironment(Map<String, String> variables, String workingDirPrefix, boolean debug) {
        this(variables, workingDirPrefix, debug, null);
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

    public String getDumpDir() {
        return dumpDir;
    }
}
