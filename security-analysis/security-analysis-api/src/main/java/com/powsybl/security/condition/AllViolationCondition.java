/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.condition;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.powsybl.security.LimitViolationType;

import java.util.*;

/**
 *
 * simulate the associated action if there is all violation on any of those network elements
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class AllViolationCondition extends AbstractFilteredCondition {

    private final List<String> violationIds;

    public static final String NAME = "ALL_VIOLATION";

    public AllViolationCondition(List<String> violationIds) {
        this(violationIds, Collections.emptySet());
    }

    public AllViolationCondition(List<String> violationIds, Set<LimitViolationType> conditionFilters) {
        super(ImmutableSet.copyOf(Objects.requireNonNull(conditionFilters)));
        this.violationIds = ImmutableList.copyOf(Objects.requireNonNull(violationIds));
    }

    @Override
    public String getType() {
        return NAME;
    }

    public List<String> getViolationIds() {
        return violationIds;
    }
}
