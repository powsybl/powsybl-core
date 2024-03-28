/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.condition;

import com.google.common.collect.ImmutableSet;
import com.powsybl.security.LimitViolationType;

import java.util.*;

/**
 *
 * simulate the associated action as soon there is any violation
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 *
 */
public class AnyViolationCondition extends AbstractFilteredCondition {

    public static final String NAME = "ANY_VIOLATION_CONDITION";

    public AnyViolationCondition() {
        this(Collections.emptySet());
    }

    public AnyViolationCondition(Set<LimitViolationType> conditionFilters) {
        super(ImmutableSet.copyOf(Objects.requireNonNull(conditionFilters)));
    }

    @Override
    public String getType() {
        return NAME;
    }
}
