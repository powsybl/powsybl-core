/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.powsybl.commons.PowsyblException;
import org.apache.commons.compress.utils.IOUtils;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * This class is responsible for carry out the .err/.out logs when execution failed.
 *
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ComputationException extends PowsyblException {

    private final Map<String, String> outMsgByLogFileName = new HashMap<>();

    private final Map<String, String> errMsgByLogFileName = new HashMap<>();

    private final Map<String, byte[]> zipBytesByFileName = new HashMap<>();

    private final List<Exception> exceptions = new ArrayList<>();

    public ComputationException(String msg) {
        super(msg);
    }

    public ComputationException(Throwable throwable) {
        super(throwable);
    }

    public ComputationException addOutLog(Path path) {
        Objects.requireNonNull(path);
        return addOutLog(path.getFileName().toString(), readFile(path));
    }

    public ComputationException addErrLog(Path path) {
        Objects.requireNonNull(path);
        return addErrLog(path.getFileName().toString(), readFile(path));
    }

    public ComputationException addFileIfExists(Path path) {
        if (!Files.exists(path)) {
            return this;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream inputStream = Files.newInputStream(path)) {
            IOUtils.copy(inputStream, baos);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try {
            baos.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        zipBytesByFileName.put(path.getParent().relativize(path).toString(), baos.toByteArray());
        return this;
    }

    public Map<String, byte[]> getZipBytes() {
        return zipBytesByFileName;
    }

    private static String readFile(Path path) {
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
            return content.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public ComputationException addOutLog(String logName, @Nullable String msg) {
        outMsgByLogFileName.put(Objects.requireNonNull(logName), msg);
        return this;
    }

    public ComputationException addErrLog(String logName, @Nullable String msg) {
        errMsgByLogFileName.put(Objects.requireNonNull(logName), msg);
        return this;
    }

    /**
     * Returns a map which log file name is {@literal key}, and standard output message is {@literal value}
     * @return
     */
    public Map<String, String> getOutLogs() {
        return outMsgByLogFileName;
    }

    /**
     * Returns a map which log file name is {@literal key}, and standard error message is {@literal value}
     * @return
     */
    public Map<String, String> getErrLogs() {
        return errMsgByLogFileName;
    }

    public ComputationException addException(Exception e) {
        Objects.requireNonNull(e);
        exceptions.add(e);
        return this;
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }
}
