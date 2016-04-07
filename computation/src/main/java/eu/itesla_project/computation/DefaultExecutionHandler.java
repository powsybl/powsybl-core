/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @param <R>
 */
public abstract class DefaultExecutionHandler<R> implements ExecutionHandler<R> {

    @Override
    public abstract List<CommandExecution> before(Path workingDir) throws IOException;

    @Override
    public void onProgress(CommandExecution execution, int executionIndex) {
    }

    @Override
    public R after(Path workingDir, ExecutionReport report) throws IOException {
        if (report.getErrors().size() > 0) {
            report.log();
            throw new RuntimeException("Execution error");
        }
        return null;
    }

}
