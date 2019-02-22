/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class SecurityAnalysisResultWithLog {

    private final SecurityAnalysisResult result;
    private Optional<File> log;

    public SecurityAnalysisResultWithLog(SecurityAnalysisResult result, File log) {
        this.result = Objects.requireNonNull(result);
        this.log = Optional.ofNullable(log);
    }

    public Optional<File> getLog() {
        return log;
    }

    public SecurityAnalysisResult getResult() {
        return result;
    }
}
