/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.condition;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * simulate the associated action as soon there is any violation
 *
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 *
 */
public class AnyViolationCondition implements Condition {

    public static final String NAME = "ANY_VIOLATION_CONDITION";
    private final List<ConditionFilter> conditionFilters;

    public AnyViolationCondition() {
        this(Collections.emptyList());
    }

    public AnyViolationCondition(List<ConditionFilter> conditionFilters) {
        this.conditionFilters = ImmutableList.copyOf(Objects.requireNonNull(conditionFilters));
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    public List<ConditionFilter> getFilters() {
        return conditionFilters;
    }
}
