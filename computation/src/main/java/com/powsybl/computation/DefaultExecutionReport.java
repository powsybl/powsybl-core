/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultExecutionReport implements ExecutionReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExecutionReport.class);

    private final List<ExecutionError> errors;

    public DefaultExecutionReport(List<ExecutionError> errors) {
        this.errors = Objects.requireNonNull(errors);
    }

    @Override
    public List<ExecutionError> getErrors() {
        return errors;
    }

    @Override
    public void log() {
        if (!errors.isEmpty()) {
            LOGGER.error("{} commands have failed: {}", errors.size(), errors);
            if (LOGGER.isTraceEnabled()) {
                for (ExecutionError error : errors) {
                    LOGGER.trace("Command {} exits with code {}", error.getCommand().toString(error.getIndex()), error.getExitCode());
                }
            }
        }
    }

}
