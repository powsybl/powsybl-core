/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.interceptors;

import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.contingency.Contingency;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ViolationContext extends AbstractExtendable<ViolationContext> {

    private final RunningContext runningContext;
    private final Contingency contingency;

    public ViolationContext(RunningContext runningContext) {
        this(runningContext, null);
    }

    public ViolationContext(RunningContext runningContext, @Nullable Contingency contingency) {
        this.runningContext = Objects.requireNonNull(runningContext);
        this.contingency = contingency;
    }

    public RunningContext getRunningContext() {
        return runningContext;
    }

    public Optional<Contingency> getContingency() {
        return Optional.ofNullable(contingency);
    }
}
