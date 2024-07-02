/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.strategy;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.security.condition.Condition;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * A conditional action is a link between a {@link Condition} and a list of actions through ids.
 * In a security analysis, we first check the condition and if verified, the list of actions is applied on
 * the network. All actions are applied and in the order defined by the list.
 *
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class OperationalLimitOverride {

    private final String networkElementId;

    private final ThreeSides side;

    private final LimitType type;

    private final double maxValue;

    private final ContingencyContext contingencyContext;

    private final String conditionalActionsId;

    public OperationalLimitOverride(String networkElementId, @Nullable ThreeSides side, LimitType type, double maxValue, ContingencyContext contingencyContext, @Nullable String conditionalActionsId) {
        this.networkElementId = Objects.requireNonNull(networkElementId);
        this.side = side;
        this.type = type;
        this.maxValue = maxValue;
        this.contingencyContext = contingencyContext;
        this.conditionalActionsId = Objects.requireNonNull(conditionalActionsId);
    }

    public String getNetworkElementId() {
        return networkElementId;
    }

    public Optional<ThreeSides> getSide() {
        return Optional.ofNullable(side);
    }

    public LimitType getType() {
        return type;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public ContingencyContext getContingencyContext() {
        return contingencyContext;
    }

    public Optional<String> getConditionalActionsId() {
        return Optional.ofNullable(conditionalActionsId);
    }
}
