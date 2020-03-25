/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.interceptors;

import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.security.LimitViolation;

import java.util.Objects;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ViolationContext extends AbstractExtendable<ViolationContext> {

    private final RunningContext runningContext;
    private final LimitViolation limitViolation;

    public ViolationContext(RunningContext runningContext, LimitViolation limitViolation) {
        this.runningContext = Objects.requireNonNull(runningContext);
        this.limitViolation = Objects.requireNonNull(limitViolation);
    }

    public RunningContext getRunningContext() {
        return runningContext;
    }

    public LimitViolation getLimitViolation() {
        return limitViolation;
    }
}
