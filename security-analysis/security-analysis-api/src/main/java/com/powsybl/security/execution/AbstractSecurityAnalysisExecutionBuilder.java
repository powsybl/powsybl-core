/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.execution;

import com.powsybl.computation.Partition;
import com.powsybl.security.distributed.ExternalSecurityAnalysisConfig;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public abstract class AbstractSecurityAnalysisExecutionBuilder<T extends AbstractSecurityAnalysisExecutionBuilder<T>> {

    protected final Supplier<ExternalSecurityAnalysisConfig> externalConfig;
    protected final String providerName;

    protected boolean forward = false;
    protected Integer taskCount = null;
    protected Partition subPart = null;

    /**
     * Create a new builder.
     *
     * @param externalConfig     The method to load an external security analysis config, only used for forwarded and distributed executions.
     * @param providerName       The named security-analysis implementation to use. If {@literal null}, the default would be used.
     */
    protected AbstractSecurityAnalysisExecutionBuilder(Supplier<ExternalSecurityAnalysisConfig> externalConfig,
                                                    String providerName) {
        this.externalConfig = Objects.requireNonNull(externalConfig);
        this.providerName = providerName;
    }

    public T forward(boolean forward) {
        this.forward = forward;
        return self();
    }

    public T distributed(Integer taskCount) {
        this.taskCount = taskCount;
        return self();
    }

    public T subTask(Partition part) {
        this.subPart = part;
        return self();
    }

    protected abstract T self();

}
