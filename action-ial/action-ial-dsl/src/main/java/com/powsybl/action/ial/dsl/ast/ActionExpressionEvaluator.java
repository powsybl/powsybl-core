/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.ial.dsl.ast;

import com.powsybl.commons.PowsyblException;
import com.powsybl.dsl.GroovyUtil;
import com.powsybl.dsl.ast.ExpressionEvaluator;
import com.powsybl.dsl.ast.ExpressionNode;
import com.powsybl.iidm.network.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ActionExpressionEvaluator extends ExpressionEvaluator implements ActionExpressionVisitor<Object, Void> {

    private final EvaluationContext context;

    public ActionExpressionEvaluator(EvaluationContext context) {
        this.context = Objects.requireNonNull(context);
    }

    public static Object evaluate(ExpressionNode node, EvaluationContext context) {
        return node.accept(new ActionExpressionEvaluator(context), null);
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
        private final TwoSides side;

        private BranchAndSide(Branch branch, TwoSides side) {
            this.branch = Objects.requireNonNull(branch);
            this.side = Objects.requireNonNull(side);
        }

        private Branch getBranch() {
            return branch;
        }

        private TwoSides getSide() {
            return side;
        }

        /**
         * TODO: to move to IIDM
         */
        private static double getPermanentLimit(Branch<?> branch, TwoSides side) {
            Objects.requireNonNull(branch);
            Objects.requireNonNull(side);
            double permanentLimit1 = branch.getCurrentLimits1().map(LoadingLimits::getPermanentLimit).orElse(Double.NaN);
            double permanentLimit2 = branch.getCurrentLimits2().map(LoadingLimits::getPermanentLimit).orElse(Double.NaN);
            return side == TwoSides.ONE ? permanentLimit1 : permanentLimit2;
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
            Overload overload1 = branchAndSide1.getBranch().checkTemporaryLimits(branchAndSide1.getSide(), LimitType.CURRENT);
            Overload overload2 = branchAndSide2.getBranch().checkTemporaryLimits(branchAndSide2.getSide(), LimitType.CURRENT);
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
            if (obj instanceof BranchAndSide branchAndSide) {
                return branchAndSide.compareTo(this) == 0;
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
                    BranchAndSide branchAndSide1 = new BranchAndSide(branch, TwoSides.ONE);
                    BranchAndSide branchAndSide2 = new BranchAndSide(branch, TwoSides.TWO);
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
            throw new IllegalStateException();
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
        double limitReduction = isOverloadedNode.getLimitReduction();

        // Iterate over all the branch Ids to be sure that all the branches exist in the network
        return isOverloadedNode.getBranchIds().stream()
                .map(id -> getBranch(id).isOverloaded(limitReduction))
                .reduce(false, (a, b) -> a || b);
    }

    @Override
    public Object visitAllOverloaded(AllOverloadedNode allOverloadedNode, Void arg) {
        double limitReduction = allOverloadedNode.getLimitReduction();

        // Iterate over all the branch Ids to be sure that all the branches exist in the network
        return allOverloadedNode.getBranchIds().stream()
                .map(id -> getBranch(id).isOverloaded(limitReduction))
                .reduce(true, (a, b) -> a && b);
    }

    private Branch getBranch(String branchId) {
        Branch branch = context.getNetwork().getBranch(branchId);
        if (branch == null) {
            throw new PowsyblException("Branch '" + branchId + "' not found");
        }
        return branch;
    }
}
