/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.powsybl.commons.PowsyblException;

import java.util.*;

/**
 * An immutable class contains all types of log collected.
 *
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ComputationException extends PowsyblException {

    private final Map<String, String> outMsgByLogFileName;

    private final Map<String, String> errMsgByLogFileName;

    private final Map<String, byte[]> zipBytesByFileName;

    private final List<Exception> exceptions;

    public ComputationException(ComputationException wrappedException, Exception e) {
        this(wrappedException.getOutLogs(), wrappedException.getErrLogs(), wrappedException.getZipBytes(), Arrays.asList(wrappedException, e));
    }

    ComputationException(Map<String, String> outMap, Map<String, String> errMap, Map<String, byte[]> zipMap, List<Exception> exceptions) {
        outMsgByLogFileName = Collections.unmodifiableMap(Objects.requireNonNull(outMap));
        errMsgByLogFileName = Collections.unmodifiableMap(Objects.requireNonNull(errMap));
        zipBytesByFileName = Collections.unmodifiableMap(Objects.requireNonNull(zipMap));
        this.exceptions = Collections.unmodifiableList(Objects.requireNonNull(exceptions));
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

    /**
     * Returns a map which zip file name is {@literal key}, and zip bytes is {@literal value}
     * @return
     */
    public Map<String, byte[]> getZipBytes() {
        return zipBytesByFileName;
    }

    /**
     * @return a list of exceptions
     */
    public List<Exception> getExceptions() {
        return exceptions;
    }
}
