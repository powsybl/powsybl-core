/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ComputationExceptionBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComputationExceptionBuilder.class);

    private final Map<String, String> outMsgByLogFileName = new HashMap<>();
    private final Map<String, String> errMsgByLogFileName = new HashMap<>();
    private final Map<String, byte[]> bytesByFileName = new HashMap<>();
    private final List<Exception> exceptions = new ArrayList<>();

    public ComputationExceptionBuilder(Exception exception) {
        Objects.requireNonNull(exception);
        exceptions.add(exception);
    }

    /**
     * Log the content of .out file. If path is {@literal null} or not exists, it skips.
     * @param path
     * @return
     */
    public ComputationExceptionBuilder addOutLogIfExists(@Nullable Path path) {
        return readFileToMap(path, outMsgByLogFileName);
    }

    /**
     * @param logName the log name, should not be null
     * @param log log content, could be null
     * @return
     */
    public ComputationExceptionBuilder addOutLog(String logName, @Nullable String log) {
        Objects.requireNonNull(logName);
        outMsgByLogFileName.put(logName, log);
        return this;
    }

    /**
     * Log the content of .err file. If path is {@literal null} or not exists, it skips.
     * @param path
     * @return
     */
    public ComputationExceptionBuilder addErrLogIfExists(@Nullable Path path) {
        return readFileToMap(path, errMsgByLogFileName);
    }

    /**
     * @param logName the log name, should not be null
     * @param log log content, could be null
     * @return
     */
    public ComputationExceptionBuilder addErrLog(String logName, @Nullable String log) {
        Objects.requireNonNull(logName);
        errMsgByLogFileName.put(logName, log);
        return this;
    }

    /**
     * @param path to the potential file
     * @return
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
     * Add bytes.
     * @param key The name of bytes.
     * @param bytes Bytes.
     * @return
     */
    public ComputationExceptionBuilder addBytes(String key, byte[] bytes) {
        Objects.requireNonNull(bytes);
        Objects.requireNonNull(key);
        bytesByFileName.put(key, bytes);
        return this;
    }

    /**
     * If the exception is {@literal Null}, do nothing. Otherwise, this exception is logged in a {@literal List}
     * @param exception
     * @return
     */
    public ComputationExceptionBuilder addException(@Nullable Exception exception) {
        if (exception == null) {
            return this;
        }
        exceptions.add(exception);
        return this;
    }

    public ComputationException build() {
        return new ComputationException(outMsgByLogFileName, errMsgByLogFileName, bytesByFileName, exceptions);
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
}
