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
public abstract class AbstractExpressionVisitor<R, A> implements ExpressionVisitor<R, A> {

    @Override
    public R visit(Attribute node, A arg) {
        return null;
    }

    @Override
    public R visit(Litteral node, A arg) {
        return null;
    }

    @Override
    public R visit(ComparisonOperator node, A arg) {
        node.getNode1().accept(this, arg);
        node.getNode2().accept(this, arg);
        return null;
    }

    @Override
    public R visit(AndOperator node, A arg) {
        node.getNode1().accept(this, arg);
        node.getNode2().accept(this, arg);
        return null;
    }

    @Override
    public R visit(OrOperator node, A arg) {
        node.getNode1().accept(this, arg);
        node.getNode2().accept(this, arg);
        return null;
    }

}
