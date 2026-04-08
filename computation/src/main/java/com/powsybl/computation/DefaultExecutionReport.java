/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DefaultExecutionReport implements ExecutionReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExecutionReport.class);

    private final Path workingDirectory;

    private final List<ExecutionError> errors;

    /**
     * Create an execution report with no execution error.
     */
    public DefaultExecutionReport(Path workingDirectory) {
        this(workingDirectory, Collections.emptyList());
    }

    /**
     * Create an execution report with the specified list of execution errors.
     */
    public DefaultExecutionReport(Path workingDirectory, List<ExecutionError> errors) {
        this.workingDirectory = Objects.requireNonNull(workingDirectory);
        this.errors = ImmutableList.copyOf(Objects.requireNonNull(errors));
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

    @Override
    public Optional<InputStream> getStdOut(Command command, int index) {
        return getOutputFile(command, index, ".out");
    }

    @Override
    public Optional<InputStream> getStdErr(Command command, int index) {
        return getOutputFile(command, index, ".err");
    }

    private Optional<InputStream> getOutputFile(Command command, int index, String extension) {
        Objects.requireNonNull(command);
        if (index < 0) {
            throw new IllegalArgumentException("Invalid index: " + index);
        }

        Path path = workingDirectory.resolve(command.getId() + "_" + index + extension);
        try {
            return Optional.of(Files.newInputStream(path, StandardOpenOption.READ));
        } catch (IOException e) {
            LOGGER.warn("Unable to read {}: {}", path, e);
        }

        return Optional.empty();
    }
}
