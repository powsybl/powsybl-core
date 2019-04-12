/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class SecurityAnalysisResultWithLog {

    private final SecurityAnalysisResult result;

    private byte[] logBytes;

    public SecurityAnalysisResultWithLog(SecurityAnalysisResult result) {
        this.result = Objects.requireNonNull(result);
    }

    public SecurityAnalysisResultWithLog(SecurityAnalysisResult result, byte[] logBytes) {
        this.result = Objects.requireNonNull(result);
        this.logBytes = logBytes;
    }

    /**
     * Gets log file in bytes.
     * @return an Optional describing the zip bytes
     */
    public Optional<byte[]> getLogBytes() {
        return Optional.ofNullable(logBytes);
    }

    public SecurityAnalysisResult getResult() {
        return result;
    }
}
