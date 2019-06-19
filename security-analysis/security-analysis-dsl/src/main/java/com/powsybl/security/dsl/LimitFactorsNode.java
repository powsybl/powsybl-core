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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * {@link LimitFactors} is implemented as a tree of conditions.
 * This type of node request a factor to its children,
 * and returns the first non empty factor it finds.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
class LimitFactorsNode implements LimitFactors {

    private final List<LimitFactors> children;

    public LimitFactorsNode() {
        this.children = new ArrayList<>();
    }

    public void addChild(LimitFactors child) {
        children.add(child);
    }

    protected List<LimitFactors> getChildren() {
        return children;
    }

    @Override
    public Optional<Float> getFactor(Contingency contingency, Branch branch, Branch.Side side, CurrentLimits.TemporaryLimit limit) {

        return children.stream()
                .map(c -> c.getFactor(contingency, branch, side, limit))
                .filter(Optional::isPresent)
                .findFirst()
                .map(Optional::get);
    }
}
