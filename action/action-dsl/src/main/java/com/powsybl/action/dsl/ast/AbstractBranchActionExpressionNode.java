/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.ast;

import java.util.List;
import java.util.Objects;

/**
 * @author Olivier Bretteville <olivier.bretteville at rte-france.com>
 */
public abstract class AbstractBranchActionExpressionNode extends AbstractActionExpressionNode {

    private final List<String> branchIds;

    private final float limitReduction;

    public AbstractBranchActionExpressionNode(List<String> branchIds, float limitReduction) {
        this.branchIds = Objects.requireNonNull(branchIds);
        if (branchIds.isEmpty()) {
            throw new IllegalArgumentException("The list of branch Ids should not be empty");
        }
        if (limitReduction < 0) {
            throw new IllegalArgumentException("Limit reduction is not valid");
        }
        this.limitReduction = limitReduction;
    }

    public List<String> getBranchIds() {
        return branchIds;
    }

    public float getLimitReduction() {
        return limitReduction;
    }
}
