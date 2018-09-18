/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.powsybl.commons.PowsyblException;

import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @param <R>
 */
public abstract class AbstractExecutionHandler<R> implements ExecutionHandler<R> {

    @Override
    public void onExecutionStart(CommandExecution execution, int executionIndex) {
    }

    @Override
    public void onExecutionCompletion(CommandExecution execution, int executionIndex) {
    }

    @Override
    public R after(Path workingDir, ExecutionReport report) throws IOException {
        if (!report.getErrors().isEmpty()) {
            report.log();
            throw new PowsyblException("Execution error");
        }
        return null;
    }

}
