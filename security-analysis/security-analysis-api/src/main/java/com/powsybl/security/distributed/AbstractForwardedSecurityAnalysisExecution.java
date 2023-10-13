/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.distributed;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

/**
 *
 * Forwards the execution of a security analysis to another itools process.
 * The {@link #forwardedTaskCount} parameter will be forward to that process, so if it is greater than one,
 * it will spawn multiple "slave" processes.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public abstract class AbstractForwardedSecurityAnalysisExecution {

    protected final ExternalSecurityAnalysisConfig config;
    protected Integer forwardedTaskCount;

    protected AbstractForwardedSecurityAnalysisExecution(ExternalSecurityAnalysisConfig config) {
        this(config, null);
    }

    protected AbstractForwardedSecurityAnalysisExecution(ExternalSecurityAnalysisConfig config, Integer forwardedTaskCount) {
        this.config = Objects.requireNonNull(config);
        this.forwardedTaskCount = checkForwardedTaskCount(forwardedTaskCount);
    }

    private static Integer checkForwardedTaskCount(Integer count) {
        checkArgument(count == null || count > 0, "Forwarded task count must be positive.");
        return count;
    }
}
