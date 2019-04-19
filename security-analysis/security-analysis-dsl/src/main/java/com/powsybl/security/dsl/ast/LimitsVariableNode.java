/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dsl.ast;

import java.util.Objects;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public final class LimitsVariableNode extends AbstractLimitsExpressionNode {

    private static final LimitsVariableNode DURATION = new LimitsVariableNode(Type.DURATION);
    private static final LimitsVariableNode VOLTAGE = new LimitsVariableNode(Type.VOLTAGE);

    private final Type type;

    public static LimitsVariableNode voltage() {
        return VOLTAGE;
    }

    public static LimitsVariableNode duration() {
        return DURATION;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        VOLTAGE,
        DURATION
    }

    private LimitsVariableNode(Type type) {
        this.type = Objects.requireNonNull(type);
    }

    @Override
    <R, A> R accept(LimitsExpressionVisitor<R, A> visitor, A arg) {
        return visitor.visitVariable(this, arg);
    }
}
