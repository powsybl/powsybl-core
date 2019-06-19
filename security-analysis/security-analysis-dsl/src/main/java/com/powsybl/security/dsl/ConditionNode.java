/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dsl;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.CurrentLimits;

import java.util.Objects;
import java.util.Optional;

/**
 *  A {@link LimitFactorsNode} which forwards requests to its children only if a condition is matched.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
class ConditionNode extends LimitFactorsNode {

    private final LimitMatcher condition;

    public ConditionNode(LimitMatcher condition) {
        super();
        this.condition = Objects.requireNonNull(condition);
    }

    @Override
    public Optional<Float> getFactor(Contingency contingency, Branch branch, Branch.Side side, CurrentLimits.TemporaryLimit limit) {

        if (condition.matches(branch, side, limit, contingency)) {
            return super.getFactor(contingency, branch, side, limit);
        }
        return Optional.empty();
    }
}
