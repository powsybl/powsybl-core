/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.strategy;

import com.powsybl.security.condition.Condition;

import java.util.List;
import java.util.Objects;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class ConditionalActions {

    private final String id;

    private final Condition condition;

    private final List<String> actionIds;

    public ConditionalActions(String id, Condition condition, List<String> actionIds) {
        this.id = Objects.requireNonNull(id);
        this.condition = Objects.requireNonNull(condition);
        this.actionIds = Objects.requireNonNull(actionIds);
    }

    /**
     * An ID which uniquely identifies this strategy, for a security analysis execution.
     */
    public String getId() {
        return id;
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
