/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.condition;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

/**
 *
 * simulate the associated action if there is a violation on any of those network elements
 *
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class AtLeastOneViolationCondition implements Condition {

    public static final String NAME = "AT_LEAST_ONE_VIOLATION";

    private final List<String> violationIds;

    public AtLeastOneViolationCondition(List<String> violationIds) {
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
