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

import java.util.Optional;

/**
 * The final node of a tree of conditions, which provides an actual factor value.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
class FinalFactorNode implements LimitFactors {

    private final float factor;

    public FinalFactorNode(float factor) {
        this.factor = factor;
    }

    @Override
    public Optional<Float> getFactor(Contingency contingency, Branch branch, Branch.Side side, CurrentLimits.TemporaryLimit limit) {
        return Optional.of(factor);
    }

}
