/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Used to ease the creation of a {@link ComputationException}.
 * The builder provides methods to register logs and files from different sources.
 *
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 * @author Sylvain LECLERC {@literal <sylvain.leclerc at rte-france.com>}
 */
public class ComputationExceptionBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputationExceptionBuilder.class);

    private String message;
    private final Throwable cause;
    private final Map<String, String> outMsgByLogFileName = new HashMap<>();
    private final Map<String, String> errMsgByLogFileName = new HashMap<>();
    private final Map<String, byte[]> bytesByFileName = new HashMap<>();

    /**
     * Initializes the builder, with no cause, no message and no logs.
     */
    public ComputationExceptionBuilder() {
        this.cause = null;
    }

    /**
     * Initializes the builder with the specified exception as the computation exception cause.
     */
    public ComputationExceptionBuilder(Throwable cause) {
        this.cause = requireNonNull(cause);
    }

    /**
     * Defines the detail message of the created computation exception.
     *
     * @param message the detail message of the computation exception.
     * @return this
     */
    public ComputationExceptionBuilder message(@Nullable String message) {
        this.message = message;
        return this;
    }

    /**
     * Reads the content of standard output file at specified path, assuming UTF-8 encoding.
     * This log will be associated to the file name.
     * If path is {@code null} or file does not exist, this is a no-op.
     *
     * @param path The path to the standard output file.
     * @return this
     */
    public ComputationExceptionBuilder addOutLogIfExists(@Nullable Path path) {
        return readFileToMap(path, outMsgByLogFileName);
    }

    /**
     * Adds a standard output log to collected data.
     *
     * @param logName the log name, must not be {@code null}
     * @param log log content, may be {@code null}
     * @return this
     */
    public ComputationExceptionBuilder addOutLog(String logName, @Nullable String log) {
        requireNonNull(logName);
        outMsgByLogFileName.put(logName, log);
        return this;
    }

    /**
     * Reads the content of standard error file at specified path, assuming UTF-8 encoding.
     * This log will be associated to the file name.
     * If path is {@code null} or file does not exist, this is a no-op.
     *
     * @param path The path to the standard error file.
     * @return this
     */
    public ComputationExceptionBuilder addErrLogIfExists(@Nullable Path path) {
        return readFileToMap(path, errMsgByLogFileName);
    }

    /**
     * Adds an error log to collected data.
     *
     * @param logName the log name, must not be {@code null}
     * @param log log content, may be {@code null}
     * @return this
     */
    public ComputationExceptionBuilder addErrLog(String logName, @Nullable String log) {
        requireNonNull(logName);
        errMsgByLogFileName.put(logName, log);
        return this;
    }

    /**
     * Adds the content of a the file at specified path to collected data.
     * It will be associated to the file name.
     * If path is {@code null} or file does not exist, this is a no-op.
     *
     * @param path to the file to be added to collected data.
     * @return this
     */
    public ComputationExceptionBuilder addFileIfExists(@Nullable Path path) {
        if (path == null || !Files.exists(path)) {
            return this;
        }
        try {
            byte[] bytes = Files.readAllBytes(path);
            bytesByFileName.put(path.getFileName().toString(), bytes);
        } catch (IOException e) {
            LOGGER.warn("Can not read zip file '{}'", path);
        }
        return this;
    }

    /**
     * Adds raw content of a file to collected data.
     *
     * @param key   The log file name.
     * @param bytes Bytes.
     * @return this
     */
    public ComputationExceptionBuilder addBytes(String key, byte[] bytes) {
        requireNonNull(bytes);
        requireNonNull(key);
        bytesByFileName.put(key, bytes);
        return this;
    }

    private ComputationExceptionBuilder readFileToMap(@Nullable Path path, Map<String, String> map) {
        if (path == null || !Files.exists(path)) {
            return this;
        }

        try {
            byte[] bytes = Files.readAllBytes(path);
            map.put(path.getFileName().toString(), new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.warn("Can not read log file '{}'", path);
        }
        return this;
    }

    /**
     * Creates the computation exception.
     *
     * @return the created computation exception.
     */
    public ComputationException build() {
        ComputationException exception;
        if (message != null && cause != null) {
            exception = new ComputationException(message, cause, outMsgByLogFileName, errMsgByLogFileName, bytesByFileName);
        } else if (message != null) {
            exception = new ComputationException(message, outMsgByLogFileName, errMsgByLogFileName, bytesByFileName);
        } else if (cause != null) {
            exception = new ComputationException(cause, outMsgByLogFileName, errMsgByLogFileName, bytesByFileName);
        } else {
            exception = new ComputationException(outMsgByLogFileName, errMsgByLogFileName, bytesByFileName);
        }
        return exception;
    }
}
