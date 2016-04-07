/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules.expr;

import eu.itesla_project.modules.histo.HistoDbAttributeId;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExpressionStatistics extends AbstractExpressionVisitor<Object, Void> {

    private final Set<HistoDbAttributeId> attributeIds = new HashSet<>();

    private int convexSetCount = 1;

    private int inequalityCount = 0;

    public static ExpressionStatistics compute(ExpressionNode node) {
        ExpressionStatistics statistics = new ExpressionStatistics();
        node.accept(statistics, null);
        return statistics;
    }

    public int getAttributeCount() {
        return attributeIds.size();
    }

    public int getConvexSetCount() {
        return convexSetCount;
    }

    public int getInequalityCount() {
        return inequalityCount;
    }

    @Override
    public Object visit(AndOperator node, Void arg) {
        return super.visit(node, arg);
    }

    @Override
    public Object visit(Attribute node, Void arg) {
        attributeIds.add(node.getId());
        return super.visit(node, arg);
    }

    @Override
    public Object visit(ComparisonOperator node, Void arg) {
        inequalityCount++;
        return super.visit(node, arg);
    }

    @Override
    public Object visit(Litteral node, Void arg) {
        return super.visit(node, arg);
    }

    @Override
    public Object visit(OrOperator node, Void arg) {
        convexSetCount++;
        return super.visit(node, arg);
    }
}
