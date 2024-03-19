/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.ial.dsl.ast;

import com.powsybl.dsl.ast.ExpressionNode;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NetworkMethodNode extends AbstractActionExpressionNode implements NetworkNode {

    private final NetworkNode parent;

    private final String methodName;

    private final Object[] args;

    public NetworkMethodNode(NetworkNode parent, String methodName, Object[] args) {
        this.parent = Objects.requireNonNull(parent);
        this.methodName = Objects.requireNonNull(methodName);
        this.args = Objects.requireNonNull(args);
    }

    public ExpressionNode getParent() {
        return parent;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    @Override
    public <R, A> R accept(ActionExpressionVisitor<R, A> visitor, A arg) {
        return visitor.visitNetworkMethod(this, arg);
    }
}
