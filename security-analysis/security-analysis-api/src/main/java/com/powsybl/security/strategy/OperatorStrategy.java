/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.strategy;

import com.google.common.collect.ImmutableList;
import com.powsybl.security.condition.Condition;

import java.util.List;
import java.util.Objects;

/**
 * Describes a strategy that an operator would apply to solve violations occuring after a contingency.
 *
 * <p>A strategy is defined for a contingency, and defines a {@link Condition} under which
 * some {@link com.powsybl.security.action.Action}s will be taken.
 *
 * <p>The security analysis implementation will check that condition after the simulation
 * of the contingency, and if true, it will simulate the actions.
 *
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class OperatorStrategy {
    private final String id;
    private final String contingencyId;
    private final Condition condition;  // under which circumstances do I want to trigger my action
    private final List<String> actionIds;

    public OperatorStrategy(String id, String contingencyId, Condition condition, List<String> actionIds) {
        this.id = Objects.requireNonNull(id);
        this.contingencyId = Objects.requireNonNull(contingencyId);
        this.condition = Objects.requireNonNull(condition);
        this.actionIds = ImmutableList.copyOf(Objects.requireNonNull(actionIds));
    }

    /**
     * An ID which uniquely identifies this strategy, for a security analysis execution.
     */
    public String getId() {
        return id;
    }

    /**
     * The contingency which this strategy applies to.
     */
    public String getContingencyId() {
        return contingencyId;
    }

    /**
     * The condition which will decided the actual application of the actions, or not.
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * An ordered list of actions, which will be simulated if the condition holds true.
     */
    public List<String> getActionIds() {
        return actionIds;
    }
}
