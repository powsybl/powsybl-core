package com.powsybl.action.dsl.ast;

import java.util.List;
import java.util.Objects;

public class AllOverloadedNode implements ExpressionNode {

    private final List<String> branchIds;
    private final float limitReduction;

    public AllOverloadedNode(List<String> branchIds, float limitReduction) {
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

    @Override
    public <R, A> R accept(ExpressionVisitor<R, A> visitor, A arg) {
        return visitor.visitAllOverloaded(this, arg);
    }

}
