/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dsl.ast;

import com.powsybl.dsl.ast.ExpressionVisitor;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public interface LimitsExpressionVisitor<R, A> extends ExpressionVisitor<R, A> {

    R visitVariable(LimitsVariableNode node, A arg);
}
