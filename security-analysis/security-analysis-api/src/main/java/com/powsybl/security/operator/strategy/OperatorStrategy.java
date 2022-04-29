/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.operator.strategy;

import com.google.common.collect.ImmutableList;
import com.powsybl.security.condition.Condition;

import java.util.List;
import java.util.Objects;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class OperatorStrategy {
    private final String id;
    private final String contingencyId;
    private final Condition condition;  // under which circumstances do I want to trigger my action
    private final List<String> actionIds;

    public String getId() {
        return id;
    }

    public String getContingencyId() {
        return contingencyId;
    }

    public Condition getCondition() {
        return condition;
    }

    public List<String> getActionIds() {
        return actionIds;
    }

    public OperatorStrategy(String id, String contingencyId, Condition condition, List<String> actionIds) {
        this.id = Objects.requireNonNull(id);
        this.contingencyId = Objects.requireNonNull(contingencyId);
        this.condition = Objects.requireNonNull(condition);
        this.actionIds = ImmutableList.copyOf(Objects.requireNonNull(actionIds));
    }
}
