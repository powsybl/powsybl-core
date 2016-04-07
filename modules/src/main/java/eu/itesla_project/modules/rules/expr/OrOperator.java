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
public class OrOperator implements SecondLevelNode {

    private final SecondLevelNode node1;

    private final SecondLevelNode node2;

    public OrOperator(SecondLevelNode node1, SecondLevelNode node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    public SecondLevelNode getNode1() {
        return node1;
    }

    public SecondLevelNode getNode2() {
        return node2;
    }

    @Override
    public <R, A> R accept(ExpressionVisitor<R, A> visitor, A arg) {
        return visitor.visit(this, arg);
    }

    @Override
    public OrOperator clone() {
        return new OrOperator(node1.clone(), node2.clone());
    }
}

