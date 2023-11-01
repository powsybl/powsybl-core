/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.condition;

import com.powsybl.security.LimitViolationType;

import java.util.Set;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public abstract class AbstractFilteredCondition implements Condition {
    protected final Set<LimitViolationType> conditionFilters;

    protected AbstractFilteredCondition(Set<LimitViolationType> filters) {
        conditionFilters = filters;
    }

    public Set<LimitViolationType> getFilters() {
        return conditionFilters;
    }
}
