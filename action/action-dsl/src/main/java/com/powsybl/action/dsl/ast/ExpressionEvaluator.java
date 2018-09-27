/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.ast;

import com.powsybl.action.dsl.GroovyUtil;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Identifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExpressionEvaluator extends DefaultExpressionVisitor<Object, Void> {

    private final EvaluationContext context;

    public ExpressionEvaluator(EvaluationContext context) {
        this.context = Objects.requireNonNull(context);
    }

    public static Object evaluate(ExpressionNode node, EvaluationContext context) {
        return node.accept(new ExpressionEvaluator(context), null);
    }

    @Override
    public Object visitLiteral(AbstractLiteralNode node, Void arg) {
        return node.getValue();
    }

    @Override
    public Object visitNetworkComponent(NetworkComponentNode node, Void arg) {
        Identifiable identifiable = context.getNetwork().getIdentifiable(node.getComponentId());
        if (identifiable == null) {
            throw new PowsyblException("Network component '" + node.getComponentId() + "' not found");
        }
        return identifiable;
    }

    @Override
    public Object visitNetworkProperty(NetworkPropertyNode node, Void arg) {
        Object parentValue = node.getParent().accept(this, arg);
        if (parentValue == null) {
            throw new PowsyblException("Cannot call a property '" + node.getPropertyName() + "' on a null object");
        }
        return GroovyUtil.callProperty(parentValue, node.getPropertyName());
    }

    @Override
    public Object visitNetworkMethod(NetworkMethodNode node, Void arg) {
        Object parentValue = node.getParent().accept(this, arg);
        if (parentValue == null) {
            throw new PowsyblException("Cannot call a method '" + node.getMethodName() + "' on a null object");
        }
        return GroovyUtil.callMethod(parentValue, node.getMethodName(), node.getArgs());
    }

    @Override
    public Object visitComparisonOperator(ComparisonOperatorNode node, Void arg) {
        Object result1 = node.getLeft().accept(this, arg);
        Object result2 = node.getRight().accept(this, arg);
        if (!(result1 instanceof Number)) {
            throw new PowsyblException("Left operand of comparison should return a number");
        }
        if (!(result2 instanceof Number)) {
            throw new PowsyblException("Right operand of comparison should return a number");
        }
        double value1 = ((Number) result1).doubleValue();
        double value2 = ((Number) result2).doubleValue();
        switch (node.getOperator()) {
            case EQUALS:
                return value1 == value2;
            case NOT_EQUALS:
                return value1 != value2;
            case GREATER_THAN:
                return value1 > value2;
            case LESS_THAN:
                return value1 < value2;
            case GREATER_THAN_OR_EQUALS_TO:
                return value1 >= value2;
            case LESS_THAN_OR_EQUALS_TO:
                return value1 <= value2;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public Object visitNotOperator(LogicalNotOperator node, Void arg) {
        Object result = node.getChild().accept(this, arg);
        if (!(result instanceof Boolean)) {
            throw new PowsyblException("Operand of not operator should return a boolean");
        }
        return !(Boolean) result;
    }

    @Override
    public Object visitLogicalOperator(LogicalBinaryOperatorNode node, Void arg) {
        Object result1 = node.getLeft().accept(this, arg);
        Object result2 = node.getRight().accept(this, arg);
        if (!(result1 instanceof Boolean)) {
            throw new PowsyblException("Left operand of comparison should return a boolean");
        }
        if (!(result2 instanceof Boolean)) {
            throw new PowsyblException("Right operand of comparison should return a boolean");
        }
        boolean value1 = (Boolean) result1;
        boolean value2 = (Boolean) result2;
        switch (node.getOperator()) {
            case AND:
                return value1 && value2;
            case OR:
                return value1 || value2;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public Object visitArithmeticOperator(ArithmeticBinaryOperatorNode node, Void arg) {
        Object result1 = node.getLeft().accept(this, arg);
        Object result2 = node.getRight().accept(this, arg);
        if (!(result1 instanceof Number)) {
            throw new PowsyblException("Left operand of arithmetic operation should return a number (" + result1.getClass() + ")");
        }
        if (!(result2 instanceof Number)) {
            throw new PowsyblException("Right operand of arithmetic operation should return a number (" + result2.getClass() + ")");
        }
        double value1 = ((Number) result1).doubleValue();
        double value2 = ((Number) result2).doubleValue();
        switch (node.getOperator()) {
            case PLUS:
                return value1 + value2;
            case MINUS:
                return value1 - value2;
            case MULTIPLY:
                return value1 * value2;
            case DIVIDE:
                return value1 / value2;
            default:
                throw new AssertionError();
        }
    }

    @Override
    public Object visitActionTaken(ActionTakenNode node, Void arg) {
        return context.isActionTaken(node.getActionId());
    }

    @Override
    public Object visitContingencyOccurred(ContingencyOccurredNode node, Void arg) {
        return context.getContingency() != null &&
                (node.getContingencyId() == null || context.getContingency().getId().equals(node.getContingencyId()));
    }

    /**
     * Utility class to compare loading on one side of a branch to loading of one side of another branch
     */
    private static final class BranchAndSide implements Comparable<BranchAndSide> {
        private final Branch branch;
        private final Branch.Side side;

        private BranchAndSide(Branch branch, Branch.Side side) {
            this.branch = Objects.requireNonNull(branch);
            this.side = Objects.requireNonNull(side);
        }

        private Branch getBranch() {
            return branch;
        }

        private Branch.Side getSide() {
            return side;
        }

        /**
         * TODO: to move to IIDM
         */
        private static double getPermanentLimit(Branch branch, Branch.Side side) {
            Objects.requireNonNull(branch);
            Objects.requireNonNull(side);
            double permanentLimit1 = branch.getCurrentLimits1() != null ? branch.getCurrentLimits1().getPermanentLimit() : Double.NaN;
            double permanentLimit2 = branch.getCurrentLimits2() != null ? branch.getCurrentLimits2().getPermanentLimit() : Double.NaN;
            return side == Branch.Side.ONE ? permanentLimit1 : permanentLimit2;
        }

        private static int compare(double value1, double value2) {
            if (Double.isNaN(value1) && Double.isNaN(value2)) {
                return 0;
            } else if (Double.isNaN(value1) && !Double.isNaN(value2)) {
                return -1;
            } else if (!Double.isNaN(value1) && Double.isNaN(value2)) {
                return 1;
            } else {
                return Double.compare(value1, value2);
            }
        }

        private static int compare(BranchAndSide branchAndSide1, BranchAndSide branchAndSide2) {
            Branch.Overload overload1 = branchAndSide1.getBranch().checkTemporaryLimits(branchAndSide1.getSide());
            Branch.Overload overload2 = branchAndSide2.getBranch().checkTemporaryLimits(branchAndSide2.getSide());
            double i1 = branchAndSide1.getBranch().getTerminal(branchAndSide1.getSide()).getI();
            double i2 = branchAndSide2.getBranch().getTerminal(branchAndSide2.getSide()).getI();
            double permanentLimit1 = getPermanentLimit(branchAndSide1.getBranch(), branchAndSide1.getSide());
            double permanentLimit2 = getPermanentLimit(branchAndSide2.getBranch(), branchAndSide2.getSide());
            int c;
            if (overload1 == null) {
                if (overload2 == null) {
                    // no overload, compare load based on permanent limit
                    c = compare(i1 / permanentLimit1, i2 / permanentLimit2);
                } else {
                    c = -1;
                }
            } else {
                if (overload2 == null) {
                    c = 1;
                } else {
                    // first compare acceptable duration
                    c = -Integer.compare(overload1.getTemporaryLimit().getAcceptableDuration(),
                            overload2.getTemporaryLimit().getAcceptableDuration());
                    if (c == 0) {
                        // and then overload based on temporary limit
                        c = compare(i1 / overload1.getTemporaryLimit().getValue(),
                                i2 / overload2.getTemporaryLimit().getValue());
                    }
                }
            }
            return c;
        }

        @Override
        public int hashCode() {
            return Objects.hash(branch, side);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof BranchAndSide) {
                return ((BranchAndSide) obj).compareTo(this) == 0;
            }
            return false;
        }

        @Override
        public int compareTo(BranchAndSide o) {
            return compare(this, o);
        }

        @Override
        public String toString() {
            return branch.getId() + "/" + side;
        }
    }

    private List<String> sortBranches(List<String> branchIds) {
        return branchIds.stream()
                .map(this::getBranch)
                .map(branch -> {
                    BranchAndSide branchAndSide1 = new BranchAndSide(branch, Branch.Side.ONE);
                    BranchAndSide branchAndSide2 = new BranchAndSide(branch, Branch.Side.TWO);
                    int c = branchAndSide1.compareTo(branchAndSide2);
                    return c >= 0 ? branchAndSide1 : branchAndSide2;
                })
                .sorted()
                .map(branchAndSide -> branchAndSide.getBranch().getId())
                .collect(Collectors.toList());
    }

    @Override
    public Object visitLoadingRank(LoadingRankNode node, Void arg) {
        List<String> branchIds = new ArrayList<>();
        node.getBranchIds().forEach(e -> branchIds.add((String) e.accept(this, arg)));

        String branchIdToRank = (String) node.getBranchIdToRankNode().accept(this, arg);
        if (!branchIds.contains(branchIdToRank)) {
            throw new PowsyblException("Branch to rank has to be in the list");
        }

        List<String> sortedBranchIds = sortBranches(branchIds);
        int i = sortedBranchIds.indexOf(branchIdToRank);
        if (i == -1) {
            throw new AssertionError();
        }
        return sortedBranchIds.size() - i; // just a convention
    }

    @Override
    public Object visitMostLoaded(MostLoadedNode node, Void arg) {
        List<String> sortedBranchIds = sortBranches(node.getBranchIds());
        return sortedBranchIds.get(sortedBranchIds.size() - 1);
    }

    @Override
    public Object visitIsOverloaded(IsOverloadedNode isOverloadedNode, Void arg) {
        float limitReduction = isOverloadedNode.getLimitReduction();

        // Iterate over all the branch Ids to be sure that all the branches exist in the network
        return isOverloadedNode.getBranchIds().stream()
                .map(id -> getBranch(id).isOverloaded(limitReduction))
                .reduce(false, (a, b) -> a || b);
    }

    private Branch getBranch(String branchId) {
        Branch branch = context.getNetwork().getBranch(branchId);
        if (branch == null) {
            throw new PowsyblException("Branch '" + branchId + "' not found");
        }
        return branch;
    }
}
