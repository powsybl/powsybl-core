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

    private final List<String> lineIds;
    private final float reduction;
    private final int duration;

    public IsOverloadedNode(List<String> lineIds, float reduction, int duration) {
        this.lineIds = Objects.requireNonNull(lineIds);
        if (lineIds.isEmpty()) {
            throw new RuntimeException("List of branch should contain at least one branchId");
        }
        if (reduction < 0) {
            throw new RuntimeException("Reduction is not valid");
        }
        this.reduction = reduction;
        if (duration < 0) {
            throw new RuntimeException("Duration is not valid");
        }
        this.duration = duration;
    }

    public List<String> getLineIds() {
        return lineIds;
    }

    public float getReduction() {
        return reduction;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public <R, A> R accept(ExpressionVisitor<R, A> visitor, A arg) {
        return visitor.visitIsOverloaded(this, arg);
    }
}
