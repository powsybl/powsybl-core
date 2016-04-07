/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation;

import java.util.Collections;
import java.util.Map;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExecutionEnvironment {

    public static final ExecutionEnvironment DEFAULT = new ExecutionEnvironment(Collections.<String, String>emptyMap(), "itesla", false);

    private final Map<String, String> variables;

    private final String workingDirPrefix;

    private final boolean debug;

    public ExecutionEnvironment(Map<String, String> variables, String workingDirPrefix, boolean debug) {
        this.variables = variables;
        this.workingDirPrefix = workingDirPrefix;
        this.debug = debug;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public String getWorkingDirPrefix() {
        return workingDirPrefix;
    }

    public boolean isDebug() {
        return debug;
    }

}
