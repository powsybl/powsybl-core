/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dsl.ast;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ExpressionVisitor<R, A> {

    R visitLiteral(AbstractLiteralNode node, A arg);

    R visitComparisonOperator(ComparisonOperatorNode node, A arg);

    R visitLogicalOperator(LogicalBinaryOperatorNode node, A arg);

    R visitArithmeticOperator(ArithmeticBinaryOperatorNode node, A arg);

    R visitNotOperator(LogicalNotOperator node, A arg);
}
