/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.rules.expr;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ComparisonOperator implements SecondLevelNode {

    public enum Type {
        LESS,
        GREATER_EQUAL
    }

    private final Attribute node1;

    private final Litteral node2;

    private final Type type;

    public ComparisonOperator(Attribute node1, Litteral node2, Type type) {
        this.node1 = node1;
        this.node2 = node2;
        this.type = type;
    }

    public Attribute getNode1() {
        return node1;
    }

    public Litteral getNode2() {
        return node2;
    }

    public Type getType() {
        return type;
    }

    @Override
    public <R, A> R accept(ExpressionVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public ComparisonOperator clone() {
        return new ComparisonOperator(node1.clone(), node2.clone(), type);
    }
}
