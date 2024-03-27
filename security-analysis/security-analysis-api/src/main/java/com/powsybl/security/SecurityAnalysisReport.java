/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.commons.extensions.AbstractExtendable;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public class SecurityAnalysisReport extends AbstractExtendable<SecurityAnalysisReport> {

    private final SecurityAnalysisResult result;

    private byte[] logBytes;

    public static SecurityAnalysisReport empty() {
        return new SecurityAnalysisReport(SecurityAnalysisResult.empty());
    }

    public SecurityAnalysisReport(SecurityAnalysisResult result) {
        this.result = Objects.requireNonNull(result);
    }

    public SecurityAnalysisResult getResult() {
        return result;
    }

    /**
     * Gets log file in bytes.
     * @return an Optional describing the zip bytes
     */
    public Optional<byte[]> getLogBytes() {
        return Optional.ofNullable(logBytes);
    }

    public SecurityAnalysisReport setLogBytes(byte[] logBytes) {
        this.logBytes = logBytes;
        return this;
    }
}
