/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ExecutionHandler<R> {

    List<CommandExecution> before(Path workingDir) throws IOException;

    void onExecutionStart(CommandExecution execution, int executionIndex);

    void onExecutionCompletion(CommandExecution execution, int executionIndex);

    R after(Path workingDir, ExecutionReport report) throws IOException;

}
