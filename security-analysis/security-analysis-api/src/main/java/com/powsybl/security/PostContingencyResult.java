/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.contingency.Contingency;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian@ at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class PostContingencyResult {

    private final Contingency contingency;

    private final LimitViolationsResult limitViolationsResult;

    public PostContingencyResult(Contingency contingency, LimitViolationsResult limitViolationsResult) {
        this.contingency = Objects.requireNonNull(contingency);
        this.limitViolationsResult = Objects.requireNonNull(limitViolationsResult);
    }

    public PostContingencyResult(Contingency contingency, boolean computationOk, List<LimitViolation> limitViolations) {
        this(contingency, new LimitViolationsResult(computationOk, limitViolations, Collections.emptyList()));
    }

    public PostContingencyResult(Contingency contingency, boolean computationOk, List<LimitViolation> limitViolations, List<String> actionsTaken) {
        this(contingency, new LimitViolationsResult(computationOk, limitViolations, actionsTaken));
    }

    public Contingency getContingency() {
        return contingency;
    }

    public LimitViolationsResult getLimitViolationsResult() {
        return limitViolationsResult;
    }
}
