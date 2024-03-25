/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.compress.ZipPackager;

import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * A special exception containing information about an error that occurred during
 * an externally executed computation.
 *
 * In particular, it may contain:
 * <ul>
 *     <li>Standard output of executed commands</li>
 *     <li>Standard error of executed commands</li>
 *     <li>Other logs of executed commands as bytes, which may represent plain text files or archives
 *     (for instance, it may contain outputs of other sub-commands of the computation)</li>
 * </ul>
 * As the computation may consist of multiple command executions, multiple files of each type may be registered.
 *
 * <p>In order to create a {@link ComputationException}, you will need to use a {@link ComputationExceptionBuilder}.
 *
 * @author Yichen TANG {@literal <yichen.tang at rte-france.com>}
 * @author Sylvain LECLERC {@literal <sylvain.leclerc at rte-france.com>}
 */
public final class ComputationException extends PowsyblException {

    private final Map<String, String> outLogs;
    private final Map<String, String> errLogs;
    private final Map<String, byte[]> fileBytes;

    ComputationException(Map<String, String> outMap, Map<String, String> errMap, Map<String, byte[]> fileBytesMap) {
        outLogs = ImmutableMap.copyOf(requireNonNull(outMap));
        errLogs = ImmutableMap.copyOf(requireNonNull(errMap));
        fileBytes = ImmutableMap.copyOf(requireNonNull(fileBytesMap));
    }

    ComputationException(String message, Map<String, String> outMap, Map<String, String> errMap, Map<String, byte[]> fileBytesMap) {
        super(message);
        outLogs = ImmutableMap.copyOf(requireNonNull(outMap));
        errLogs = ImmutableMap.copyOf(requireNonNull(errMap));
        fileBytes = ImmutableMap.copyOf(requireNonNull(fileBytesMap));
    }

    ComputationException(Throwable cause, Map<String, String> outMap, Map<String, String> errMap, Map<String, byte[]> fileBytesMap) {
        super(cause);
        outLogs = ImmutableMap.copyOf(requireNonNull(outMap));
        errLogs = ImmutableMap.copyOf(requireNonNull(errMap));
        fileBytes = ImmutableMap.copyOf(requireNonNull(fileBytesMap));
    }

    ComputationException(String message, Throwable cause, Map<String, String> outMap, Map<String, String> errMap, Map<String, byte[]> fileBytesMap) {
        super(message, cause);
        outLogs = ImmutableMap.copyOf(requireNonNull(outMap));
        errLogs = ImmutableMap.copyOf(requireNonNull(errMap));
        fileBytes = ImmutableMap.copyOf(requireNonNull(fileBytesMap));
    }

    /**
     * Returns a map which log file name is {@literal key}, and standard output message is {@literal value}
     * @return a map which log file name is {@literal key}, and standard output message is {@literal value}
     */
    public Map<String, String> getOutLogs() {
        return outLogs;
    }

    /**
     * Returns a map which log file name is {@literal key}, and standard error message is {@literal value}
     * @return a map which log file name is {@literal key}, and standard error message is {@literal value}
     */
    public Map<String, String> getErrLogs() {
        return errLogs;
    }

    /**
     * Returns a map which file name is {@literal key}, and file content (as raw bytes) is {@literal value}
     * @return a map which file name is {@literal key}, and file content (as raw bytes) is {@literal value}
     */
    public Map<String, byte[]> getFileBytes() {
        return fileBytes;
    }

    /**
     * Serialize logs(.out/.err/files) to zip bytes.
     */
    public byte[] toZipBytes() {
        ZipPackager zipPackager = new ZipPackager();
        outLogs.forEach((k, v) -> zipPackager.addString(k, v == null ? "" : v));
        errLogs.forEach((k, v) -> zipPackager.addString(k, v == null ? "" : v));
        fileBytes.forEach(zipPackager::addBytes);
        return zipPackager.toZipBytes();
    }
}
