/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.ast;

import java.util.List;
import java.util.Objects;


public class IsOverloadedNode implements ExpressionNode {

    private final List<String> branchIds;
    private final float reduction;

    public IsOverloadedNode(List<String> branchIds, float reduction) {
        this.branchIds = Objects.requireNonNull(branchIds);
        if (branchIds.isEmpty()) {
            throw new RuntimeException("List of branch should contain at least one branchId");
        }
        if (reduction < 0) {
            throw new RuntimeException("Reduction is not valid");
        }
        this.reduction = reduction;
    }

    public List<String> getBranchIds() {
        return branchIds;
    }

    public float getReduction() {
        return reduction;
    }

    @Override
    public <R, A> R accept(ExpressionVisitor<R, A> visitor, A arg) {
        return visitor.visitIsOverloaded(this, arg);
    }
}
