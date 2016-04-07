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
public interface ExpressionVisitor<R, A> {

    R visit(Attribute node, A arg);

    R visit(Litteral node, A arg);

    R visit(ComparisonOperator node, A arg);

    R visit(AndOperator node, A arg);

    R visit(OrOperator node, A arg);

}
