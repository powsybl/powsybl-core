/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.ast;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class ExpressionHelper {

    private ExpressionHelper() {
    }

    public static ComparisonOperatorNode newComparisonOperator(ExpressionNode left, ExpressionNode right, ComparisonOperator operator) {
        return new ComparisonOperatorNode(left, right, operator);
    }

    public static LogicalBinaryOperatorNode newLogicalBinaryOperator(ExpressionNode left, ExpressionNode right, LogicalBinaryOperator operator) {
        return new LogicalBinaryOperatorNode(left, right, operator);
    }

    public static ArithmeticBinaryOperatorNode newArithmeticBinaryOperator(ExpressionNode left, ExpressionNode right, ArithmeticBinaryOperator operator) {
        return new ArithmeticBinaryOperatorNode(left, right, operator);
    }

    public static LogicalNotOperator newLogicalNotOperator(ExpressionNode child) {
        return new LogicalNotOperator(child);
    }

    public static NetworkComponentNode newNetworkComponent(String id, NetworkComponentNode.ComponentType componentType) {
        return new NetworkComponentNode(id, componentType);
    }

    public static NetworkPropertyNode newNetworkProperty(NetworkNode parent, String propertyName) {
        return new NetworkPropertyNode(parent, propertyName);
    }

    public static NetworkMethodNode newNetworkMethod(NetworkNode parent, String methodName, Object[] args) {
        return new NetworkMethodNode(parent, methodName, args);
    }

    public static FloatLiteralNode newFloatLiteral(float value) {
        return new FloatLiteralNode(value);
    }

    public static DoubleLiteralNode newDoubleLiteral(double value) {
        return new DoubleLiteralNode(value);
    }

    public static BigDecimalLiteralNode newBigDecimalLiteral(BigDecimal value) {
        return new BigDecimalLiteralNode(value);
    }

    public static IntegerLiteralNode newIntegerLiteral(int value) {
        return new IntegerLiteralNode(value);
    }

    public static BooleanLiteralNode newBooleanLiteral(boolean value) {
        return new BooleanLiteralNode(value);
    }

    public static ActionTakenNode newActionTaken(String actionId) {
        return new ActionTakenNode(actionId);
    }

    public static ContingencyOccurredNode newContingencyOccured() {
        return new ContingencyOccurredNode();
    }

    public static LoadingRankNode newLoadingRank(ExpressionNode branchIdToRank, List<ExpressionNode> branchIds) {
        return new LoadingRankNode(branchIdToRank, branchIds);
    }

    public static MostLoadedNode newMostLoaded(List<String> branchIds) {
        return new MostLoadedNode(branchIds);
    }

    public static IsOverloadedNode newIsOverloadedNode(List<String> branchIds, float limitReduction) {
        return new IsOverloadedNode(branchIds, limitReduction);
    }

    public static ContingencyOccurredNode newContingencyOccured(String contingencyId) {
        return new ContingencyOccurredNode(contingencyId);
    }

    public static ExpressionNode newStringLiteral(String value) {
        return new StringLiteralNode(value);
    }
}
