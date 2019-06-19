/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dsl.ast;

import com.powsybl.dsl.ast.ExpressionNode;
import com.powsybl.dsl.ast.ExpressionVisitor;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public abstract class AbstractLimitsExpressionNode implements ExpressionNode {

    @Override
    public <R, A> R accept(ExpressionVisitor<R, A> visitor, A arg) {
        return accept((LimitsExpressionVisitor<R, A>) visitor, arg);
    }

    abstract <R, A> R accept(LimitsExpressionVisitor<R, A> visitor, A arg);
}
