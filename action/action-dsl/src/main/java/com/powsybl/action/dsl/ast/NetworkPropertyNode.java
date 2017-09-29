/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.ast;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkPropertyNode implements NetworkNode {

    private final NetworkNode parent;

    private final String propertyName;

    public NetworkPropertyNode(NetworkNode parent, String propertyName) {
        this.parent = Objects.requireNonNull(parent);
        this.propertyName = Objects.requireNonNull(propertyName);
    }

    public ExpressionNode getParent() {
        return parent;
    }

    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public <R, A> R accept(ExpressionVisitor<R, A> visitor, A arg) {
        return visitor.visitNetworkProperty(this, arg);
    }
}
