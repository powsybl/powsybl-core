/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.strategy;

import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.security.condition.Condition;

import java.util.List;
import java.util.Objects;

/**
 * Describes a strategy that an operator would apply to solve violations occurring after a contingency.
 *
 * <p>A single stage strategy is defined for a contingency, and defines a {@link Condition} under which
 * some {@link com.powsybl.action.Action}s will be taken. A multiple stage strategy is defined after a contingency
 * too through a list of conditional actions (also called stage). For each stage, if the {@link Condition} is verified
 * the list of actions is applied.
 *
 * <p>For single stage operator strategy, the security analysis implementation will check the condition
 * after the simulation of the contingency, and if true, it will simulate the actions. For a multiple stage operator
 * strategy, the security analysis implementation will check the condition of the first stage. If verified,
 * the list of actions of this stage is applied. Then, on the network with maybe partial actions applied,
 * the condition of the second stage is checked and so on until the last stage of the strategy.
 *
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class OperatorStrategy extends AbstractExtendable<OperatorStrategy> {
    private final String id;
    private final ContingencyContext contingencyContext;
    private final List<ConditionalActions> conditionalActions;

    /**
     * Single stage operator strategy
     * @param id The id of the operator strategy
     * @param contingencyContext The contingency context in which to apply the operator strategy
     * @param condition The condition to trigger the operator strategy
     * @param actionIds The list of action ids to apply within this strategy
     */
    public OperatorStrategy(String id, ContingencyContext contingencyContext, Condition condition, List<String> actionIds) {
        this.id = Objects.requireNonNull(id);
        this.contingencyContext = Objects.requireNonNull(contingencyContext);
        this.conditionalActions = List.of(new ConditionalActions("default", condition, actionIds));
    }

    /**
     * Multiple stage operator strategy
     * @param id The id of the operator strategy
     * @param contingencyContext The contingency context in which to apply the operator strategy
     * @param stages The list of stages for this operator strategy
     */
    public OperatorStrategy(String id, ContingencyContext contingencyContext, List<ConditionalActions> stages) {
        this.id = Objects.requireNonNull(id);
        this.contingencyContext = Objects.requireNonNull(contingencyContext);
        this.conditionalActions = List.copyOf(Objects.requireNonNull(stages));
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
    public ContingencyContext getContingencyContext() {
        return contingencyContext;
    }

    public List<ConditionalActions> getConditionalActions() {
        return conditionalActions;
    }
}
