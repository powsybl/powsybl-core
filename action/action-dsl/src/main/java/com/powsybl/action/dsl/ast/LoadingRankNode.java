/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.ast;

import com.powsybl.commons.PowsyblException;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadingRankNode implements ExpressionNode {

    private final ExpressionNode branchIdToRankNode;

    private final List<ExpressionNode> branchIds;

    public LoadingRankNode(ExpressionNode branchIdToRankNode, List<ExpressionNode> branchIds) {
        this.branchIdToRankNode = Objects.requireNonNull(branchIdToRankNode);
        this.branchIds = Objects.requireNonNull(branchIds);
        if (branchIds.isEmpty()) {
            throw new PowsyblException("List of branch to compare has to be greater or equal to one");
        }
    }

    public ExpressionNode getBranchIdToRankNode() {
        return branchIdToRankNode;
    }

    public List<ExpressionNode> getBranchIds() {
        return branchIds;
    }

    @Override
    public <R, A> R accept(ExpressionVisitor<R, A> visitor, A arg) {
        return visitor.visitLoadingRank(this, arg);
    }
}
