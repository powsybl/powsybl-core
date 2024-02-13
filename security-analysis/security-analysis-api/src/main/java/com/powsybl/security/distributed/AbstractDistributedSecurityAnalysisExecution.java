/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.distributed;

import com.powsybl.computation.ComputationManager;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * Execute a security analysis by spawning a specified number of subtasks, each of which
 * will consist to a separate call to {@literal itools security-analysis} through the specified
 * {@link ComputationManager}.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public abstract class AbstractDistributedSecurityAnalysisExecution {

    protected final ExternalSecurityAnalysisConfig config;
    protected final int subtaskCount;

    protected AbstractDistributedSecurityAnalysisExecution(ExternalSecurityAnalysisConfig config, int subtaskCount) {
        this.config = requireNonNull(config);
        checkArgument(subtaskCount > 0, "Sub-tasks count must be positive.");
        this.subtaskCount = checkSubtaskCount(subtaskCount);
    }

    private static int checkSubtaskCount(int count) {
        checkArgument(count > 0, "Sub-tasks count must be positive.");
        return count;
    }
}
