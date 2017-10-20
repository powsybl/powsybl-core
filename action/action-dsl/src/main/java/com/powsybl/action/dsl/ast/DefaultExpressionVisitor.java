/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.ast;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultExpressionVisitor<R, A> implements ExpressionVisitor<R, A> {
    @Override
    public R visitComparisonOperator(ComparisonOperatorNode node, A arg) {
        node.getLeft().accept(this, arg);
        node.getRight().accept(this, arg);
        return null;
    }

    @Override
    public R visitLogicalOperator(LogicalBinaryOperatorNode node, A arg) {
        node.getLeft().accept(this, arg);
        node.getRight().accept(this, arg);
        return null;
    }

    @Override
    public R visitArithmeticOperator(ArithmeticBinaryOperatorNode node, A arg) {
        node.getLeft().accept(this, arg);
        node.getRight().accept(this, arg);
        return null;
    }

    @Override
    public R visitNotOperator(LogicalNotOperator node, A arg) {
        node.getChild().accept(this, arg);
        return null;
    }

    @Override
    public R visitLiteral(AbstractLiteralNode node, A arg) {
        return null;
    }

    @Override
    public R visitNetworkComponent(NetworkComponentNode node, A arg) {
        return null;
    }

    @Override
    public R visitNetworkProperty(NetworkPropertyNode node, A arg) {
        node.getParent().accept(this, arg);
        return null;
    }

    @Override
    public R visitNetworkMethod(NetworkMethodNode node, A arg) {
        node.getParent().accept(this, arg);
        return null;
    }

    @Override
    public R visitActionTaken(ActionTakenNode node, A arg) {
        return null;
    }

    @Override
    public R visitContingencyOccurred(ContingencyOccurredNode node, A arg) {
        return null;
    }

    @Override
    public R visitLoadingRank(LoadingRankNode node, A arg) {
        return null;
    }

    @Override
    public R visitMostLoaded(MostLoadedNode mostLoadedNode, A arg) {
        return null;
    }

    @Override
    public R visitIsOverloaded(IsOverloadedNode isOverloadedNode, A arg) {
        return null;
    }
}
