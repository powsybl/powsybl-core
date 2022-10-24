/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class LimitViolationsResult {

    @Deprecated(forRemoval = true)
    private final boolean computationOk;

    private final List<LimitViolation> limitViolations;

    private final List<String> actionsTaken;

    public static LimitViolationsResult empty() {
        return new LimitViolationsResult(Collections.emptyList());
    }

    public LimitViolationsResult(List<LimitViolation> limitViolations) {
        this(true, limitViolations, Collections.emptyList());
    }

    @Deprecated(forRemoval = true)
    public LimitViolationsResult(boolean computationOk, List<LimitViolation> limitViolations, List<String> actionsTaken) {
        this.computationOk = computationOk;
        this.limitViolations = Objects.requireNonNull(limitViolations);
        this.actionsTaken = Objects.requireNonNull(actionsTaken);
    }

    public LimitViolationsResult(List<LimitViolation> limitViolations, List<String> actionsTaken) {
        this.computationOk = true;
        this.limitViolations = Objects.requireNonNull(limitViolations);
        this.actionsTaken = Objects.requireNonNull(actionsTaken);
    }

    public boolean isComputationOk() {
        return computationOk;
    }

    public List<LimitViolation> getLimitViolations() {
        return limitViolations;
    }

    public List<String> getActionsTaken() {
        return actionsTaken;
    }

}
