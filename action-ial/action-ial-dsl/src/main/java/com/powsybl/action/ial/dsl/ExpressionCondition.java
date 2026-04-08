/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.ial.dsl;

import com.powsybl.dsl.ast.ExpressionNode;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ExpressionCondition implements Condition {

    private final ExpressionNode node;

    public ExpressionCondition(ExpressionNode node) {
        this.node = Objects.requireNonNull(node);
    }

    @Override
    public ConditionType getType() {
        return ConditionType.EXPRESSION;
    }

    public ExpressionNode getNode() {
        return node;
    }
}
