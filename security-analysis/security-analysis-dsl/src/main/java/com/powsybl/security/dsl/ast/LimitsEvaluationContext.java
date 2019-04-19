/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dsl.ast;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.CurrentLimits.TemporaryLimit;

import java.util.Objects;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class LimitsEvaluationContext {

    private final Branch branch;
    private final Branch.Side side;
    private final TemporaryLimit limit;
    private final Contingency contingency;

    public LimitsEvaluationContext(Contingency contingency, Branch branch, Branch.Side side, TemporaryLimit limit) {
        this.branch = Objects.requireNonNull(branch);
        this.side = Objects.requireNonNull(side);
        this.limit = limit;
        this.contingency = contingency;
    }

    public Branch getBranch() {
        return branch;
    }

    public Branch.Side getSide() {
        return side;
    }

    public TemporaryLimit getLimit() {
        return limit;
    }

    public Contingency getContingency() {
        return contingency;
    }
}
