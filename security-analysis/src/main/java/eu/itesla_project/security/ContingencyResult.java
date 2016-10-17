/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security;

import java.util.List;
import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
abstract class ContingencyResult {

    private final boolean computationOk;

    private final List<LimitViolation> limitViolations;

    public ContingencyResult(boolean computationOk, List<LimitViolation> limitViolations) {
        this.computationOk = computationOk;
        this.limitViolations = Objects.requireNonNull(limitViolations);
    }

    public boolean isComputationOk() {
        return computationOk;
    }

    public List<LimitViolation> getLimitViolations() {
        return limitViolations;
    }
}
